"""Build a feature/target dictionary from the local INEI variable dictionary."""

from __future__ import annotations

import argparse
from pathlib import Path

import pandas as pd

try:
    from .train_compare import CATEGORICAL, NUMERIC, TARGET
except ImportError:  # pragma: no cover - direct script mode
    from train_compare import CATEGORICAL, NUMERIC, TARGET


PROJECT_DIR = Path(__file__).resolve().parents[2]
DEFAULT_DICTIONARY = PROJECT_DIR / "Proyecto_Feminicidio_Datos" / "02_Diccionarios" / "DiccionarioDatos_ENDES.csv"
DEFAULT_OUTPUT = PROJECT_DIR / "ml-service" / "docs" / "feature_dictionary.md"
DESIGN_ONLY = ["V005", "V021", "V022", "V044", "V501", "V502"]
EXCLUDED_LEAKAGE = ["D105E", "D105F", "D105G", "D106", "D108"]


def _cell(value: object) -> str:
    if pd.isna(value):
        return "No especificado en el diccionario local"
    return " ".join(str(value).split()).replace("|", "\\|")


def render_dictionary(dictionary: pd.DataFrame) -> str:
    required = {"Variable", "Descripción", "Información Adicional", "Recurso relacionado"}
    missing_columns = required - set(dictionary.columns)
    if missing_columns:
        raise ValueError(f"The local dictionary lacks required columns: {sorted(missing_columns)}")
    wanted = [*CATEGORICAL, *NUMERIC, TARGET, *DESIGN_ONLY, *EXCLUDED_LEAKAGE]
    selected = dictionary[dictionary["Variable"].isin(wanted)].copy()
    duplicates = selected[selected["Variable"].duplicated(keep=False)]["Variable"].unique().tolist()
    if duplicates:
        raise ValueError(f"Duplicate variable definitions must be reviewed: {sorted(duplicates)}")
    indexed = selected.set_index("Variable")
    missing_variables = sorted(set(wanted) - set(indexed.index))
    if missing_variables:
        raise ValueError(f"The local dictionary lacks experiment fields: {missing_variables}")

    rows: list[str] = []
    for variable in [*CATEGORICAL, *NUMERIC, TARGET, *DESIGN_ONLY, *EXCLUDED_LEAKAGE]:
        item = indexed.loc[variable]
        if variable == TARGET:
            role = "Objetivo retrospectivo"
            handling = "Se admiten códigos observados 0/1; filas vacías se excluyen del entrenamiento, no se convierten en 0. No representa un evento futuro."
        elif variable in DESIGN_ONLY:
            role = {"V005": "Peso muestral general", "V021": "Grupo de partición", "V022": "Estrato de diseño (diagnóstico)", "V044": "Selección/respuesta del módulo", "V501": "Diagnóstico de datos faltantes", "V502": "Diagnóstico de datos faltantes"}[variable]
            handling = {
                "V005": "No es predictor y no se usa en este experimento. Es el factor general de ponderación de mujeres. La ficha ENDES 2024 del indicador de violencia especifica V005 salvo el año 2020; para el objetivo D107 y la elegibilidad exacta V044=1 en el extracto 2025 no se encontró una especificación 2025 directa, por lo que las métricas de este experimento se mantienen sin ponderar y no se interpretan como estimaciones poblacionales.",
                "V021": "No es predictor. Agrupa registros para evitar que un mismo conglomerado cruce entrenamiento y prueba.",
                "V022": "No es predictor ni variable de partición actual; se audita. La estratificación simultánea no se forzó por la escasez de positivos en celdas estrato-clase.",
                "V044": "No es predictor. Define elegibilidad: solo se incluyen respuestas D107 observadas con código 1 (seleccionada y entrevistada); otros estados y vacíos de D107 no son negativos.",
                "V501": "No es predictor ni filtro de elegibilidad. Se enlaza desde RE516171 para describir faltantes de D107; los vacíos coinciden con V501=0/V502=0, pero no se recodifican como negativos.",
                "V502": "No es predictor ni filtro de elegibilidad. Se enlaza desde RE516171 solo para describir D107 no observado; los vacíos observados coinciden con V501=0/V502=0 y no se recodifican como negativos.",
            }[variable]
        elif variable in EXCLUDED_LEAKAGE:
            role = "Excluida por fuga/derivación"
            handling = "No se entrega al modelo. D105E/F/G son componentes de D107; D106 y D108 son agregados del mismo módulo."
        else:
            role = "Predictor experimental"
            handling = (
                (
                    "Se conserva el código original como categoría; en las filas elegibles, D114 vacío ocurre con D113=0 y se mantiene como nivel separado, nunca se convierte en 0 ('Nunca'). La API experimental valida este patrón."
                    if variable == "D114"
                    else "Se conserva el código original como categoría, incluidos códigos especiales y vacíos como nivel separado; no se recodifica a Sí/No."
                )
                if variable in CATEGORICAL
                else "Se trata como numérica; mediana e indicador de ausencia se ajustan dentro del pliegue de entrenamiento y luego se escala."
            )
        rows.append(
            f"| `{variable}` | {role} | {_cell(item['Descripción'])} | {_cell(item['Información Adicional'])} | {handling} |"
        )

    lines = [
        "# Diccionario del experimento retrospectivo ENDES 2025", "",
        "> Fuente semántica: `Proyecto_Feminicidio_Datos/02_Diccionarios/DiccionarioDatos_ENDES.csv`, diccionario local de ENDES 2025. Los nombres y códigos se reproducen sin inferir equivalencias con V1–V12.", "",
        "## Variables utilizadas y controladas", "",
        "| Campo | Rol | Descripción/códigos según el diccionario | Información/códigos | Tratamiento en el experimento |",
        "|---|---|---|---|---|",
        *rows,
        "",
        "## Reglas del experimento", "",
        "- D107 es el objetivo de clasificación retrospectiva; el diccionario lo define a partir de D105E/F/G. No etiqueta feminicidio ni una ocurrencia futura.",
        "- Solo se incluyen filas con V044=1 y D107 observado. V044 codifica selección/entrevista; V501 y V502 solo se usan en la auditoría de datos faltantes, no como filtros ni predictores.",
        "- D105E/F/G, D106 y D108 se excluyen de las entradas. CASEID no es predictor ni se escribe en los resultados.",
        "- El experimento no usa ponderadores y sus métricas son de muestra. Aunque la ficha ENDES 2024 de violencia especifica V005 (salvo 2020), no se encontró una especificación 2025 directa para el objetivo D107 y la elegibilidad exacta V044=1. V021 agrupa particiones; V022 diagnostica el diseño; V044 controla elegibilidad.",
        "- Las doce preguntas actuales de la aplicación no son equivalentes validadas a este conjunto de predictores.",
        "",
        "Este diccionario documenta exclusivamente el experimento de D107. No define el cuestionario clínico del producto ni autoriza inferencias sobre riesgo futuro.",
        "",
    ]
    return "\n".join(lines)


def build(dictionary_path: Path, output_path: Path) -> None:
    if not dictionary_path.is_file():
        raise FileNotFoundError("Local INEI dictionary CSV not found in Proyecto_Feminicidio_Datos/02_Diccionarios.")
    dictionary = pd.read_csv(dictionary_path, dtype="string", encoding="utf-8-sig")
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(render_dictionary(dictionary), encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dictionary", type=Path, default=DEFAULT_DICTIONARY)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    args = parser.parse_args()
    build(args.dictionary, args.output)
    print(f"Feature dictionary written to {args.output}; no observation-level records were read or saved.")


if __name__ == "__main__":
    main()
