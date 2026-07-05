package com.paper.reviewer.ai.provider;

import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "qwen", matchIfMissing = true)
public class QwenChatModelProvider implements ChatModelProvider {
    private static final Logger log = LoggerFactory.getLogger(QwenChatModelProvider.class);
    private final ObjectProvider<ChatModel> chatModel;

    public QwenChatModelProvider(ObjectProvider<ChatModel> chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        try {
            ChatModel model = chatModel.getIfAvailable();
            if (model == null) throw new IllegalStateException("Qwen ChatModel is not configured");
            return model.call(new Prompt(List.of(
                    new SystemMessage(systemPrompt), new UserMessage(userPrompt))))
                    .getResult().getOutput().getText();
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Qwen model request failed; inspect the root cause and HTTP status below", exception);
            throw new BusinessException(ErrorCode.AI_PROVIDER_ERROR,
                    "Qwen model request failed", exception);
        }
    }
}
