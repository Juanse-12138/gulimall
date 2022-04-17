package com.hyl.gulimall.product.web;

import com.hyl.gulimall.product.entity.CategoryEntity;
import com.hyl.gulimall.product.service.CategoryService;
import com.hyl.gulimall.product.vo.catelogvo.Catelog2Vo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * @author hyl_marco
 * @data 2022/4/14 - 0:08
 */
@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        /*TODO：查出所有的1级分类*/
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();


        model.addAttribute("categorys",categoryEntities);
        /*//classpath:/templates/ + 返回值 + .html*/
        /*前缀 + 后缀*/
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){

        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }
}
