package com.hly.gulimall.search.service;

import com.hly.gulimall.search.vo.SearchParam;
import com.hly.gulimall.search.vo.SearchResult;

public interface MallSearchService {

    SearchResult search(SearchParam param);
}
