package com.jhict.quality.service.api;

import com.jhict.quality.vo.AiSuggestionVO;

public interface AiSuggestionService {

    AiSuggestionVO getReinspectionSuggestion(String judgmentId);

    AiSuggestionVO getRejudgmentSuggestion(String judgmentId);
}
