"""Compare six models for a retrospective ENDES D107 experiment.

The target is reported severe violence in the survey, not future femicide.
Rows with an unknown target are excluded, source identifiers are never used as
features or written, and the final holdout is split by sampling cluster.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import zipfile
from pathlib import Path
from typing import Any

import joblib
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import sklearn
from sklearn.compose import ColumnTransformer
from sklearn.impute import SimpleImputer
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    average_precision_score,
    brier_score_loss,
    confusion_matrix,
    f1_score,
    precision_score,
    precision_recall_curve,
    recall_score,
    roc_auc_score,
    roc_curve,
)
from sklearn.model_selection import StratifiedGroupKFold
from sklearn.neighbors import KNeighborsClassifier
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.svm import LinearSVC
from sklearn.tree import DecisionTreeClassifier
from sklearn.ensemble import RandomForestClassifier
from xgboost import XGBClassifier

try:  # Support both `python scripts/train_compare.py` and package-style tests.
    from .audit_endes import DEFAULT_ARCHIVE, read_nested_csv
except ImportError:  # pragma: no cover - exercised by direct script invocation
    from audit_endes import DEFAULT_ARCHIVE, read_nested_csv


PROJECT_DIR = Path(__file__).resolve().parents[2]
DEFAULT_OUTPUT = PROJECT_DIR / "ml-service" / "docs" / "model_results.md"
DEFAULT_ARTIFACT_DIR = PROJECT_DIR / "ml-service" / "artifacts"
ARTIFACT_VERSION = "endes-2025-d107-research-candidate-v1"
TARGET = "D107"
GROUP = "V021"
CATEGORICAL = [
    "D101A", "D101B", "D101C", "D101D", "D101E", "D101F",
    "D103A", "D103B", "D103D",
    "D105A", "D105B", "D105C", "D105D", "D113", "D114",
    "V025", "V106", "V190",
]
NUMERIC = ["V012"]
FEATURES = CATEGORICAL + NUMERIC
SUBGROUP_FIELDS = ["V025", "V106", "V190"]
BOOTSTRAP_REPLICATES = 500
FORBIDDEN = {"CASEID", "D105E", "D105F", "D105G", "D106", "D107", "D108", "V001", "V021", "V022", "V044", "V005", "V501", "V502"}


def eligible_training_rows(joined: pd.DataFrame) -> pd.Series:
    """Require an observed outcome from a woman interviewed for the module."""
    observed_target = joined[TARGET].isin(["0", "1"])
    if (observed_target & joined["V044"].ne("1")).any():
        raise ValueError("D107 is observed outside V044=1; review module selection before training.")
    return observed_target & joined["V044"].eq("1") & joined[GROUP].notna()


def load_experiment_data(archive_path: Path) -> tuple[pd.DataFrame, pd.Series, pd.Series, pd.Series]:
    """Load eligible observations in memory; never persist row-level data."""
    with zipfile.ZipFile(archive_path) as archive:
        violence = read_nested_csv(archive, "REC84DV_2025.csv")
        individual = read_nested_csv(archive, "REC0111_2025.csv")
    if violence["CASEID"].duplicated().any() or individual["CASEID"].duplicated().any():
        raise ValueError("Duplicate survey keys; refusing an ambiguous join.")
    joined = violence.merge(
        individual[["CASEID", GROUP, "V022", "V044", "V025", "V012", "V106", "V190"]],
        on="CASEID", how="inner", validate="one_to_one",
    )
    missing_columns = sorted(set([TARGET, GROUP, "V044", *FEATURES]) - set(joined.columns))
    if missing_columns:
        raise ValueError(f"Expected experiment fields absent: {missing_columns}")
    eligible = eligible_training_rows(joined)
    selected = joined.loc[eligible, FEATURES + [TARGET, GROUP, "V022"]].copy()
    y = selected.pop(TARGET).astype("int8")
    groups = selected.pop(GROUP).astype("string")
    strata = selected.pop("V022").astype("string")
    selected[NUMERIC] = selected[NUMERIC].apply(pd.to_numeric, errors="coerce")
    for column in CATEGORICAL:
        selected[column] = selected[column].astype("string").fillna("__MISSING__")
    # Keep explicit missing indicators for age; categorical missingness is its own level.
    return selected, y, groups, strata


def make_preprocessor() -> ColumnTransformer:
    categorical_pipe = Pipeline([
        ("fill", SimpleImputer(strategy="constant", fill_value="__MISSING__")),
        ("onehot", OneHotEncoder(handle_unknown="ignore")),
    ])
    numeric_pipe = Pipeline([
        ("fill", SimpleImputer(strategy="median", add_indicator=True)),
        ("scale", StandardScaler()),
    ])
    return ColumnTransformer([
        ("categorical", categorical_pipe, CATEGORICAL),
        ("numeric", numeric_pipe, NUMERIC),
    ])


def model_candidates(seed: int) -> dict[str, list[Any]]:
    """Small predeclared grids keep tuning bounded and comparable."""
    return {
        "Decision Tree": [
            DecisionTreeClassifier(max_depth=depth, min_samples_leaf=leaf, random_state=seed)
            for depth, leaf in [(5, 20), (10, 50)]
        ],
        "Random Forest": [
            RandomForestClassifier(n_estimators=200, max_depth=depth, min_samples_leaf=leaf,
                                   random_state=seed, n_jobs=-1)
            for depth, leaf in [(12, 20), (None, 50)]
        ],
        "Logistic Regression": [
            LogisticRegression(C=c, max_iter=1500, solver="liblinear", random_state=seed)
            for c in [0.1, 1.0]
        ],
        "XGBoost": [
            XGBClassifier(n_estimators=180, max_depth=depth, learning_rate=0.05,
                          subsample=0.85, colsample_bytree=0.85, reg_lambda=1.0,
                          eval_metric="logloss", random_state=seed, n_jobs=4)
            for depth in [3, 5]
        ],
        "SVM": [
            LinearSVC(C=c, dual="auto", max_iter=5000, random_state=seed)
            for c in [0.5, 2.0]
        ],
        "KNN": [
            KNeighborsClassifier(n_neighbors=k, weights=weight, n_jobs=-1)
            for k, weight in [(15, "distance"), (31, "distance")]
        ],
    }


def _positive_score(estimator: Pipeline, features: pd.DataFrame) -> tuple[np.ndarray, np.ndarray]:
    predicted = estimator.predict(features).astype(int)
    if isinstance(estimator.named_steps["model"], LinearSVC):
        score = estimator.decision_function(features)
    else:
        score = estimator.predict_proba(features)[:, 1]
    return predicted, np.asarray(score, dtype=float)


def calibration_points(y: pd.Series, probabilities: np.ndarray,
                       bins: int = 10) -> tuple[np.ndarray, np.ndarray]:
    """Compute unweighted means by quantile bin for the observed sample."""
    bucket = pd.qcut(pd.Series(probabilities), q=bins, duplicates="drop")
    table = pd.DataFrame({"bucket": bucket, "observed": np.asarray(y), "predicted": probabilities})
    grouped = table.groupby("bucket", observed=True)
    observed = grouped["observed"].mean().to_numpy()
    predicted = grouped["predicted"].mean().to_numpy()
    return predicted, observed


def metric_row(name: str, y: pd.Series, predicted: np.ndarray, score: np.ndarray,
               has_probability: bool) -> dict[str, float | str | int | None]:
    tn, fp, fn, tp = confusion_matrix(y, predicted, labels=[0, 1]).ravel()
    result: dict[str, float | str | int | None] = {
        "model": name,
        "accuracy": accuracy_score(y, predicted),
        "precision": precision_score(y, predicted, zero_division=0),
        "recall": recall_score(y, predicted, zero_division=0),
        "f1": f1_score(y, predicted, zero_division=0),
        "specificity": float(tn / (tn + fp)) if (tn + fp) else None,
        "roc_auc": roc_auc_score(y, score),
        "pr_auc": average_precision_score(y, score),
        "tn": int(tn), "fp": int(fp), "fn": int(fn), "tp": int(tp),
        "brier": brier_score_loss(y, score) if has_probability else None,
    }
    return result


def subgroup_metric_rows(model_name: str, y: pd.Series, predicted: np.ndarray,
                         x: pd.DataFrame) -> list[dict[str, Any]]:
    """Summarize holdout errors by predefined ENDES demographic categories."""
    rows: list[dict[str, Any]] = []
    for field in SUBGROUP_FIELDS:
        values = x[field].astype("string").fillna("<blank>").to_numpy()
        for category in sorted(set(values)):
            positions = np.flatnonzero(values == category)
            y_group = y.iloc[positions]
            predicted_group = predicted[positions]
            tn, fp, fn, tp = confusion_matrix(y_group, predicted_group, labels=[0, 1]).ravel()
            rows.append({
                "model": model_name,
                "field": field,
                "code": str(category),
                "rows": int(len(positions)),
                "positive_rows": int(y_group.sum()),
                "recall": float(tp / (tp + fn)) if (tp + fn) else None,
                "specificity": float(tn / (tn + fp)) if (tn + fp) else None,
                "false_negatives": int(((y_group == 1) & (predicted_group == 0)).sum()),
            })
    return rows


def cluster_bootstrap_intervals(
    y: pd.Series,
    predicted: np.ndarray,
    score: np.ndarray,
    groups: pd.Series,
    has_probability: bool,
    replicates: int = 500,
    seed: int = 2028,
) -> dict[str, tuple[float, float]]:
    """Percentile intervals from resampling whole holdout clusters.

    This is an exploratory PSU bootstrap; it does not reproduce ENDES's full
    stratified survey variance estimator and must not be reported as such.
    """
    group_values = groups.astype("string").to_numpy()
    unique_groups = np.unique(group_values)
    positions_by_group = {
        group: np.flatnonzero(group_values == group) for group in unique_groups
    }
    rng = np.random.default_rng(seed)
    samples: dict[str, list[float]] = {
        "recall": [], "specificity": [], "pr_auc": [], "roc_auc": [], "brier": []
    }
    y_values = y.to_numpy()
    for _ in range(replicates):
        sampled_groups = rng.choice(unique_groups, size=len(unique_groups), replace=True)
        indices = np.concatenate([positions_by_group[group] for group in sampled_groups])
        y_boot = y_values[indices]
        if np.unique(y_boot).size < 2:
            continue
        pred_boot = predicted[indices]
        score_boot = score[indices]
        tn, fp, fn, tp = confusion_matrix(y_boot, pred_boot, labels=[0, 1]).ravel()
        samples["recall"].append(float(tp / (tp + fn)) if (tp + fn) else np.nan)
        samples["specificity"].append(float(tn / (tn + fp)) if (tn + fp) else np.nan)
        samples["pr_auc"].append(float(average_precision_score(y_boot, score_boot)))
        samples["roc_auc"].append(float(roc_auc_score(y_boot, score_boot)))
        if has_probability:
            samples["brier"].append(float(brier_score_loss(y_boot, score_boot)))

    intervals: dict[str, tuple[float, float]] = {}
    for metric, values in samples.items():
        finite_values = np.asarray(values, dtype=float)
        finite_values = finite_values[np.isfinite(finite_values)]
        if len(finite_values) >= max(20, int(replicates * 0.8)):
            low, high = np.quantile(finite_values, [0.025, 0.975])
            intervals[metric] = (float(low), float(high))
    return intervals


def run_experiment(archive_path: Path, output_path: Path, seed: int = 2026) -> dict[str, Any]:
    x, y, groups, strata = load_experiment_data(archive_path)
    if set(FEATURES) & FORBIDDEN:
        raise AssertionError("Leakage/identifier fields were added to model inputs.")
    outer = StratifiedGroupKFold(n_splits=5, shuffle=True, random_state=seed)
    train_idx, test_idx = next(outer.split(x, y, groups))
    x_train, x_test = x.iloc[train_idx], x.iloc[test_idx]
    y_train, y_test = y.iloc[train_idx], y.iloc[test_idx]
    g_train = groups.iloc[train_idx]
    holdout_prevalence = float(y_test.mean())
    if set(g_train) & set(groups.iloc[test_idx]):
        raise AssertionError("Cluster leakage between train and holdout.")
    design_rows = pd.DataFrame({"stratum": strata, "target": y, "cluster": groups})
    stratum_summary = design_rows.groupby("stratum", dropna=False).agg(
        rows=("target", "size"), positives=("target", "sum"), clusters=("cluster", "nunique")
    )
    positive_clusters_by_stratum = (
        design_rows[design_rows["target"] == 1]
        .groupby("stratum", dropna=False)["cluster"].nunique()
    )

    cv = StratifiedGroupKFold(n_splits=3, shuffle=True, random_state=seed + 1)
    candidates = model_candidates(seed)
    cv_rows: list[dict[str, Any]] = []
    holdout_rows: list[dict[str, Any]] = []
    subgroup_rows: list[dict[str, Any]] = []
    predictions_for_bootstrap: dict[str, np.ndarray] = {}
    scores_for_plot: dict[str, tuple[np.ndarray, bool]] = {}
    for name, models in candidates.items():
        candidate_scores: list[tuple[float, Any]] = []
        for candidate_index, model in enumerate(models, start=1):
            fold_scores = []
            for fit_idx, val_idx in cv.split(x_train, y_train, g_train):
                pipeline = Pipeline([("preprocess", make_preprocessor()), ("model", model)])
                pipeline.fit(x_train.iloc[fit_idx], y_train.iloc[fit_idx])
                _, score = _positive_score(pipeline, x_train.iloc[val_idx])
                fold_scores.append(average_precision_score(y_train.iloc[val_idx], score))
            mean_pr = float(np.mean(fold_scores))
            cv_rows.append({"model": name, "candidate": candidate_index, "cv_pr_auc_mean": mean_pr,
                            "cv_pr_auc_sd": float(np.std(fold_scores, ddof=1)), "folds": len(fold_scores)})
            candidate_scores.append((mean_pr, model))
        best_cv, best_model = max(candidate_scores, key=lambda item: item[0])
        fitted = Pipeline([("preprocess", make_preprocessor()), ("model", best_model)])
        fitted.fit(x_train, y_train)
        predicted, score = _positive_score(fitted, x_test)
        has_probability = not isinstance(best_model, LinearSVC)
        holdout_rows.append(metric_row(name, y_test, predicted, score, has_probability))
        subgroup_rows.extend(subgroup_metric_rows(name, y_test, predicted, x_test))
        predictions_for_bootstrap[name] = predicted
        scores_for_plot[name] = (score, has_probability)
        print(f"Completed model: {name}; selected grouped-CV PR-AUC={best_cv:.4f}.", flush=True)

    cv_frame = pd.DataFrame(cv_rows)
    metric_frame = pd.DataFrame(holdout_rows)
    bootstrap_intervals = {
        name: cluster_bootstrap_intervals(
            y_test,
            predictions_for_bootstrap[name],
            score,
            groups.iloc[test_idx],
            has_probability,
            replicates=BOOTSTRAP_REPLICATES,
            seed=seed + 2,
        )
        for name, (score, has_probability) in scores_for_plot.items()
    }
    output_path.parent.mkdir(parents=True, exist_ok=True)
    plot_path = output_path.parent / "model_comparison.png"
    fig, axes = plt.subplots(2, 2, figsize=(14, 10))
    ax, roc_ax, pr_ax, calibration_ax = axes.flatten()
    ax.bar(metric_frame["model"], metric_frame["pr_auc"], color="#7257A8")
    ax.set_ylabel("PR-AUC sin ponderar")
    ax.set_title("Resumen de rendimiento")
    ax.tick_params(axis="x", rotation=25)
    for name, (score, _) in scores_for_plot.items():
        fpr, tpr, _ = roc_curve(y_test, score)
        roc_ax.plot(fpr, tpr, label=name)
        precision, recall, _ = precision_recall_curve(y_test, score)
        pr_ax.plot(recall, precision, label=name)
    pr_ax.axhline(
        holdout_prevalence,
        linestyle="--",
        color="gray",
        label="Prevalencia del holdout (referencia)",
    )
    roc_ax.plot([0, 1], [0, 1], linestyle="--", color="gray", label="Azar")
    roc_ax.set(xlabel="Tasa de falsos positivos", ylabel="Sensibilidad", title="ROC sin ponderar", xlim=(0, 1), ylim=(0, 1))
    roc_ax.legend(fontsize="small")
    pr_ax.set(xlabel="Recall", ylabel="Precision", title="Precisión-recall sin ponderar", xlim=(0, 1), ylim=(0, 1))
    pr_ax.legend(fontsize="small")
    for name, (probabilities, has_probability) in scores_for_plot.items():
        if not has_probability:
            continue
        predicted_mean, observed = calibration_points(y_test, probabilities)
        calibration_ax.plot(predicted_mean, observed, marker=".", label=name)
    calibration_ax.plot([0, 1], [0, 1], linestyle="--", color="gray", label="Ideal")
    calibration_ax.set(xlabel="Probabilidad media predicha", ylabel="Frecuencia observada",
                       title="Calibración (SVM usa score, no probabilidad)", xlim=(0, 1), ylim=(0, 1))
    calibration_ax.legend(fontsize="small")
    fig.tight_layout()
    fig.savefig(plot_path, dpi=160)
    plt.close(fig)

    result = {
        "eligible_rows": int(len(x)),
        "positive_rows": int(y.sum()),
        "positive_rate_unweighted": float(y.mean()),
        "holdout_prevalence": holdout_prevalence,
        "clusters": int(groups.nunique()),
        "strata": int(len(stratum_summary)),
        "strata_without_positive": int((stratum_summary["positives"] == 0).sum()),
        "positive_strata_under_five_clusters": int((positive_clusters_by_stratum < 5).sum()),
        "train_rows": int(len(train_idx)),
        "test_rows": int(len(test_idx)),
        "train_clusters": int(g_train.nunique()),
        "test_clusters": int(groups.iloc[test_idx].nunique()),
        "seed": seed,
        "features": FEATURES,
        "target": TARGET,
        "cv": cv_rows,
        "metrics": holdout_rows,
        "subgroups": subgroup_rows,
        "bootstrap_intervals": bootstrap_intervals,
        "bootstrap_replicates": BOOTSTRAP_REPLICATES,
        "known_caveats": [
            "D107 es violencia severa reportada en la encuesta; no feminicidio ni evento futuro.",
            "La elegibilidad requiere D107 observado y V044=1. Los vacíos de D107 coinciden con V501=0/V502=0 (nunca casada); el cuestionario 2025 dirige ese estado al ítem 1002 y no muestra un salto que por sí solo explique los vacíos. No se presume que sean negativos ni se usan V501/V502 como filtros.",
            "Entre D107 observado y V044=1, D114 solo está vacío cuando D113=0; se conserva como categoría faltante y no se convierte en el código 0 ('Nunca'). La API experimental valida esta relación condicional.",
            "El análisis usa métricas y ajuste sin ponderar, a nivel de la muestra elegible; no produce estimaciones poblacionales ni inferencia oficial del diseño ENDES.",
            "La ficha ENDES 2024 del indicador de violencia especifica V005/1,000,000 salvo en 2020; el informe PPR del primer semestre 2025 documenta V005 para indicadores de mujeres y reporta resultados de violencia. No se encontró una especificación 2025 directa para D107 y la elegibilidad exacta V044=1. Por ello, el experimento conserva métricas sin ponderar, no hace estimaciones poblacionales y deja cualquier análisis con V005 como sensibilidad metodológica separada.",
            "El holdout se separa por V021, pero esto no reemplaza una estimación completa de varianza del diseño complejo.",
            "Los intervalos son bootstrap percentil por conglomerado sin respetar V022; son exploratorios y no son intervalos oficiales del diseño ENDES.",
            "V022 se analizó pero no se usó para estratificar los folds: hay estratos sin positivos y numerosas celdas positivas con menos de cinco conglomerados.",
            "Los resultados son exploratorios, una sola encuesta/año, sin validación externa ni instrumento equivalente a las 12 preguntas del producto.",
            "Los umbrales de clasificación son los predeterminados; no se ajustaron para optimizar decisiones clínicas.",
        ],
    }
    candidate_row = cv_frame.loc[cv_frame["cv_pr_auc_mean"].idxmax()]
    candidate_model = candidates[str(candidate_row["model"])][int(candidate_row["candidate"]) - 1]
    candidate_pipeline = Pipeline([("preprocess", make_preprocessor()), ("model", candidate_model)])
    candidate_pipeline.fit(x, y)
    artifact_dir = DEFAULT_ARTIFACT_DIR
    artifact_dir.mkdir(parents=True, exist_ok=True)
    artifact_name = f"{ARTIFACT_VERSION}.joblib"
    artifact_path = artifact_dir / artifact_name
    temporary_artifact_path = artifact_dir / f"{ARTIFACT_VERSION}.tmp.joblib"
    joblib.dump(candidate_pipeline, temporary_artifact_path, compress=3)
    temporary_artifact_path.replace(artifact_path)
    artifact_hash = hashlib.sha256()
    with artifact_path.open("rb") as artifact_stream:
        for chunk in iter(lambda: artifact_stream.read(1024 * 1024), b""):
            artifact_hash.update(chunk)
    archive_hash = hashlib.sha256()
    with archive_path.open("rb") as archive_stream:
        for chunk in iter(lambda: archive_stream.read(1024 * 1024), b""):
            archive_hash.update(chunk)
    metadata = {
        "model_version": ARTIFACT_VERSION,
        "artifact_file": artifact_name,
        "artifact_sha256": artifact_hash.hexdigest(),
        "artifact_scope": "research_only_retrospective_classification",
        "product_model_selected": False,
        "model_family": str(candidate_row["model"]),
        "candidate_index": int(candidate_row["candidate"]),
        "selection_criterion": "highest mean unweighted PR-AUC in 3-fold StratifiedGroupKFold by V021 on the training partition",
        "cross_validation_pr_auc_mean": float(candidate_row["cv_pr_auc_mean"]),
        "cross_validation_pr_auc_sd": float(candidate_row["cv_pr_auc_sd"]),
        "refit_on_all_eligible_rows_after_holdout_evaluation": True,
        "training_rows": int(len(x)),
        "target": TARGET,
        "target_definition": "D107=1 when severe violence was reported (derived from D105E/F/G); D107=0 otherwise, among observed eligible responses",
        "features": FEATURES,
        "feature_codes_are_original_categories": True,
        "decision_threshold": "estimator default predict() threshold; not clinically selected",
        "source_archive_sha256": archive_hash.hexdigest(),
        "python_version": __import__("platform").python_version(),
        "pandas_version": pd.__version__,
        "scikit_learn_version": sklearn.__version__,
        "xgboost_version": __import__("xgboost").__version__,
        "joblib_version": joblib.__version__,
        "limitations": [
            "not a future femicide prediction",
            "not clinically validated",
            "not calibrated for individual decisions or ENDES complex-design population inference",
            "not compatible with the product V1-V12 form",
            "research candidate only; no model selected for product use",
        ],
    }
    metadata_path = artifact_dir / f"{ARTIFACT_VERSION}.json"
    temporary_metadata_path = artifact_dir / f"{ARTIFACT_VERSION}.tmp.json"
    temporary_metadata_path.write_text(json.dumps(metadata, indent=2, ensure_ascii=False), encoding="utf-8")
    temporary_metadata_path.replace(metadata_path)
    result["candidate_artifact"] = {
        "model_version": ARTIFACT_VERSION,
        "model_family": metadata["model_family"],
        "cv_pr_auc_mean": metadata["cross_validation_pr_auc_mean"],
        "metadata_file": metadata_path.name,
        "artifact_file": artifact_name,
        "product_model_selected": False,
    }
    output_path.write_text(render_report(result, cv_frame, metric_frame, plot_path.name), encoding="utf-8")
    return result


def render_report(result: dict[str, Any], cv: pd.DataFrame, metrics: pd.DataFrame, figure_name: str) -> str:
    top_cv = cv.loc[cv["cv_pr_auc_mean"].idxmax()]
    top_holdout = metrics.loc[metrics["pr_auc"].idxmax()]
    lines = [
        "# Comparación experimental de modelos ENDES 2025", "",
        "> **Experimento retrospectivo, no clínico.** D107 representa violencia severa reportada (combinación derivada de D105E/F/G). No predice feminicidio ni riesgo futuro.", "",
        "## Diseño", "",
        f"- Elegibles con D107 observado y `V044=1`: {result['eligible_rows']:,}; positivos reportados: {result['positive_rows']:,} ({result['positive_rate_unweighted']:.2%}, frecuencia sin ponderar). Los vacíos corresponden al patrón V501=0/V502=0; el cuestionario 2025 dirige 'Nunca casada' al ítem 1002 y no muestra un salto explícito que explique por sí solo los vacíos. No se recodifican como negativos ni se filtra por estado conyugal.",
        f"- Conglomerados: {result['clusters']:,}. División de holdout: {result['train_rows']:,} registros/{result['train_clusters']:,} conglomerados para entrenamiento y {result['test_rows']:,}/{result['test_clusters']:,} para prueba.",
        f"- Prevalencia positiva sin ponderar del holdout (referencia para PR-AUC): {result['holdout_prevalence']:.4f}.",
        f"- Diseño por estrato: {result['strata']:,} estratos V022 observados; {result['strata_without_positive']} sin positivos D107 y {result['positive_strata_under_five_clusters']} estratos positivos con menos de cinco conglomerados. Los folds se estratifican por etiqueta y agrupan por V021; V022 no se fuerza en la partición debido a la escasez de positivos en algunas celdas.",
        f"- Semilla: `{result['seed']}`. Pliegues: 3 folds agrupados y estratificados; prueba final separada por V021.",
        "- Entradas predeclaradas: D101A-F, D103A/B/D, D105A-D, D113/D114, V012, V025, V106 y V190. Se excluyen CASEID, componentes de D107 (D105E/F/G), agregados D106/D107/D108 y variables de diseño/ponderación como predictores.",
        "- Códigos categóricos se mantienen como categorías; desconocidos/vacíos no se convierten a ‘No’. Imputación y escalado se ajustan dentro de cada pliegue por medio del pipeline.",
        "- Se ajusta entre dos configuraciones acotadas por modelo. Criterio de selección: PR-AUC sin ponderar en validación agrupada. El ajuste y las métricas se calculan a nivel de la muestra elegible; no son estimaciones poblacionales.",
        "- No se aplicó sobremuestreo ni pesos de clase sintéticos; se mantuvo la prevalencia de la muestra elegible y se priorizó PR-AUC/recall al evaluar el desbalance.",
        "- El algoritmo SVM se implementa como LinearSVC; produce un score de decisión, no probabilidades calibradas, por lo que no se calcula Brier ni se muestra en la curva de calibración.",
        "",
        "## Selección en validación cruzada", "",
        "| Modelo | Configuración | PR-AUC promedio | Desv. estándar |", "|---|---:|---:|---:|",
    ]
    best_cv = cv.sort_values("cv_pr_auc_mean", ascending=False).groupby("model", as_index=False).first()
    lines.extend(f"| {r.model} | {int(r.candidate)} | {r.cv_pr_auc_mean:.4f} | {r.cv_pr_auc_sd:.4f} |" for r in best_cv.itertuples())
    lines.extend(["", "## Holdout por conglomerado", "",
                  "Métricas sin ponderar de la muestra elegible; no representan estimaciones poblacionales corregidas por el diseño complejo.", "",
                  "| Modelo | Accuracy | Precision | Recall | F1 | Especificidad | ROC-AUC | PR-AUC | Brier |", "|---|---:|---:|---:|---:|---:|---:|---:|---:|"])
    for row in metrics.itertuples():
        brier = "N/D (sin probabilidad)" if pd.isna(row.brier) else f"{row.brier:.4f}"
        lines.append(f"| {row.model} | {row.accuracy:.4f} | {row.precision:.4f} | {row.recall:.4f} | {row.f1:.4f} | {row.specificity:.4f} | {row.roc_auc:.4f} | {row.pr_auc:.4f} | {brier} |")
    lines.extend(["", f"### Intervalos percentiles del 95% (bootstrap por conglomerado, {result['bootstrap_replicates']} réplicas)", "",
                  "Cada réplica remuestrea conglomerados V021 completos. Este bootstrap exploratorio no estratifica por V022 ni sustituye la varianza oficial del diseño ENDES.", "",
                  "| Modelo | Recall | Especificidad | PR-AUC | ROC-AUC | Brier |", "|---|---:|---:|---:|---:|---:|"])
    for row in metrics.itertuples():
        intervals = result["bootstrap_intervals"][row.model]
        def interval_text(metric: str) -> str:
            interval = intervals.get(metric)
            return "N/D" if interval is None else f"[{interval[0]:.4f}, {interval[1]:.4f}]"
        lines.append(f"| {row.model} | {interval_text('recall')} | {interval_text('specificity')} | {interval_text('pr_auc')} | {interval_text('roc_auc')} | {interval_text('brier')} |")
    lines.extend(["", "### Confusión absoluta (umbral predeterminado)", "", "| Modelo | TN | FP | FN | TP |", "|---|---:|---:|---:|---:|"])
    lines.extend(f"| {r.model} | {r.tn} | {r.fp} | {r.fn} | {r.tp} |" for r in metrics.itertuples())
    lines.extend(["", "### Rendimiento descriptivo por subgrupos del holdout", "",
                  "Resultados por códigos V025 (área), V106 (educación) y V190 (riqueza), con las definiciones del diccionario local. Son descriptivos; no constituyen una prueba de equidad y no incluyen intervalos de incertidumbre.", "",
                  "| Modelo | Variable | Código | n | D107 positivos | Recall | Especificidad | Falsos negativos |", "|---|---|---:|---:|---:|---:|---:|---:|"])
    for row in result["subgroups"]:
        recall_value = "N/D" if row["recall"] is None else f"{row['recall']:.4f}"
        specificity_value = "N/D" if row["specificity"] is None else f"{row['specificity']:.4f}"
        lines.append(f"| {row['model']} | `{row['field']}` | {row['code']} | {row['rows']} | {row['positive_rows']} | {recall_value} | {specificity_value} | {row['false_negatives']} |")
    lines.extend(["", f"![Curvas ROC, precisión-recall y calibración del holdout]({figure_name})", "",
                  "## Lectura responsable", "",
                  f"En esta corrida, {top_cv['model']} obtuvo el mayor PR-AUC medio de validación agrupada ({top_cv['cv_pr_auc_mean']:.4f}) y {top_holdout['model']} el mayor PR-AUC del holdout ({top_holdout['pr_auc']:.4f}). Las diferencias son pequeñas y provienen de una única partición final; no bastan para elegir un modelo generalizable. {top_holdout['model']} tuvo {int(top_holdout['fn'])} falsos negativos entre {int(top_holdout['fn'] + top_holdout['tp'])} positivos observados en el holdout, bajo el umbral predeterminado.", "",
                  "El algoritmo con el mayor PR-AUC observado en este único holdout no se declara modelo ganador para uso real. La comparación no constituye validación externa, evaluación de calibración por diseño muestral ni validación clínica. Los falsos negativos son errores de clasificación de D107 retrospectivo y no estiman falsos negativos de feminicidio futuro.", "",
                  "La encuesta es transversal y no tiene desenlace individual posterior ni horizonte temporal para feminicidio. Diagnóstico para predicción futura: **NO APTO con ENDES 2025 sola**. El experimento de D107 es **APTO CON LIMITACIONES** únicamente como clasificación retrospectiva exploratoria.", "",
                  "## Limitaciones y siguientes validaciones", ""])
    lines.extend(f"- {item}" for item in result["known_caveats"])
    lines.extend(["- Se requiere documentar elegibilidad y saltos del cuestionario con el manual completo, verificar la unidad de análisis, e incorporar inferencia de encuesta (estratos, conglomerados y pesos) con un especialista en muestreo.",
                  "- Se requiere validación temporal o externa independiente antes de afirmar generalización; para feminicidio futuro hacen falta datos longitudinales enlazados con desenlace y fecha, además de protocolo ético/legal.",
                  "- Las 12 preguntas del producto no son equivalentes a estas variables ENDES y no se deben enviar a estos modelos.",
                  f"- Se guarda el candidato de mejor PR-AUC CV como artefacto local ignorado por Git (`artifacts/{result['candidate_artifact']['artifact_file']}`) con metadatos (`artifacts/{result['candidate_artifact']['metadata_file']}`). Se reentrena con todas las filas elegibles después de la evaluación, pero no se selecciona para el producto ni se habilita como predictor de feminicidio.",
                  "- No se guarda dataset procesado ni identificadores; el artefacto solo se puede reproducir con el ZIP original local protegido por `.gitignore`.", "",
                  "Fuentes: INEI ENDES 2025 ficha técnica y diccionario REC84DV, referenciados en [`data_audit.md`](data_audit.md).", ""])
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--archive", type=Path, default=DEFAULT_ARCHIVE)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--seed", type=int, default=2026)
    args = parser.parse_args()
    result = run_experiment(args.archive, args.output, args.seed)
    print(json.dumps({key: result[key] for key in ["eligible_rows", "positive_rows", "clusters", "train_rows", "test_rows"]}, ensure_ascii=False))
    print(f"Aggregated report written to {args.output}; no row-level data or identifiers persisted.")


if __name__ == "__main__":
    main()
