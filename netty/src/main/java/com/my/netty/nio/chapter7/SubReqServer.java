package com.my.netty.nio.chapter7;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SubReqServer {

	public void bind(int port) throws InterruptedException {
		//配置服务端的NIO线程组
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 100)
				.handler(new LoggingHandler(LogLevel.INFO))
				.childHandler(new ChannelInitializer<SocketChannel>() {
	
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						//创建了一格新的ObjectDecoder,它负责对实现Serializable的POJO对象进行解码, 它有多个构造函数, 支持不同的ClassResolver, 在此我们
						//使用weakCachingConcurrentResolver创建线程安全的WeakReferenceMap对类加载器进行缓存, 它支持多线程并发访问,当虚拟机内存不足时,
						//会释放缓存中的内存, 防止内存泄露.这里将单个对象最大序列化后的字节数组长度设置为1M 
						ch.pipeline().addLast(new ObjectDecoder(1024 * 1024, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
						ch.pipeline().addLast(new ObjectEncoder());
						ch.pipeline().addLast(new SubReqServerHandler());
					}
					
				});
			
				//绑定端口,同步等待成功
				ChannelFuture f = b.bind(port).sync();
				
				//等待服务端监听端口关闭
				f.channel().closeFuture().sync();
		} finally {
			//优雅退出, 释放线程池资源
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception {
		int port = 8080;
		if (args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				//采用默认值
			}
		}
		new SubReqServer().bind(port);
	}

}
