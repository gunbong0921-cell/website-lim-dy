package com.edu.springboot.domain.board;

import java.util.List;
import java.util.Optional;

public interface ArchiveBoardRepository {

	ArchiveBoard save(ArchiveBoard board);

	Optional<ArchiveBoard> findById(Long id);

	void update(ArchiveBoard board);

	void delete(Long id);

	List<ArchiveBoard> findPage(String searchType, String keyword, int offset, int size);

	long count(String searchType, String keyword);

	void increaseVisitCount(Long id);

	void increaseLikeCount(Long id);
}
