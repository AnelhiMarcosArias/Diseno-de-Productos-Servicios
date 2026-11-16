import hashlib
import pandas as pd
import pytest

from scripts.audit_endes import create_summary, distribution, file_fingerprint, render_markdown
from scripts.train_compare import eligible_training_rows


def test_audit_summary_never_renders_caseid_values():
    violence = pd.DataFrame(
        {
            "CASEID": ["private-key-a", "private-key-b", "private-key-c", "private-key-d"],
            "D101A": ["0", pd.NA, "1", pd.NA],
            "D107": ["0", "1", pd.NA, pd.NA],
            "D113": ["0", "1", "1", "0"],
            "D114": [pd.NA, "2", "0", pd.NA],
        },
        dtype="string",
    )
    individual = pd.DataFrame(
        {
            "CASEID": ["private-key-a", "private-key-b", "private-key-c", "private-key-d"],
            "V001": ["1", "1", "2", "2"],
            "V005": ["100", "200", "300", "400"],
            "V021": ["1", "1", "2", "3"],
            "V022": ["10", "10", "11", "12"],
            "V044": ["1", "1", "0", "1"],
            "V023": ["1", "1", "2", "2"],
            "V024": ["1", "1", "2", "2"],
            "V025": ["1", "1", "2", "2"],
            "V012": ["21", "35", "48", "28"],
            "V106": ["2", "3", "1", "2"],
            "V190": ["2", "4", "1", "2"],
        },
        dtype="string",
    )
    relationship = pd.DataFrame(
        {
            "CASEID": ["private-key-a", "private-key-b", "private-key-c", "private-key-d"],
            "V501": ["1", "2", "0", "0"],
            "V502": ["1", "1", "0", "0"],
        },
        dtype="string",
    )

    summary = create_summary(violence, individual, relationship)
    report = render_markdown(summary)

    assert summary["rows"] == 4
    assert summary["duplicate_case_keys"] == 0
    assert summary["d107_counts"] == {"0": 1, "1": 1, "<blank>": 2}
    assert summary["design_rows_unmatched"] == 0
    assert summary["d107_observed_rows"] == 2
    assert summary["d107_strata_without_positive"] == 0
    assert summary["d107_positive_stratum_cells_under_five_clusters"] == 1
    assert summary["selection_status_counts"] == {"0": 1, "1": 3}
    assert summary["selection_target_counts"]["1"] == {
        "rows": 3, "d107_observed": 2, "d107_missing": 1, "d107_positive": 1,
    }
    assert summary["selection_relationship_target_counts"]["1/0/0"]["d107_missing"] == 1
    assert summary["selection_relationship_target_counts"]["1/0/0"]["d107_observed"] == 0
    assert summary["selection_relationship_target_counts"]["1/1/1"]["d107_missing"] == 0
    assert summary["relationship_union_target_counts"]["0/0"]["d107_missing"] == 2
    assert summary["d114_by_d113"] == {
        "0": {"rows": 1, "d114_missing": 1, "d114_observed": 0},
        "1": {"rows": 1, "d114_missing": 0, "d114_observed": 1},
    }
    assert "RE516171" in report
    assert "V501=0/V502=0" in report
    assert "páginas impresas 70–72" in report
    assert "no se recodifica a 'Nunca'" in report
    assert "V005" in report
    assert "no se encontró D005 ni V005V" in report
    assert all(key not in report for key in violence["CASEID"])


def test_training_requires_selected_and_interviewed_violence_module_records():
    frame = pd.DataFrame({
        "D107": ["0", "1", pd.NA, pd.NA],
        "V044": ["1", "1", "0", "2"],
        "V501": ["1", "2", "0", "5"],
        "V502": ["1", "1", "0", "2"],
        "V021": ["a", "b", "c", "d"],
    }, dtype="string")

    assert eligible_training_rows(frame).tolist() == [True, True, False, False]


def test_training_rejects_observed_target_for_noninterviewed_module_record():
    frame = pd.DataFrame({
        "D107": ["1"], "V044": ["2"], "V501": ["1"], "V502": ["1"], "V021": ["a"],
    }, dtype="string")

    with pytest.raises(ValueError, match="D107 is observed outside V044=1"):
        eligible_training_rows(frame)


def test_training_does_not_use_marital_status_as_an_eligibility_shortcut():
    frame = pd.DataFrame({
        "D107": ["1"], "V044": ["1"], "V501": ["0"], "V502": ["0"], "V021": ["a"],
    }, dtype="string")

    assert eligible_training_rows(frame).tolist() == [True]


def test_identifier_distribution_is_rejected():
    frame = pd.DataFrame({"CASEID": ["private-key"]}, dtype="string")

    with pytest.raises(ValueError, match="Identifier distributions are forbidden"):
        distribution(frame, "CASEID")


def test_duplicate_case_keys_are_counted_without_disclosing_them():
    frame = pd.DataFrame({"CASEID": ["private-key", "private-key"]}, dtype="string")

    assert duplicate_count(frame) == 1


def test_file_fingerprint_identifies_local_bytes_without_rendering_file_contents(tmp_path):
    archive = tmp_path / "2025.zip"
    payload = b"synthetic test bytes only"
    archive.write_bytes(payload)

    fingerprint = file_fingerprint(archive)

    assert fingerprint == {
        "file_name": "2025.zip",
        "size_bytes": len(payload),
        "sha256": hashlib.sha256(payload).hexdigest(),
    }
    assert payload.decode() not in str(fingerprint)


def duplicate_count(frame: pd.DataFrame) -> int:
    from scripts.audit_endes import duplicate_key_count

    return duplicate_key_count(frame)
