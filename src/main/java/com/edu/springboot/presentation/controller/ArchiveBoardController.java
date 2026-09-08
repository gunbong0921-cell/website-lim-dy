package com.edu.springboot.presentation.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.edu.springboot.application.board.ArchiveBoardService;
import com.edu.springboot.application.board.ArchiveBoardService.UploadFile;
import com.edu.springboot.application.board.LikeService;
import com.edu.springboot.application.board.dto.BoardDetailResponse;
import com.edu.springboot.application.board.dto.BoardSummaryResponse;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.common.PageResponse;
import com.edu.springboot.domain.board.ViewCountPolicy;
import com.edu.springboot.presentation.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boards/archive")
@RequiredArgsConstructor
public class ArchiveBoardController {

	private final ArchiveBoardService archiveBoardService;
	private final LikeService likeService;
	private final ViewCountPolicy viewCountPolicy;

	@GetMapping
	public ApiResponse<PageResponse<BoardSummaryResponse>> list(
		@RequestParam(name = "page", defaultValue = "1") int page,
		@RequestParam(name = "size", defaultValue = "10") int size,
		@RequestParam(name = "searchType", required = false) String searchType,
		@RequestParam(name = "keyword", required = false) String keyword
	) {
		return ApiResponse.ok(archiveBoardService.list(searchType, keyword, page, size));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<BoardDetailResponse>> read(@PathVariable("id") Long id, HttpServletRequest request) {
		String cookieName = viewCountPolicy.cookieName("ARCHIVE", id);
		boolean viewed = CookieSupport.has(request, cookieName);
		BoardDetailResponse detail = archiveBoardService.read(id, viewed);
		var body = ApiResponse.ok(detail);
		if (!viewed && detail.visitIncreased()) {
			ResponseCookie cookie = CookieSupport.viewedToday(
				cookieName,
				viewCountPolicy.cookieMaxAgeSeconds(LocalDateTime.now())
			);
			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(body);
		}
		return ResponseEntity.ok(body);
	}

	@PostMapping
	public ApiResponse<Long> write(
		Authentication authentication,
		@RequestParam("title") String title,
		@RequestParam("content") String content,
		@RequestParam("files") List<MultipartFile> files
	) {
		return ApiResponse.ok(archiveBoardService.write(title, content, authentication.getName(), toUploads(files)),
			"등록되었습니다.");
	}

	@PutMapping("/{id}")
	public ApiResponse<Void> update(
		@PathVariable("id") Long id,
		Authentication authentication,
		@RequestParam("title") String title,
		@RequestParam("content") String content,
		@RequestParam(value = "files", required = false) List<MultipartFile> files
	) {
		archiveBoardService.update(id, title, content, authentication.getName(), toUploads(files));
		return ApiResponse.ok(null, "수정되었습니다.");
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable("id") Long id, Authentication authentication) {
		archiveBoardService.delete(id, authentication.getName());
		return ApiResponse.ok(null, "삭제되었습니다.");
	}

	@PostMapping("/{id}/like")
	public ApiResponse<Integer> like(@PathVariable("id") Long id, Authentication authentication) {
		return ApiResponse.ok(likeService.like("ARCHIVE", id, AuthSupport.loginId(authentication), false).count(),
			"좋아요가 반영되었습니다.");
	}

	private List<UploadFile> toUploads(List<MultipartFile> files) {
		if (files == null) {
			return List.of();
		}
		List<UploadFile> uploads = new ArrayList<>();
		for (MultipartFile file : files) {
			if (file.isEmpty()) {
				continue;
			}
			try {
				uploads.add(new UploadFile(
					file.getOriginalFilename(),
					file.getContentType(),
					file.getSize(),
					file.getInputStream()
				));
			} catch (Exception ex) {
				throw new BusinessException("파일을 읽을 수 없습니다.");
			}
		}
		return uploads;
	}
}
