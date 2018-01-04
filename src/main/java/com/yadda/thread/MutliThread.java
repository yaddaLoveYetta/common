package com.yadda.thread;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/25
 */
public class MutliThread extends Thread {

    public MutliThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println(Thread.currentThread().getName() + i);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new MutliThread("testThread");
        thread.start();
        sleep(1000);

        //IllegalThreadStateException
        thread.start();
    }
}
