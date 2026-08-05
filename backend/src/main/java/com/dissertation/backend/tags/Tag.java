package com.dissertation.backend.tags;

import jakarta.persistence.*;

@Entity
@Table(
        name = "tag",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tag_name",
                columnNames = {"name"}
        )
)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;

    protected Tag() {}

    public Tag(String name) {
        this.name = name;
    }
    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
}
