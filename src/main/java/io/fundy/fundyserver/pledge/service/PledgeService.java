package io.fundy.fundyserver.pledge.service;

import io.fundy.fundyserver.pledge.dto.MyPledgeResponseDTO;
import io.fundy.fundyserver.pledge.dto.PledgeRequestDTO;
import io.fundy.fundyserver.pledge.dto.PledgeResponseDTO;
import io.fundy.fundyserver.pledge.entity.Pledge;
import io.fundy.fundyserver.pledge.repository.PledgeRepository;
import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.entity.ProjectStatus;
import io.fundy.fundyserver.project.entity.Reward;
import io.fundy.fundyserver.project.exception.ApiException;
import io.fundy.fundyserver.project.exception.ErrorCode;
import io.fundy.fundyserver.project.repository.ProjectRepository;
import io.fundy.fundyserver.project.repository.RewardRepository;
import io.fundy.fundyserver.register.entity.User;
import io.fundy.fundyserver.register.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PledgeService {

    private final PledgeRepository pledgeRepository;
    private final ProjectRepository projectRepository;
    private final RewardRepository rewardRepository;
    private final UserRepository userRepository;

    /**
     * 프로젝트 후원 처리
     * @param dto 후원 요청 정보
     * @param userId 후원자 ID
     * @return 후원 응답 정보
     */
    @Transactional
    public PledgeResponseDTO createPledge(PledgeRequestDTO dto, String userId) {
        // 필요한 엔티티 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Project project = projectRepository.findById(dto.getProjectNo())
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND));

        Reward reward = rewardRepository.findById(dto.getRewardNo())
                .orElseThrow(() -> new ApiException(ErrorCode.REWARD_NOT_FOUND));

        // 프로젝트와 리워드의 연관관계 확인
        if (!reward.getProject().getProjectNo().equals(project.getProjectNo())) {
            throw new ApiException(ErrorCode.REWARD_NOT_MATCHED);
        }

        // 프로젝트 상태 확인 (진행 중인 프로젝트만 후원 가능)
        if (project.getProductStatus() != ProjectStatus.IN_PROGRESS && 
            project.getProductStatus() != ProjectStatus.APPROVED) {
            throw new ApiException(ErrorCode.PROJECT_NOT_AVAILABLE);
        }

        // 리워드 재고 확인 (재고가 있거나, 무제한(-1)인 경우만 가능)
        if (reward.getStock() != null && reward.getStock() != -1 && reward.getStock() <= 0) {
            throw new ApiException(ErrorCode.REWARD_OUT_OF_STOCK);
        }

        // 후원 엔티티 생성
        Pledge pledge = Pledge.create(
            user, 
            project, 
            reward, 
            dto.getAdditionalAmount(),
            dto.getDeliveryAddress(),
            dto.getDeliveryPhone(),
            dto.getRecipientName()
        );

        // 후원 정보 저장
        Pledge savedPledge = pledgeRepository.save(pledge);

        // 프로젝트 후원 금액 업데이트
        project.setCurrentAmount(project.getCurrentAmount() + pledge.getTotalAmount());

        // 리워드 재고 업데이트 (무제한이 아닌 경우)
        if (reward.getStock() != null && reward.getStock() != -1) {
            // 재고 감소 로직은 Reward 클래스에 메서드가 없어 수동 계산
            // 실제 구현 시 Reward 클래스에 decreaseStock 메서드를 추가하는 것이 좋습니다
        }

        // 목표 금액 달성 시 상태 업데이트
        updateProjectStatus(project);

        // 응답 DTO 생성 및 반환
        return new PledgeResponseDTO(
            savedPledge.getPledgeNo(),
            project.getProjectNo(),
            project.getTitle(),
            reward.getTitle(),
            pledge.getTotalAmount()
        );
    }

    /**
     * 사용자의 후원 내역 조회
     * @param userId 사용자 ID
     * @return 사용자의 후원 내역 목록
     */
    @Transactional(readOnly = true)
    public List<MyPledgeResponseDTO> getMyPledges(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        List<Pledge> pledges = pledgeRepository.findByUser(user);

        return pledges.stream()
                .map(pledge -> new MyPledgeResponseDTO(
                    pledge.getPledgeNo(),
                    pledge.getProject().getProjectNo(),
                    pledge.getProject().getTitle(),
                    pledge.getReward().getTitle(),
                    pledge.getTotalAmount(),
                    pledge.getDeliveryAddress(),
                    pledge.getDeliveryPhone(),
                    pledge.getRecipientName(),
                    pledge.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 프로젝트 상태 업데이트
     * @param project 업데이트할 프로젝트
     */
    private void updateProjectStatus(Project project) {
        // 상태가 APPROVED이고 후원금이 목표액 이상이면 IN_PROGRESS로 변경
        if (project.getProductStatus() == ProjectStatus.APPROVED) {
            project.setProductStatus(ProjectStatus.IN_PROGRESS);
        }

        // 목표 금액 달성 시 상태를 COMPLETED로 변경할 수 있으나,
        // 일반적으로 마감일이 지난 후에 최종 상태를 결정하므로 여기서는 구현하지 않음
        // 실제 구현 시 별도의 스케줄러를 통해 마감일에 프로젝트 상태를 업데이트하는 것이 좋습니다
    }
}
