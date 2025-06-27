package io.fundy.fundyserver.project.repository;

import io.fundy.fundyserver.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Project save(Project project);

    // Admin project 추가 부분
    @Query("SELECT p FROM Project p WHERE p.category.category_no = :categoryNo")
    Page<Project> findByCategory_CategoryNo(Long categoryNo, Pageable pageable);
}
