package com.c2se.roomily.entity;

import com.c2se.roomily.enums.TagCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = "tag_name")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tag_id")
    private String id;
    @Column(name = "tag_name")
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private TagCategory category;
    private String displayName;
}
