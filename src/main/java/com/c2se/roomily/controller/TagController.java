package com.c2se.roomily.controller;

import com.c2se.roomily.entity.Tag;
import com.c2se.roomily.service.TagService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tags")
public class TagController {
    TagService tagService;
    @GetMapping
    public ResponseEntity<List<Tag>> getAllTags() {
        return ResponseEntity.ok(tagService.getAllTags());
    }
    @GetMapping("/{tagId}")
    public ResponseEntity<Tag> getTagById(@PathVariable String tagId) {
        return ResponseEntity.ok(tagService.getTagById(tagId));
    }
    @PostMapping
    public ResponseEntity<Boolean> createTag(@RequestBody String tagName) {
        return ResponseEntity.ok(tagService.createTag(tagName));
    }
    @PatchMapping("/{tagId}")
    public ResponseEntity<Tag> updateTag(@PathVariable String tagId, @RequestBody String tagName) {
        return ResponseEntity.ok(tagService.updateTag(tagId, tagName));
    }
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Boolean> deleteTag(@PathVariable String tagId) {
        return ResponseEntity.ok(tagService.deleteTag(tagId));
    }
}
