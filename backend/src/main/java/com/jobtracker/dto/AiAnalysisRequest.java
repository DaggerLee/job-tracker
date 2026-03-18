package com.jobtracker.dto;

import lombok.Data;

@Data
public class AiAnalysisRequest {
    private String apiKey;
    private String resumeText;
    private String jdText;
}
