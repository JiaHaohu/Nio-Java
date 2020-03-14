package com.jiahao;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * 客户端处理接受服务器消息线程
 */
public class ClientHandler implements Runnable{

    private Selector selector;

    public ClientHandler(Selector selector){
        this.selector = selector;
    }


    @Override
    public void run() {
        try {
            for (;;){
                int readyChannels =0;

                    readyChannels = selector.select();

                if (readyChannels == 0) continue;

                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                Iterator<SelectionKey> iterator = selectionKeys.iterator();

                while (iterator.hasNext()){
                    /**
                     * 取出SelectionKey实例
                     */
                    SelectionKey selectionKey = iterator.next();
                    /**
                     * 移除当前SelectionKey
                     */
                    iterator.remove();

                    if (selectionKey.isReadable()){
                        readHandler(selectionKey,selector);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Error");
        }
    }

    /**
     * 可读事件处理器
     */

    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {
        /**
         * 从SelectionKey 中获取到已经就绪的channel
         */

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        /**
         * 创建Buffer
         */

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        /**
         * 循环读取服务端信息
         */
        String request = "";

        while (socketChannel.read(byteBuffer)>0){
            /**
             * 切换为Buffer读模式
             */
            byteBuffer.flip();
            /**
             * 读取buffer中的内容
             */
            request += Charset.forName("utf-8").decode(byteBuffer);
        }

        /**
         * 将Channel在此注册到Selector上，监听他的可读事件
         */
        socketChannel.register(selector,SelectionKey.OP_READ);

        /**
         * 打印消息
         */
        if (request.length()>0){
            System.out.println(request);
        }
    }
}
