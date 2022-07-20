package com.it.common.exception;

/**
 * @author : code1997
 * @date : 2021/7/19 21:25
 */
public class NoStockException extends RuntimeException {

    public NoStockException() {
        super("商品没有足够的库存了");
    }

    public NoStockException(Long skuId) {
        super("商品id:" + skuId + ",没有足够的库存了");
    }

}
