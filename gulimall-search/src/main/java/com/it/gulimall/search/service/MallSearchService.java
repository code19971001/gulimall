package com.it.gulimall.search.service;

import com.it.gulimall.search.vo.SearchParams;
import com.it.gulimall.search.vo.SearchResult;

/**
 * @author : code1997
 * @date : 2021/5/31 21:03
 */
public interface MallSearchService {
    /**
     * 查询页面中需要的信息。
     * @param params ：需要检索的参数
     * @return ：根据参数查找到的结果
     */
    SearchResult search(SearchParams params);
}
