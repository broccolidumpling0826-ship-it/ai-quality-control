# AI Prompt Registry

Git-managed prompt templates for model-invoking business scenes.

## Layout

- `manifest.yaml` — scene → active version, businessType, citationStyle, model params
- `shared/` — citation answer rule text shared across scenes
- `<scene>/` — versioned system, user-skeleton, optional append snippets

## Supported scenes

| Scene | Active version | Builder |
|-------|----------------|---------|
| JUDGMENT_EXPLANATION | judgment-explanation-v3 | `JudgmentExplanationPromptBuilder` |
| STANDARD_RAG | standard-rag-p0-v3 | `StandardRagPromptBuilder` |
| CERT_QA | cert-qa-v4 | `CertQaPromptBuilder` |
| CONCESSION_RISK | concession-risk-v1 | `ConcessionRiskPromptBuilder` |
| VISION_OCR | vision-ocr-v1 | `VisionOcrPromptProvider` |

## Bump a prompt version

1. Copy the version folder/files (e.g. `cert-qa-v4.*` → `cert-qa-v5.*`).
2. Edit the new files; update `*.meta.yaml` changelog.
3. Point `manifest.yaml` `activeVersion` to the new version.
4. Update golden tests under `backend/src/test/java/.../prompt/`.
5. Run `cd backend && mvn test`.
6. Optional: run evaluation script for RAG/judgment scenes.

## Review checklist

- [ ] manifest `activeVersion` matches new files
- [ ] changelog updated in `*.meta.yaml`
- [ ] golden / builder tests updated
- [ ] citation rule changes paired with `CitationReferenceSupportTest`
- [ ] locked safety phrases preserved (no fabrication, citation grounding)
- [ ] no business condition logic moved into template files

## Configuration

```yaml
app:
  ai:
    prompts:
      base-path: classpath:ai-prompts/
```

Vision OCR user prompt override (higher priority than manifest default):

```yaml
app:
  ai:
    model:
      vision:
        ocr-prompt: ${AI_VISION_OCR_PROMPT:...}
```

## Placeholders

User skeletons use `{{name}}` placeholders filled by PromptBuilder at runtime.
Citation blocks and evidence lists are assembled in Java, not in template files.
