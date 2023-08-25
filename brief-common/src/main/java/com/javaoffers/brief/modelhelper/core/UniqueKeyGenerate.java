package com.javaoffers.brief.modelhelper.core;

/**
 * @author mingJie
 */
public interface UniqueKeyGenerate<K> {

    /**
     * 生成唯一key.
     */
    K generate();

    /**
     * 初始化key值.
     */
    default void initKey(K k){};

}
