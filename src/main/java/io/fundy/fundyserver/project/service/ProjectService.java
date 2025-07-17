package io.fundy.fundyserver.project.service;

import io.fundy.fundyserver.notification.service.NotificationService;
import io.fundy.fundyserver.pledge.service.PledgeService;
import io.fundy.fundyserver.project.dto.project.*;
import io.fundy.fundyserver.project.dto.reward.RewardRequestDTO;
import io.fundy.fundyserver.project.entity.Category;
import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.entity.ProjectStatus;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PledgeService pledgeService;
    private final NotificationService notificationService;

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
     */
    public ProjectListPageResponseDTO getProjects(Pageable pageable) {
        LocalDate today = LocalDate.now();

        // ✅ APPROVED + IN_PROGRESS 상태 프로젝트 조회
        List<ProjectStatus> statusList = List.of(
                ProjectStatus.APPROVED,
                ProjectStatus.IN_PROGRESS,
                ProjectStatus.COMPLETED,
                ProjectStatus.FAILED);

        Page<Project> projectPage = projectRepository.findByProductStatusIn(
                statusList, pageable
        );

        long approvedCount = projectRepository.countByProductStatusIn(
                statusList
        );

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
                        p.getProductStatus().name(),
                        p.getCreatorName(),
                        calculatePercent(p),
                        p.getCreatedAt()
                )).toList();

        ProjectListPageResponseDTO.PaginationDTO pagination = new ProjectListPageResponseDTO.PaginationDTO(
                projectPage.getNumber(),
                projectPage.getSize(),
                projectPage.getTotalPages(),
                projectPage.getTotalElements()
        );

        return new ProjectListPageResponseDTO(dtoList, pagination, approvedCount);
    }

    @Transactional
    public void updateProjectStatusesByStartLine() {
        LocalDate today = LocalDate.now();
        List<Project> approvedProjects = projectRepository
                .findByProductStatusAndStartLineLessThanEqual(ProjectStatus.APPROVED, today);

        for (Project p : approvedProjects) {
            p.setProductStatus(ProjectStatus.IN_PROGRESS);
        }
        projectRepository.saveAll(approvedProjects);
    }

    @Transactional
    public void updateProjectStatusesAfterDeadline() {
        LocalDate today = LocalDate.now();
        List<Project> expiredProjects = projectRepository
                .findByProductStatusInAndDeadLineBefore(
                        List.of(ProjectStatus.APPROVED, ProjectStatus.IN_PROGRESS), today
                );

        for (Project p : expiredProjects) {

            List<String> supporterIds = pledgeService.getSupporterUserIdsByProjectNo(p.getProjectNo());

            if (p.getCurrentAmount() >= p.getGoalAmount()) {
                p.setProductStatus(ProjectStatus.COMPLETED);
                notificationService.sendProjectSuccess(
                        p.getTitle(),
                        p.getProjectNo(),
                        p.getUser().getUserId(),
                        supporterIds
                );
            } else {
                p.setProductStatus(ProjectStatus.FAILED);
                notificationService.sendProjectFail(
                        p.getTitle(),
                        p.getProjectNo(),
                        p.getUser().getUserId(),
                        supporterIds
                );
            }
        }
        projectRepository.saveAll(expiredProjects);
    }

    /***
     * 프로젝트 (목표금액 / 모인금액) 퍼센티지 계산
     * @param project
     * @return
     */
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

    /**
     * 후원 완료 알림 요청 메서드
     * 사용자가 프로젝트를 후원했을 때, 창작자에게 후원 완료 알림을 전송합니다.
     *
     * @param supporterId 후원자 ID
     * @param projectNo 프로젝트 번호
     * @param projectTitle 프로젝트 제목
     * @param supporterName 후원자 닉네임
     */
    @Transactional
    public void sendSupportCompleteNotification(String supporterId, Long projectNo, String projectTitle, String supporterName) {
        notificationService.sendSupportComplete(supporterId, projectNo, projectTitle, supporterName);
    }

    /**
     * 프로젝트 성공 알림 요청 메서드
     * 프로젝트가 성공한 경우, 창작자와 해당 프로젝트의 후원자들에게 성공 알림을 전송합니다.
     *
     * @param creatorId 창작자 ID
     * @param projectNo 프로젝트 번호
     * @param projectTitle 프로젝트 제목
     */
    @Transactional
    public void sendProjectSuccessNotification(String creatorId, Long projectNo, String projectTitle) {
        List<String> supporterIds = pledgeService.getSupporterUserIdsByProjectNo(projectNo);
        notificationService.sendProjectSuccess(projectTitle, projectNo, creatorId, supporterIds);
    }

    /**
     * 프로젝트 실패 알림 요청 메서드
     * 프로젝트가 마감됐지만 목표 금액을 달성하지 못한 경우,
     * 창작자와 후원자들에게 실패 알림을 전송합니다.
     *
     * @param creatorId 창작자 ID
     * @param projectNo 프로젝트 번호
     * @param projectTitle 프로젝트 제목
     */
    @Transactional
    public void sendProjectFailNotification(String creatorId, Long projectNo, String projectTitle) {


        List<String> supporterIds = pledgeService.getSupporterUserIdsByProjectNo(projectNo);
        notificationService.sendProjectFail(projectTitle, projectNo, creatorId, supporterIds);
    }
}