package com.ssafy.ssabre.upload.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UploadService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.base-url:}")
    private String baseUrl;

    /**
     * 이미지를 temp 폴더에 업로드
     */
    public String uploadImage(MultipartFile file) throws IOException {
        validateImageFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString().substring(0, 8) + "." + extension;

        String relativePath = "temp";

        Path uploadPath = Paths.get(uploadDir, relativePath);
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(newFilename);
        file.transferTo(filePath.toFile());

        return buildUrl(relativePath + "/" + newFilename);
    }

    /**
     * 식단 이미지를 캠퍼스/날짜별 폴더에 업로드
     * @param file 업로드할 이미지 파일
     * @param campusId 캠퍼스 ID
     * @param date 날짜
     * @return 업로드된 이미지 URL
     */
    public String uploadMealImage(MultipartFile file, Long campusId, LocalDate date) throws IOException {
        validateImageFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString().substring(0, 8) + "." + extension;

        String relativePath = "meal/" + campusId + "/" + date.toString();

        Path uploadPath = Paths.get(uploadDir, relativePath);
        Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(newFilename);
        file.transferTo(filePath.toFile());

        return buildUrl(relativePath + "/" + newFilename);
    }

    /**
     * temp 폴더의 파일들을 특정 폴더로 이동
     * @param tempUrls temp 폴더에 있는 파일 URL 목록
     * @param targetFolder 이동할 대상 폴더 (예: "post/123")
     * @return 이동된 파일들의 새 URL 목록
     */
    public List<String> moveFromTemp(List<String> tempUrls, String targetFolder) throws IOException {
        List<String> newUrls = new ArrayList<>();

        Path targetPath = Paths.get(uploadDir, targetFolder);
        Files.createDirectories(targetPath);

        for (String tempUrl : tempUrls) {
            String filename = extractFilename(tempUrl);
            if (filename == null) continue;

            Path sourcePath = Paths.get(uploadDir, "temp", filename);
            Path destPath = targetPath.resolve(filename);

            if (Files.exists(sourcePath)) {
                Files.move(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                newUrls.add(buildUrl(targetFolder + "/" + filename));
            } else {
                // temp에 없으면 원래 URL 유지 (이미 이동된 경우)
                newUrls.add(tempUrl);
            }
        }

        return newUrls;
    }

    /**
     * 특정 폴더 전체 삭제 (게시글 삭제 시 사용)
     * @param folderPath 삭제할 폴더 경로 (예: "post/123")
     */
    public void deleteFolder(String folderPath) throws IOException {
        Path targetPath = Paths.get(uploadDir, folderPath);

        if (!Files.exists(targetPath)) {
            return;
        }

        Files.walkFileTree(targetPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 단일 파일 삭제
     */
    public void deleteFile(String fileUrl) throws IOException {
        String relativePath = extractRelativePath(fileUrl);
        if (relativePath == null) return;

        Path filePath = Paths.get(uploadDir, relativePath);
        Files.deleteIfExists(filePath);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다.");
        }

        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String buildUrl(String relativePath) {
        if (baseUrl != null && !baseUrl.isEmpty()) {
            return baseUrl + "/uploads/" + relativePath;
        }
        return "/uploads/" + relativePath;
    }

    private String extractFilename(String url) {
        if (url == null) return null;
        int lastSlash = url.lastIndexOf("/");
        return lastSlash >= 0 ? url.substring(lastSlash + 1) : url;
    }

    private String extractRelativePath(String url) {
        if (url == null) return null;
        int uploadsIndex = url.indexOf("/uploads/");
        if (uploadsIndex >= 0) {
            return url.substring(uploadsIndex + "/uploads/".length());
        }
        return null;
    }
}
