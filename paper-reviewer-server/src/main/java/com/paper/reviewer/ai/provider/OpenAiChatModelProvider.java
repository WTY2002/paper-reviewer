package com.paper.reviewer.ai.provider;

import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Reserved adapter; intentionally unusable in the first release. */
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
public class OpenAiChatModelProvider implements ChatModelProvider {
    @Override
    public String complete(String systemPrompt, String userPrompt) {
        throw new BusinessException(ErrorCode.AI_PROVIDER_ERROR,
                "OpenAI provider is disabled in this release");
    }
}
