package com.yadda.io.netty.webchatroom;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/28
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 当服务器监听到客户端活动时调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("[用户:]" + incoming.remoteAddress() + "在线中" + "\n");
    }

    /**
     * 服务器监听到客户端离线是调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("[用户:]" + incoming.remoteAddress() + "离线" + "\n");
    }

    /**
     * 有客户端连接时调用，把客户端的通道记录下来，加入队列中
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // 获取客户端通道
        Channel incoming = ctx.channel();

        for (Channel ch : channels) {
            if (ch != null && ch.isActive()) {
                ch.writeAndFlush(new TextWebSocketFrame("[欢迎]" + incoming.remoteAddress() + "进入聊天室" + "\n"));
            }
        }
        incoming.writeAndFlush(new TextWebSocketFrame("[系统消息:]欢迎你进入聊天室" + "\n"));

        // 加入到队列中
        channels.add(incoming);
    }

    /**
     * 有客户端断开连接时调用
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();

        for (Channel ch : channels) {
            // 通知其他人有人离开聊天室
            if (incoming != null && ch.isActive()) {
                ch.writeAndFlush(new TextWebSocketFrame("[再见:]" + ch.remoteAddress() + "离开聊天室" + "\n"));
            }
        }
        channels.remove(incoming);
    }

    /**
     * 每当客户端有消息写入时调用
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        Channel incoming = ctx.channel();
        // 将数据转发给其他人
        for (Channel ch : channels) {
            // 通知其他人有人离开聊天室
            if (ch != incoming && ch.isActive()) {
                ch.writeAndFlush(new TextWebSocketFrame("[用户:" + ch.remoteAddress() + "说:]" + msg.text().toString() + "\n"));
            } else {
                ch.writeAndFlush(new TextWebSocketFrame("[我说:]" + msg.text() + "\n"));
            }
        }

        if (channels.size() == 1) {
            // only myself
            incoming.writeAndFlush(new TextWebSocketFrame("[系统消息:]整个聊天室当前就你一个人" + "\n"));
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel incoming = ctx.channel();
        System.out.println("[用户:" + incoming.remoteAddress() + "]异常，被关闭" + "\n");
        ctx.close();
    }
}
