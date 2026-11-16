"""Load a research-only ENDES D107 classifier artifact safely."""

from __future__ import annotations

import json
import hashlib
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import joblib

from scripts.train_compare import FEATURES


ARTIFACT_DIR = Path(__file__).resolve().parents[1] / "artifacts"
EXPECTED_SCOPE = "research_only_retrospective_classification"
EXPECTED_MODEL_VERSION = "endes-2025-d107-research-candidate-v1"


@dataclass(frozen=True)
class ModelBundle:
    pipeline: Any
    metadata: dict[str, Any]


def load_model_bundle(artifact_dir: Path = ARTIFACT_DIR) -> ModelBundle | None:
    """Load the artifact only when its metadata explicitly marks research scope."""
    metadata_path = artifact_dir / f"{EXPECTED_MODEL_VERSION}.json"
    if not metadata_path.is_file():
        return None
    metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
    if metadata.get("model_version") != EXPECTED_MODEL_VERSION:
        raise RuntimeError("Artifact metadata has an unexpected model version.")
    if metadata.get("artifact_scope") != EXPECTED_SCOPE:
        raise RuntimeError("Refusing to load an artifact without research-only scope metadata.")
    if metadata.get("product_model_selected") is not False:
        raise RuntimeError("Refusing to load an artifact marked as selected for product use.")
    if metadata.get("target") != "D107" or metadata.get("features") != FEATURES:
        raise RuntimeError("Artifact target or feature schema does not match the API contract.")

    expected_artifact_name = f"{EXPECTED_MODEL_VERSION}.joblib"
    if metadata.get("artifact_file") != expected_artifact_name:
        raise RuntimeError("Artifact metadata has an unexpected artifact filename.")
    artifact_path = artifact_dir / expected_artifact_name
    if not artifact_path.is_file():
        raise RuntimeError("Model metadata exists but its artifact file is missing.")
    digest = hashlib.sha256()
    with artifact_path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    if digest.hexdigest() != metadata.get("artifact_sha256"):
        raise RuntimeError("Artifact checksum does not match its metadata.")
    return ModelBundle(joblib.load(artifact_path), metadata)
