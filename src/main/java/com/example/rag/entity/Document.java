package com.example.rag.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    
    private String id;
    private String title;
    private String content;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
