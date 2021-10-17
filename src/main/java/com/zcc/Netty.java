package com.zcc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import org.testng.annotations.Test;

import java.net.InetSocketAddress;

/**
 * @author ：zcc
 * @date ：Created in 2021/10/16 22:39
 * @description：netty
 * @version: 1
 */

public class Netty {


    @Test
    public void start(){
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup(1);
//        eventExecutors.register()
        NioServerSocketChannel ser = new NioServerSocketChannel();

        eventExecutors.register(ser);

        ChannelPipeline pipeline = ser.pipeline();
        pipeline.addLast(new acceptHandler(eventExecutors, new readHandler()));
        ChannelFuture bind = ser.bind(new InetSocketAddress(9090));
        try {
            bind.sync().channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @ChannelHandler.Sharable
    private static class readHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("read register.......");
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

    private static class acceptHandler extends ChannelInboundHandlerAdapter {

        NioEventLoopGroup selector = null;
        readHandler readHandler = null;

        public acceptHandler(NioEventLoopGroup nioEventLoopGroup, ChannelHandler channelHandler) {
            selector = nioEventLoopGroup;
            readHandler = (Netty.readHandler) channelHandler;
        }

//        @Override
//        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//            System.out.println("accept register.......");
//        }
//
//        @Override
//        public void channelActive(ChannelHandlerContext ctx) throws Exception {
//            System.out.println("accept active.........");
//        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            SocketChannel client = (SocketChannel) msg;
            System.out.println("客户端登录" + client.remoteAddress());
            selector.register(client);
            ChannelPipeline pipeline = client.pipeline();
            pipeline.addLast(readHandler);
        }
    }

}
