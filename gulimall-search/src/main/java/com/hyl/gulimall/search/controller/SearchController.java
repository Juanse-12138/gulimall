package com.hyl.gulimall.search.controller;

import com.hyl.gulimall.search.service.MallSearchService;
import com.hyl.gulimall.search.vo.SearchParamVo;
import com.hyl.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author hyl_marco
 * @data 2022/4/21 - 17:34
 */
@Controller
public class SearchController {

    @Autowired
    MallSearchService mallSearchService;


    /**
     * 利用SpringMVC自动封装查询的参数为指定对象
     * @param paramVo
     * @return
     */
    @GetMapping("/list.html")
    public String lisgPage(SearchParamVo paramVo, Model model, HttpServletRequest request){
        //根据页面传递的数据查询参数，去es中检索商品
        String queryString = request.getQueryString();
        paramVo.set_queryString(queryString);
        SearchResult reslut = mallSearchService.search(paramVo);
        model.addAttribute("result",reslut);


        return "list";
    }
}
