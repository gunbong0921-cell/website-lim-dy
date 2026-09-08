package com.edu.springboot.application.board;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.board.ArchiveBoardRepository;
import com.edu.springboot.domain.board.FreeBoardRepository;
import com.edu.springboot.domain.board.LikeRepository;
import com.edu.springboot.domain.board.QnaBoardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

	private final LikeRepository likeRepository;
	private final FreeBoardRepository freeBoardRepository;
	private final QnaBoardRepository qnaBoardRepository;
	private final ArchiveBoardRepository archiveBoardRepository;

	public int like(String boardType, Long boardId, String loginId) {
		if (loginId == null || loginId.isBlank()) {
			throw new BusinessException("로그인 후 좋아요를 누를 수 있습니다.");
		}
		String type = boardType.toUpperCase();
		if (likeRepository.exists(type, boardId, loginId)) {
			throw new BusinessException("이미 좋아요를 눌렀습니다.");
		}
		likeRepository.insert(type, boardId, loginId);
		return switch (type) {
			case "FREE" -> {
				freeBoardRepository.increaseLikeCount(boardId);
				yield freeBoardRepository.findById(boardId)
					.orElseThrow(() -> new BusinessException("게시글을 찾을 수 없습니다."))
					.getLikeCount();
			}
			case "QNA" -> {
				qnaBoardRepository.increaseLikeCount(boardId);
				yield qnaBoardRepository.findById(boardId)
					.orElseThrow(() -> new BusinessException("게시글을 찾을 수 없습니다."))
					.getLikeCount();
			}
			case "ARCHIVE" -> {
				archiveBoardRepository.increaseLikeCount(boardId);
				yield archiveBoardRepository.findById(boardId)
					.orElseThrow(() -> new BusinessException("게시글을 찾을 수 없습니다."))
					.getLikeCount();
			}
			default -> throw new BusinessException("지원하지 않는 게시판입니다.");
		};
	}
}
