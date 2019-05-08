package com.imooc;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * @ClassName: NioClientHandler
 * @Author: ren
 * @Description:
 * @CreateTIme: 2019/5/5 0005 上午 11:26
 **/
public class NioClientHandler implements Runnable {
    private Selector selector;

    public NioClientHandler(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {

        try {


            for (; ; ) {

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
                     *  处理可读事件
                     */
                    if (selectionKey.isReadable()) {
                        readHandler(selectionKey, selector);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
         *  循环读取服务器端请求数据
         */

        String reponse = "";
        while (socketChannel.read(byteBuffer) > 0) {
            /**
             *  切换为读模式
             */

            byteBuffer.flip();

            /**
             *  读取 buffer 中的内容
             */
            reponse += Charset.forName("UTF-8").decode(byteBuffer);
        }


        /**
         *  将Channel再次注册到 selector 上 ，监听可读事件
         */
        socketChannel.register(selector, SelectionKey.OP_READ);

        /**
         *  将客户端接收到的请求信息，广播给其他客户端
         */
        if (reponse.length() > 0) {
            // 广播到其他客户端
            System.out.println(reponse);
        }

    }
}
