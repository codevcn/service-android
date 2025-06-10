package com.example.taskmanager.controller;

import com.example.taskmanager.dto.ApiResponse;
import com.example.taskmanager.dto.FileDTO;
import com.example.taskmanager.model.File;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.FileRepository;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.FileStorageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "File management APIs")
public class FileController {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/upload/{taskId}")
    public ResponseEntity<ApiResponse> uploadTaskFile(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String storedFileName = fileStorageService.storeFile(file);

            File fileEntity = new File();
            fileEntity.setFileName(file.getOriginalFilename());
            fileEntity.setFilePath(storedFileName);
            fileEntity.setFileType(file.getContentType());
            fileEntity.setFileSize(file.getSize());
            fileEntity.setTask(task);
            fileEntity.setUser(user);

            fileRepository.save(fileEntity);

            return ResponseEntity
                    .ok(new ApiResponse("success", FileDTO.fromEntity(fileEntity), null));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Could not upload file: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<ApiResponse> getFilesByTask(@PathVariable Long taskId) {
        List<FileDTO> files = fileRepository.findByTask_IdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(FileDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new ApiResponse("success", files, null));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse> getFileDetails(@PathVariable Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
        return ResponseEntity.ok(new ApiResponse("success", FileDTO.fromEntity(file), null));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse> deleteFile(@PathVariable Long fileId) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));
            Files.deleteIfExists(Paths.get(file.getFilePath()));
            fileRepository.delete(file);
            return ResponseEntity.ok(new ApiResponse("success", "File deleted successfully", null));
        } catch (IOException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Could not delete file: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable Long fileId) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new RuntimeException("File not found"));

            Resource resource = fileStorageService.loadFileAsResource(file.getFilePath());

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"")
                    .header("Content-Type", file.getFileType())
                    .body(resource);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Could not download file: " + ex.getMessage(), null));
        }
    }

    @PostMapping("/upload/user/avatar")
    public ResponseEntity<ApiResponse> uploadUserAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Get current user
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse("error", "Only image files are allowed", null));
            }

            // Store file
            String storedFileName = fileStorageService.storeFile(file);

            // Update user avatar
            user.setAvatar(storedFileName);
            userRepository.save(user);

            return ResponseEntity.ok(new ApiResponse("success", new UploadUserAvatarResponse(file.getOriginalFilename(),
                    storedFileName, file.getContentType(), file.getSize()), null));

        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Could not upload avatar: " + ex.getMessage(), null));
        }
    }

    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<?> getImage(@PathVariable String fileName) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(fileName);
            String contentType = Files.probeContentType(resource.getFile().toPath());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse("error", "Could not load image: " + ex.getMessage(), null));
        }
    }

    private record UploadUserAvatarResponse(String filename, String filePath, String fileType, long fileSize) {
    }
}