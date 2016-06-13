package com.my.netty.nio.chapter7;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class SubReqClient {
	
	public void connect(int port, String host) throws Exception {
		//���ÿͻ���NIO�߳���
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
	
					@Override
					protected void initChannel(SocketChannel ch) throws Exception {
						//���ǽ�ֹ������������л��棬���ڻ���osgi�Ķ�̬ģ�黯����о���ʹ�á�����OSGI��bundle���Խ����Ȳ����������������ĳ��bundle����������Ӧ���������
						//Ҳ��һ������������ڶ�̬ģ�黯��̹����У������ٶ��ݼ��������л��棬��Ϊ����ʱ���ܻᷢ���仯��
						ch.pipeline().addLast(new ObjectDecoder(1024, ClassResolvers.cacheDisabled(this.getClass().getClassLoader())));
						ch.pipeline().addLast(new ObjectEncoder());
						ch.pipeline().addLast(new SubReqClientHandler());
					}				
				});
			
			//�����첽���Ӳ���
			ChannelFuture f = b.connect(host, port).sync();
			
			//�ȴ��ͻ�����·�ر�
			f.channel().closeFuture().sync();
		} finally {
			//�����˳�,�ͷ�NIO�߳���
			group.shutdownGracefully();
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
		new SubReqClient().connect(port, "127.0.0.1");
	}

}
