package com.yadda.io.netty.chatroom;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/27
 */
public class ChatServer {

    private int port;


    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        
        // 创建两个线程组-一个负责客户端连接，一个负责与客户端通讯
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {


            ServerBootstrap bootstrap = new ServerBootstrap();
            // 设置服务器
            bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                    // 缓冲区大小
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 保持连接
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    // 设置回调类
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            System.out.println("有客户端接入:" + channel.remoteAddress());
                            ChannelPipeline pipeline = channel.pipeline();

                            //发送的数据在管道中是无缝流动的
                            // 所以在数据量大时，为了分割数据，可以用一下几种方式
                            //1:定长
                            // 2: 固定分隔符
                            //3:将消息分成固定长度消息头，可变长度消息体，在消息头中用一个数组说明消息体的长度
                            // 4:其他复杂自定义协议

                            // 下面用第二种方式分隔
                            pipeline.addLast("frame", new DelimiterBasedFrameDecoder(10, Delimiters.lineDelimiter()));
                            // 消息解码器
                            pipeline.addLast("decoder", new StringDecoder());
                            // 消息编码器
                            pipeline.addLast("encoder", new StringEncoder());
                            // 业务处理类
                            pipeline.addLast("serverHandler", new ChatServerHandler());
                        }
                    });
            // 启动服务器-同步
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("server start success");
            // 关闭服务器-同步
            future.channel().closeFuture().sync();

            System.out.println("server shutdown success");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }


    public static void main(String[] args) {
        new ChatServer(8001).start();
    }
}
