package com.sp.app;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.sp.app.domain.Photo;
import com.sp.app.service.PhotoService;

@Controller
public class HomeController {
	@Autowired
	private PhotoService photoService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		Map<String, Object> map = new HashMap<>();
		map.put("offset", 0);
		map.put("size", 5);
		List<Photo> listPhoto = photoService.listPhoto(map);
		
		model.addAttribute("listPhoto", listPhoto);
		
		return ".home";
	}
}
