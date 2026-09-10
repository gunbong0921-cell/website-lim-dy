package com.edu.springboot.application.board;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.edu.springboot.application.board.dto.LikeResult;
import com.edu.springboot.application.common.BusinessException;
import com.edu.springboot.domain.board.BoardLikeCounter;
import com.edu.springboot.domain.board.LikePolicy;
import com.edu.springboot.domain.board.LikeRepository;

@Service
@Transactional
public class LikeService {

	private final LikePolicy likePolicy;
	private final LikeRepository likeRepository;
	private final Map<String, BoardLikeCounter> counters;

	public LikeService(LikePolicy likePolicy, LikeRepository likeRepository, List<BoardLikeCounter> counters) {
		this.likePolicy = likePolicy;
		this.likeRepository = likeRepository;
		this.counters = counters.stream()
			.collect(Collectors.toUnmodifiableMap(c -> c.boardType().toUpperCase(), Function.identity()));
	}

	public LikeResult like(String boardType, Long boardId, String loginId, boolean alreadyLikedByCookie) {
		String type = boardType == null ? "" : boardType.toUpperCase();
		boolean guest = loginId == null || loginId.isBlank();
		if (guest && !likePolicy.allowsGuest(type)) {
			throw new BusinessException("로그인 후 좋아요를 누를 수 있습니다.");
		}
		if (guest) {
			if (alreadyLikedByCookie) {
				throw new BusinessException("이미 좋아요를 눌렀습니다.");
			}
			return new LikeResult(increase(type, boardId), true);
		}
		if (likeRepository.exists(type, boardId, loginId)) {
			throw new BusinessException("이미 좋아요를 눌렀습니다.");
		}
		likeRepository.insert(type, boardId, loginId);
		return new LikeResult(increase(type, boardId), false);
	}

	private int increase(String type, Long boardId) {
		BoardLikeCounter counter = counters.get(type);
		if (counter == null) {
			throw new BusinessException("지원하지 않는 게시판입니다.");
		}
		return counter.increaseAndCount(boardId)
			.orElseThrow(() -> new BusinessException("게시글을 찾을 수 없습니다."));
	}
}
