package com.paper.reviewer.export.dto;
import com.paper.reviewer.export.domain.ExportType;
public record CreateExportRequest(ExportType exportType) { public CreateExportRequest { if(exportType==null)exportType=ExportType.MARKDOWN; } }
