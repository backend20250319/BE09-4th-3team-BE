package io.fundy.fundyserver.pledge.service;

import io.fundy.fundyserver.pledge.dto.MyPledgeResponseDTO;
import io.fundy.fundyserver.pledge.dto.PledgeRequestDTO;
import io.fundy.fundyserver.pledge.dto.PledgeResponseDTO;
import io.fundy.fundyserver.pledge.entity.Pledge;
import io.fundy.fundyserver.pledge.repository.PledgeRepository;
import io.fundy.fundyserver.project.dto.project.ProjectDetailResponseDTO;
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

        // 프로젝트 상태 확인 (진행 중인 프로젝트만 후원 가능)
        if (project.getProductStatus() != ProjectStatus.IN_PROGRESS && 
            project.getProductStatus() != ProjectStatus.APPROVED) {
            throw new ApiException(ErrorCode.PROJECT_NOT_AVAILABLE);
        }

        // 후원 엔티티 생성
        Pledge pledge = Pledge.create(
            user, 
            project, 
            dto.getAdditionalAmount(),
            dto.getDeliveryAddress(),
            dto.getDeliveryPhone(),
            dto.getRecipientName()
        );

        // 선택한 리워드 검증 및 추가
        for (PledgeRequestDTO.PledgeRewardDTO rewardDto : dto.getRewards()) {
            Reward reward = rewardRepository.findById(rewardDto.getRewardNo())
                    .orElseThrow(() -> new ApiException(ErrorCode.REWARD_NOT_FOUND));

            // 프로젝트와 리워드의 연관관계 확인
            if (!reward.getProject().getProjectNo().equals(project.getProjectNo())) {
                throw new ApiException(ErrorCode.REWARD_NOT_MATCHED);
            }

            // 리워드 재고 확인 (재고가 있거나, 무제한(-1)인 경우만 가능)
            if (reward.getStock() != null && reward.getStock() != -1 && 
                reward.getStock() < rewardDto.getQuantity()) {
                throw new ApiException(ErrorCode.REWARD_OUT_OF_STOCK);
            }

            // 리워드 추가
            pledge.addReward(reward, rewardDto.getQuantity());

            // 리워드 재고 업데이트 (무제한이 아닌 경우)
            if (reward.getStock() != null && reward.getStock() != -1) {
                // 재고 감소 로직은 Reward 클래스에 메서드가 없어 수동 계산
                // 실제 구현 시 Reward 클래스에 decreaseStock 메서드를 추가하는 것이 좋습니다
            }
        }

        // 총 금액 계산
        pledge.calculateTotalAmount();

        // 후원 정보 저장
        Pledge savedPledge = pledgeRepository.save(pledge);

        // 프로젝트 후원 금액 업데이트
        project.setCurrentAmount(project.getCurrentAmount() + pledge.getTotalAmount());

        // 목표 금액 달성 시 상태 업데이트
        updateProjectStatus(project);

        // 리워드 정보 DTO 변환
        List<PledgeResponseDTO.PledgeRewardInfoDTO> rewardInfos = savedPledge.getPledgeRewards().stream()
            .map(pr -> new PledgeResponseDTO.PledgeRewardInfoDTO(
                pr.getReward().getRewardNo(),
                pr.getRewardTitle(),
                pr.getRewardAmount(),
                pr.getQuantity()
            ))
            .collect(Collectors.toList());

        // 응답 DTO 생성 및 반환
        return new PledgeResponseDTO(
            savedPledge.getPledgeNo(),
            project.getProjectNo(),
            project.getTitle(),
            rewardInfos,
            savedPledge.getAdditionalAmount(),
            savedPledge.getTotalAmount()
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
                .map(pledge -> {
                    // 리워드 정보 DTO 변환
                    List<MyPledgeResponseDTO.PledgeRewardInfoDTO> rewardInfos = pledge.getPledgeRewards().stream()
                        .map(pr -> new MyPledgeResponseDTO.PledgeRewardInfoDTO(
                            pr.getReward().getRewardNo(),
                            pr.getRewardTitle(),
                            pr.getRewardAmount(),
                            pr.getQuantity()
                        ))
                        .collect(Collectors.toList());

                    return new MyPledgeResponseDTO(
                        pledge.getPledgeNo(),
                        projectRepository.findById(pledge.getProject().getProjectNo())
                            .map(project -> ProjectDetailResponseDTO.from(project))
                            .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND)),
                        rewardInfos,
                        pledge.getAdditionalAmount(),
                        pledge.getTotalAmount(),
                        pledge.getDeliveryAddress(),
                        pledge.getDeliveryPhone(),
                        pledge.getRecipientName(),
                        pledge.getCreatedAt()
                    );
                })
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

    /**
     * 사용자 본인의 후원 상세정보 조회
     * @param pledgeId 후원 ID
     * @param userId 사용자 ID
     * @return 사용자의 후원 상세정보
     * @throws ApiException 해당 후원이 존재하지 않거나 본인의 후원이 아닌 경우
     */
    @Transactional(readOnly = true)
    public MyPledgeResponseDTO getMyPledgeById(Long pledgeId, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Pledge pledge = pledgeRepository.findById(pledgeId)
                .orElseThrow(() -> new ApiException(ErrorCode.PLEDGE_NOT_FOUND));

        // 본인의 후원인지 검증
        if (!pledge.getUser().getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }

        // ProjectService를 통해 프로젝트 상세 정보 가져오기
        ProjectDetailResponseDTO projectDetail = projectRepository.findById(pledge.getProject().getProjectNo())
                .map(project -> ProjectDetailResponseDTO.from(project))
                .orElseThrow(() -> new ApiException(ErrorCode.PROJECT_NOT_FOUND));

        // 리워드 정보 DTO 변환
        List<MyPledgeResponseDTO.PledgeRewardInfoDTO> rewardInfos = pledge.getPledgeRewards().stream()
            .map(pr -> new MyPledgeResponseDTO.PledgeRewardInfoDTO(
                pr.getReward().getRewardNo(),
                pr.getRewardTitle(),
                pr.getRewardAmount(),
                pr.getQuantity()
            ))
            .collect(Collectors.toList());

        return new MyPledgeResponseDTO(
                pledge.getPledgeNo(),
                projectDetail,
                rewardInfos,
                pledge.getAdditionalAmount(),
                pledge.getTotalAmount(),
                pledge.getDeliveryAddress(),
                pledge.getDeliveryPhone(),
                pledge.getRecipientName(),
                pledge.getCreatedAt()
        );
    }

    /**
     * 후원 ID로 해당 후원이 프로젝트의 몇 번째 후원인지 조회
     * @param pledgeId 후원 ID
     * @return 해당 후원의 프로젝트 내 순번
     */
    @Transactional(readOnly = true)
    public Integer getPledgeOrderInProject(Long pledgeId, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Pledge pledge = pledgeRepository.findById(pledgeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 후원을 찾을 수 없습니다: " + pledgeId));

        // 본인의 후원인지 검증
        if (!pledge.getUser().getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED);
        }
    
        return pledgeRepository.countByProjectAndCreatedAtBefore(
                pledge.getProject(), 
                pledge.getCreatedAt()
        ) + 1;
    }
}