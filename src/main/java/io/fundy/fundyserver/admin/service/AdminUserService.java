//package io.fundy.fundyserver.admin.service;
//
//import io.fundy.fundyserver.admin.dto.AdminUserResponseDto;
//import io.fundy.fundyserver.register.entity.User;
//import io.fundy.fundyserver.register.repository.UserRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class AdminUserService {
//
//    private final UserRepository userRepository;
//
//    public Page<AdminUserResponseDto> getUsers(int page, String nickname) {
//        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
//
//        Page<User> users;
//
//        if (nickname != null && !nickname.trim().isEmpty()) {
//            users = userRepository.findByNicknameContainingIgnoreCase(nickname, pageable);
//        } else {
//            users = userRepository.findAll(pageable);
//        }
//
//        return users.map(AdminUserResponseDto::fromEntity);
//    }
//}
