"""Privacy-preserving, reproducible audit of ENDES 2025 ZIP tables.

Only aggregates and variable names are written to Markdown. CASEID values and
row-level data are never emitted or persisted.
"""

from __future__ import annotations

import argparse
import hashlib
import io
import zipfile
from collections import Counter
from pathlib import Path
from typing import Any

import pandas as pd


PROJECT_DIR = Path(__file__).resolve().parents[2]
DEFAULT_ARCHIVE = (
    PROJECT_DIR / "Proyecto_Feminicidio_Datos" / "01_Datos_Originales" / "2025.zip"
)
DEFAULT_OUTPUT = PROJECT_DIR / "ml-service" / "docs" / "data_audit.md"
DESIGN_COLUMNS = [
    "CASEID",
    "V001",
    "V005",
    "V021",
    "V022",
    "V044",
    "V023",
    "V024",
    "V025",
    "V012",
    "V106",
    "V190",
]
RELEVANT_PREFIXES = ("D101", "D103", "D105", "D110")
RELEVANT_EXACT = {"D104", "D106", "D107", "D108", "D109", "D112", "D113", "D114"}
LIKELY_FEATURES = [
    "D101A",
    "D101B",
    "D101C",
    "D101D",
    "D101E",
    "D101F",
    "D103A",
    "D103B",
    "D103C",
    "D103D",
    "D104",
    "D105A",
    "D105B",
    "D105C",
    "D105D",
    "D110A",
    "D110B",
    "D110C",
    "D113",
    "D114",
]


def read_nested_csv(archive: zipfile.ZipFile, filename: str) -> pd.DataFrame:
    """Read one uniquely named CSV from a nested ZIP without extracting it."""
    matches: list[str] = []
    payload: bytes | None = None
    for outer_name in archive.namelist():
        if not outer_name.lower().endswith(".zip"):
            continue
        with archive.open(outer_name) as nested_stream:
            with zipfile.ZipFile(nested_stream) as nested:
                for inner_name in nested.namelist():
                    if Path(inner_name).name.casefold() == filename.casefold():
                        matches.append(f"{outer_name}/{inner_name}")
                        if payload is None:
                            payload = nested.read(inner_name)
    if len(matches) != 1:
        raise ValueError(f"Expected one {filename} in the source archive; found {len(matches)}.")
    assert payload is not None
    frame = pd.read_csv(io.BytesIO(payload), dtype="string", keep_default_na=False)
    for column in frame.columns:
        frame[column] = frame[column].str.strip()
    return frame.replace(r"^\s*$", pd.NA, regex=True)


def duplicate_key_count(frame: pd.DataFrame) -> int:
    if "CASEID" not in frame:
        raise ValueError("The expected join key CASEID is absent.")
    keys = frame["CASEID"].dropna()
    return int(keys.duplicated().sum())


def file_fingerprint(path: Path) -> dict[str, str | int]:
    """Identify the local source archive without exposing its contents."""
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return {
        "file_name": path.name,
        "size_bytes": path.stat().st_size,
        "sha256": digest.hexdigest(),
    }


def distribution(frame: pd.DataFrame, column: str) -> dict[str, int]:
    """Return code counts for a non-identifier field, including missing values."""
    if column == "CASEID":
        raise ValueError("Identifier distributions are forbidden in audit output.")
    counts = frame[column].fillna("<blank>").value_counts(dropna=False).to_dict()
    return {str(code): int(count) for code, count in sorted(counts.items(), key=lambda item: str(item[0]))}


def _type_counts(frame: pd.DataFrame) -> Counter[str]:
    result: Counter[str] = Counter()
    for column in frame.columns:
        values = frame[column].dropna()
        if values.empty:
            result["all blank"] += 1
        elif pd.to_numeric(values, errors="coerce").notna().all():
            result["numeric-compatible codes"] += 1
        else:
            result["text or identifier"] += 1
    return result


def create_summary(
    violence: pd.DataFrame,
    individual: pd.DataFrame,
    relationship: pd.DataFrame,
) -> dict[str, Any]:
    if any(duplicate_key_count(frame) for frame in (violence, individual, relationship)):
        raise ValueError("Duplicate CASEID keys must be resolved before joining.")
    missing_violence = sorted({"D107", "D113", "D114"} - set(violence.columns))
    if missing_violence:
        raise ValueError(f"REC84DV is missing expected target/conditional fields: {missing_violence}")

    missing_design = sorted(set(DESIGN_COLUMNS) - set(individual.columns))
    if missing_design:
        raise ValueError(f"REC0111 is missing expected design fields: {missing_design}")
    missing_relationship = sorted({"V501", "V502"} - set(relationship.columns))
    if missing_relationship:
        raise ValueError(f"RE516171 is missing expected relationship fields: {missing_relationship}")

    design = individual[DESIGN_COLUMNS].copy()
    design["CASEID"] = design["CASEID"].str.strip()
    relationship_data = relationship[["CASEID", "V501", "V502"]].copy()
    relationship_data["CASEID"] = relationship_data["CASEID"].str.strip()
    source = violence.copy()
    source["CASEID"] = source["CASEID"].str.strip()
    merged = source.merge(
        design,
        on="CASEID",
        how="left",
        validate="one_to_one",
        indicator=True,
    )
    merged = merged.merge(
        relationship_data,
        on="CASEID",
        how="left",
        validate="one_to_one",
    )

    relevant = [
        column
        for column in violence.columns
        if column.startswith(RELEVANT_PREFIXES) or column in RELEVANT_EXACT
    ]
    variable_counts = {
        column: distribution(violence, column)
        for column in relevant
        if column != "CASEID"
    }
    missing_counts = violence.isna().sum().sort_values(ascending=False)
    missing_top = [
        {
            "variable": str(column),
            "missing": int(count),
            "percent": round(100 * int(count) / len(violence), 2),
        }
        for column, count in missing_counts.head(12).items()
        if count > 0
    ]

    d107 = variable_counts.get("D107", {})
    numeric_weights = pd.to_numeric(merged["V005"], errors="coerce")
    eligible_item_count = int(violence.get("D101A", pd.Series(dtype="string")).notna().sum())
    d107_eligible = merged[merged["D107"].isin(["0", "1"])].copy()
    d107_by_stratum = d107_eligible.groupby("V022", dropna=False).agg(
        rows=("D107", "size"), clusters=("V021", "nunique"),
        positives=("D107", lambda values: int((values == "1").sum())),
    )
    positive_cells = (
        d107_eligible[d107_eligible["D107"] == "1"]
        .groupby("V022", dropna=False)["V021"].nunique()
    )
    selection_status = merged["V044"].fillna("<blank>").astype(str)
    selection_target_counts: dict[str, dict[str, int]] = {}
    for status, rows in merged.groupby(selection_status, dropna=False):
        observed = rows["D107"].isin(["0", "1"])
        selection_target_counts[str(status)] = {
            "rows": int(len(rows)),
            "d107_observed": int(observed.sum()),
            "d107_missing": int((~observed).sum()),
            "d107_positive": int((rows.loc[observed, "D107"] == "1").sum()),
        }
    relationship_status = merged["V501"].fillna("<blank>").astype(str)
    relationship_target_counts: dict[str, dict[str, int]] = {}
    for status, rows in merged.groupby(relationship_status, dropna=False):
        observed = rows["D107"].isin(["0", "1"])
        relationship_target_counts[str(status)] = {
            "rows": int(len(rows)),
            "d107_observed": int(observed.sum()),
            "d107_missing": int((~observed).sum()),
            "d107_positive": int((rows.loc[observed, "D107"] == "1").sum()),
        }
    selection_relationship_target_counts: dict[str, dict[str, int]] = {}
    for (selection, marital_status, union_status), rows in merged.groupby(
        ["V044", "V501", "V502"], dropna=False
    ):
        observed = rows["D107"].isin(["0", "1"])
        selection_relationship_target_counts[
            f"{selection}/{marital_status}/{union_status}"
        ] = {
            "rows": int(len(rows)),
            "d107_observed": int(observed.sum()),
            "d107_missing": int((~observed).sum()),
            "d107_positive": int((rows.loc[observed, "D107"] == "1").sum()),
        }
    eligible_d107 = merged[
        merged["V044"].eq("1") & merged["D107"].isin(["0", "1"])
    ]
    d114_by_d113: dict[str, dict[str, int]] = {}
    for d113, rows in eligible_d107.groupby("D113", dropna=False):
        d114_values = rows["D114"].fillna("<blank>").astype(str)
        d114_by_d113[str(d113)] = {
            "rows": int(len(rows)),
            "d114_missing": int((d114_values == "<blank>").sum()),
            "d114_observed": int((d114_values != "<blank>").sum()),
        }
    relationship_union_target_counts: dict[str, dict[str, int]] = {}
    for (marital_status, union_status), rows in merged.groupby(
        ["V501", "V502"], dropna=False
    ):
        observed = rows["D107"].isin(["0", "1"])
        relationship_union_target_counts[f"{marital_status}/{union_status}"] = {
            "rows": int(len(rows)),
            "d107_observed": int(observed.sum()),
            "d107_missing": int((~observed).sum()),
            "d107_positive": int((rows.loc[observed, "D107"] == "1").sum()),
        }

    return {
        "rows": int(len(violence)),
        "columns": int(len(violence.columns)),
        "distinct_case_keys": int(violence["CASEID"].nunique(dropna=True)),
        "blank_case_keys": int(violence["CASEID"].isna().sum()),
        "duplicate_case_keys": duplicate_key_count(violence),
        "design_rows_unmatched": int((merged["_merge"] != "both").sum()),
        "design_cluster_count": int(merged["V021"].nunique(dropna=True)),
        "design_strata_count": int(merged["V022"].nunique(dropna=True)),
        "invalid_or_missing_weights": int((numeric_weights.isna() | (numeric_weights <= 0)).sum()),
        "violence_specific_weight_fields_present": [
            column for column in ("D005", "V005V") if column in violence.columns
        ],
        "selection_status_counts": distribution(individual, "V044"),
        "selection_target_counts": selection_target_counts,
        "relationship_target_counts": relationship_target_counts,
        "relationship_union_target_counts": relationship_union_target_counts,
        "selection_relationship_target_counts": selection_relationship_target_counts,
        "d114_by_d113": d114_by_d113,
        "d107_observed_rows": int(len(d107_eligible)),
        "d107_strata_without_positive": int((d107_by_stratum["positives"] == 0).sum()),
        "d107_positive_stratum_cells_under_five_clusters": int((positive_cells < 5).sum()),
        "d107_strata_with_observed_target": int(len(d107_by_stratum)),
        "records_with_D101A_answer": eligible_item_count,
        "type_counts": dict(sorted(_type_counts(violence).items())),
        "missing_top": missing_top,
        "variables": variable_counts,
        "d107_counts": d107,
        "candidate_features_present": [
            column for column in LIKELY_FEATURES if column in violence.columns
        ],
        "candidate_features_all_blank": [
            column
            for column in LIKELY_FEATURES
            if column in violence.columns and violence[column].notna().sum() == 0
        ],
    }


def render_markdown(summary: dict[str, Any]) -> str:
    source_archive = summary.get("source_archive")
    lines = [
        "# Auditoría agregada de ENDES 2025: REC84DV",
        "",
        "> Informe reproducible. No contiene filas, identificadores ni valores individuales.",
        "",
        "## Cobertura y calidad",
        "",
        f"- Registros: {summary['rows']:,}.",
        f"- Variables: {summary['columns']}.",
        f"- CASEID no vacíos distintos: {summary['distinct_case_keys']:,}; duplicados: {summary['duplicate_case_keys']}; vacíos: {summary['blank_case_keys']}.",
        f"- Filas sin correspondencia al unir el módulo individual por CASEID: {summary['design_rows_unmatched']}.",
        f"- Conglomerados V021 observados: {summary['design_cluster_count']}; estratos V022: {summary['design_strata_count']}.",
        f"- Pesos V005 vacíos, no numéricos o no positivos: {summary['invalid_or_missing_weights']}.",
        f"- Estado de selección V044 en REC0111 (todas las filas del archivo individual): " + "; ".join(f"`{code}`: {count:,}" for code, count in summary["selection_status_counts"].items()) + ".",
        "- D107 por V044 en REC84DV enlazado: " + "; ".join(
            f"`{code}`: {values['d107_observed']:,} observados/{values['rows']:,}, {values['d107_missing']:,} sin D107 y {values['d107_positive']:,} positivos"
            for code, values in summary["selection_target_counts"].items()
        ) + ".",
        "- D107 por estado civil V501 (RE516171): " + "; ".join(
            f"`{code}`: {values['d107_observed']:,} observados/{values['rows']:,}, {values['d107_missing']:,} sin D107 y {values['d107_positive']:,} positivos"
            for code, values in summary["relationship_target_counts"].items()
        ) + ".",
        "- D107 por estado civil V501 y situación conyugal V502 (RE516171): " + "; ".join(
            f"`{code}`: {values['d107_observed']:,} observados/{values['rows']:,}, {values['d107_missing']:,} sin D107 y {values['d107_positive']:,} positivos"
            for code, values in summary["relationship_union_target_counts"].items()
        ) + ".",
        f"- Campos de ponderación específicos del módulo encontrados en REC84DV: {', '.join(summary['violence_specific_weight_fields_present']) or 'ninguno (no se encontró D005 ni V005V)' }.",
        f"- En filas con D107 observado: {summary['d107_strata_with_observed_target']} estratos V022; {summary['d107_strata_without_positive']} sin positivos y {summary['d107_positive_stratum_cells_under_five_clusters']} estratos positivos con menos de cinco conglomerados.",
        f"- Registros con D101A no vacío: {summary['records_with_D101A_answer']:,}. Este conteo no se interpreta por sí solo como elegibilidad.",
        "",
        "## Identificación y procedencia del archivo",
        "",
        *(
            [
                f"- Archivo local utilizado: `{source_archive['file_name']}` ({source_archive['size_bytes']:,} bytes).",
                f"- SHA-256 del archivo local: `{source_archive['sha256']}`.",
            ]
            if source_archive
            else ["- Huella del archivo no disponible en esta llamada de resumen en memoria."]
        ),
        "- El catálogo oficial del INEI publica un recurso denominado Data completa ENDES 2025. La estructura interna local incluye tablas ENDES 2025 compatibles con el análisis.",
        "- La huella identifica los bytes usados para reproducir este informe; no demuestra por sí sola que el ZIP local sea idéntico al recurso oficial. No se dispuso de una suma de verificación oficial para compararla.",
        "",
        "### Tipos de almacenamiento observados",
        "",
        "Los CSV se leen como cadenas para conservar los códigos originales. La siguiente clasificación es solo una comprobación de compatibilidad numérica; la interpretación semántica requiere el diccionario oficial.",
        "",
    ]
    lines.extend(f"- {kind}: {count} columnas." for kind, count in summary["type_counts"].items())
    lines.extend(["", "### Mayor proporción de valores vacíos", "", "| Variable | Vacíos | % |", "|---|---:|---:|"])
    lines.extend(
        f"| {item['variable']} | {item['missing']:,} | {item['percent']:.2f}% |"
        for item in summary["missing_top"]
    )
    lines.extend(["", "## Distribución de códigos de violencia", "", "Las frecuencias son agregadas y conservan el código literal; no traducen códigos especiales automáticamente.", "", "| Variable | Códigos y recuentos |", "|---|---|"])
    lines.extend(
        f"| {variable} | " + "; ".join(f"`{code}`: {count:,}" for code, count in counts.items()) + " |"
        for variable, counts in summary["variables"].items()
    )
    lines.extend(
        [
            "",
            "## Correspondencia preliminar con las preguntas de la aplicación",
            "",
            "El frontend y el backend guardan doce booleanos genéricos; no hay correspondencia validada con ENDES. Esta tabla indica cercanía textual, no una recodificación ni autorización para usar estas respuestas como entradas del modelo.",
            "",
            "| Pregunta del frontend | Campo(s) ENDES cercano(s) | Diferencia que impide equipararlos |",
            "|---|---|---|",
            "| Celos y aislamiento | D101A, D101C, D101D | ENDES separa celos, impedir visitas de amistades y limitar contactos familiares; la aplicación los agrupa y añade estudios. |",
            "| Control de actividades | D101E, D101F | ENDES pregunta por conocer lugares y dinero; no cubre celular, redes ni ubicación digital. |",
            "| Insultos y humillación | D103A; D103C en diccionario | D103A es humillación delante de otras personas; D103C está vacío en todas las filas observadas. |",
            "| Intimidación / destrucción de objetos | Sin correspondencia exacta | D103B pregunta por amenaza de daño, no por gritos o destrucción de pertenencias. |",
            "| Agresión física | D105A, D105B | Incluyen algunos actos concretos, no toda la formulación agregada. |",
            "| Lesiones graves con atención médica | D110A-C | Hay ítems de dolor/moretones, lesiones y atención médica; deben validarse filtros y códigos antes de mapearlos. |",
            "| Escalamiento en seis meses | Sin correspondencia exacta | No se observó un campo de aumento de frecuencia/intensidad en esa ventana. |",
            "| Alcohol o sustancias | D113-D114 parcialmente | Miden consumo/frecuencia de alcohol de la pareja, no consumo problemático, drogas ni antecedentes. |",
            "| Violencia ante ruptura/separación | D109 solo como contexto | Registra el inicio de ciertos hechos relativo a la unión/separación, no una reacción violenta ante ruptura. |",
            "| Acoso persecutorio | D101E parcialmente | Saber dónde está no equivale a perseguir o acosar. |",
            "| Amenazas graves | D103B y D103D parcialmente | Amenaza de daño o de irse/quitar hijos/ayuda económica; no identifica explícitamente muerte o suicidio. |",
            "| Estrangulamiento o acceso a armas | D105E/F/G parcialmente | D105E incluye estrangulamiento o quemadura; F amenaza con arma; G agresión con arma. El acceso a armas no es medido. |",
        ]
    )
    lines.extend(
        [
            "",
            "## Desenlace retrospectivo disponible y fuga de información",
            "",
            "El diccionario identifica D107 como indicador derivado de D105E, D105F o D105G (violencia severa reportada). Si se usa D107 como etiqueta experimental, se deben excluir D105E/F/G y los agregados D106/D107/D108 de las entradas. El objetivo mide reporte retrospectivo; no mide feminicidio futuro.",
            "",
            "Recuentos observados para D107:",
            "",
        ]
    )
    lines.extend(f"- Código `{code}`: {count:,}." for code, count in summary["d107_counts"].items())
    lines.extend(
        [
            "",
            "### Diagnóstico de viabilidad",
            "",
            "| Objetivo | Estado | Motivo |",
            "|---|---|---|",
            "| A. Clasificar violencia reportada retrospectivamente | APTO CON LIMITACIONES | D104/D106/D107/D108 son indicadores observados; para D107 hay que excluir sus componentes y agregados relacionados de las entradas. La salida solo describiría reporte retrospectivo. |",
            "| B. Estimar indicadores de severidad reportada | APTO CON LIMITACIONES | Es un análisis descriptivo de la definición de la encuesta, no una herramienta clínica ni una predicción futura. |",
            "| C. Predecir violencia grave o feminicidio futuro | NO APTO con ENDES 2025 sola | No hay etiqueta individual posterior ni horizonte temporal de seguimiento en este módulo transversal. |",
            "",
            "## Diseño muestral e interpretación",
            "",
            "La ficha ENDES 2025 describe una muestra probabilística bietápica, estratificada e independiente. `V021` (conglomerado), `V022` (estrato), `V005` (factor general de ponderación de mujeres) y `V044` (selección/resultado del módulo de violencia) se enlazan desde REC0111. La ficha técnica del indicador ENDES 2024 de violencia física/sexual especifica `V005/1,000,000` para las estimaciones (con excepción de 2020, cuando indica `V005V`); el informe PPR del primer semestre de 2025 también documenta el uso de `V005` en indicadores de mujeres y publica indicadores de violencia. Sin embargo, no se encontró una especificación 2025 que documente el ajuste del factor para el objetivo retrospectivo D107 y la elegibilidad exacta `V044=1`. Este experimento conserva métricas sin ponderar y no las presenta como estimaciones poblacionales; una evaluación ponderada con V005 debe presentarse separadamente como sensibilidad metodológica, no como inferencia oficial validada.",
            "La existencia histórica de `V005V` en 2020 y su ausencia en REC84DV 2025 no prueba por sí sola que el peso V005 sea inadecuado en 2025: la ficha ENDES 2024 de violencia indica V005 salvo para 2020. Se requiere confirmar el año/versión y universo exactos antes de atribuir representatividad poblacional a métricas de D107.",
        ]
    )
    lines.extend(
        [
            "",
            "## Interpretación y límites",
            "",
        "- Entre las filas con D107 observado y V044=1, D114 está vacío en "
        + "; ".join(
            f"D113=`{code}`: {values['d114_missing']:,}/{values['rows']:,}"
            for code, values in summary["d114_by_d113"].items()
        )
        + ". En el extracto ENDES 2025, el vacío de frecuencia se conserva como categoría faltante; no se recodifica a 'Nunca'. La API experimental acepta D114 nulo únicamente cuando D113=0, según este patrón observado.",
            f"- Entre las filas con V044=1, V501=0/V502=0 concentra {summary['selection_relationship_target_counts'].get('1/0/0', {}).get('rows', 0):,} registros, de los cuales {summary['selection_relationship_target_counts'].get('1/0/0', {}).get('d107_missing', 0):,} tienen D107 vacío. Los estados V501=1:5/V502=1:2 tienen {sum(value['d107_missing'] for key, value in summary['selection_relationship_target_counts'].items() if key.startswith('1/') and key.split('/')[2] in ('1', '2')):,} vacíos. El diccionario local describe V502=0 como 'Nunca casada'. El cuestionario individual ENDES 2025 (páginas impresas 70–72) dirige 'Nunca casada' desde 1001 a 1002, pregunta por el último esposo/compañero en 1005 y pasa de 1006 a 1009 si no hubo respuestas afirmativas; esas páginas no muestran un salto explícito desde 1001 que por sí solo explique D107 vacío. El patrón es consistente con un universo condicionado por relación de pareja, pero el motivo exacto debe confirmarse contra la sintaxis/universo oficial de construcción de REC84DV; no se recodifica como negativo.",
            "- La tabla de violencia REC84DV no contiene las variables de diseño ni un peso específico visible. La auditoría enlaza V021, V022, V005 y V044 desde REC0111 por CASEID y reporta solo recuentos. V044 distingue mujeres no seleccionadas, seleccionadas/entrevistadas, sin privacidad y no entrevistadas; los vacíos de D107 no se convierten en negativos.",
            "- V501 y V502 provienen de RE516171 y solo se usan para describir la asociación con D107 no observado; no son predictores ni filtros de inclusión.",
            "- La ENDES 2025 es una encuesta transversal. Con este módulo no se observa una etiqueta de feminicidio posterior ni un horizonte temporal; no sustenta predicción futura de feminicidio.",
            "- La compatibilidad entre las doce preguntas de la aplicación y los ítems ENDES es parcial y requiere revisión del instrumento. Este informe no recodifica ni renombra esas preguntas.",
            "- No se entrenó ningún modelo en esta auditoría.",
            "",
            "## Reproducción",
            "",
            "Desde `sistema-feminicidio/ml-service`: `python .\\scripts\\audit_endes.py`.",
            "La salida contiene agregados y reemplaza este informe. Los ZIP originales se leen en memoria y no se extraen.",
            "",
            "## Fuentes metodológicas",
            "",
            "- INEI, catálogo oficial de microdatos: <https://proyectos.inei.gob.pe/microdatos/>.",
            "- Plataforma Nacional de Datos Abiertos, conjunto ENDES 2025 publicado por INEI: <https://www.datosabiertos.gob.pe/dataset/encuesta-demogr%C3%A1fica-y-de-salud-familiar-endes-2025-instituto-nacional-de-estad%C3%ADstica-e-5>.",
            "- INEI, ENDES 2025, informe preliminar y ficha técnica: <https://proyectos.inei.gob.pe/endes/2025/ppr/Informe_Indicadores_de_los_Programas_Presupuestales_ENDES_2025.pdf>.",
            "- INEI, ENDES 2025, archivo local `2025.zip`, módulo `1036-Modulo1637.zip`: contiene `Diccionario - REC84DV.pdf` y `CUESTIONARIO INDIVIDUAL ENDES 2025.pdf` (sección 10, páginas impresas 70–72).",
            "- INEI, ficha técnica del Indicador 37 ENDES 2024 (especifica V005/1,000,000, salvo el año 2020): <https://proyectos.inei.gob.pe/endes/2024/ppr2/Indicadores_de_Resultados_de_los_Programas_Presupuestales_ENDES_2024.pdf>.",
            "- INEI, indicadores ENDES primer semestre 2025 (ficha de factores muestrales de mujeres y resultados de violencia): <https://proyectos.inei.gob.pe/endes/2025/ppr2/Indicadores_de_Resultados_de_los_Programas_Presupuestales_ENDES_Primer_Semestre_2025.pdf>.",
            "- INEI, Informe Principal ENDES 2020 (documenta el factor excepcional V005V para 2020): <https://encuestas.inei.gob.pe/endes/2020/INFORME_PRINCIPAL_2020/INFORME_PRINCIPAL_ENDES_2020.pdf>.",
            "- Diccionario CSV local incluido en `Proyecto_Feminicidio_Datos/02_Diccionarios/` para los nombres y códigos del archivo 2025.",
            "",
        ]
    )
    return "\n".join(lines)


def run_audit(archive_path: Path, output_path: Path) -> dict[str, Any]:
    if not archive_path.is_file():
        raise FileNotFoundError(
            "ENDES source ZIP not found. Keep the original local under "
            "Proyecto_Feminicidio_Datos/01_Datos_Originales/."
        )
    with zipfile.ZipFile(archive_path) as archive:
        violence = read_nested_csv(archive, "REC84DV_2025.csv")
        individual = read_nested_csv(archive, "REC0111_2025.csv")
        relationship = read_nested_csv(archive, "RE516171_2025.csv")
    summary = create_summary(violence, individual, relationship)
    summary["source_archive"] = file_fingerprint(archive_path)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(render_markdown(summary), encoding="utf-8")
    return summary


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--archive", type=Path, default=DEFAULT_ARCHIVE)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    summary = run_audit(args.archive, args.output)
    print(
        "ENDES audit complete: "
        f"{summary['rows']:,} rows, {summary['columns']} columns; "
        f"report written to {args.output}. No row-level data or identifiers printed."
    )


if __name__ == "__main__":
    main()
