package com.yadda.io.netty.webchatroom;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/28
 */
public class WebChatServer {

    private int port;

    public WebChatServer(int port) {
        this.port = port;
    }

    public void start() {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        try {


            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            /**
                             * webSocket通讯连接的建立过程:
                             * 1：建立websocket连接时，客户浏览器首先要向服务端发送一个握手的请求，这个请求是http协议请求
                             * 请求头信息中有一个头信息"upgrade websocket",向服务端表名提升协议，使用websocket协议
                             * 2：服务端收到这个请求(http)，会生成应答信息返回客户端，这个应答信息还是http协议的response响应
                             * 3：客户端收到响应，则websocket的连接就建立完毕，后续的通讯就用websocket协议而不用http协议了
                             */
                            // 将请求和应答消息解码或编码成http协议信息
                            pipeline.addLast(new HttpServerCodec());
                            // 拆包粘包处理(64K)
                            pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                            // 用于向客户端发送HTML5的页面文件
                            pipeline.addLast(new ChunkedWriteHandler());
                            // 用来区分用户的请求是请求的页面还是发送的websocket请求,读取html页面并返回客户端
                            pipeline.addLast(new HttpRequestHandler("/chat"));
                            // 控制
                            pipeline.addLast(new WebSocketServerProtocolHandler("/chat"));
                            // 业务处理器
                            pipeline.addLast(new TextWebSocketFrameHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(port).sync();

            System.out.println("websocket 服务器已启动");

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new WebChatServer(8002).start();
    }
}
