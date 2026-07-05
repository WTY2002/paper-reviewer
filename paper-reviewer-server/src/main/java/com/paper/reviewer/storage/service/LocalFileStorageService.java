package com.paper.reviewer.storage.service;

import com.paper.reviewer.storage.domain.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class LocalFileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    private final Path paperRoot;
    private final Path exportRoot;
    private final Path temporaryRoot;
    private final List<Path> managedRoots;

    @Autowired
    public LocalFileStorageService(
            @Value("${app.storage.paper-root:storage/papers}") String paperRoot,
            @Value("${app.storage.export-root:storage/exports}") String exportRoot,
            @Value("${app.storage.temp-root:storage/tmp}") String temporaryRoot) {
        this(Path.of(paperRoot), Path.of(exportRoot), Path.of(temporaryRoot));
    }

    public LocalFileStorageService(Path paperRoot, Path exportRoot, Path temporaryRoot) {
        this.paperRoot = initializeRoot(paperRoot, "paperRoot");
        this.exportRoot = initializeRoot(exportRoot, "exportRoot");
        this.temporaryRoot = initializeRoot(temporaryRoot, "temporaryRoot");
        this.managedRoots = List.of(this.paperRoot, this.exportRoot, this.temporaryRoot);
    }

    public Path paperDirectory(long userId, long paperId) {
        requirePositive(userId, "userId");
        requirePositive(paperId, "paperId");
        return resolveUnder(paperRoot, Long.toString(userId), Long.toString(paperId));
    }

    public Path exportDirectory(long userId, long reviewId) {
        requirePositive(userId, "userId");
        requirePositive(reviewId, "reviewId");
        return resolveUnder(exportRoot, Long.toString(userId), Long.toString(reviewId));
    }

    public Path saveTemporary(InputStream content, String originalFilename) {
        Objects.requireNonNull(content, "content");
        String filename = validateFilename(originalFilename);
        Path uploadDirectory = resolveUnder(temporaryRoot, UUID.randomUUID().toString());
        Path target = resolveUnder(uploadDirectory, filename);
        try {
            Files.createDirectories(uploadDirectory);
            verifyNoSymbolicLinks(temporaryRoot, target.getParent());
            Files.copy(content, target);
            return target;
        } catch (IOException exception) {
            deleteDirectory(uploadDirectory);
            throw new StorageException("Failed to save temporary file", exception);
        }
    }

    public Path moveTemporaryToPaper(Path temporaryFile, long userId, long paperId, String filename) {
        return moveTemporary(temporaryFile, paperDirectory(userId, paperId), filename);
    }

    public Path moveTemporaryToExport(Path temporaryFile, long userId, long reviewId, String filename) {
        return moveTemporary(temporaryFile, exportDirectory(userId, reviewId), filename);
    }

    public Path writePaperText(long userId, long paperId, String filename, String content) {
        Path directory = paperDirectory(userId, paperId);
        Path target = resolveUnder(directory, validateFilename(filename));
        try {
            Files.createDirectories(directory);
            verifyNoSymbolicLinks(paperRoot, directory);
            Files.writeString(target, Objects.requireNonNull(content, "content"), StandardCharsets.UTF_8);
            return target;
        } catch (IOException exception) {
            throw new StorageException("Failed to write paper text", exception);
        }
    }

    public Path writeExport(long userId, long reviewId, String filename, byte[] content) {
        Path directory = exportDirectory(userId, reviewId);
        Path target = resolveUnder(directory, validateFilename(filename));
        try {
            Files.createDirectories(directory);
            verifyNoSymbolicLinks(exportRoot, directory);
            Files.write(target, Objects.requireNonNull(content, "content"));
            return target;
        } catch (IOException exception) {
            throw new StorageException("Failed to write export file", exception);
        }
    }

    public Path readableExport(Path file) {
        Path safe = validateUnderRoot(file, exportRoot, true);
        if (!Files.isRegularFile(safe, LinkOption.NOFOLLOW_LINKS)) {
            throw new StorageException("Export file does not exist");
        }
        return safe;
    }

    public Path paperFile(long userId, long paperId, String filename) {
        return resolveUnder(paperDirectory(userId, paperId), validateFilename(filename));
    }

    public boolean deleteFile(Path file) {
        try {
            Path safeFile = validateManagedPath(file, false);
            if (isManagedRoot(safeFile)) {
                throw new IllegalArgumentException("A storage root cannot be deleted");
            }
            if (Files.exists(safeFile, LinkOption.NOFOLLOW_LINKS)
                    && !Files.isRegularFile(safeFile, LinkOption.NOFOLLOW_LINKS)) {
                throw new IllegalArgumentException("Path is not a regular storage file");
            }
            return Files.deleteIfExists(safeFile);
        } catch (IOException | RuntimeException exception) {
            log.error("Failed to physically delete storage file: {}", file, exception);
            return false;
        }
    }

    public boolean deleteDirectory(Path directory) {
        try {
            Path safeDirectory = validateManagedPath(directory, false);
            if (isManagedRoot(safeDirectory)) {
                throw new IllegalArgumentException("A storage root cannot be deleted");
            }
            if (!Files.exists(safeDirectory, LinkOption.NOFOLLOW_LINKS)) {
                return false;
            }
            if (!Files.isDirectory(safeDirectory, LinkOption.NOFOLLOW_LINKS)) {
                throw new IllegalArgumentException("Path is not a storage directory");
            }
            Files.walkFileTree(safeDirectory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exception) throws IOException {
                    if (exception != null) {
                        throw exception;
                    }
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            return true;
        } catch (IOException | RuntimeException exception) {
            log.error("Failed to physically delete storage directory: {}", directory, exception);
            return false;
        }
    }

    private Path moveTemporary(Path temporaryFile, Path destinationDirectory, String filename) {
        Path source = validateUnderRoot(temporaryFile, temporaryRoot, true);
        String safeFilename = validateFilename(filename);
        if (!Files.isRegularFile(source, LinkOption.NOFOLLOW_LINKS)) {
            throw new StorageException("Temporary file does not exist or is not a regular file");
        }
        Path target = resolveUnder(destinationDirectory, safeFilename);
        try {
            Files.createDirectories(destinationDirectory);
            verifyNoSymbolicLinks(rootFor(target), target.getParent());
            if (Files.exists(target, LinkOption.NOFOLLOW_LINKS)) {
                throw new StorageException("Permanent storage target already exists");
            }
            try {
                Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(source, target);
            }
            removeEmptyTemporaryParent(source.getParent());
            return target;
        } catch (IOException exception) {
            throw new StorageException("Failed to move temporary file to permanent storage", exception);
        }
    }

    private void removeEmptyTemporaryParent(Path directory) {
        if (directory == null || directory.equals(temporaryRoot)) {
            return;
        }
        try {
            Files.deleteIfExists(directory);
        } catch (IOException exception) {
            log.debug("Could not remove temporary upload directory: {}", directory, exception);
        }
    }

    private Path validateManagedPath(Path path, boolean mustExist) {
        Path normalized = normalize(path);
        Path root = managedRoots.stream()
                .filter(normalized::startsWith)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Path is outside managed storage roots"));
        verifyNoSymbolicLinks(root, mustExist ? normalized : nearestExistingParent(normalized));
        return normalized;
    }

    private Path validateUnderRoot(Path path, Path root, boolean mustExist) {
        Path normalized = normalize(path);
        if (!normalized.startsWith(root)) {
            throw new IllegalArgumentException("Path is outside the required storage root");
        }
        if (mustExist && !Files.exists(normalized, LinkOption.NOFOLLOW_LINKS)) {
            throw new StorageException("Storage path does not exist");
        }
        verifyNoSymbolicLinks(root, mustExist ? normalized : nearestExistingParent(normalized));
        return normalized;
    }

    private Path resolveUnder(Path root, String... segments) {
        Path resolved = root;
        for (String segment : segments) {
            resolved = resolved.resolve(segment);
        }
        return validateUnderRoot(resolved, root, false);
    }

    private void verifyNoSymbolicLinks(Path root, Path path) {
        if (path == null) {
            return;
        }
        Path current = root;
        Path relative = root.relativize(path);
        for (Path segment : relative) {
            current = current.resolve(segment);
            if (Files.isSymbolicLink(current)) {
                throw new IllegalArgumentException("Symbolic links are not allowed in storage paths");
            }
        }
    }

    private Path nearestExistingParent(Path path) {
        Path current = path;
        while (current != null && !Files.exists(current, LinkOption.NOFOLLOW_LINKS)) {
            current = current.getParent();
        }
        return current;
    }

    private Path rootFor(Path path) {
        return managedRoots.stream()
                .filter(path::startsWith)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Path is outside managed storage roots"));
    }

    private boolean isManagedRoot(Path path) {
        return managedRoots.contains(path);
    }

    private static String validateFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("filename cannot be blank");
        }
        if (!filename.equals(Path.of(filename).getFileName().toString())
                || filename.equals(".") || filename.equals("..")
                || filename.indexOf('/') >= 0 || filename.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("filename must not contain path segments");
        }
        return filename;
    }

    private static Path initializeRoot(Path root, String name) {
        Objects.requireNonNull(root, name);
        Path normalized = normalize(root);
        try {
            Files.createDirectories(normalized);
            if (Files.isSymbolicLink(normalized)) {
                throw new IllegalArgumentException(name + " must not be a symbolic link");
            }
            return normalized;
        } catch (IOException exception) {
            throw new StorageException("Failed to initialize " + name, exception);
        }
    }

    private static Path normalize(Path path) {
        return Objects.requireNonNull(path, "path").toAbsolutePath().normalize();
    }

    private static void requirePositive(long value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }
}
