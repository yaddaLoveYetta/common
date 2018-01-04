package com.yadda.io.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.ByteBuffer;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/18
 */
public class NettyServer {

    void bind(int port) throws InterruptedException {

        //创建BOSS线程组 用于服务端接受客户端的连接
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(2);
        //创建WORK线程组 用于进行SocketChannel的网络读写
        NioEventLoopGroup workGroup = new NioEventLoopGroup(3);

        try {
            // 创建ServerBootStrap实例,ServerBootstrap 用于启动NIO服务端的辅助启动类，目的是降低服务端的开发复杂度
            ServerBootstrap b = new ServerBootstrap();
            // 绑定Reactor线程池
            b.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new MyHandler())
                    .childHandler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            System.out.println("initChannel");
                            channel.pipeline().addLast("myHandler", new MyHandler());
                        }
                    });
            // 绑定端口，同步等待成功
            ChannelFuture future = b.bind(port).sync();
            // 等待服务端监听端口关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 优雅地关闭
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }

    private class MyHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelRegistered" + ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {

            ctx.writeAndFlush("hello".getBytes());
            System.out.println("channelActive");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("ChannelHandlerContext");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("channelRead");

            if (!(msg instanceof ByteBuf)) {
                return;
            }
            ByteBuf byteBuf = (ByteBuf) msg;
            byte[] b = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(b);
            System.out.println("client msg:" + new String(b));
            ctx.channel().writeAndFlush(Unpooled.copiedBuffer("欢迎".getBytes()));
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelReadComplete");
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelWritabilityChanged");
        }
    }

    public static void main(String[] args) throws Exception {
        new NettyServer().bind(8001);
    }
}
