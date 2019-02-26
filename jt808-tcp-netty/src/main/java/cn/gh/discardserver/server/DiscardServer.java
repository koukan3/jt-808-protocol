package cn.gh.discardserver.server;

import cn.gh.discardserver.channel.DiscardServerHandler;
import io.netty.bootstrap.ServerBootstrap;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 丢弃任何进入的数据
 */
public class DiscardServer {

    private int port;

    public DiscardServer(int port) {
        this.port = port;
    }

    public void run() throws Exception {
        /*
        NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器，
        Netty 提供了许多不同的 EventLoopGroup 的实现用来处理不同的传输。
         */
        //boss:用来接收进来的连接。一旦收到连接，就会把连接信息注册到‘worker’上
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        //worker:用来处理已经被接收的连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            //ServerBootstrap 是一个启动 NIO 服务的辅助启动类
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 使用NIO selector来接收新的连接。
                    .childHandler(new ChannelInitializer<SocketChannel>() { // 事件处理类：帮助使用者配置一个新的 Channel。
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DiscardServerHandler());
                        }
                    })  //option() 是提供给NioServerSocketChannel 用来接收进来的连接。
                    .option(ChannelOption.SO_BACKLOG, 128)          // 设置channel的配置参数
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // childOption() 是提供给由父管道 ServerChannel 接收到的连接.

            // 绑定端口，开始接收进来的连接
            ChannelFuture f = b.bind(port).sync(); // (7)

            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        } else {
            port = 8080;
        }
        new DiscardServer(port).run();
    }
}
