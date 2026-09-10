package com.edu.springboot.domain.board;

/**
 * Hexaq
 * 계층: Domain
 * 객체: LikeRepository
 * 책임: 저장 포트. Application 은 이 인터페이스만 봄
 * 문서: [docs/features/07-boards.md](../../../../../../../../docs/features/07-boards.md) · [docs/features/08-views-likes-comments.md](../../../../../../../../docs/features/08-views-likes-comments.md) · [docs/technical/01-architecture.md](../../../../../../../../docs/technical/01-architecture.md)
 */
public interface LikeRepository {

	boolean exists(String boardType, Long boardId, String memberLoginId);

	void insert(String boardType, Long boardId, String memberLoginId);
}
