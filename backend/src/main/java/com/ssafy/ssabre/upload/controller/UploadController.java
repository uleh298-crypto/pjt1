package com.ssafy.ssabre.upload.controller;

import com.ssafy.ssabre.campus.service.CampusService;
import com.ssafy.ssabre.upload.dto.ImageUploadResponse;
import com.ssafy.ssabre.upload.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@Tag(name = "Uploads", description = "파일 업로드 API")
public class UploadController {

    private final UploadService uploadService;
    private final CampusService campusService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드", description = "이미지 파일을 업로드합니다.")
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {

        String url = uploadService.uploadImage(file);
        return ResponseEntity.ok(ImageUploadResponse.of(url));
    }

    @PostMapping(value = "/meals", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "식단 이미지 업로드", description = "캠퍼스별, 날짜별 폴더에 식단 이미지를 업로드하고 Menu에 저장합니다.")
    public ResponseEntity<ImageUploadResponse> uploadMealImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("campusId") Long campusId,
            @RequestParam("date") LocalDate date) throws IOException {

        String url = uploadService.uploadMealImage(file, campusId, date);
        campusService.createMenu(campusId, date, url);
        return ResponseEntity.ok(ImageUploadResponse.of(url));
    }

    @DeleteMapping("/images")
    @Operation(summary = "이미지 삭제", description = "업로드된 이미지를 삭제합니다.")
    public ResponseEntity<ImageDeleteResponse> deleteImage(
            @RequestBody ImageDeleteRequest request) throws IOException {

        uploadService.deleteFile(request.url());
        return ResponseEntity.ok(new ImageDeleteResponse(true));
    }

    public record ImageDeleteRequest(String url) {}
    public record ImageDeleteResponse(boolean success) {}
}
