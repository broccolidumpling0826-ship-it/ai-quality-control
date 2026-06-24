# ai-interaction-ux

## Purpose

Consistent, user-friendly frontend loading feedback while backend AI model calls are in progress, so operators understand the system is working and do not dismiss dialogs prematurely.

## Requirements

### Requirement: AI loading copy is centralized and scenario-specific
The frontend SHALL use shared loading copy constants for all pages that invoke backend AI model calls, with scenario-specific user-facing messages.

#### Scenario: Judgment explanation page loading
- **WHEN** the judgment explanation page is waiting for `GET /judgments/{id}/explanation`
- **THEN** the UI SHALL show loading text equivalent to `AI 正在分析判定依据并生成解释，请稍候…`

#### Scenario: Reinspection advice loading
- **WHEN** a user opens AI reinspection advice from the judgment explanation page
- **THEN** the UI SHALL show loading text equivalent to `AI 正在分析判定依据并生成复检建议，请稍候…`

#### Scenario: Rejudgment advice loading
- **WHEN** a user opens AI rejudgment advice from the judgment explanation page
- **THEN** the UI SHALL show loading text equivalent to `AI 正在分析判定依据并生成改判建议，请稍候…`

#### Scenario: Concession risk assessment loading
- **WHEN** a user triggers concession risk assessment
- **THEN** the UI SHALL show loading text equivalent to `AI 正在分析让步风险并生成评估，请稍候…`

#### Scenario: Certificate Q&A loading
- **WHEN** a user submits a certificate Q&A question
- **THEN** the UI SHALL show loading text equivalent to `AI 正在分析质保书与判定依据，请稍候…`

#### Scenario: Standard RAG retrieval loading
- **WHEN** a user submits a standard RAG query
- **THEN** the UI SHALL show loading text equivalent to `AI 正在检索标准条款并生成回答，请稍候…`

### Requirement: AI loading indicator is visibly centered
The frontend SHALL center the loading spinner and message within the waiting container rather than pinning them to the top edge.

#### Scenario: Centered loading overlay
- **WHEN** any covered AI interaction surface enters a loading state
- **THEN** the loading mask SHALL vertically and horizontally center the spinner and message within the active container

### Requirement: AI loading containers preserve minimum height
The frontend SHALL give loading containers enough minimum height that centered loading feedback remains visible and does not appear as an empty blank area.

#### Scenario: Advice modal minimum height
- **WHEN** an AI advice modal is waiting for a model response
- **THEN** the modal body SHALL maintain a minimum height sufficient to show centered loading feedback

### Requirement: AI loading modals prevent premature dismissal
The frontend SHALL disable dismiss actions while an AI model request is pending in modal workflows.

#### Scenario: Advice modal pending request
- **WHEN** AI reinspection or rejudgment advice is loading
- **THEN** the modal SHALL disable close-on-click-modal, close-on-escape, header close button, and footer action buttons until the request completes or fails

### Requirement: Covered AI interaction surfaces
The loading behavior SHALL apply at minimum to judgment explanation, AI reinspection/rejudgment advice modal, concession risk assessment, certificate Q&A, standard RAG retrieval, concession apply judgment preload, and rejudgment apply judgment preload.

#### Scenario: Shared loading implementation
- **WHEN** a new AI model-calling page is added
- **THEN** it SHALL reuse the shared loading copy constants and centered loading class rather than inventing page-local blank loading states
