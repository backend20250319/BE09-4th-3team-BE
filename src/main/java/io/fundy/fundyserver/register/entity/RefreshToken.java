package io.fundy.fundyserver.register.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class RefreshToken {
    @Id
    private String userId;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;
}