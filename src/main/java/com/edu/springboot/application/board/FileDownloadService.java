package com.edu.springboot.application.board;

import java.nio.file.Path;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.board.BoardFile;
import com.edu.springboot.domain.board.BoardFileRepository;
import com.edu.springboot.domain.file.FileStorage;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Application
 * 객체: FileDownloadService
 * 책임: 유스케이스. @Transactional. Domain 포트만 의존
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/features/09-archive-attachments.md](../../../../../../../../docs/features/09-archive-attachments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileDownloadService {

	private final BoardFileRepository boardFileRepository;
	private final FileStorage fileStorage;

	public FileDownload get(Long fileId) {
		BoardFile file = boardFileRepository.findById(fileId)
			.orElseThrow(() -> new BusinessException("파일을 찾을 수 없습니다."));
		return new FileDownload(file.getOriginalName(), file.getContentType(), fileStorage.resolve(file.getStoredName()));
	}

	public record FileDownload(String originalName, String contentType, Path path) {
	}
}
