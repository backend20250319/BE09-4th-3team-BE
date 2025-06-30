package io.fundy.fundyserver.project.service;

import io.fundy.fundyserver.project.dto.project.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    /***
     * 프로젝트 등록
     * @param dto
     * @param userId
     * @return
     */
    @Transactional
    public ProjectResponseDTO createService(ProjectRequestDTO dto, String userId) {

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        Category category = categoryRepository.findByCategoryNo(dto.getCategoryNo())
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

    /***
     * 프로젝트 목록 조회
     * @param page
     * @param size
     * @return
     */
    public ProjectListPageResponseDTO getProjects(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Project> projectPage = projectRepository.findAll(pageable);

        System.out.println("총 프로젝트 수: " + projectPage.getTotalElements());
        System.out.println("조회된 프로젝트: " + projectPage.getContent().size());
        projectPage.getContent().forEach(p -> System.out.println(p.getTitle()));

        List<ProjectListResponseDTO> dtoList = projectPage.stream()
                .map(p -> new ProjectListResponseDTO(
                        p.getProjectNo(),
                        p.getTitle(),
                        p.getThumbnailUrl(),
                        p.getGoalAmount(),
                        p.getCurrentAmount(),
                        p.getStartLine().toString(),
                        p.getDeadLine().toString(),
                        p.getCategory().getName(),
                        calculatePercent(p)
                )).toList();

        ProjectListPageResponseDTO.PaginationDTO pagination = new ProjectListPageResponseDTO.PaginationDTO(
                projectPage.getNumber(),
                projectPage.getSize(),
                projectPage.getTotalPages(),
                projectPage.getTotalElements()
        );

        return new ProjectListPageResponseDTO(dtoList, pagination);
    }

    private int calculatePercent(Project project) {
        if (project.getGoalAmount() == 0) return 0;
        return (int) ((project.getCurrentAmount() / (double) project.getGoalAmount()) * 100);
    }

    /***
     * 프로젝트 상세 조회
     * @param projectNo
     * @return
     */
    @Transactional(readOnly = true)
    public ProjectDetailResponseDTO getProjectById(Long projectNo) {
        Project project = projectRepository.findById(projectNo)
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND));

        return ProjectDetailResponseDTO.from(project); // 정적 팩토리 메서드 또는 생성자 방식
    }
}

