package io.fundy.fundyserver.register.dto;

import jakarta.validation.constraints.*;
import lombok.*;


// 회원가입 및 회원 정보 수정 요청용 DTO
// userStatus, roleType 은 클라이언트 입력에서 제외

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString

public class UserRequestDTO {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 5, max = 10, message = "※ 아이디는 영문 5-10자까지 입력 가능합니다.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, max = 30, message = "※ 비밀번호는 영문과 특수문자를 혼용하여 8-30자를 입력해 주세요.")
    private String password;

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 20, message = "※ 닉네임은 한글과 영문 2-20자만 가능합니다.")
    private String nickname;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식을 입력해 주세요.")
    @Size(max = 20, message = "※ 이메일은 최대 20자까지 입력 가능합니다.")
    private String email;

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Size(max = 15, message = "※ 전화번호는 숫자만 입력해주세요.")
    private String phone;

    @Size(max = 200, message = "※ 주소는 최대 200자까지 입력 가능합니다.")
    private String address;

    @Size(max = 15, message = "※ 계좌번호는 최대 15자까지 입력 가능합니다.")
    private String accountNumber;
}
