package com.jiahao;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO服务器
 *
 */
public class Server {

    public void start() throws IOException {

        /**
         * 1.创建Selector
         */
        Selector selector = Selector.open();


        /**
         * 2.通过ServerSocketChannel 创建Channel通道
         */

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();


        /**
         * 3.为Channel通道绑定监听端口
         */

        serverSocketChannel.bind(new InetSocketAddress(8000));

        /**
         * 4.设置Channel为非阻塞模式
         */

        serverSocketChannel.configureBlocking(false);

        /**
         * 5.将Channel注册在Selector上，监听连接事件
         */

        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("服务器启动成功。。。");

        /**
         * 6.循环等待新接入的连接
         */

        for (;;){
            int readyChannels = selector.select();

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

                /**
                 * 如果是接入事件，要怎样处理
                 */
                if (selectionKey.isAcceptable()){
                    acceptHandler(serverSocketChannel,selector);
                }

                /**
                 * 如果是可读事件，要怎样处理
                 */
                if (selectionKey.isReadable()){
                    readHandler(selectionKey,selector);
                }
            }

        }

        /**
         * 7.根据就绪状态调用新的Handler处理
         */


    }

    /**
     * 处理接入事件
     */
    private void acceptHandler(ServerSocketChannel serverSocketChannel,Selector selector) throws IOException {
        /**
         * 创建一个SocketChannel
         */
        SocketChannel socketChannel = serverSocketChannel.accept();

        /**
         * 将socketChannel设置为非阻塞模式
         */

        socketChannel.configureBlocking(false);

        /**
         * 将Channel注册到Selector上，监听可读事件
         */

        socketChannel.register(selector,SelectionKey.OP_READ);

        /**
         * 回复客户端提示信息
         */
        socketChannel.write(Charset.forName("Utf-8").encode("你与聊天室其他人都不是朋友，请注意隐私安全"));
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
         * 循环读取客户端信息
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
            request +=Charset.forName("utf-8").decode(byteBuffer);
        }

        /**
         * 将Channel在此注册到Selector上，监听他的可读事件
         */
        socketChannel.register(selector,SelectionKey.OP_READ);

        /**
         * 将客户端发送的请求信息广播出去
         */
        if (request.length()>0){
            broadCast(selector,socketChannel,request);
        }
    }

    /**
     * 广播给所有客户端
     */
    private void broadCast(Selector selector,SocketChannel sourceChannel,String request){
        /**
         * 获取所有已经接入的客户端
         */
        Set<SelectionKey> keys = selector.keys();

        keys.forEach(selectionKey -> {
            Channel targetChannel = selectionKey.channel();
            //提出发消息的客户端
            if (targetChannel instanceof  SocketChannel && targetChannel != sourceChannel){
                try {
                    ((SocketChannel) targetChannel).write(Charset.forName("UTF-8").encode(request));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    /**
     * 主方法
     * @param args
     */
    public static void main(String[] args) throws IOException {
        Server server  = new Server();
        server.start();
    }

}
