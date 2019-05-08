package com.imooc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @ClassName: NioServer
 * @Author: ren
 * @Description:
 * @CreateTIme: 2019/5/5 0005 上午 9:45
 **/
public class NioServer {

    public static void main(String[] args) throws IOException {
        NioServer nioServer = new NioServer();
        nioServer.start();
    }


    /**
     * 启动服务器
     */
    public void start() throws IOException {

        /**
         * 1. 创建Selector
         */
        Selector selector = Selector.open();

        /**
         *  2. 通过ServerSocketChannel 创建 Channel
         */
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        /**
         *  3. 为通道绑定监听端口
         */
        serverSocketChannel.bind(new InetSocketAddress(8000));
        /**
         *  4. 设置Channel 为非阻塞模式
         */
        serverSocketChannel.configureBlocking(false);
        /**
         *  5. 将Channel 注册到Selector上
         */
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务器启动..................................");
        /**
         *  6. 循环等待新接入连接
         */

        for (; ; ) {
            /**
             *  获取可用的Channel 个数
             */
            int readyChannel = selector.select();

            if (readyChannel == 0) {
                continue;
            }

            /**
             *  获取可用channel
             */
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();

            Iterator<SelectionKey> iterator = selectionKeySet.iterator();

            while (iterator.hasNext()) {

                /**
                 *  获取 selectionKey 实例
                 */
                SelectionKey selectionKey = iterator.next();

                /**
                 *  移除已处理实例
                 */
                iterator.remove();

                /**
                 * 7. 根据对应状态调用对应方法处理的业务逻辑
                 */

                /**
                 *  接入事件处理器
                 */
                if (selectionKey.isAcceptable()) {
                    acceptHandler(serverSocketChannel, selector);
                }
                /**
                 *  处理可读事件
                 */
                if (selectionKey.isReadable()) {
                    readHandler(selectionKey, selector);
                }

            }
        }

    }

    /**
     * 接入事件处理器
     */
    private void acceptHandler(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {

        /**
         *  如果是接入事件，创建SocketChannel
         */
        SocketChannel socketChannel = serverSocketChannel.accept();
        /**
         *  将 socketChannel 设置为 非阻塞 工作模式
         */
        socketChannel.configureBlocking(false);

        /**
         *  将Channel 注册到selector 上，监听可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);
        /**
         *  回复客户端
         */
        socketChannel.write(Charset.forName("UTF-8").encode("欢迎接入聊天室"));
    }


    /**
     * 可读事件处理器
     */
    private void readHandler(SelectionKey selectionKey, Selector selector) throws IOException {


        /**
         *  要从可读事件中，获取已经就绪的Channel
         */
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        /**
         * 创建Buffer
         */
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        /**
         *  循环读取客户端的请求信息
         */

        String request = "";
        while (socketChannel.read(byteBuffer) > 0) {
            /**
             *  切换为读模式
             */

            byteBuffer.flip();

            /**
             *  读取 buffer 中的内容
             */
            request += Charset.forName("UTF-8").decode(byteBuffer);
        }


        /**
         *  将Channel再次注册到 selector 上 ，监听可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);

        /**
         *  将客户端接收到的请求信息，广播给其他客户端
         */
        if (request.length() > 0) {
            // 广播到其他客户端
            System.out.println("接收数据：" + request);

            // 写法1
            broadCast(selector, socketChannel, request);

            // 写法2
//            writeHandler(selector, request);
        }

    }

    private void broadCast(Selector selector, SocketChannel sourceChannel, String msg) {

        Set<SelectionKey> selectionKeySet = selector.keys();


        selectionKeySet.forEach(selectionKey -> {
            Channel targetChannel = selectionKey.channel();

            if (targetChannel instanceof SocketChannel && targetChannel != sourceChannel) {
                try {
                    ((SocketChannel) targetChannel).write(Charset.forName("UTF-8").encode(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
//
//    private void writeHandler(Selector selector, String msg) throws IOException {
//
//        selector.keys().forEach(selectionKey -> {
//
//
//            Channel channel =  selectionKey.channel();
//
//            if(channel instanceof SocketChannel){
////
////                /**
////                 *  将Channel再次注册到 selector 上 ，监听写事件
////                 */
//////        socketChannel.register(selector, SelectionKey.OP_WRITE);
////                /**
////                 * 创建Buffer
////                 */
////                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
////                byteBuffer.put(Charset.forName("UTF-8").encode(msg));
//
//                /**
//                 *  推送数据
//                 */
//                try {
//                    ((SocketChannel) channel).write(Charset.forName("UTF-8").encode(msg));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        });
//
//    }


}
