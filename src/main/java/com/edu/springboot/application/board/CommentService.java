package com.edu.springboot.application.board;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.board.dto.CommentResponse;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.board.Comment;
import com.edu.springboot.domain.board.CommentRepository;
import com.edu.springboot.domain.board.QnaBoardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

	private final CommentRepository commentRepository;
	private final QnaBoardRepository qnaBoardRepository;

	@Transactional(readOnly = true)
	public List<CommentResponse> list(Long boardId) {
		return commentRepository.findByBoardId(boardId).stream().map(this::toResponse).toList();
	}

	public CommentResponse write(Long boardId, String writer, String content) {
		qnaBoardRepository.findById(boardId)
			.orElseThrow(() -> new BusinessException("게시글을 찾을 수 없습니다."));
		if (content == null || content.isBlank()) {
			throw new BusinessException("댓글 내용을 입력하세요.");
		}
		Comment comment = new Comment();
		comment.setBoardId(boardId);
		comment.setWriter(writer);
		comment.setContent(content);
		comment.setCreatedAt(LocalDateTime.now());
		return toResponse(commentRepository.save(comment));
	}

	public CommentResponse update(Long commentId, String loginId, String content) {
		Comment comment = find(commentId);
		assertOwner(comment, loginId);
		comment.setContent(content);
		comment.setUpdatedAt(LocalDateTime.now());
		commentRepository.update(comment);
		return toResponse(comment);
	}

	public void delete(Long commentId, String loginId) {
		Comment comment = find(commentId);
		assertOwner(comment, loginId);
		commentRepository.delete(commentId);
	}

	private Comment find(Long id) {
		return commentRepository.findById(id)
			.orElseThrow(() -> new BusinessException("댓글을 찾을 수 없습니다."));
	}

	private void assertOwner(Comment comment, String loginId) {
		if (!comment.getWriter().equals(loginId)) {
			throw new BusinessException("작성자만 수정/삭제할 수 있습니다.");
		}
	}

	private CommentResponse toResponse(Comment comment) {
		return new CommentResponse(
			comment.getId(), comment.getBoardId(), comment.getWriter(),
			comment.getContent(), comment.getCreatedAt(), comment.getUpdatedAt()
		);
	}
}
