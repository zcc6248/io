package com.zcc;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author ：zcc
 * @date ：Created in 2021/10/16 13:52
 * @description：
 * bio 阻塞同步模型
 *     缺点：线程太多
 *     演进：nio
 * @version: 1
 */

class BIOServer {
    static ServerSocket server;

    public static void main(String[] args) {
        {
            try {
                server = new ServerSocket();
                server.bind(new InetSocketAddress(9090));   //绑定端口

                System.out.println("服务器启动......");
                while (true) {
                    Socket client = server.accept(); //接收客户端链接 阻塞
                    if (client == null) {
                    } else {
                        System.out.println("新连接" + client.getRemoteSocketAddress());
                    }
                    new Thread(()->{
                        try {
                            InputStream inputStream = client.getInputStream();
                            OutputStream outputStream = client.getOutputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            while (true) {
                                String s = reader.readLine();   //读取数据 阻塞
                                if (s != null) {
                                    outputStream.write(s.getBytes());
                                }else {
                                    System.out.println(Thread.currentThread().getName() + "断开连接" + client.getRemoteSocketAddress());
                                    client.close();
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
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
