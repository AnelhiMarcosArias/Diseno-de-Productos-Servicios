from types import SimpleNamespace

import numpy as np
import pytest
from fastapi import HTTPException
from pydantic import ValidationError

from app import main
from app.main import app, health


def valid_features():
    return {
        "D101A": 1, "D101B": 0, "D101C": 0, "D101D": 0, "D101E": 1, "D101F": 0,
        "D103A": 0, "D103B": 0, "D103D": 0,
        "D105A": 1, "D105B": 0, "D105C": 0, "D105D": 0,
        "D113": 0, "D114": None,
        "V025": 1, "V106": 2, "V190": 3, "V012": 30,
    }


def test_health_reports_process_and_experimental_model_readiness_separately():
    result = health()

    assert result["status"] == "ok"
    assert result["model_loaded"] is (main.model_bundle is not None)
    assert result["experimental_prediction_ready"] is (main.model_bundle is not None)
    assert result["product_prediction_enabled"] is False
    assert result["experiment_scope"] == "retrospective ENDES D107 only"
    assert result["synthetic_prediction_enabled"] is (main.synthetic_model is not None)
    assert result["synthetic_model_version"] == (main.SYNTHETIC_MODEL_VERSION if main.synthetic_model is not None else "unavailable")


def test_invalid_endes_codes_are_rejected_before_inference():
    features = valid_features()
    features["D101A"] = 9
    try:
        main.D107PredictionRequest.model_validate({"features": features})
    except ValidationError as error:
        assert error.errors()[0]["loc"][-1] == "D101A"
    else:
        raise AssertionError("Invalid ENDES code was accepted.")


def test_valid_request_reports_service_unavailable_without_artifact(monkeypatch):
    monkeypatch.setattr(main, "model_bundle", None)

    try:
        main.classify_d107(main.D107PredictionRequest(features=valid_features()))
    except HTTPException as error:
        assert error.status_code == 503
        assert "artifact is not available" in error.detail
    else:
        raise AssertionError("Missing model artifact did not return service unavailable.")


def test_result_is_explicitly_retrospective_and_not_a_future_risk(monkeypatch):
    class StubPipeline:
        def predict(self, frame):
            assert len(frame) == 1
            return np.array([1])

        def predict_proba(self, frame):
            return np.array([[0.2, 0.8]])

    bundle = SimpleNamespace(
        pipeline=StubPipeline(),
        metadata={"model_version": "test-research-candidate"},
    )
    monkeypatch.setattr(main, "model_bundle", bundle)

    result = main.classify_d107(
        main.D107PredictionRequest(features=valid_features())
    )
    assert result["status"] == "experimental"
    assert result["target"] == "D107"
    assert result["d107_class"] == 1
    assert result["model_score_for_d107_positive"] == 0.8
    assert result["product_model_selected"] is False
    assert result["future_femicide_prediction"] is False
    assert "not a calibrated individual risk probability" in result["score_semantics"]
    assert "not compatible with the product V1-V12 form" in result["warning"]


def test_d114_missingness_is_preserved_only_when_d113_is_zero():
    features = valid_features()
    assert main.D107Features.model_validate(features).D114 is None

    features["D114"] = 0
    with pytest.raises(ValidationError, match="D114 must be absent"):
        main.D107Features.model_validate(features)

    features["D113"] = 1
    features["D114"] = None
    with pytest.raises(ValidationError, match="D114 is required"):
        main.D107Features.model_validate(features)


def test_no_generic_product_predict_route_is_exposed():
    assert not any(
        route.path == "/predict" and "POST" in getattr(route, "methods", set())
        for route in app.routes
    )


def test_synthetic_endpoint_requires_exactly_twelve_categorical_answers():
    answers = {f"V{i}": "NO" for i in range(1, 13)}
    request = main.SyntheticV1V12Request.model_validate({"answers": answers})
    assert len(request.answers) == 12
    answers.pop("V12")
    with pytest.raises(ValidationError):
        main.SyntheticV1V12Request.model_validate({"answers": answers})


def test_synthetic_inference_uses_only_stubbed_xgboost_model(monkeypatch):
    class StubModel:
        def predict_proba(self, values):
            assert values.shape == (1, 12)
            assert values[0, 0] == 2  # uncertainty is not recoded as a negative
            return np.array([[0.27, 0.73]])

    monkeypatch.setattr(main, "synthetic_model", StubModel())
    answers = {f"V{i}": "NO" for i in range(1, 13)}
    answers["V1"] = "NO_ESTOY_SEGURA"
    answers["V11"] = "SI"
    result = main.simulate_v1_v12(main.SyntheticV1V12Request(answers=answers))
    assert result["index"] == 73.0
    assert result["model_type"] == "synthetic_academic_demonstration"
    assert result["uncertain_answers"] == ["V1"]
    assert result["critical_signals"] == ["V11"]
    assert "not calibrated" in result["warning"]


def test_generated_candidate_artifact_loads_and_supports_research_inference():
    if main.model_bundle is None:
        pytest.skip("The ignored local ENDES artifact has not been generated in this checkout.")

    result = main.classify_d107(
        main.D107PredictionRequest(features=valid_features())
    )

    assert result["model_version"] == "endes-2025-d107-research-candidate-v1"
    assert result["d107_class"] in (0, 1)
    assert result["product_model_selected"] is False
    assert result["future_femicide_prediction"] is False
    assert any(
        route.path == "/experimental/classify-d107" and "POST" in getattr(route, "methods", set())
        for route in app.routes
    )
