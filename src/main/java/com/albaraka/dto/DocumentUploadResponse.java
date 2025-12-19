package com.albaraka.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {
    private Long documentId;
    private String fileName;
    private String fileType;
    private String message;
}

