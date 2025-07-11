package io.fundy.fundyserver.pledge.repository;

import io.fundy.fundyserver.pledge.entity.PledgeReward;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface PledgeRewardRepository extends JpaRepository<PledgeReward, Long> {

    @EntityGraph(attributePaths = {
            "pledge.user",
            "pledge.project",
            "reward"
    })
    @Query("SELECT pr FROM PledgeReward pr")
    Page<PledgeReward> findAllWithAssociations(Pageable pageable);

    @Query("SELECT COUNT(DISTINCT pr.pledge.pledgeNo) FROM PledgeReward pr")
    Long countTotalPledges();

    @Query("SELECT COALESCE(SUM(pr.rewardAmount * pr.quantity), 0) FROM PledgeReward pr")
    Long sumTotalPledgedAmount();

    @Query("SELECT COUNT(DISTINCT pr.pledge.pledgeNo) FROM PledgeReward pr WHERE pr.pledge.createdAt >= :startOfDay")
    Long countTodayPledges(LocalDateTime startOfDay);

    @Query("SELECT COUNT(DISTINCT pr.pledge.user.userId) FROM PledgeReward pr")
    Long countDistinctBackers();
}
