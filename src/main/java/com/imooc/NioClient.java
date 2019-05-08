package com.imooc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * @ClassName: NioClient
 * @Author: ren
 * @Description:
 * @CreateTIme: 2019/5/5 0005 上午 9:45
 **/
public class NioClient {


    public static void main(String[] args) throws IOException {
        new NioClient().start();
    }

    /**
     * 启动
     */
    public void start() throws IOException {

        /**
         *  连接到服务器
         */
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 8000));

        /**
         *  接收服务器端响应
         */
        // 新开线程，专门接收服务器响应
        Selector selector = Selector.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
        new Thread(new NioClientHandler(selector)).start();


        /**
         *  向服务器发送数据
         */
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String request = scanner.nextLine();
            if (request != null && request.length() > 0) {
                socketChannel.write(Charset.forName("UTF-8").encode(request));
            }
        }

    }

}
