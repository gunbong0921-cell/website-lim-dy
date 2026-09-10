package com.edu.springboot.presentation.controller;

import java.nio.file.Files;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edu.springboot.application.board.FileDownloadService;
import com.edu.springboot.application.board.FileDownloadService.FileDownload;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Presentation
 * 객체: FileController
 * 책임: HTTP 입구. 검증·Application 호출·ApiResponse. Domain 객체 비노출
 * 문서: [docs/technical/02-technical-specification.md](../../../../../../../../docs/technical/02-technical-specification.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

	private final FileDownloadService fileDownloadService;

	@GetMapping("/{id}")
	public ResponseEntity<Resource> download(@PathVariable("id") Long id) throws Exception {
		FileDownload file = fileDownloadService.get(id);
		MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
		if (file.contentType() != null && !file.contentType().isBlank()) {
			mediaType = MediaType.parseMediaType(file.contentType());
		}
		String encoded = java.net.URLEncoder.encode(file.originalName(), java.nio.charset.StandardCharsets.UTF_8)
			.replace("+", "%20");
		return ResponseEntity.ok()
			.contentType(mediaType)
			.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encoded)
			.contentLength(Files.size(file.path()))
			.body(new FileSystemResource(file.path()));
	}
}
