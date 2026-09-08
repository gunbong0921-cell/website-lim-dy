package com.edu.springboot.domain.board;

import java.util.List;
import java.util.Optional;

public interface FreeBoardRepository {

	FreeBoard save(FreeBoard board);

	Optional<FreeBoard> findById(Long id);

	void update(FreeBoard board);

	void delete(Long id);

	List<FreeBoard> findPage(String searchType, String keyword, int offset, int size);

	long count(String searchType, String keyword);

	void increaseVisitCount(Long id);

	void increaseLikeCount(Long id);
}
