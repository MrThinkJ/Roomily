package com.c2se.roomily.service;

import com.c2se.roomily.entity.Tag;

import java.util.List;

public interface TagService {
    List<Tag> getAllTags();
    Tag getTagById(String tagId);
    Boolean createTag(String tagName);
    Tag updateTag(String tagId, String tagName);
    Boolean deleteTag(String tagId);
}
