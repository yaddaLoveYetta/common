package com.yadda.stack;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/22
 */
public interface Stack<T extends Comparable<T>> {
    /**
     * 入栈
     *
     * @param t
     */
    void push(T t);

    /**
     * 出栈
     *
     * @return
     */
    T pop();

    /**
     * 获取最小栈
     *
     * @return
     */
    T getMin();
}
