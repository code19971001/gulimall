package com.it.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author : code1997
 * @date : 2021/5/22 16:27
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catalog2Vo {

    //1级父分类id
    private String catalog1Id;
    //3级子分类数组
    private List<Catalog3Vo> catalog3List;

    private String id;
    private String name;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Catalog3Vo {
        private String catalog2Id;
        private String id;
        private String name;
    }
}
