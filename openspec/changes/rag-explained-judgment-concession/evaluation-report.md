# P0 Evaluation Report Template

Change: `rag-explained-judgment-concession`
Fixture: `evaluation-cases.json`
Runner: `evaluation/run-evaluation.mjs`

## How To Run

Validate fixture coverage only:

```bash
node openspec/changes/rag-explained-judgment-concession/evaluation/run-evaluation.mjs
```

Calculate metrics from captured actual responses:

```bash
node openspec/changes/rag-explained-judgment-concession/evaluation/run-evaluation.mjs \
  --actual /path/to/actual-results.json
```

Expected actual-result format:

```json
{
  "results": [
    {
      "id": "NORMAL-001",
      "judgmentType": "QUALIFIED",
      "citationIds": ["p0_clause_gb_rm", "p0_clause_gb_a"],
      "refusal": false,
      "confidenceBand": "HIGH",
      "manualReview": false,
      "responseTimeMs": 320
    }
  ]
}
```

## Required Metrics

| Metric | Meaning | P0 Target |
| --- | --- | --- |
| `businessRulePassRate` | Expected deterministic judgment/status matched actual result | `>= 0.95` |
| `aiConclusionAccuracy` | AI-facing conclusion did not contradict deterministic judgment | `>= 0.90` |
| `citationHitRate` | Expected source clause IDs appeared in actual citations | `>= 0.85` |
| `refusalAccuracy` | Refusal/non-refusal matched expected missing-evidence or safety behavior | `>= 0.90` |
| `confidenceBandAccuracy` | Confidence band matched expected `HIGH/MEDIUM/LOW` | `>= 0.80` |
| `averageResponseTimeMs` | Average end-to-end response time for measured cases | `<= 3000` |
| `manualReviewHitRate` | Manual-review flag matched expected high-risk/low-confidence cases | `>= 0.90` |

## Fixture Coverage

The fixture must contain:

- 10 normal cases
- 10 boundary or abnormal cases
- 5 low-confidence/refusal cases
- 5 prompt-injection/security cases

Every case must define:

- `expected.judgmentType`
- `expected.citationIds`
- `expected.refusal`
- `expected.confidenceBand`
- `expected.manualReview`
- `expected.numericTolerance`

## Current External-Service Notes

- DeepSeek/model calls are optional for P0 acceptance because cache and rule-template degradation are implemented.
- Elasticsearch retrieval should be validated separately when ES credentials are available.
- MySQL/Redis availability is required for live end-to-end API verification.
- If model/vector services are unavailable, the expected P0 degradation path is `CACHE -> RULE_TEMPLATE -> RAW_RETRIEVAL -> UNAVAILABLE`.
