import pandas as pd

from scripts.build_feature_dictionary import (
    CATEGORICAL,
    DESIGN_ONLY,
    EXCLUDED_LEAKAGE,
    NUMERIC,
    TARGET,
    render_dictionary,
)


def test_dictionary_records_feature_roles_and_missing_handling():
    variables = [*CATEGORICAL, *NUMERIC, TARGET, *DESIGN_ONLY, *EXCLUDED_LEAKAGE]
    source = pd.DataFrame({
        "Variable": variables,
        "Descripción": [f"Description {variable}" for variable in variables],
        "Información Adicional": ["Codes from local dictionary" for _ in variables],
        "Recurso relacionado": ["REC test dictionary" for _ in variables],
    }, dtype="string")

    document = render_dictionary(source)

    assert "`D107` | Objetivo retrospectivo" in document
    assert "`D105E` | Excluida por fuga/derivación" in document
    assert "vacíos como nivel separado" in document
    assert "mediana e indicador de ausencia" in document
    assert "CASEID no es predictor ni se escribe" in document


def test_dictionary_rejects_missing_variable_definitions():
    source = pd.DataFrame({
        "Variable": ["D107"],
        "Descripción": ["retrospective indicator"],
        "Información Adicional": ["0=no; 1=yes"],
        "Recurso relacionado": ["REC84DV"],
    }, dtype="string")

    try:
        render_dictionary(source)
    except ValueError as error:
        assert "lacks experiment fields" in str(error)
    else:
        raise AssertionError("The feature dictionary must reject absent definitions.")
