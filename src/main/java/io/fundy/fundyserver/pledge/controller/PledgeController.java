package io.fundy.fundyserver.pledge.controller;

import io.fundy.fundyserver.pledge.dto.MyPledgeResponseDTO;
import io.fundy.fundyserver.pledge.dto.PledgeRequestDTO;
import io.fundy.fundyserver.pledge.dto.PledgeResponseDTO;
import io.fundy.fundyserver.pledge.service.PledgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pledge")
@RequiredArgsConstructor
public class PledgeController {

    private final PledgeService pledgeService;

    /**
     * 프로젝트 후원 요청 처리
     * @param dto 후원 요청 정보
     * @param userId 인증된 사용자 ID
     * @return 후원 처리 결과
     */
    @PostMapping
    public ResponseEntity<PledgeResponseDTO> createPledge(
            @RequestBody @Valid PledgeRequestDTO dto,
            @AuthenticationPrincipal String userId) {

        PledgeResponseDTO response = pledgeService.createPledge(dto, userId);

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자의 후원 내역 조회
     * @param userId 인증된 사용자 ID
     * @return 사용자의 후원 내역 목록
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyPledges(@AuthenticationPrincipal String userId) {
        List<MyPledgeResponseDTO> myPledges = pledgeService.getMyPledges(userId);
        return ResponseEntity.ok(myPledges);
    }
}
