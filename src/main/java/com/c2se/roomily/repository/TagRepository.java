package com.c2se.roomily.repository;

import com.c2se.roomily.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, String> {
    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = "DELETE FROM room_tags WHERE tag_id = :tagId", nativeQuery = true)
    void deleteTagForRoom(@Param("tagId") String tagId);

    Tag findByName(String name);

    List<Tag> findByIdIn(List<String> tagIds);
}
