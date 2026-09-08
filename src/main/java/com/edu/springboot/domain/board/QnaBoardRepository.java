package com.edu.springboot.domain.board;

import java.util.List;
import java.util.Optional;

public interface QnaBoardRepository {

	QnaBoard save(QnaBoard board);

	Optional<QnaBoard> findById(Long id);

	void update(QnaBoard board);

	void delete(Long id);

	List<QnaBoard> findPage(String solution, String searchType, String keyword, int offset, int size);

	long count(String solution, String searchType, String keyword);

	void increaseVisitCount(Long id);

	void increaseLikeCount(Long id);
}
