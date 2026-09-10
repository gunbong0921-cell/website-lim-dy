package com.edu.springboot.application.board;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.board.dto.BoardDetailResponse;
import com.edu.springboot.application.board.dto.BoardSummaryResponse;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.common.PageResponse;
import com.edu.springboot.domain.board.CommentRepository;
import com.edu.springboot.domain.board.QnaBoard;
import com.edu.springboot.domain.board.QnaBoardRepository;
import com.edu.springboot.domain.board.QnaSolution;
import com.edu.springboot.domain.board.ViewCountPolicy;

import lombok.RequiredArgsConstructor;

/**
 * Hexaq
 * 계층: Application
 * 객체: QnaBoardService
 * 책임: 유스케이스. @Transactional. Domain 포트만 의존
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/features/09-archive-attachments.md](../../../../../../../../docs/features/09-archive-attachments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class QnaBoardService {

	private final QnaBoardRepository qnaBoardRepository;
	private final CommentRepository commentRepository;
	private final ViewCountPolicy viewCountPolicy;

	@Transactional(readOnly = true)
	public PageResponse<BoardSummaryResponse> list(String solution, String searchType, String keyword, int page, int size) {
		String area = requireSolution(solution);
		int offset = (page - 1) * size;
		var rows = qnaBoardRepository.findPage(area, searchType, keyword, offset, size).stream()
			.map(this::toSummary)
			.toList();
		return PageResponse.of(rows, page, size, qnaBoardRepository.count(area, searchType, keyword));
	}

	public BoardDetailResponse read(Long id, String solution, boolean alreadyViewedToday) {
		QnaBoard board = find(id);
		if (solution != null && !solution.isBlank() && !requireSolution(solution).equals(board.getSolution())) {
			throw new BusinessException("게시글을 찾을 수 없습니다.");
		}
		boolean increased = viewCountPolicy.shouldIncrease(alreadyViewedToday);
		if (increased) {
			qnaBoardRepository.increaseVisitCount(id);
			board.setVisitCount(board.getVisitCount() + 1);
		}
		return toDetail(board, increased);
	}

	public Long write(String title, String content, String writer, String solution) {
		String area = requireSolution(solution);
		require(title, content, writer);
		QnaBoard board = new QnaBoard();
		board.setTitle(title);
		board.setContent(content);
		board.setWriter(writer);
		board.setSolution(area);
		board.setCreatedAt(LocalDateTime.now());
		return qnaBoardRepository.save(board).getId();
	}

	public void update(Long id, String title, String content, String loginId) {
		QnaBoard board = find(id);
		assertOwner(board, loginId);
		require(title, content, loginId);
		board.setTitle(title);
		board.setContent(content);
		board.setUpdatedAt(LocalDateTime.now());
		qnaBoardRepository.update(board);
	}

	public void delete(Long id, String loginId) {
		QnaBoard board = find(id);
		assertOwner(board, loginId);
		commentRepository.deleteByBoardId(id);
		qnaBoardRepository.delete(id);
	}

	private QnaBoard find(Long id) {
		return qnaBoardRepository.findById(id)
			.orElseThrow(() -> new BusinessException("게시글을 찾을 수 없습니다."));
	}

	private void assertOwner(QnaBoard board, String loginId) {
		if (!board.getWriter().equals(loginId)) {
			throw new BusinessException("작성자만 수정/삭제할 수 있습니다.");
		}
	}

	private void require(String title, String content, String writer) {
		if (title == null || title.isBlank() || content == null || content.isBlank()
			|| writer == null || writer.isBlank()) {
			throw new BusinessException("제목과 내용을 입력하세요.");
		}
	}

	private String requireSolution(String solution) {
		String normalized = QnaSolution.normalize(solution);
		if (!QnaSolution.isAllowed(normalized)) {
			throw new BusinessException("지원하지 않는 Q&A 영역입니다.");
		}
		return normalized;
	}

	private BoardSummaryResponse toSummary(QnaBoard board) {
		return new BoardSummaryResponse(
			board.getId(), board.getTitle(), board.getWriter(),
			board.getVisitCount(), board.getLikeCount(), board.getCreatedAt()
		);
	}

	private BoardDetailResponse toDetail(QnaBoard board, boolean increased) {
		return new BoardDetailResponse(
			board.getId(), board.getTitle(), board.getContent(), board.getWriter(),
			board.getVisitCount(), board.getLikeCount(), board.getCreatedAt(), board.getUpdatedAt(),
			increased, java.util.List.of()
		);
	}
}
