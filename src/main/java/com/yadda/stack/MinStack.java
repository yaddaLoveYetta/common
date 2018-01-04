package com.yadda.stack;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/22
 */
public interface MinStack<T extends Comparable<T>> extends Stack<T> {

    /**
     * 获取最小栈
     *
     * @return
     */
    T getMin();
}
