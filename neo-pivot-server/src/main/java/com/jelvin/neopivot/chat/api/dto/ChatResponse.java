package com.jelvin.neopivot.chat.api.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 问答响应体。
 *
 * @author Jelvin
 */
public class ChatResponse {

    private String answer;

    private List<CitationDto> citations = new ArrayList<>();

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<CitationDto> getCitations() {
        return citations;
    }

    public void setCitations(List<CitationDto> citations) {
        this.citations = citations;
    }
}
