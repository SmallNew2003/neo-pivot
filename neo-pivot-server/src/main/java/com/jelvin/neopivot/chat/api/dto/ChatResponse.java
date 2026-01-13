package com.jelvin.neopivot.chat.api.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * 问答响应体。
 *
 * @author Jelvin
 */
@Getter
@Setter
public class ChatResponse {

    private String answer;

    private List<CitationDto> citations = new ArrayList<>();
}
