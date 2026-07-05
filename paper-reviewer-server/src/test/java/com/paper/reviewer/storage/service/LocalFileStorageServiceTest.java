package com.paper.reviewer.storage.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDirectory;

    private Path papers;
    private Path exports;
    private Path uploads;
    private LocalFileStorageService service;

    @BeforeEach
    void setUp() {
        papers = tempDirectory.resolve("storage/papers");
        exports = tempDirectory.resolve("storage/exports");
        uploads = tempDirectory.resolve("storage/tmp");
        service = new LocalFileStorageService(papers, exports, uploads);
    }

    @Test
    void generatesPaperAndExportDirectoriesUsingDocumentedConvention() {
        assertTrue(service.paperDirectory(12, 34).endsWith(Path.of("papers", "12", "34")));
        assertTrue(service.exportDirectory(12, 56).endsWith(Path.of("exports", "12", "56")));
    }

    @Test
    void savesTemporaryFileThenMovesItToPaperDirectory() throws Exception {
        byte[] content = "pdf-content".getBytes(StandardCharsets.UTF_8);

        Path temporaryFile = service.saveTemporary(new ByteArrayInputStream(content), "upload.pdf");
        assertTrue(temporaryFile.startsWith(uploads.toAbsolutePath()));

        Path permanentFile = service.moveTemporaryToPaper(temporaryFile, 7, 9, "original.pdf");

        assertTrue(permanentFile.endsWith(Path.of("papers", "7", "9", "original.pdf")));
        assertArrayEquals(content, Files.readAllBytes(permanentFile));
        assertFalse(Files.exists(temporaryFile));
    }

    @Test
    void movesTemporaryFileToExportDirectory() {
        Path temporaryFile = service.saveTemporary(
                new ByteArrayInputStream("report".getBytes(StandardCharsets.UTF_8)), "report.md");

        Path exportFile = service.moveTemporaryToExport(temporaryFile, 3, 8, "review.md");

        assertTrue(exportFile.endsWith(Path.of("exports", "3", "8", "review.md")));
        assertTrue(Files.isRegularFile(exportFile));
    }

    @Test
    void rejectsTraversalInFilenameAndTemporarySource() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> service.saveTemporary(new ByteArrayInputStream(new byte[0]), "../paper.pdf"));

        Path outside = tempDirectory.resolve("outside.pdf");
        Files.writeString(outside, "outside");
        assertThrows(IllegalArgumentException.class,
                () -> service.moveTemporaryToPaper(outside, 1, 2, "original.pdf"));
        assertTrue(Files.exists(outside));
    }

    @Test
    void physicallyDeletesSingleFile() throws Exception {
        Path directory = service.paperDirectory(1, 2);
        Files.createDirectories(directory);
        Path file = Files.writeString(directory.resolve("original.pdf"), "content");

        assertTrue(service.deleteFile(file));
        assertFalse(Files.exists(file));
        assertFalse(service.deleteFile(file));
    }

    @Test
    void physicallyDeletesDirectoryRecursively() throws Exception {
        Path directory = service.exportDirectory(1, 22);
        Files.createDirectories(directory.resolve("nested"));
        Files.writeString(directory.resolve("review.md"), "content");
        Files.writeString(directory.resolve("nested/review.pdf"), "content");

        assertTrue(service.deleteDirectory(directory));
        assertFalse(Files.exists(directory));
    }

    @Test
    void refusesToDeleteAnythingOutsideStorageRootsOrStorageRootsThemselves() throws Exception {
        Path outside = Files.writeString(tempDirectory.resolve("outside.txt"), "keep");

        assertFalse(service.deleteFile(outside));
        assertFalse(service.deleteDirectory(papers));
        assertTrue(Files.exists(outside));
        assertTrue(Files.exists(papers));
    }
}
