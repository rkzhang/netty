package com.my.netty.nio.chapter4;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

public class TimeServer {

	public void bind(int port) throws Exception {
		//���÷���˵�NIO�߳���
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_BACKLOG, 1024)
				.childHandler(new ChildChannelHandler());
			
			//�󶨶˿�,ͬ���ȴ��ɹ�
			ChannelFuture f = b.bind(port).sync();
			
			System.out.println("Service has started ...");
			//�ȴ�����˼����˿ڹر�
			f.channel().closeFuture().sync();
		} finally {
			//�����˳�, �ͷ��̳߳���Դ
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			System.out.println("Service has closed ...");
		}
	}
	
	
	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel arg) throws Exception {
			arg.pipeline().addLast(new LineBasedFrameDecoder(1024));
			arg.pipeline().addLast(new StringDecoder());
			arg.pipeline().addLast(new TimeServerHandler());
		}
		
	}
	
	public static void main(String[] args) throws Exception {
		int port = 8080;
		if(args != null && args.length > 0) {
			try {
				port = Integer.valueOf(args[0]);
			} catch (NumberFormatException e) {
				//����Ĭ��ֵ
			}		
		}
		
		new TimeServer().bind(port);
	}

}
