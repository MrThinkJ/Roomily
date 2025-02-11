package com.c2se.roomily.service.impl;

import com.c2se.roomily.entity.Tag;
import com.c2se.roomily.exception.ResourceNotFoundException;
import com.c2se.roomily.repository.TagRepository;
import com.c2se.roomily.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TagServiceImpl implements TagService {
    TagRepository tagRepository;
    @Override
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    public Tag getTagById(String tagId) {
        return tagRepository.findById(tagId).orElseThrow(
                () -> new ResourceNotFoundException("Tag", "id", tagId)
        );
    }

    @Override
    public Boolean createTag(String tagName) {
        Tag tag = Tag.builder().name(tagName).build();
        tagRepository.save(tag);
        return true;
    }

    @Override
    public Tag updateTag(String tagId, String tagName) {
        Tag tag = tagRepository.findById(tagId).orElseThrow(
                () -> new ResourceNotFoundException("Tag", "id", tagId)
        );
        tag.setName(tagName);
        tagRepository.save(tag);
        return tag;
    }

    @Override
    public Boolean deleteTag(String tagId) {
        Tag tag = tagRepository.findById(tagId).orElseThrow(
                () -> new ResourceNotFoundException("Tag", "id", tagId)
        );
        tagRepository.deleteTagForRoom(tagId);
        tagRepository.delete(tag);
        return true;
    }
}
