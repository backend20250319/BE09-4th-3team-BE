package io.fundy.fundyserver.register.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UserUpdateRequestDTO {

    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Size(min = 2, max = 20, message = "※ 닉네임은 한글과 영문 2-20자만 가능합니다.")
    private String nickname;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식을 입력해 주세요.")
    @Size(max = 20, message = "※ 이메일은 최대 20자까지 입력 가능합니다.")
    private String email;

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "유효한 전화번호를 입력해주세요.(예:010-1234-1234)")
    @Size(max = 15, message = "※ 유효한 전화번호를 입력해주세요.(예:010-1234-1234)")
    private String phone;

    @Size(max = 200, message = "※ 주소는 최대 200자까지 입력 가능합니다.")
    private String address;

    @Size(max = 200, message = "※ 상세 주소는 최대 200자까지 입력 가능합니다.")
    private String addressDetail;

    private String profileImg;

    // getter, setter 추가
    public String getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }
}
