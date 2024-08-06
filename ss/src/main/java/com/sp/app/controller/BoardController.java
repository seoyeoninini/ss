package com.sp.app.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sp.app.common.FileManager;
import com.sp.app.common.MyUtil;
import com.sp.app.domain.Board;
import com.sp.app.domain.Reply;
import com.sp.app.domain.SessionInfo;
import com.sp.app.service.BoardService;

@Controller
@RequestMapping("/bbs/*")
public class BoardController {
	@Autowired
	private BoardService service;
	
	@Autowired
	private MyUtil myUtil;
	
	@Autowired
	private FileManager fileManager;

	@RequestMapping(value = "list")
	public String list(@RequestParam(value = "page", defaultValue = "1") int current_page,
			@RequestParam(defaultValue = "all") String schType,
			@RequestParam(defaultValue = "") String kwd,
			HttpServletRequest req,
			Model model) throws Exception {

		int size = 10;
		int total_page = 0;
		int dataCount = 0;

		if (req.getMethod().equalsIgnoreCase("GET")) { // GET 방식인 경우
			kwd = URLDecoder.decode(kwd, "utf-8");
		}

		// 전체 페이지 수
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("schType", schType);
		map.put("kwd", kwd);

		dataCount = service.dataCount(map);
		if (dataCount != 0) {
			total_page = dataCount / size + (dataCount % size > 0 ? 1 : 0);
		}
		
		// 다른 사람이 자료를 삭제하여 전체 페이지수가 변화 된 경우
		if (total_page < current_page) {
			current_page = total_page;
		}

		// 리스트에 출력할 데이터를 가져오기
		int offset = (current_page - 1) * size;
		if(offset < 0) offset = 0;

		map.put("offset", offset);
		map.put("size", size);

		// 글 리스트
		List<Board> list = service.listBoard(map);

		model.addAttribute("list", list);
		model.addAttribute("page", current_page);
		model.addAttribute("dataCount", dataCount);
		model.addAttribute("size", size);
		model.addAttribute("total_page", total_page);

		model.addAttribute("schType", schType);
		model.addAttribute("kwd", kwd);

		return ".bbs.list";
	}

	@GetMapping("write")
	public String writeForm(Model model) throws Exception {

		model.addAttribute("mode", "write");

		return ".bbs.write";
	}

	@PostMapping("write")
	public String writeSubmit(Board dto, HttpSession session) throws Exception {
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		String root = session.getServletContext().getRealPath("/");
		String pathname = root + "uploads" + File.separator + "bbs";

		try {
			dto.setUserId(info.getUserId());
			service.insertBoard(dto, pathname);
		} catch (Exception e) {
		}

		return "redirect:/bbs/list";
	}

	@GetMapping("article")
	public String article(@RequestParam long num,
			@RequestParam String page,
			@RequestParam(defaultValue = "all") String schType,
			@RequestParam(defaultValue = "") String kwd,
			HttpSession session,
			Model model) throws Exception {
		
		kwd = URLDecoder.decode(kwd, "utf-8");

		String query = "page=" + page;
		if (kwd.length() != 0) {
			query += "&schType=" + schType + 
					"&kwd=" + URLEncoder.encode(kwd, "UTF-8");
		}

		service.updateHitCount(num);

		// 해당 레코드 가져 오기
		Board dto = service.findById(num);
		if (dto == null) {
			return "redirect:/bbs/list?" + query;
		}
		
		// 스마트 에디터를 사용하므로
		// dto.setContent(myUtil.htmlSymbols(dto.getContent()));
		dto.setUserName(myUtil.nameMasking(dto.getUserName()));

		// 이전 글, 다음 글
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("schType", schType);
		map.put("kwd", kwd);
		map.put("num", num);

		Board prevDto = service.findByPrev(map);
		Board nextDto = service.findByNext(map);

		SessionInfo info = (SessionInfo) session.getAttribute("member");
		// 게시글 좋아요 여부
		map.put("userId", info.getUserId());
		boolean userBoardLiked = service.userBoardLiked(map);
		
		model.addAttribute("dto", dto);
		model.addAttribute("prevDto", prevDto);
		model.addAttribute("nextDto", nextDto);

		model.addAttribute("page", page);
		model.addAttribute("query", query);

		model.addAttribute("userBoardLiked", userBoardLiked);
		
		return ".bbs.article";
	}

	@GetMapping("update")
	public String updateForm(@RequestParam long num,
			@RequestParam String page,
			HttpSession session,
			Model model) throws Exception {
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		Board dto = service.findById(num);
		if (dto == null || ! info.getUserId().equals(dto.getUserId())) {
			return "redirect:/bbs/list?page=" + page;
		}

		model.addAttribute("dto", dto);
		model.addAttribute("mode", "update");
		model.addAttribute("page", page);

		return ".bbs.write";
	}

	@PostMapping("update")
	public String updateSubmit(Board dto,
			@RequestParam String page,
			HttpSession session) throws Exception {

		String root = session.getServletContext().getRealPath("/");
		String pathname = root + "uploads" + File.separator + "bbs";

		try {
			service.updateBoard(dto, pathname);
		} catch (Exception e) {
		}

		return "redirect:/bbs/list?page=" + page;
	}

	@GetMapping("deleteFile")
	public String deleteFile(@RequestParam long num,
			@RequestParam String page,
			HttpSession session) throws Exception {
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		String root = session.getServletContext().getRealPath("/");
		String pathname = root + "uploads" + File.separator + "bbs";

		Board dto = service.findById(num);
		if (dto == null) {
			return "redirect:/bbs/list?page=" + page;
		}

		if(! info.getUserId().equals(dto.getUserId())) {
			return "redirect:/bbs/list?page=" + page;
		}

		try {
			if (dto.getSaveFilename() != null) {
				fileManager.doFileDelete(dto.getSaveFilename(), pathname); // 실제파일삭제
				dto.setSaveFilename("");
				dto.setOriginalFilename("");
				service.updateBoard(dto, pathname); // DB 테이블의 파일명 변경(삭제)
			}
		} catch (Exception e) {
		}

		return "redirect:/bbs/update?num=" + num + "&page=" + page;
	}

	@GetMapping("delete")
	public String delete(@RequestParam long num,
			@RequestParam String page,
			@RequestParam(defaultValue = "all") String schType,
			@RequestParam(defaultValue = "") String kwd,
			HttpSession session) throws Exception {
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		kwd = URLDecoder.decode(kwd, "utf-8");
		String query = "page=" + page;
		if (kwd.length() != 0) {
			query += "&schType=" + schType + "&kwd=" + URLEncoder.encode(kwd, "UTF-8");
		}

		String root = session.getServletContext().getRealPath("/");
		String pathname = root + "uploads" + File.separator + "bbs";

		service.deleteBoard(num, pathname, info.getUserId(), info.getMembership());

		return "redirect:/bbs/list?" + query;
	}

	@GetMapping("download")
	public String download(@RequestParam long num, 
			HttpServletRequest req, HttpServletResponse resp,
			HttpSession session) throws Exception {

		String root = session.getServletContext().getRealPath("/");
		String pathname = root + "uploads" + File.separator + "bbs";

		Board dto = service.findById(num);

		if (dto != null) {
			boolean b = fileManager.doFileDownload(dto.getSaveFilename(), 
					dto.getOriginalFilename(), pathname, resp);
			if (b) {
				// void 반환 유형(또는 null 반환 값)이 있는 메소드는 
				//    ServletResponse, OutputStream 인수 또는 @ResponseStatus 주석
				//    도 있는 경우 응답을 완전히 처리한 것으로 간주
				return null;
			}
		}

		return ".error.filedownloadFailure";
	}
	
	// 게시글 좋아요 추가/삭제 : AJAX-JSON
	@PostMapping("insertBoardLike")
	@ResponseBody
	public Map<String, Object> insertBoardLike(@RequestParam long num, 
			@RequestParam boolean userLiked,
			HttpSession session) {
		String state = "true";
		int boardLikeCount = 0;
		SessionInfo info = (SessionInfo) session.getAttribute("member");

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("num", num);
		paramMap.put("userId", info.getUserId());

		try {
			if(userLiked) {
				service.deleteBoardLike(paramMap);
			} else {
				service.insertBoardLike(paramMap);
			}
		} catch (DuplicateKeyException e) {
			state = "liked";
		} catch (Exception e) {
			state = "false";
		}

		boardLikeCount = service.boardLikeCount(num);

		Map<String, Object> model = new HashMap<>();
		model.put("state", state);
		model.put("boardLikeCount", boardLikeCount);

		return model;
	}

	// 댓글 리스트 : AJAX-TEXT
	@GetMapping("listReply")
	public String listReply(@RequestParam long num, 
			@RequestParam(value = "pageNo", defaultValue = "1") int current_page,
			HttpSession session,
			Model model) throws Exception {

		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		int size = 5;
		int total_page = 0;
		int dataCount = 0;

		Map<String, Object> map = new HashMap<>();
		map.put("num", num);
		
		map.put("membership", info.getMembership());
		map.put("userId", info.getUserId());
		
		dataCount = service.replyCount(map);
		total_page = myUtil.pageCount(dataCount, size);
		if (current_page > total_page) {
			current_page = total_page;
		}

		int offset = (current_page - 1) * size;
		if(offset < 0) offset = 0;

		map.put("offset", offset);
		map.put("size", size);
		
		List<Reply> listReply = service.listReply(map);

		// AJAX 용 페이징
		String paging = myUtil.pagingMethod(current_page, total_page, "listPage");

		// 포워딩할 jsp로 넘길 데이터
		model.addAttribute("listReply", listReply);
		model.addAttribute("pageNo", current_page);
		model.addAttribute("replyCount", dataCount);
		model.addAttribute("total_page", total_page);
		model.addAttribute("paging", paging);

		return "bbs/listReply";
	}

	// 댓글 및 댓글의 답글 등록 : AJAX-JSON
	@PostMapping("insertReply")
	@ResponseBody
	public Map<String, Object> insertReply(Reply dto, HttpSession session) {
		SessionInfo info = (SessionInfo) session.getAttribute("member");
		String state = "true";

		try {
			dto.setUserId(info.getUserId());
			service.insertReply(dto);
		} catch (Exception e) {
			state = "false";
		}

		Map<String, Object> model = new HashMap<>();
		model.put("state", state);
		return model;
	}

	// 댓글 및 댓글의 답글 삭제 : AJAX-JSON
	@PostMapping("deleteReply")
	@ResponseBody
	public Map<String, Object> deleteReply(@RequestParam Map<String, Object> paramMap) {
		String state = "true";
		
		try {
			service.deleteReply(paramMap);
		} catch (Exception e) {
			state = "false";
		}

		Map<String, Object> map = new HashMap<>();
		map.put("state", state);
		return map;
	}

	// 댓글의 답글 리스트 : AJAX-TEXT
	@GetMapping("listReplyAnswer")
	public String listReplyAnswer(@RequestParam Map<String, Object> paramMap, 
			HttpSession session, Model model) throws Exception {
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		paramMap.put("membership", info.getMembership());
		paramMap.put("userId", info.getUserId());
		
		List<Reply> listReplyAnswer = service.listReplyAnswer(paramMap);

		model.addAttribute("listReplyAnswer", listReplyAnswer);
		return "bbs/listReplyAnswer";
	}

	// 댓글의 답글 개수 : AJAX-JSON
	@PostMapping(value = "countReplyAnswer")
	@ResponseBody
	public Map<String, Object> countReplyAnswer(@RequestParam Map<String, Object> paramMap,
			HttpSession session) {
		SessionInfo info = (SessionInfo)session.getAttribute("member");
		
		paramMap.put("membership", info.getMembership());
		paramMap.put("userId", info.getUserId());
		
		int count = service.replyAnswerCount(paramMap);

		Map<String, Object> model = new HashMap<>();
		model.put("count", count);
		return model;
	}

	// 댓글의 좋아요/싫어요 추가 : AJAX-JSON
	@PostMapping("insertReplyLike")
	@ResponseBody
	public Map<String, Object> insertReplyLike(@RequestParam Map<String, Object> paramMap,
			HttpSession session) {
		String state = "true";

		SessionInfo info = (SessionInfo) session.getAttribute("member");
		Map<String, Object> model = new HashMap<>();

		try {
			paramMap.put("userId", info.getUserId());
			service.insertReplyLike(paramMap);
		} catch (DuplicateKeyException e) {
			state = "liked";
		} catch (Exception e) {
			state = "false";
		}

		Map<String, Object> countMap = service.replyLikeCount(paramMap);

		// 마이바티스의 resultType이 map인 경우 int는 BigDecimal로 넘어옴
		int likeCount = ((BigDecimal) countMap.get("LIKECOUNT")).intValue();
		int disLikeCount = ((BigDecimal)countMap.get("DISLIKECOUNT")).intValue();
		
		model.put("likeCount", likeCount);
		model.put("disLikeCount", disLikeCount);
		model.put("state", state);
		return model;
	}

	// 댓글의 좋아요/싫어요 개수 : AJAX-JSON
	@PostMapping("countReplyLike")
	@ResponseBody
	public Map<String, Object> countReplyLike(@RequestParam Map<String, Object> paramMap,
			HttpSession session) {

		Map<String, Object> countMap = service.replyLikeCount(paramMap);
		// 마이바티스의 resultType이 map인 경우 int는 BigDecimal로 넘어옴
		// countMap를 model에 담아 JSON으로 넘겨도 가능
		int likeCount = ((BigDecimal) countMap.get("LIKECOUNT")).intValue();
		int disLikeCount = ((BigDecimal)countMap.get("DISLIKECOUNT")).intValue();
		
		Map<String, Object> model = new HashMap<>();
		model.put("likeCount", likeCount);
		model.put("disLikeCount", disLikeCount);

		return model;
	}
	
	// 댓글 숨김/표시 추가 : AJAX-JSON
	@PostMapping("replyShowHide")
	@ResponseBody
	public Map<String, Object> replyShowHide(@RequestParam Map<String, Object> paramMap,
			HttpSession session) {
		String state = "true";

		SessionInfo info = (SessionInfo) session.getAttribute("member");

		try {
			paramMap.put("userId", info.getUserId());
			service.updateReplyShowHide(paramMap);
		} catch (Exception e) {
			state = "false";
		}

		Map<String, Object> model = new HashMap<>();
		model.put("state", state);
		return model;
	}	
}
