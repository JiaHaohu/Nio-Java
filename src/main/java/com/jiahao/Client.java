package com.jiahao;

import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.Set;

public class Client {

    /**
     * 启动
     */
    public void satrt(String name) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1",8000));

        /**
         * 新开线程,处理服务端信息
         */
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector,SelectionKey.OP_READ);
        new Thread(new ClientHandler(selector)).start();

        /**
         * 向服务器发送数据
         */
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()){
            String request = scanner.nextLine();
            if (request != null && request.length()>0){
                socketChannel.write(Charset.forName("utf-8").encode(name+":"+request));
            }
        }

        /**
         * 新开线程,处理服务端信息
         */


    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.satrt("A");
    }
}
