package com.zcc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * @author ：zcc
 * @date ：Created in 2021/10/16 22:39
 * @description：netty
 * @version: 1
 */

public class Netty {

    public static void main(String[] args) {
        start();
    }

    public static void start(){
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup(1);
//        eventExecutors.register()
        NioServerSocketChannel ser = new NioServerSocketChannel();

        ChannelPipeline pipeline = ser.pipeline();
        pipeline.addLast(new acceptHandler(eventExecutors, new initHandler()));
        eventExecutors.register(ser);

        ChannelFuture bind = ser.bind(new InetSocketAddress(9090));
        try {
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @ChannelHandler.Sharable
    private static class initHandler extends ChannelInboundHandlerAdapter{

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("init register");
            Channel channel = ctx.channel();
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addLast(new readHandler());
            pipeline.remove(this);
            super.channelRegistered(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("init read");
            super.channelRead(ctx, msg);
        }
    }

    private static class acceptHandler extends ChannelInboundHandlerAdapter {

        NioEventLoopGroup selector = null;
        ChannelHandler readHandler = null;

        public acceptHandler(NioEventLoopGroup nioEventLoopGroup, ChannelHandler channelHandler) {
            selector = nioEventLoopGroup;
            readHandler = channelHandler;
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("accept register.......");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("accept active.........");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            NioSocketChannel client = (NioSocketChannel) msg;
            System.out.println("客户端登录" + client.remoteAddress());
            ChannelPipeline pipeline = client.pipeline();
            pipeline.addLast(readHandler);
            selector.register(client);
        }
    }

    private static class readHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("read Register.......");
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("read active.........");
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("客户端断开链接......");
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf by = (ByteBuf) msg;
            CharSequence charSequence = by.getCharSequence(0, by.readableBytes(), CharsetUtil.UTF_8);
            System.out.println("客户端发送数据:" + charSequence.toString());
            ctx.writeAndFlush(by);
        }
    }
}
