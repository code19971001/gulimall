package com.it.gulimall.search.web;

import com.it.gulimall.search.service.MallSearchService;
import com.it.gulimall.search.vo.SearchParams;
import com.it.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author : code1997
 * @date : 2021/5/27 23:18
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String listPage(SearchParams params, Model model , HttpServletRequest request) {
        params.setQueryString(request.getQueryString());
        //根据页面传递来的参数，去es中查询。
        SearchResult searchResult = mallSearchService.search(params);
        model.addAttribute("result", searchResult);

        return "list";
    }


}
