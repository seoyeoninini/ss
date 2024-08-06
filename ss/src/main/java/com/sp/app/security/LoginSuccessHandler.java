package com.sp.app.security;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import com.sp.app.domain.Member;
import com.sp.app.domain.SessionInfo;
import com.sp.app.service.MemberService;

// 로그인 성공후 세션 및 쿠키등의 처리를 할 수 있다.
// 로그인 전 정보를 Cache : 로그인 되지 않은 상태에서 로그인 상태에서만 사용할 수 있는 페이지로 이동할 경우
//    로그인 페이지로 이동하고 로그인 후 로그인 전 페이지로 이동
public class LoginSuccessHandler implements AuthenticationSuccessHandler { 
	private String defaultUrl;
	
	private RequestCache requestCache = new HttpSessionRequestCache();
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
	
	@Autowired
	private MemberService memberService;
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		// 로그인 아이디 : authentication.getName()
		try {
			// 로그인 날짜 변경
			memberService.updateLastLogin(authentication.getName());
		} catch (Exception e) {

		}
		// 로그인 유저 정보를 세션에 저장
		HttpSession session = request.getSession();
		
		Member member = memberService.findById(authentication.getName());
		SessionInfo info = new SessionInfo();
		
		info.setMemberIdx(member.getMemberIdx());
		info.setMembership(member.getMembership());
		info.setUserId(member.getUserId());
		
		session.setAttribute("member", info);
		
		// 패스워드 변경 날짜가 90일이 지난 경우 패스워드 변경 페이지로 이동
		try {
			Date now = new Date();
			long gap;
			
			SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date modifyDate = sdf.parse(member.getModify_date());
			
			gap = (now.getTime() - modifyDate.getTime()) / (24 * 60 * 60 * 100);
			if(gap >= 90) {
				String targetUrl = "/member/updatePwd";
				
				redirectStrategy.sendRedirect(request, response, targetUrl);
				return;
			}
			
		} catch (Exception e) {
		}
		
		// redirect 설정
		resultRedirectStrategy(request, response, authentication);
	}

	public void setDefaultUrl(String defaultUrl) {
		this.defaultUrl = defaultUrl;
	}
	
	protected void resultRedirectStrategy(HttpServletRequest request, HttpServletResponse response, Authentication authentication )  throws IOException, ServletException {
		SavedRequest savedRequest = requestCache.getRequest(request, response);
		
		if(savedRequest != null) {
			// 로그인 전 권한이 필요한  페이지에 접근한 경우(게시판 등)
			String targetUrl = savedRequest.getRedirectUrl();
			redirectStrategy.sendRedirect(request, response, targetUrl);
		} else {
			// 로그인 페이지로 직접 이동한 경우
			redirectStrategy.sendRedirect(request, response, defaultUrl);
		}
		
	}
	
	

}
