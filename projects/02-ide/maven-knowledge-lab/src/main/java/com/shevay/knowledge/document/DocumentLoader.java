package com.shevay.knowledge.document;

import com.shevay.knowledge.model.Document;
import com.shevay.knowledge.util.HashUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service responsible for discovering, reading, and constructing Document objects
 * recursively from a knowledge directory.
 */
public class DocumentLoader {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(".md", ".txt");

    /**
     * Loads documents recursively from the specified root path string.
     *
     * @param rootPath directory path string
     * @return list of constructed Document models
     * @throws IOException if an I/O error occurs
     */
    public List<Document> loadDocuments(String rootPath) throws IOException {
        if (rootPath == null || rootPath.isBlank()) {
            throw new IllegalArgumentException("Knowledge root path must not be null or blank");
        }
        return loadDocuments(com.shevay.knowledge.config.AppConfig.resolvePath(rootPath));
    }

    /**
     * Loads documents recursively from the specified root Path.
     *
     * @param rootDir root directory Path
     * @return list of constructed Document models
     * @throws IOException if an I/O error occurs
     */
    public List<Document> loadDocuments(Path rootDir) throws IOException {
        if (rootDir == null || !Files.exists(rootDir)) {
            throw new IllegalArgumentException("Knowledge directory does not exist: " + rootDir);
        }
        if (!Files.isDirectory(rootDir)) {
            throw new IllegalArgumentException("Knowledge path is not a directory: " + rootDir);
        }

        List<Document> documents = new ArrayList<>();
        Path normalizedRoot = rootDir.toAbsolutePath().normalize();

        try (Stream<Path> stream = Files.walk(normalizedRoot)) {
            List<Path> filePaths = stream.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::toString))
                    .collect(Collectors.toList());

            for (Path filePath : filePaths) {
                String fileName = filePath.getFileName().toString();
                if (!isSupportedExtension(fileName)) {
                    continue;
                }

                Path relativePath = normalizedRoot.relativize(filePath);
                String normalizedRelPath = HashUtil.normalizePath(relativePath.toString());

                String content = Files.readString(filePath, StandardCharsets.UTF_8);
                if (content.isBlank()) {
                    continue; // Skip empty files according to empty file policy
                }

                String docId = HashUtil.sha256(normalizedRelPath);
                String contentHash = HashUtil.sha256(content);
                String title = extractTitle(filePath, content);

                Document doc = new Document(
                        docId,
                        title,
                        content,
                        normalizedRelPath,
                        contentHash,
                        Map.of(
                                "fileName", fileName,
                                "extension", getExtension(fileName)
                        )
                );
                documents.add(doc);
            }
        }
        return documents;
    }

    public static boolean isSupportedExtension(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.stream().anyMatch(lower::endsWith);
    }

    private static String getExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(idx) : "";
    }

    private static String extractTitle(Path filePath, String content) {
        for (String line : content.split("\r?\n")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("# ")) {
                return trimmed.substring(2).trim();
            }
        }
        return filePath.getFileName().toString();
    }
}
