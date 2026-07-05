package com.paper.reviewer.extraction.service;

import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class PaperExtractionService {
    public PdfDocumentInfo inspect(Path pdf) {
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            String title = document.getDocumentInformation().getTitle();
            return new PdfDocumentInfo(document.getNumberOfPages(), title == null || title.isBlank() ? null : title.strip());
        } catch (IOException | RuntimeException exception) {
            throw new BusinessException(ErrorCode.PAPER_INVALID_TYPE, "File is not a valid PDF", exception);
        }
    }

    public String extractText(Path pdf) {
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            return new PDFTextStripper().getText(document);
        } catch (IOException | RuntimeException exception) {
            throw new BusinessException(ErrorCode.PDF_EXTRACTION_FAILED, nullSafeMessage(exception), exception);
        }
    }

    private String nullSafeMessage(Exception exception) {
        return exception.getMessage() == null ? ErrorCode.PDF_EXTRACTION_FAILED.getDefaultMessage()
                : ErrorCode.PDF_EXTRACTION_FAILED.getDefaultMessage() + ": " + exception.getMessage();
    }
}
