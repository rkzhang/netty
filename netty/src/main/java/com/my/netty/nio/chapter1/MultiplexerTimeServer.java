package com.my.netty.nio.chapter1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerTimeServer implements Runnable {

	private Selector selector;
	
	private ServerSocketChannel servChannel;
	
	private volatile boolean stop;
	
	/**
	 * 初始化多路复用器,绑定监听端口
	 * @param port
	 */
	public MultiplexerTimeServer(int port) {
		try {			
			//步骤1,打开ServerSocketChannel, 用于监听客户端的连接, 它是所有客户端连接的父管道
			servChannel = ServerSocketChannel.open();
			
			//步骤2, 绑定监听端口, 设置连接为非阻塞模式
			servChannel.socket().bind(new InetSocketAddress(port), 1024);
			servChannel.configureBlocking(false);
			
			//步骤3, 创建Reactor线程
			selector = Selector.open();
			
			//步骤4, 将ServerSocketChannel注册到Reactor线程的多路复用器Selector上
			servChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			System.out.println("The time server is start in port : " + port);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void stop() {
		this.stop = true;
	}
	
	@Override
	public void run() {
		while(!stop) {			
				try {
					selector.select(1000); //休眠时间为1s, selector每隔1s都被唤醒一次
					Set<SelectionKey> selectedKeys = selector.selectedKeys(); //selector将返回就绪状态的Channel的SelectionKey集合
					Iterator<SelectionKey> it = selectedKeys.iterator();
					SelectionKey key = null;
					
					while(it.hasNext()) {
						key = it.next();
						it.remove();
						try {
							handleInput(key);
						} catch (Exception e) {
							if(key != null) {
								key.cancel();
								if (key.channel() != null) 
									key.channel().close();
							}
						}
					}
					
				} catch (IOException e) {					
					e.printStackTrace();
				}					
		}
		
		//多路复用器关闭后, 所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭, 所以不需要重复释放资源
		if(selector != null) {
			try {
				selector.close();
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
		
	}

	private void handleInput(SelectionKey key) throws IOException {
		if(!key.isValid()) {
			return;
		}
		
		//处理新接入的请求消息
		if(key.isAcceptable()) {
			//Accept the new connection
			ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
			SocketChannel sc = ssc.accept();
			sc.configureBlocking(false);
			//Add the new connection to the selector
			sc.register(selector, SelectionKey.OP_READ);
		}
		
		
		if(key.isReadable()) {
			//Read the data
			SocketChannel sc = (SocketChannel)key.channel();
			ByteBuffer readBuffer = ByteBuffer.allocate(1024);	//开辟1K的缓冲区
			int readBytes = sc.read(readBuffer); //调用SocketChannel的read方法读取请求码流, readBytes为读到的字节数
			
			if (readBytes > 0) {
				//返回值大于0: 读到了字节, 对字节进行编码解码
				readBuffer.flip();
				byte[] bytes = new byte[readBuffer.remaining()];
				readBuffer.get(bytes);
				String body = new String(bytes, "UTF-8");
				System.out.println("The time server receive order : " + body);
				
				String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body) ? new java.util.Date(System.currentTimeMillis()).toString() : "BAD ORDER";
				
				doWrite(sc, currentTime);
			} else if (readBytes < 0) {
				//返回值为-1, 链路已经关闭, 需要关闭SocketChannel,释放资源 .
				key.cancel();
				sc.close();
			} else
				; //读到0字节, 没有读取到字节, 属于正常场景, 忽略
			
		}
	}

	private void doWrite(SocketChannel channel, String response) throws IOException {
		if(response != null && response.trim().length() > 0) {
			byte[] bytes = response.getBytes();
			ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
			writeBuffer.put(bytes);
			writeBuffer.flip();
			channel.write(writeBuffer);
		}
		
	}

}
