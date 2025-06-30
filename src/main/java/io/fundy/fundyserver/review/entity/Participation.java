package io.fundy.fundyserver.review.entity;

import io.fundy.fundyserver.project.entity.Project;
import io.fundy.fundyserver.register.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "participations")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_no")
    private Long participationNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_no", nullable = false)
    private Project project;

}