package com.yadda.io.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/27
 */
public class NettyClient {


    public void connect(String host, int port) throws Exception {
        Bootstrap b = new Bootstrap();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        b.group(workGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer() {

            @Override
            protected void initChannel(Channel ch) throws Exception {

                ch.pipeline().addLast(new ClientHandler());
            }
        });
        ChannelFuture future = b.connect(host, 8001);

        future.channel().writeAndFlush(Unpooled.copiedBuffer("hello".getBytes()));

        future.channel().closeFuture().sync();

        workGroup.shutdownGracefully();
    }

    private class ClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            ByteBuf byteBuf = (ByteBuf) msg;

            byte[] data = new byte[byteBuf.readableBytes()];
            byteBuf.readBytes(data);

            System.out.println("receive server msg:" + new String(data));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            super.exceptionCaught(ctx, cause);
            ctx.close();
        }
    }

    public static void main(String[] args) throws Exception {

        new NettyClient().connect("127.0.0.1", 8001);
    }

}

