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

    /**
     * 후원 ID로 사용자 본인의 후원 상세정보 조회
     * @param pledgeId 후원 ID
     * @param userId 인증된 사용자 ID
     * @return 후원 상세정보
     */
    @GetMapping("/{pledgeId}")
    public ResponseEntity<MyPledgeResponseDTO> getMyPledgeById(
            @PathVariable Long pledgeId,
            @AuthenticationPrincipal String userId) {
        MyPledgeResponseDTO response = pledgeService.getMyPledgeById(pledgeId, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 후원 ID로 해당 후원이 프로젝트의 몇 번째 후원인지 조회
     * @param pledgeId 후원 ID
     * @return 해당 후원의 프로젝트 내 순번
     */
    @GetMapping("/{pledgeId}/order")
    public ResponseEntity<Integer> getPledgeOrderInProject(
            @PathVariable Long pledgeId,
            @AuthenticationPrincipal String userId) {
        Integer order = pledgeService.getPledgeOrderInProject(pledgeId, userId);
        return ResponseEntity.ok(order);
    }
}
