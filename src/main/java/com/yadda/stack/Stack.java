package com.yadda.stack;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2018/1/4
 */
public interface Stack<T> {
    /**
     * 入栈
     *
     * @param t T
     */
    void push(T t);

    /**
     * 获取栈顶元素
     *
     * @return
     */
    T pop();

    /**
     * 查找某个元素在栈中的位置,找不到返回-1
     * <p>
     * 1:从栈顶开始找
     * 2:返回第一次元素出现的位置
     *
     * @param t
     * @return
     */
    int search(T t);

    /**
     * 栈元素个数
     *
     * @return
     */
    int size();

}
