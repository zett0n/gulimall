package cn.edu.zjut.search.web;

import cn.edu.zjut.search.service.SearchService;
import cn.edu.zjut.search.vo.SearchParam;
import cn.edu.zjut.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 自动将页面提交过来的所有请求参数封装成我们指定的对象
     */
    @GetMapping("/list.html")
    public String listPage(SearchParam param, Model model, HttpServletRequest request) {

        param.set_queryString(request.getQueryString());

        // 根据传递来的页面的查询参数，去es中检索商品
        SearchResult result = this.searchService.search(param);

        model.addAttribute("result", result);

        return "list";
    }

}
