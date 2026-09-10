package com.edu.springboot.application.board;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.board.dto.BoardDetailResponse;
import com.edu.springboot.application.board.dto.BoardSummaryResponse;
import com.edu.springboot.application.captcha.VerifyCaptchaService;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.application.common.PageResponse;
import com.edu.springboot.domain.board.FreeBoard;
import com.edu.springboot.domain.board.FreeBoardRepository;
import com.edu.springboot.domain.board.ViewCountPolicy;
import com.edu.springboot.domain.captcha.CaptchaAction;
import com.edu.springboot.domain.member.PasswordEncryptor;

/**
 * Hexaq
 * 계층: Application
 * 객체: FreeBoardService
 * 책임: 유스케이스. @Transactional. Domain 포트만 의존
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/features/09-archive-attachments.md](../../../../../../../../docs/features/09-archive-attachments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
@Service
@Transactional
public class FreeBoardService {

	private final FreeBoardRepository freeBoardRepository;
	private final PasswordEncryptor passwordEncryptor;
	private final ViewCountPolicy viewCountPolicy;
	private final VerifyCaptchaService verifyCaptchaService;

	public FreeBoardService(
		FreeBoardRepository freeBoardRepository,
		PasswordEncryptor passwordEncryptor,
		ViewCountPolicy viewCountPolicy,
		VerifyCaptchaService verifyCaptchaService
	) {
		this.freeBoardRepository = freeBoardRepository;
		this.passwordEncryptor = passwordEncryptor;
		this.viewCountPolicy = viewCountPolicy;
		this.verifyCaptchaService = verifyCaptchaService;
	}

	@Transactional(readOnly = true)
	public PageResponse<BoardSummaryResponse> list(String searchType, String keyword, int page, int size) {
		int offset = (page - 1) * size;
		var rows = freeBoardRepository.findPage(searchType, keyword, offset, size).stream()
			.map(this::toSummary)
			.toList();
		long total = freeBoardRepository.count(searchType, keyword);
		return PageResponse.of(rows, page, size, total);
	}

	public BoardDetailResponse read(Long id, boolean alreadyViewedToday) {
		FreeBoard board = find(id);
		boolean increased = viewCountPolicy.shouldIncrease(alreadyViewedToday);
		if (increased) {
			freeBoardRepository.increaseVisitCount(id);
			board.setVisitCount(board.getVisitCount() + 1);
		}
		return toDetail(board, increased);
	}

	public Long write(String title, String content, String writer, String password, String recaptchaToken,
		String clientIp, String requestHost) {
		verifyCaptchaService.require(recaptchaToken, CaptchaAction.BOARD_WRITE_FREE, clientIp, requestHost);
		requireText(title, "제목을 입력하세요.");
		requireText(content, "내용을 입력하세요.");
		requireText(writer, "작성자를 입력하세요.");
		requireText(password, "글 비밀번호를 입력하세요.");
		FreeBoard board = new FreeBoard();
		board.setTitle(title);
		board.setContent(content);
		board.setWriter(writer);
		board.setPassword(passwordEncryptor.encode(password));
		board.setCreatedAt(LocalDateTime.now());
		return freeBoardRepository.save(board).getId();
	}

	public void update(Long id, String title, String content, String password) {
		FreeBoard board = find(id);
		assertPassword(board, password);
		requireText(title, "제목을 입력하세요.");
		requireText(content, "내용을 입력하세요.");
		board.setTitle(title);
		board.setContent(content);
		board.setUpdatedAt(LocalDateTime.now());
		freeBoardRepository.update(board);
	}

	public void delete(Long id, String password) {
		FreeBoard board = find(id);
		assertPassword(board, password);
		freeBoardRepository.delete(id);
	}

	private FreeBoard find(Long id) {
		return freeBoardRepository.findById(id)
			.orElseThrow(() -> new BusinessException("게시글을 찾을 수 없습니다."));
	}

	private void assertPassword(FreeBoard board, String password) {
		if (!passwordEncryptor.matches(password, board.getPassword())) {
			throw new BusinessException("글 비밀번호가 일치하지 않습니다.");
		}
	}

	private void requireText(String value, String message) {
		if (value == null || value.isBlank()) {
			throw new BusinessException(message);
		}
	}

	private BoardSummaryResponse toSummary(FreeBoard board) {
		return new BoardSummaryResponse(
			board.getId(), board.getTitle(), board.getWriter(),
			board.getVisitCount(), board.getLikeCount(), board.getCreatedAt()
		);
	}

	private BoardDetailResponse toDetail(FreeBoard board, boolean increased) {
		return new BoardDetailResponse(
			board.getId(), board.getTitle(), board.getContent(), board.getWriter(),
			board.getVisitCount(), board.getLikeCount(), board.getCreatedAt(), board.getUpdatedAt(),
			increased, java.util.List.of()
		);
	}
}
