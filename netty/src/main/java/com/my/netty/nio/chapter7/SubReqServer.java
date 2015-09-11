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
		//���÷���˵�NIO�߳���
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
						//������һ���µ�ObjectDecoder,�������ʵ��Serializable��POJO������н���, ���ж�����캯��, ֧�ֲ�ͬ��ClassResolver, �ڴ�����
						//ʹ��weakCachingConcurrentResolver�����̰߳�ȫ��WeakReferenceMap������������л���, ��֧�ֶ��̲߳�������,��������ڴ治��ʱ,
						//���ͷŻ����е��ڴ�, ��ֹ�ڴ�й¶.���ｫ��������������л�����ֽ����鳤������Ϊ1M 
						ch.pipeline().addLast(new ObjectDecoder(1024 * 1024, ClassResolvers.weakCachingConcurrentResolver(this.getClass().getClassLoader())));
						ch.pipeline().addLast(new ObjectEncoder());
						ch.pipeline().addLast(new SubReqServerHandler());
					}
					
				});
			
				//�󶨶˿�,ͬ���ȴ��ɹ�
				ChannelFuture f = b.bind(port).sync();
				
				//�ȴ�����˼����˿ڹر�
				f.channel().closeFuture().sync();
		} finally {
			//�����˳�, �ͷ��̳߳���Դ
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
				//����Ĭ��ֵ
			}
		}
		new SubReqServer().bind(port);
	}

}
