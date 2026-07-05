package com.paper.reviewer.paper.service;

import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.config.UploadProperties;
import com.paper.reviewer.extraction.domain.PaperExtraction;
import com.paper.reviewer.extraction.repository.PaperExtractionRepository;
import com.paper.reviewer.extraction.service.PaperExtractionService;
import com.paper.reviewer.extraction.service.PdfDocumentInfo;
import com.paper.reviewer.paper.domain.Paper;
import com.paper.reviewer.paper.repository.PaperRepository;
import com.paper.reviewer.storage.service.LocalFileStorageService;
import com.paper.reviewer.storage.service.StorageQuotaService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

@Service
public class PaperService {
    private static final long BYTES_PER_MEBIBYTE = 1024L * 1024L;
    private static final byte[] PDF_MAGIC = {'%', 'P', 'D', 'F', '-'};
    private final PaperRepository paperRepository;
    private final PaperExtractionRepository extractionRepository;
    private final PaperExtractionService extractionService;
    private final LocalFileStorageService storageService;
    private final StorageQuotaService quotaService;
    private final UploadProperties uploadProperties;

    public PaperService(PaperRepository paperRepository, PaperExtractionRepository extractionRepository,
                        PaperExtractionService extractionService, LocalFileStorageService storageService,
                        StorageQuotaService quotaService, UploadProperties uploadProperties) {
        this.paperRepository = paperRepository;
        this.extractionRepository = extractionRepository;
        this.extractionService = extractionService;
        this.storageService = storageService;
        this.quotaService = quotaService;
        this.uploadProperties = uploadProperties;
    }

    @Transactional
    public Paper upload(long userId, MultipartFile file) {
        validateBasic(file);
        String originalFilename = safeFilename(file.getOriginalFilename());
        Path temporary = null;
        Paper saved = null;
        try {
            temporary = storageService.saveTemporary(file.getInputStream(), originalFilename);
            validateMagic(temporary);
            PdfDocumentInfo info = extractionService.inspect(temporary);
            if (info.pageCount() > uploadProperties.maxPageCount()) {
                throw new BusinessException(ErrorCode.PAPER_PAGE_LIMIT_EXCEEDED);
            }
            quotaService.validateCanStore(userId, file.getSize());
            String title = info.title() == null ? filenameStem(originalFilename) : info.title();
            saved = paperRepository.save(new Paper(null, userId, title, originalFilename, "pending",
                    file.getSize(), info.pageCount(), null, "EXTRACTING", null, null));
            Path original = storageService.moveTemporaryToPaper(temporary, userId, saved.id(), "original.pdf");
            temporary = null;
            String text = extractionService.extractText(original);
            storageService.writePaperText(userId, saved.id(), "extraction.txt", text);
            extractionRepository.save(new PaperExtraction(null, saved.id(), text, info.pageCount(),
                    "COMPLETED", null, null, null));
            // Update status and final path without exposing a generic cross-user update operation.
            Paper completed = new Paper(saved.id(), saved.userId(), saved.title(), saved.originalFilename(),
                    original.toString(), saved.fileSize(), saved.pageCount(), saved.language(), "EXTRACTED",
                    saved.createdAt(), saved.updatedAt());
            paperRepository.update(completed);
            return paperRepository.findOwnedById(userId, saved.id()).orElse(completed);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.PDF_EXTRACTION_FAILED, "Failed to read uploaded file", exception);
        } catch (RuntimeException exception) {
            if (saved != null) storageService.deleteDirectory(storageService.paperDirectory(userId, saved.id()));
            throw exception;
        } finally {
            if (temporary != null) storageService.deleteDirectory(temporary.getParent());
        }
    }

    public List<Paper> list(long userId) { return paperRepository.findAllOwnedBy(userId); }

    public Paper get(long userId, long paperId) {
        return paperRepository.findOwnedById(userId, paperId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAPER_NOT_FOUND));
    }

    public Resource getPdf(long userId, long paperId) {
        Paper paper = get(userId, paperId);
        Path path = Path.of(paper.filePath());
        if (!Files.isRegularFile(path)) throw new BusinessException(ErrorCode.PAPER_NOT_FOUND);
        return new FileSystemResource(path);
    }

    @Transactional(noRollbackFor = BusinessException.class)
    public void delete(long userId, long paperId) {
        Paper paper = get(userId, paperId);
        if (!paperRepository.deleteOwnedById(userId, paperId)) throw new BusinessException(ErrorCode.PAPER_NOT_FOUND);
        if (!storageService.deleteDirectory(storageService.paperDirectory(userId, paper.id()))) {
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    private void validateBasic(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BusinessException(ErrorCode.PAPER_INVALID_TYPE, "PDF file must not be empty");
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".pdf")
                || !"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new BusinessException(ErrorCode.PAPER_INVALID_TYPE);
        }
        long maximum = Math.multiplyExact(uploadProperties.maxFileSizeMb(), BYTES_PER_MEBIBYTE);
        if (file.getSize() > maximum) throw new BusinessException(ErrorCode.PAPER_FILE_TOO_LARGE);
    }

    private void validateMagic(Path file) {
        try (InputStream input = Files.newInputStream(file)) {
            byte[] bytes = input.readNBytes(PDF_MAGIC.length);
            if (bytes.length != PDF_MAGIC.length) throw new BusinessException(ErrorCode.PAPER_INVALID_TYPE);
            for (int i = 0; i < PDF_MAGIC.length; i++) if (bytes[i] != PDF_MAGIC[i])
                throw new BusinessException(ErrorCode.PAPER_INVALID_TYPE);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.PAPER_INVALID_TYPE, "Could not validate PDF", exception);
        }
    }

    private String safeFilename(String filename) {
        return Path.of(filename).getFileName().toString();
    }

    private String filenameStem(String filename) {
        return filename.substring(0, filename.length() - 4);
    }
}
