package com.zcc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author ：zcc
 * @date ：Created in 2021/10/16 21:15
 * @description：多路复用
 * Selector : 非阻塞同步
 * java中进行了封装，默认使用epoll。Selector选择器。Selector.open()==epoll_create(), register()==epoll_ctl(), select()==epoll_wait()
 *         select：将用户层遍历每个客户端的事情交给内核去完成。减少了系统调用。但还是要遍历。并且可遍历的客户端集合大小为1024
 *         poll：相较于select，可遍历集合无大小限制。
 *         epoll：引用了event。创建多路复用器epoll_create(), 服务器注册监听epoll_ctl()每个fd事件，内核将其加入到红黑树中，
 *         如果注册事件有消息，就将其放入fd一个链表中，等待用户代码进行读取epoll_wait()。
 * @version: 1
 */

public class SelectorServer {
    Selector selector = null;   //选择器 一套接口，兼容select、poll、epoll
    ServerSocketChannel server = null;

    SelectorServer(){
    }

    public void start(){
        try {
            selector = Selector.open();     //epoll_create()
            server = ServerSocketChannel.open();
            server.configureBlocking(false);        //设置非阻塞 1
            server.bind(new InetSocketAddress(9090));
            server.register(selector, SelectionKey.OP_ACCEPT);      //注册监听 epoll_ctl()
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("服务器启动.....");
        while (true){
            try {
                if(selector.select() > 0){ //epoll_wait()
                    Set<SelectionKey> keys = selector.selectedKeys();       //到达事件即链表中的fd
                    Iterator<SelectionKey> iterator = keys.iterator();
                    while (iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isAcceptable()){
                            acceptHandler(key);
                        }else if(key.isReadable()){
                            readHandler(key);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readHandler(SelectionKey key) {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer attachment = ByteBuffer.allocateDirect(1024);
        try {
            int num = client.read(attachment);
            if (num > 0){
                attachment.flip();
                client.write(attachment);
            }else if (num == 0){}
            else {
                System.out.println("客户端断开连接："+ client.getRemoteAddress());
                client.close();
                key.channel();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptHandler(SelectionKey key) {
        ServerSocketChannel ser = (ServerSocketChannel) key.channel();
        try {
            SocketChannel client = ser.accept();        //非阻塞 1处设置
            System.out.println("客户端链接:"+ client.getRemoteAddress());
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SelectorServer selectorServer = new SelectorServer();
        selectorServer.start();
    }
}
