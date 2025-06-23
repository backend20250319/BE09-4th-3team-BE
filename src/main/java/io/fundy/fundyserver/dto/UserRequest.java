// UserRequest.java
package io.fundy.fundyserver.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * 회원가입 및 회원 정보 수정 요청용 DTO
 *  - userStatus, roleType 은 클라이언트 입력에서 제외
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(max = 50, message = "아이디는 최대 50자까지 입력 가능합니다.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 255, message = "비밀번호는 최소 8자 이상 입력해야 합니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(max = 50, message = "닉네임은 최대 50자까지 입력 가능합니다.")
    private String nickname;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식을 입력해 주세요.")
    @Size(max = 100, message = "이메일은 최대 100자까지 입력 가능합니다.")
    private String email;

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Size(max = 20, message = "전화번호는 최대 20자까지 입력 가능합니다.")
    private String phone;

    @Size(max = 200, message = "주소는 최대 200자까지 입력 가능합니다.")
    private String address;

    @Size(max = 30, message = "계좌번호는 최대 30자까지 입력 가능합니다.")
    private String accountNumber;
}