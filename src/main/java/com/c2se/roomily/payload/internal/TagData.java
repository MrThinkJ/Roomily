package com.c2se.roomily.payload.internal;

import com.c2se.roomily.enums.TagCategory;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TagData {
    private String name;
    private TagCategory category;
    private String displayName;
}
