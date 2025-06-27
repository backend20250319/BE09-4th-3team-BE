package io.fundy.fundyserver.project.entity;

import jakarta.persistence.*;

@Entity
public class ProjectImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;
    private String imageUrl;
    private Boolean isThumbnail;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}

