package com.example.rag.controller;

import com.example.rag.entity.Document;
import com.example.rag.service.DocumentService;
import com.example.rag.service.RagService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RagController {

    private final DocumentService documentService;
    private final RagService ragService;

    /**
     * 上传文档
     */
    @PostMapping("/documents")
    public ResponseEntity<Document> uploadDocument(@RequestBody DocumentRequest request) {
        Document document = documentService.saveDocument(
                request.getTitle(),
                request.getContent(),
                request.getSource()
        );
        return ResponseEntity.ok(document);
    }

    /**
     * RAG 问答
     */
    @PostMapping("/rag/query")
    public ResponseEntity<RagResponse> query(@RequestBody QueryRequest request) {
        String answer = ragService.query(request.getQuestion(), request.getTopK() != null ? request.getTopK() : 3);
        return ResponseEntity.ok(new RagResponse(answer));
    }

    @Data
    public static class DocumentRequest {
        private String title;
        private String content;
        private String source;
    }

    @Data
    public static class QueryRequest {
        private String question;
        private Integer topK;
    }

    @Data
    @AllArgsConstructor
    public static class RagResponse {
        private String answer;
    }
}
