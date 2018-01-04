package com.yadda.stack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2018/1/4
 */
public class DefaultStack<T> implements Stack<T> {

    protected final static int DEFAULT_SIZE = 10;

    protected List<T> stack;

    public DefaultStack() {
        this(DEFAULT_SIZE);
    }

    public DefaultStack(int size) {
        this.stack = new ArrayList<>(size);
    }

    @Override
    public void push(T t) {
        stack.add(t);
    }

    @Override
    public T pop() {

        if (stack.isEmpty()) {
            throw new RuntimeException("there is no item in stack");
        }
        return stack.remove(stack.size() - 1);
    }

    @Override
    public int search(T t) {

        if (!stack.contains(t)) {
            return -1;
        }

        if (t == null) {
            for (int i = stack.size(); i >= 0; i--) {
                if (stack.get(i) == null) {
                    return i;
                }
            }
        } else {
            for (int i = stack.size(); i >= 0; i--) {
                if (t.equals(stack.get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public int size() {
        return stack.size();
    }


    public static void main(String[] args) {
        DefaultStack<String> stack = new DefaultStack<>(5);

        stack.push("1");
        stack.push("2");
        stack.push("3");
        stack.push("4");
        stack.push("5");
        stack.push("6");
        stack.push("7");

        for (int i = 0, size = stack.size(); i < size; i++) {
            System.out.println(stack.pop());
        }
    }

}
