package com.my.netty.nio.chapter3;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TimeServer {

	public void bind(int port) throws InterruptedException {
		// 配置服务段的NIO线程组, NioEventLoopGroup是个线程租,它包含了一组NIO线程, 专门用于网络事件的处理,
		// 实际上它们就是Reactor线程组.这里创建两个的原因是一个用于服务端接受客户端的连接,另一个用于进行 SocketChannel的网络读写
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			//ServerBootstrap是Netty用于启动NIO服务端的辅助启动类, 目的是降低服务端的开发复杂度
			ServerBootstrap b = new ServerBootstrap();
			
			b.group(bossGroup, workerGroup)
				//设置创建的Channel为NioServerSocketChannel,它的功能对应于JDK NIO类库中的ServerSocketChannel类.
				.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 1024)
					//绑定I/O事件的处理类ChildChannelHandler
					.childHandler(new ChildChannelHandler());
		
			//绑定端口, 同步等待成功
			ChannelFuture f = b.bind(port).sync();
			
			//等待服务端监听端口关闭
			f.channel().closeFuture().sync();
			System.out.println("f.channel().closeFuture().sync()");
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			System.out.println("finally");
		}
	}

	/**
	 * 作用类似于Reactor模式中的handler类,主要用于处理网络I/O事件, 例如记录日志,对消息进行编码解码等.
	 * @author Administrator
	 *
	 */
	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel arg) throws Exception {
			arg.pipeline().addLast(new TimeServerHandler());

		}

	}
	
	public static void main(String[] args) throws InterruptedException {
		System.out.println("begin");
		int port = 8080;
		if(args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				
			}
			
		}

		new TimeServer().bind(port);
	}
}
