package com.yadda.io.netty.chatroom;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/27
 */
public class ChatClient {

    private String host;
    private int port;

    public ChatClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {

        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {

            Bootstrap bootstrap = new Bootstrap();

            bootstrap.group(workGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {

                    ChannelPipeline pipeline = channel.pipeline();

                    //发送的数据在管道中是无缝流动的
                    // 所以在数据量大时，为了分割数据，可以用一下几种方式
                    //1:定长
                    // 2: 固定分隔符
                    //3:将消息分成固定长度消息头，可变长度消息体，在消息头中用一个数组说明消息体的长度
                    // 4:其他复杂自定义协议
                    // 下面用第二种方式分隔
                    pipeline.addLast("frame", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                    // 消息解码器
                    pipeline.addLast("decoder", new StringDecoder());
                    // 消息编码器
                    pipeline.addLast("encoder", new StringEncoder());
                    // 业务处理类
                    pipeline.addLast("serverHandler", new ChatClientHandler());
                }
            });

            ChannelFuture future = bootstrap.connect(this.host, this.port).sync();
            System.out.println("客户端连接成功……");

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                future.channel().writeAndFlush(reader.readLine() + "\n");
            }

            //future.channel().closeFuture().sync();
            //System.out.println("客户端关闭……");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new ChatClient("127.0.0.1", 8001).start();
    }
}
