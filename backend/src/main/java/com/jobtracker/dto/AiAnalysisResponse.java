package com.jobtracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiAnalysisResponse {
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private int score;
    private String summary;
}
