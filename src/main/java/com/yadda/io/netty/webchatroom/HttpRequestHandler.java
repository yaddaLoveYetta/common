package com.yadda.io.netty.webchatroom;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedNioFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * 用来区分用户的请求是请求的页面还是发送的websocket请求,读取html页面并返回客户端
 *
 * @author yadda<silenceisok@163.com>
 * @since 2017/12/28
 */
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    // wensocket请求地址
    private final String chatUri;
    private static File indexFile = null;

    static {
        URL local = HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            String path = local.toURI() + "index.html";
            path = !path.contains("file:") ? path : path.substring(6);
            indexFile = new File(path);
            System.out.println(path);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public HttpRequestHandler(String chatUri) {
        this.chatUri = chatUri;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        System.out.println(request.uri());

        if (chatUri.equals(request.uri()) || "/favicon.ico".equals(request.uri())) {
            // 用户请求的是websocket的地址，那么就不做处理，直接将请求转给管道的下一个handler处理
            ctx.fireChannelRead(request.retain());
        } else {
            // 返回页面给客户端
            if (HttpUtil.is100ContinueExpected(request)) {
                //100 请求
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
                ctx.writeAndFlush(request);
            }

            // 200请求
            RandomAccessFile file = new RandomAccessFile(indexFile, "r");
            // 设置http响应头
            HttpResponse response = new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=utf-8");
            boolean keepAlive = HttpUtil.isKeepAlive(request);


            if (keepAlive) {
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, file.length());
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }

            ctx.write(response);
            ctx.write(new ChunkedNioFile(file.getChannel()));

            ChannelFuture future = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);

            if (!keepAlive) {
                future.addListener(ChannelFutureListener.CLOSE);
            }

            file.close();
        }
    }
}
