//package io.fundy.fundyserver.project.controller.admin;
//
//
//import io.fundy.fundyserver.notification.dto.admin.AdminUserRequestDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
////@RestController
////@RequestMapping("/admin/users")
////@RequiredArgsConstructor
////public class AdminUserController {
////
////    private final UserService userService;
////
////    @PostMapping("/status")
////    public ResponseEntity<Void> updateUserStatus(@RequestBody AdminUserRequestDto dto) {
////        // 요청이 "BAN"이면 내부적으로 "BANNED"로 매핑
////        String status = "BAN".equalsIgnoreCase(dto.getUserStatus())
////                ? "BANNED"
////                : dto.getUserStatus().toUpperCase();
////
////        userService.updateUserStatus(dto.getUserId(), status);
////        return ResponseEntity.ok().build();
////    }
////}
//
