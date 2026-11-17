"""Research-only ENDES D107 classifier API, separate from product risk scoring."""

from typing import Literal

import numpy as np
import pandas as pd
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, ConfigDict, conint, model_validator

from app.model_runtime import ModelBundle, load_model_bundle
from scripts.train_compare import CATEGORICAL, FEATURES

app = FastAPI(
    title="ENDES D107 retrospective experiment API",
    version="0.2.0",
    description=(
        "Research-only endpoint for a retrospective ENDES D107 classification. "
        "It does not estimate future violence or femicide, is not selected for the "
        "product, and is not compatible with the product's V1-V12 questionnaire."
    ),
)


class D107Features(BaseModel):
    """Exact feature schema used by the exploratory ENDES experiment."""

    model_config = ConfigDict(extra="forbid")

    D101A: Literal[0, 1, 8]
    D101B: Literal[0, 1, 8]
    D101C: Literal[0, 1, 8]
    D101D: Literal[0, 1, 8]
    D101E: Literal[0, 1, 8]
    D101F: Literal[0, 1, 8]
    D103A: Literal[0, 1, 2, 3]
    D103B: Literal[0, 1, 2, 3]
    D103D: Literal[0, 1, 2, 3]
    D105A: Literal[0, 1, 2, 3]
    D105B: Literal[0, 1, 2, 3]
    D105C: Literal[0, 1, 2, 3]
    D105D: Literal[0, 1, 2, 3]
    D113: Literal[0, 1]
    D114: Literal[0, 1, 2] | None
    V025: Literal[1, 2]
    V106: Literal[0, 1, 2, 3]
    V190: Literal[1, 2, 3, 4, 5]
    V012: conint(ge=12, le=49)

    @model_validator(mode="after")
    def validate_alcohol_frequency_universe(self):
        if self.D113 == 0 and self.D114 is not None:
            raise ValueError("D114 must be absent when D113=0, matching the ENDES observed universe.")
        if self.D113 == 1 and self.D114 is None:
            raise ValueError("D114 is required when D113=1; otherwise the input is insufficient for this experiment.")
        return self


class D107PredictionRequest(BaseModel):
    model_config = ConfigDict(extra="forbid")

    features: D107Features


model_bundle: ModelBundle | None = load_model_bundle()


@app.get("/health", tags=["health"])
def health() -> dict[str, str | bool]:
    """Report process/model readiness without implying product readiness."""
    return {
        "status": "ok",
        "model_loaded": model_bundle is not None,
        "experimental_prediction_ready": model_bundle is not None,
        "product_prediction_enabled": False,
        "experiment_scope": "retrospective ENDES D107 only",
    }


@app.post("/experimental/classify-d107", tags=["experimental"])
def classify_d107(request: D107PredictionRequest) -> dict[str, str | int | float | bool]:
    """Return a retrospective D107 class with a prominent research-only scope."""
    if model_bundle is None:
        raise HTTPException(
            status_code=503,
            detail="The ENDES D107 research artifact is not available. Run the comparison script first.",
        )

    input_values = request.features.model_dump()
    for field in CATEGORICAL:
        value = input_values[field]
        input_values[field] = "__MISSING__" if value is None else str(value)
    frame = pd.DataFrame([input_values], columns=FEATURES)
    predicted = int(model_bundle.pipeline.predict(frame)[0])
    score_positive: float | None = None
    if hasattr(model_bundle.pipeline, "predict_proba"):
        score_positive = float(np.asarray(model_bundle.pipeline.predict_proba(frame))[0, 1])

    return {
        "status": "experimental",
        "target": "D107",
        "target_definition": "retrospective report of severe violence derived from ENDES items D105E, D105F or D105G",
        "d107_class": predicted,
        "model_score_for_d107_positive": score_positive if score_positive is not None else "not_available",
        "score_semantics": "raw model score; not a calibrated individual risk probability",
        "model_version": model_bundle.metadata["model_version"],
        "product_model_selected": False,
        "future_femicide_prediction": False,
        "warning": "Research only. Do not interpret as future violence/femicide risk or use for clinical decisions. The output is not compatible with the product V1-V12 form.",
    }
