package io.fundy.fundyserver.project.service;

import io.fundy.fundyserver.project.dto.CategoryDTO;
import io.fundy.fundyserver.project.entity.Category;
import io.fundy.fundyserver.project.exception.ApiException;
import io.fundy.fundyserver.project.exception.ErrorCode;
import io.fundy.fundyserver.project.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new ApiException(ErrorCode.DUPLICATE_CATEGORY);
        }

        Category category = Category.builder()
                .name(dto.getName())
                .build();

        Category saved = categoryRepository.save(category);
        return new CategoryDTO(saved.getCategoryNo(), saved.getName());
    }
}
