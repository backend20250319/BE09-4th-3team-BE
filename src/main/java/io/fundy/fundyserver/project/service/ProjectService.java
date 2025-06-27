package io.fundy.fundyserver.project.service;

import io.fundy.fundyserver.project.dto.project.ProjectRequestDTO;
import io.fundy.fundyserver.project.dto.project.ProjectResponseDTO;
import io.fundy.fundyserver.project.dto.reward.RewardRequestDTO;
import io.fundy.fundyserver.project.entity.Category;
import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.entity.Reward;
import io.fundy.fundyserver.project.exception.ApiException;
import io.fundy.fundyserver.project.exception.ErrorCode;
import io.fundy.fundyserver.project.repository.CategoryRepository;
import io.fundy.fundyserver.project.repository.ProjectRepository;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProjectResponseDTO createService(ProjectRequestDTO dto, String userId) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND));

        Project project = Project.create(user, category, dto);

        if (dto.getRewards() != null) {
            for (RewardRequestDTO rewardDto : dto.getRewards()) {
                Reward reward = Reward.of(rewardDto, project);
                project.addReward(reward); // 연관관계 메서드 사용
            }
        }

        Project saved = projectRepository.save(project);
        return new ProjectResponseDTO(saved.getProjectNo(), saved.getProductStatus().name());
    }
}
