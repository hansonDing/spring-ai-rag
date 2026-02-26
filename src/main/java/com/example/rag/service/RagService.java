package com.example.rag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final ChatClient chatClient;
    private final EmbeddingService embeddingService;

    @Value("classpath:/prompts/rag-system-prompt.st")
    private Resource ragSystemPrompt;

    /**
     * RAG 问答
     */
    public String query(String question, int topK) {
        // 1. 检索相关文档
        List<Document> relevantDocs = embeddingService.searchSimilarDocuments(question, topK);
        
        // 2. 构建上下文
        String context = buildContext(relevantDocs);
        
        // 3. 构建提示词
        String systemPrompt = buildSystemPrompt(context);
        
        // 4. 调用大模型
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(systemPrompt));
        messages.add(new UserMessage(question));
        
        Prompt prompt = new Prompt(messages);
        
        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    /**
     * 构建上下文
     */
    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "无相关文档";
        }
        
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            context.append("[文档").append(i + 1).append("]\n");
            context.append(doc.getContent()).append("\n\n");
        }
        return context.toString();
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String context) {
        return String.format("""
                你是一个专业的问答助手。请基于以下参考文档回答用户的问题。
                如果参考文档中没有相关信息，请明确告知用户。
                
                参考文档：
                %s
                
                请根据以上文档内容回答问题，保持回答准确、简洁。
                """, context);
    }
}
