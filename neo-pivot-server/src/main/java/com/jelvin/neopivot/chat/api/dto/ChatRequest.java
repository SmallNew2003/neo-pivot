package com.jelvin.neopivot.chat.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * 问答请求体。
 *
 * @author Jelvin
 */
@Getter
@Setter
public class ChatRequest {

    @NotBlank
    private String question;

    @Positive
    private Integer topK;
}
