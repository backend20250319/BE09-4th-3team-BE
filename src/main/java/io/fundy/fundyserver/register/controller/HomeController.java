package io.fundy.fundyserver.register.controller;

import io.fundy.fundyserver.register.dto.oauth.SessionUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final HttpSession httpSession;

    // 1. 루트는 로그인 페이지로
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    // 2. 로그인 페이지
    @GetMapping("/login")
    public String login(Model model) {
        if (httpSession.getAttribute("user") != null) {
            return "redirect:/home2";
        }
        model.addAttribute("message", "OAuth2 로그인 페이지입니다.");
        return "login";  // src/main/resources/templates/login.html
    }

    // 3. 일반 홈 (예: 회원가입 직후)
    @GetMapping("/home")
    public String home(Model model) {
        return renderHome(model, false);
    }

    // 4. 소셜 로그인 성공 후 페이지
    @GetMapping("/home2")
    public String home2(Model model) {
        return renderHome(model, true);
    }

    // 5. 로그아웃 처리 (세션 무효화)
    @GetMapping("/logout")
    public String logout() {
        httpSession.invalidate();
        return "redirect:/login";
    }

    // home/home2 공통 렌더링
    // @param model 뷰 모델
    // @param socialSuccess true면 home2, false면 home 로직
    private String renderHome(Model model, boolean socialSuccess) {
        SessionUser user = (SessionUser) httpSession.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        if (socialSuccess) {
            model.addAttribute("message",
                    "[소셜 로그인 성공] " + user.getRegistrationId() + " 계정으로 로그인하셨습니다.");
            return "success";
        } else {
            model.addAttribute("message",
                    user.getRegistrationId() + " 로그인에 성공했습니다!");
            return "home";
        }
    }
}
