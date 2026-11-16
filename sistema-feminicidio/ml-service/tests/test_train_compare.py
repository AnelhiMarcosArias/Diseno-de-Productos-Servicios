import numpy as np
import pandas as pd

from scripts.train_compare import (
    FEATURES,
    FORBIDDEN,
    cluster_bootstrap_intervals,
    metric_row,
    subgroup_metric_rows,
    calibration_points,
)


def test_model_features_exclude_target_components_identifiers_and_design_fields():
    assert set(FEATURES).isdisjoint(FORBIDDEN)
    assert "D105E" not in FEATURES
    assert "D105F" not in FEATURES
    assert "D105G" not in FEATURES
    assert "D106" not in FEATURES
    assert "D107" not in FEATURES
    assert "CASEID" not in FEATURES
    assert "V021" not in FEATURES
    assert "V005" not in FEATURES


def test_metric_row_reports_unweighted_sample_metrics_and_confusion_counts():
    result = metric_row(
        "fixture",
        pd.Series([0, 0, 1, 1]),
        np.array([0, 1, 0, 1]),
        np.array([0.1, 0.8, 0.2, 0.9]),
        has_probability=True,
    )
    assert result["specificity"] == 0.5
    assert result["brier"] is not None
    assert result["fn"] == 1
    assert result["tn"] == 1


def test_calibration_returns_probability_and_observed_sample_means():
    predicted, observed = calibration_points(
        pd.Series([0, 0, 1, 1]),
        np.array([0.1, 0.2, 0.7, 0.8]),
        bins=2,
    )
    assert len(predicted) == 2
    assert np.allclose(predicted, [0.15, 0.75])
    assert np.allclose(observed, [0.0, 1.0])


def test_subgroup_metrics_report_unweighted_counts_and_false_negative_rates():
    x = pd.DataFrame({
        "V025": ["1", "1", "2"],
        "V106": ["1", "2", "2"],
        "V190": ["1", "2", "3"],
    })
    rows = subgroup_metric_rows(
        "fixture",
        pd.Series([1, 0, 1]),
        np.array([0, 0, 1]),
        x,
    )
    urban = next(row for row in rows if row["field"] == "V025" and row["code"] == "1")
    assert urban["rows"] == 2
    assert urban["positive_rows"] == 1
    assert urban["false_negatives"] == 1
    assert urban["recall"] == 0.0


def test_cluster_bootstrap_intervals_are_deterministic_and_skip_brier_without_probabilities():
    y = pd.Series([0, 1, 0, 1, 0, 1, 0, 1])
    predicted = np.array([0, 1, 1, 1, 0, 0, 0, 1])
    scores = np.array([0.1, 0.9, 0.7, 0.8, 0.2, 0.4, 0.3, 0.75])
    clusters = pd.Series(["a", "a", "b", "b", "c", "c", "d", "d"])

    first = cluster_bootstrap_intervals(
        y, predicted, scores, clusters, has_probability=False,
        replicates=50, seed=17,
    )
    second = cluster_bootstrap_intervals(
        y, predicted, scores, clusters, has_probability=False,
        replicates=50, seed=17,
    )

    assert first == second
    assert {"recall", "specificity", "pr_auc", "roc_auc"}.issubset(first)
    assert "brier" not in first
    assert all(lower <= upper for lower, upper in first.values())
