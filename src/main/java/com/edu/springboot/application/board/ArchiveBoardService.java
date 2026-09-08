package com.edu.springboot.application.board;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.board.dto.BoardDetailResponse;
import com.edu.springboot.application.board.dto.BoardSummaryResponse;
import com.edu.springboot.application.board.dto.FileResponse;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.common.PageResponse;
import com.edu.springboot.domain.board.ArchiveBoard;
import com.edu.springboot.domain.board.ArchiveBoardRepository;
import com.edu.springboot.domain.board.BoardFile;
import com.edu.springboot.domain.board.BoardFileRepository;
import com.edu.springboot.domain.board.ViewCountPolicy;
import com.edu.springboot.domain.file.FileStorage;
import com.edu.springboot.domain.file.FileTypeClassifier;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ArchiveBoardService {

	private final ArchiveBoardRepository archiveBoardRepository;
	private final BoardFileRepository boardFileRepository;
	private final FileStorage fileStorage;
	private final FileTypeClassifier fileTypeClassifier;
	private final ViewCountPolicy viewCountPolicy;

	@Transactional(readOnly = true)
	public PageResponse<BoardSummaryResponse> list(String searchType, String keyword, int page, int size) {
		int offset = (page - 1) * size;
		var rows = archiveBoardRepository.findPage(searchType, keyword, offset, size).stream()
			.map(this::toSummary)
			.toList();
		return PageResponse.of(rows, page, size, archiveBoardRepository.count(searchType, keyword));
	}

	public BoardDetailResponse read(Long id, boolean alreadyViewedToday) {
		ArchiveBoard board = find(id);
		boolean increased = viewCountPolicy.shouldIncrease(alreadyViewedToday);
		if (increased) {
			archiveBoardRepository.increaseVisitCount(id);
			board.setVisitCount(board.getVisitCount() + 1);
		}
		return toDetail(board, increased);
	}

	public Long write(String title, String content, String writer, List<UploadFile> files) {
		if (title == null || title.isBlank() || content == null || content.isBlank()) {
			throw new BusinessException("제목과 내용을 입력하세요.");
		}
		if (files == null || files.isEmpty()) {
			throw new BusinessException("자료실은 첨부파일이 필요합니다.");
		}
		ArchiveBoard board = new ArchiveBoard();
		board.setTitle(title);
		board.setContent(content);
		board.setWriter(writer);
		board.setCreatedAt(LocalDateTime.now());
		Long id = archiveBoardRepository.save(board).getId();
		storeFiles(id, files);
		return id;
	}

	public void update(Long id, String title, String content, String loginId, List<UploadFile> files) {
		ArchiveBoard board = find(id);
		assertOwner(board, loginId);
		board.setTitle(title);
		board.setContent(content);
		board.setUpdatedAt(LocalDateTime.now());
		archiveBoardRepository.update(board);
		if (files != null && !files.isEmpty()) {
			boardFileRepository.deleteByBoardId(id);
			storeFiles(id, files);
		}
	}

	public void delete(Long id, String loginId) {
		ArchiveBoard board = find(id);
		assertOwner(board, loginId);
		boardFileRepository.deleteByBoardId(id);
		archiveBoardRepository.delete(id);
	}

	public record UploadFile(String originalName, String contentType, long size, InputStream content) {
	}

	private void storeFiles(Long boardId, List<UploadFile> files) {
		for (UploadFile file : files) {
			try {
				String stored = fileStorage.store(file.originalName(), file.content());
				BoardFile row = new BoardFile();
				row.setBoardId(boardId);
				row.setOriginalName(file.originalName());
				row.setStoredName(stored);
				row.setContentType(file.contentType());
				row.setFileSize(file.size());
				row.setFileType(fileTypeClassifier.classify(file.originalName()).name());
				boardFileRepository.save(row);
			} catch (IOException ex) {
				throw new BusinessException("파일 저장에 실패했습니다.");
			}
		}
	}

	private ArchiveBoard find(Long id) {
		return archiveBoardRepository.findById(id)
			.orElseThrow(() -> new BusinessException("게시글을 찾을 수 없습니다."));
	}

	private void assertOwner(ArchiveBoard board, String loginId) {
		if (!board.getWriter().equals(loginId)) {
			throw new BusinessException("작성자만 수정/삭제할 수 있습니다.");
		}
	}

	private BoardSummaryResponse toSummary(ArchiveBoard board) {
		return new BoardSummaryResponse(
			board.getId(), board.getTitle(), board.getWriter(),
			board.getVisitCount(), board.getLikeCount(), board.getCreatedAt()
		);
	}

	private BoardDetailResponse toDetail(ArchiveBoard board, boolean increased) {
		List<FileResponse> files = boardFileRepository.findByBoardId(board.getId()).stream()
			.map(f -> new FileResponse(
				f.getId(),
				f.getOriginalName(),
				"/api/files/" + f.getId(),
				f.getFileType(),
				f.getFileSize()
			))
			.toList();
		return new BoardDetailResponse(
			board.getId(), board.getTitle(), board.getContent(), board.getWriter(),
			board.getVisitCount(), board.getLikeCount(), board.getCreatedAt(), board.getUpdatedAt(),
			increased, files
		);
	}
}
