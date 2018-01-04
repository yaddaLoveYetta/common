package com.yadda.stack;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/22
 */
public class DefaultMinStack extends DefaultStack<Integer> implements MinStack<Integer> {

    /**
     * 栈元素大小排序结果
     */
    private TreeMap<Integer, Integer> stackIndex = new TreeMap<>();


    /**
     * 入栈
     *
     * @param v
     */
    @Override
    public void push(Integer v) {

        if (stack.size() == 0) {
            stack.add(v);
            // 值，位置
            stackIndex.put(stack.get(0), 0);
            return;
        }

        // 加到list最后
        stack.add(v);
        // 值，位置
        stackIndex.put(stack.get(stack.size() - 1), stack.size() - 1);
    }

    /**
     * 出栈
     *
     * @return
     */
    @Override
    public Integer pop() {

        if (stack.size() == 0) {
            return null;
        }
        stackIndex.remove(stack.get(stack.size() - 1), stack.size() - 1);
        return stack.remove(stack.size() - 1);
    }

    @Override
    public int size() {
        return stack.size();
    }

    /**
     * 获取最小栈
     *
     * @return
     */
    @Override
    public Integer getMin() {
        Map.Entry<Integer, Integer> min = stackIndex.firstEntry();
        return stack.get(min.getValue().intValue());
    }


    public static void main(String[] args) {

        DefaultMinStack minStack = new DefaultMinStack();

        minStack.push(6);
        minStack.push(2);
        minStack.push(5);
        minStack.push(4);
        minStack.push(9);
        minStack.push(5);

        for (int i = 0, n = minStack.stackIndex.size(); i < n; i++) {
            System.out.println(minStack.getMin());
        }

    }
}
