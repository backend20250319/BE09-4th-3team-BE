package io.fundy.fundyserver.project.entity;

import jakarta.annotation.Resource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long category_no; // 카테고리 ID

    @Column(nullable = false, length = 50)
    private String name; // 카테고리 이름

    @OneToMany(mappedBy = "category")
    private List<Project> projects = new ArrayList<>();
}

