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
		// ���÷���ε�NIO�߳���, NioEventLoopGroup�Ǹ��߳���,��������һ��NIO�߳�, ר�����������¼��Ĵ���,
		// ʵ�������Ǿ���Reactor�߳���.���ﴴ��������ԭ����һ�����ڷ���˽��ܿͻ��˵�����,��һ�����ڽ��� SocketChannel�������д
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			//ServerBootstrap��Netty��������NIO����˵ĸ���������, Ŀ���ǽ��ͷ���˵Ŀ������Ӷ�
			ServerBootstrap b = new ServerBootstrap();
			
			b.group(bossGroup, workerGroup)
				//���ô�����ChannelΪNioServerSocketChannel,���Ĺ��ܶ�Ӧ��JDK NIO����е�ServerSocketChannel��.
				.channel(NioServerSocketChannel.class)
					.option(ChannelOption.SO_BACKLOG, 1024)
					//��I/O�¼��Ĵ�����ChildChannelHandler
					.childHandler(new ChildChannelHandler());
		
			//�󶨶˿�, ͬ���ȴ��ɹ�
			ChannelFuture f = b.bind(port).sync();
			
			//�ȴ�����˼����˿ڹر�
			f.channel().closeFuture().sync();
			System.out.println("f.channel().closeFuture().sync()");
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			System.out.println("finally");
		}
	}

	/**
	 * ����������Reactorģʽ�е�handler��,��Ҫ���ڴ�������I/O�¼�, �����¼��־,����Ϣ���б�������.
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
