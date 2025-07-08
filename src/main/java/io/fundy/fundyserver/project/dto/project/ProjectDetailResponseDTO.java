package io.fundy.fundyserver.project.dto.project;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.project.entity.Reward;
import io.fundy.fundyserver.register.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class ProjectDetailResponseDTO {
    private Long projectNo;
    private String title;
    private String description;
    private String nickname;
    private Integer goalAmount;
    private Integer currentAmount;
    private String startLine;
    private String deadline;
    private String creatorName;
    private String creatorInfo;
    private String status;
    private String thumbnailUrl;
    private List<RewardDTO> rewards;

    public static ProjectDetailResponseDTO from(Project project) {
        return new ProjectDetailResponseDTO(
                project.getProjectNo(),
                project.getTitle(),
                project.getDescription(),
                project.getUser().getNickname(),
                project.getGoalAmount(),
                project.getCurrentAmount(),
                project.getStartLine().toString(),
                project.getDeadLine().toString(),
                project.getProductStatus().name(),
                project.getCreatorName(),
                project.getCreatorName(),
                project.getThumbnailUrl(),
                project.getRewards().stream()
                        .map(RewardDTO::from)
                        .collect(Collectors.toList())
        );
    }

    @Getter
    @AllArgsConstructor
    public static class RewardDTO {
        private Long id;
        private String title;
        private Integer amount;
        private String description;

        public static RewardDTO from(Reward reward) {
            return new RewardDTO(
                    reward.getRewardNo(),
                    reward.getTitle(),
                    reward.getAmount(),
                    reward.getDescription()
            );
        }
    }
}

