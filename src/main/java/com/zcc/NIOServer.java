package com.zcc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;


/**
 * @author ：zcc
 * @date ：Created in 2021/10/16 13:52
 * @description：
 * nio 非阻塞同步模型
 *     优点：单线程实现读写, 操作是非阻塞，不像bio，一个客户端一个线程。
 *     缺点：如果queue中的客户端超过10万条，但只有一个客户端有数据可读，那么每次都要进行10万次read操作，浪费时间。
 *     演进：多路复用器  Java（selector）
 * @version: 1
 */


public class NIOServer {
    static ServerSocketChannel server;
    static Set<SocketChannel> queue = new HashSet<>();

    public static void main(String[] args) {
        {
            try {
                server = ServerSocketChannel.open();
                server.configureBlocking(false);  //设置为非阻塞 1
                server.bind(new InetSocketAddress(9090));   //绑定端口

                System.out.println("服务器启动......");
                while (true) {
                    SocketChannel client = server.accept(); //接收客户端链接 非阻塞 1处设置
                    if (client == null) {
                    } else {
                        System.out.println("新连接" + client.getRemoteAddress());
                        client.configureBlocking(false); //设置非阻塞 2
                        queue.add(client);
                    }

                    ByteBuffer by = ByteBuffer.allocate(1024);
                    for (SocketChannel sock : queue) {
                        by.clear();     //数据清空 索引归位
                        int num = sock.read(by);    //非阻塞读 2处设置
                        if (num > 0) {
                            by.flip();      //设置为读模式
                            sock.write(by);
                        } else if (num == 0) {
                        } else {
                            System.out.println("断开连接" + sock.getRemoteAddress());
                            queue.remove(sock);
                            sock.close();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    server.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
