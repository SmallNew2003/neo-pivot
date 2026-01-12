package com.jelvin.neopivot.chat.api;

import com.jelvin.neopivot.chat.api.dto.ChatRequest;
import com.jelvin.neopivot.chat.api.dto.ChatResponse;
import com.jelvin.neopivot.common.api.ApiNotImplementedException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RAG 问答接口（模式A：最终答案由底座生成）。
 *
 * <p>骨架阶段仅占位，后续将接入检索（PGVector）与 LLM（默认 OpenAI）形成完整闭环。
 *
 * @author Jelvin
 */
@RestController
@RequestMapping("/api")
public class ChatController {

    /**
     * 问答接口。
     *
     * @param request 问答请求
     * @return 问答响应
     */
    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        throw new ApiNotImplementedException("Chat 主链路尚未实现：后续将按 OpenSpec 接入检索与生成并返回 citations。");
    }
}
