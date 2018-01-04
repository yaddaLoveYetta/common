package com.yadda.io.nio;

import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/14
 */
public class NioServerTest {

    @Test
    public void channel() throws IOException {

        RandomAccessFile file = new RandomAccessFile("test.txt", "rw");

        FileChannel channel = file.getChannel();

        ByteBuffer buf = ByteBuffer.allocate(10);

        int bytesRead = channel.read(buf);

        while (bytesRead != -1) {
            buf.flip();
            while (buf.hasRemaining()) {
                System.out.print((char) buf.get());
            }

            buf.clear();
            bytesRead = channel.read(buf);
        }

//        file.close();


        String newData = "New String to write to file..." + System.currentTimeMillis();

        buf.clear();
        buf.put(newData.getBytes());

        buf.flip();

        while (buf.hasRemaining()) {
            channel.write(buf);
        }

        file.close();
        channel.close();

    }

    @Test
    public void selector() throws IOException {


        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        // 绑定8080端口
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // 通过调用 select 方法, 阻塞地等待 channel I/O 可操作
            if (selector.select(3000) == 0) {
                // 没有连接可用
                System.out.print(".");
                continue;
            }

            // 获取 I/O 操作就绪的 SelectionKey, 通过 SelectionKey 可以知道哪些 Channel 的哪类 I/O 操作已经就绪.
            Set<SelectionKey> selectionKeys = selector.selectedKeys();

            Iterator<SelectionKey> it = selectionKeys.iterator();

            while (it.hasNext()) {

                SelectionKey selectionKey = it.next();


                if (selectionKey.isAcceptable()) {
                    // 当 OP_ACCEPT 事件到来时, 我们就有从 ServerSocketChannel 中获取一个 SocketChannel,
                    // 代表客户端的连接
                    // 注意, 在 OP_ACCEPT 事件中, 从 key.channel() 返回的 Channel 是 ServerSocketChannel.
                    // 而在 OP_WRITE 和 OP_READ 中, 从 key.channel() 返回的是 SocketChannel.

                    SocketChannel clientChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
                    clientChannel.configureBlocking(false);
                    //在 OP_ACCEPT 到来时, 再将这个 Channel 的 OP_READ 注册到 Selector 中.
                    // 注意, 这里我们如果没有设置 OP_READ 的话, 即 interest set 仍然是 OP_CONNECT 的话, 那么 select 方法会一直直接返回.
                    clientChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                }

                if (selectionKey.isReadable()) {

                    SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer buf = (ByteBuffer) selectionKey.attachment();

                    int bytesRead = clientChannel.read(buf);

                    if (bytesRead == -1) {
                        clientChannel.close();
                    } else if (bytesRead > 0) {
                        selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        System.out.println("Get data length: " + bytesRead);
                    }

                }

                if (selectionKey.isValid() && selectionKey.isWritable()) {
                    ByteBuffer buf = (ByteBuffer) selectionKey.attachment();
                    buf.flip();
                    SocketChannel clientChannel = (SocketChannel) selectionKey.channel();

                    clientChannel.write(buf);

                    if (!buf.hasRemaining()) {
                        selectionKey.interestOps(SelectionKey.OP_READ);
                    }
                    buf.compact();
                }

                // 当获取一个 SelectionKey 后, 就要将它删除, 表示我们已经对这个 IO 事件进行了处理.
                it.remove();
            }

        }
    }

}
