package com.paper.reviewer.export.dto;
import com.paper.reviewer.export.domain.ReviewExport;
public record ExportResponse(long exportId,String status,String exportType,String downloadUrl){public static ExportResponse from(ReviewExport value){return new ExportResponse(value.id(),value.status().name(),value.exportType().name(),"/api/exports/"+value.id());}}
