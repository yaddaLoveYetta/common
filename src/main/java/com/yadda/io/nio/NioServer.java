package com.yadda.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/18
 */
public class NioServer {

    private ByteBuffer receiveBuffer = ByteBuffer.allocate(32);
    private ByteBuffer sendBuffer = ByteBuffer.allocate(32);
    private Selector selector;

    public NioServer(int port) throws Exception {

        ServerSocketChannel channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        channel.socket().bind(new InetSocketAddress(port));
        selector = Selector.open();
        channel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("server started ...");
    }


    private void listen() throws IOException {

        while (true) {

            if (selector.select(3000) == 0) {
                // 没有管道准备好了,不阻塞而是设定超时时间来循环检查管道状态
                continue;
            }

            Set<SelectionKey> selectorKeys = selector.selectedKeys();

            Iterator<SelectionKey> it = selectorKeys.iterator();

            while (it.hasNext()) {
                SelectionKey selectionKey = it.next();
                it.remove();
                handleKey(selectionKey);
            }

        }


    }

    private void handleKey(SelectionKey selectionKey) throws IOException {

        ServerSocketChannel serverChannel;
        SocketChannel clientChannel;

        // 测试此键的通道是否已准备好接受新的套接字连接。
        if (selectionKey.isAcceptable()) {
            // 返回为之创建此键的通道。
            serverChannel = (ServerSocketChannel) selectionKey.channel();
            // 接受到此通道套接字的连接。
            // 此方法返回的套接字通道（如果有）将处于阻塞模式。
            clientChannel = serverChannel.accept();
            // 配置为非阻塞
            clientChannel.configureBlocking(false);
            // 注册到selector，等待连接
            clientChannel.register(selector, SelectionKey.OP_READ);
        } else if (selectionKey.isReadable()) {
            // 返回为之创建此键的通道。
            clientChannel = (SocketChannel) selectionKey.channel();
            //将缓冲区清空以备下次读取
            receiveBuffer.clear();
            //读取服务器发送来的数据到缓冲区中
            int readBytes = clientChannel.read(receiveBuffer);

            while (readBytes > 0) {
                System.out.println("服务器端接受客户端数据--:" + new String(receiveBuffer.array(), 0, readBytes));
                readBytes = clientChannel.read(receiveBuffer);
            }

            clientChannel.register(selector, SelectionKey.OP_WRITE);

        } else if (selectionKey.isWritable()) {
            // 返回为之创建此键的通道。
            clientChannel = (SocketChannel) selectionKey.channel();
            //将缓冲区清空以备下次写入
            sendBuffer.clear();
            //向缓冲区中输入数据
            sendBuffer.put("message from server--".getBytes());
            //将缓冲区各标志复位,因为向里面put了数据标志被改变要想从中读取数据发向服务器,就要复位
            sendBuffer.flip();
            //输出到通道
            clientChannel.write(sendBuffer);
            System.out.println("服务器端向客户端发送数据--：message from server--");
            clientChannel.register(selector, SelectionKey.OP_READ);
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8001;
        NioServer server = new NioServer(port);
        server.listen();
    }
}
