package com.hyl.gulimall.search.service;

import com.hyl.gulimall.search.vo.SearchParamVo;
import com.hyl.gulimall.search.vo.SearchResult;
import org.springframework.stereotype.Service;


public interface MallSearchService {
    /**
     * 检索结果
     * @param paramVo
     * @return
     */
    SearchResult search(SearchParamVo paramVo);
}
