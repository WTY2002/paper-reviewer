package com.paper.reviewer.paper.service;

import com.paper.reviewer.common.BusinessException;
import com.paper.reviewer.common.ErrorCode;
import com.paper.reviewer.config.UploadProperties;
import com.paper.reviewer.extraction.repository.PaperExtractionRepository;
import com.paper.reviewer.extraction.service.PaperExtractionService;
import com.paper.reviewer.paper.domain.Paper;
import com.paper.reviewer.paper.repository.PaperRepository;
import com.paper.reviewer.storage.service.LocalFileStorageService;
import com.paper.reviewer.storage.service.StorageQuotaService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaperServiceTest {
    @TempDir Path temp;
    private PaperRepository papers;
    private PaperExtractionRepository extractions;
    private StorageQuotaService quota;
    private PaperService service;

    @BeforeEach
    void setUp() {
        papers = mock(PaperRepository.class);
        extractions = mock(PaperExtractionRepository.class);
        quota = mock(StorageQuotaService.class);
        LocalFileStorageService storage = new LocalFileStorageService(
                temp.resolve("papers"), temp.resolve("exports"), temp.resolve("tmp"));
        service = new PaperService(papers, extractions, new PaperExtractionService(), storage, quota,
                new UploadProperties(20, 300, 500));
    }

    @Test
    void uploadsValidPdfThroughTemporaryStorageAndExtractsText() throws Exception {
        byte[] bytes = pdf(1, "A useful paper", "Extract me");
        Paper inserted = paper(7L, "EXTRACTING", "pending", bytes.length);
        Path original = temp.resolve("papers/3/7/original.pdf").toAbsolutePath();
        Paper completed = paper(7L, "EXTRACTED", original.toString(), bytes.length);
        when(papers.save(any())).thenReturn(inserted);
        when(papers.update(any())).thenReturn(completed);
        when(papers.findOwnedById(3L, 7L)).thenReturn(Optional.of(completed));

        Paper result = service.upload(3L,
                new MockMultipartFile("file", "paper.pdf", "application/pdf", bytes));

        assertThat(result.status()).isEqualTo("EXTRACTED");
        assertThat(Files.readString(temp.resolve("papers/3/7/extraction.txt"))).contains("Extract me");
        assertThat(Files.readAllBytes(temp.resolve("papers/3/7/original.pdf"))).isEqualTo(bytes);
        verify(quota).validateCanStore(3L, bytes.length);
        verify(extractions).save(any());
    }

    @Test
    void rejectsEmptyWrongMimeAndForgedMagic() {
        assertCode(new MockMultipartFile("file", "paper.pdf", "application/pdf", new byte[0]),
                ErrorCode.PAPER_INVALID_TYPE);
        assertCode(new MockMultipartFile("file", "paper.pdf", "text/plain", "%PDF-".getBytes()),
                ErrorCode.PAPER_INVALID_TYPE);
        assertCode(new MockMultipartFile("file", "paper.pdf", "application/pdf", "not pdf".getBytes()),
                ErrorCode.PAPER_INVALID_TYPE);
    }

    @Test
    void rejectsFileLargerThanConfiguredMaximum() {
        PaperService tinyLimit = new PaperService(papers, extractions, new PaperExtractionService(),
                new LocalFileStorageService(temp.resolve("p2"), temp.resolve("e2"), temp.resolve("t2")),
                quota, new UploadProperties(1, 300, 500));
        MockMultipartFile file = new MockMultipartFile("file", "large.pdf", "application/pdf",
                new byte[1024 * 1024 + 1]);
        assertThatThrownBy(() -> tinyLimit.upload(3L, file))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.PAPER_FILE_TOO_LARGE));
    }

    @Test
    void rejectsPdfAbovePageLimit() throws Exception {
        byte[] bytes = pdf(2, null, null);
        PaperService onePage = new PaperService(papers, extractions, new PaperExtractionService(),
                new LocalFileStorageService(temp.resolve("p3"), temp.resolve("e3"), temp.resolve("t3")),
                quota, new UploadProperties(20, 1, 500));
        assertThatThrownBy(() -> onePage.upload(3L,
                new MockMultipartFile("file", "pages.pdf", "application/pdf", bytes)))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.PAPER_PAGE_LIMIT_EXCEEDED));
    }

    @Test
    void rejectsCrossUserReadsAndDeletesAsNotFound() {
        when(papers.findOwnedById(99L, 7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.get(99L, 7L))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(ErrorCode.PAPER_NOT_FOUND));
        assertThatThrownBy(() -> service.delete(99L, 7L)).isInstanceOf(BusinessException.class);
    }

    private void assertCode(MockMultipartFile file, ErrorCode code) {
        assertThatThrownBy(() -> service.upload(3L, file))
                .isInstanceOfSatisfying(BusinessException.class,
                        error -> assertThat(error.getErrorCode()).isEqualTo(code));
    }

    private Paper paper(long id, String status, String path, long size) {
        return new Paper(id, 3L, "A useful paper", "paper.pdf", path, size, 1, null,
                status, LocalDateTime.now(), LocalDateTime.now());
    }

    private byte[] pdf(int pages, String title, String text) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.getDocumentInformation().setTitle(title);
            for (int i = 0; i < pages; i++) document.addPage(new PDPage());
            if (text != null) {
                try (PDPageContentStream stream = new PDPageContentStream(document, document.getPage(0))) {
                    stream.beginText();
                    stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                    stream.newLineAtOffset(72, 720);
                    stream.showText(text);
                    stream.endText();
                }
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
