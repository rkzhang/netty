package com.my.netty.nio.chapter1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClientHandle implements Runnable {

	private String host;
	
	private int port;
	
	private Selector selector;
	
	private SocketChannel socketChannel;
	
	private volatile boolean stop;
	
	public TimeClientHandle(String host, int port) {
		this.host = host == null ? "127.0.0.1" : host;
		this.port = port;
		try {
			//初始化NIO的多路复用器何SocketChannel对象
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			//设置为异步非阻塞模式
			socketChannel.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	@Override
	public void run() {		
		try {
			doConnect();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		while (!stop) {
			try {
				selector.select(1000);
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> it = selectedKeys.iterator();
				SelectionKey key = null;
				
				while(it.hasNext()) {
					//处理就绪的Channel
					key = it.next();
					it.remove();
					try {
						handleInput(key);
					} catch (Exception e) {
						if(key != null) {
							key.cancel();
							if(key.channel() != null) 
								key.channel().close();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		//多路复用器关闭后,所有注册在上面的Channel和Pipe等资源都会被自动去注册并关闭,所以不需要重复释放资源
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
		
		SocketChannel sc = (SocketChannel)key.channel();
		
		//判断是否处于连接状态
		if(key.isConnectable()) {
			//对连接结果进行判断
			if(sc.finishConnect()) {
				//客户端连接成功
				sc.register(selector, SelectionKey.OP_READ);
				doWrite(sc);
			} else {
				//连接失败
				System.exit(1);
			}
			
			if(key.isReadable()) {
				ByteBuffer readBuffer = ByteBuffer.allocate(1024);
				int readBytes = sc.read(readBuffer);
				if(readBytes > 0) {
					readBuffer.flip();
					byte[] bytes = new byte[readBuffer.remaining()];
					readBuffer.get(bytes);
					String body = new String(bytes, "UTF-8");
					System.out.println("Now is " + body);
					this.stop = true;
				} else if (readBytes < 0) {
					//对端链路关闭
					key.cancel();
					sc.close();
				} else 
					; //读到0字节,忽略
				
			}
			
		}
	}

	private void doConnect() throws IOException {
		//对SocketChannel的connect操作进行判断
		if(socketChannel.connect(new InetSocketAddress(host, port))) {
			//如果连接成功,则将SocketChannel注册到多路复用器Selector上, 注册SelectionKey.OP_READ
			socketChannel.register(selector, SelectionKey.OP_READ);
			doWrite(socketChannel);
		} else {
			//如果没有直接连接成功,则说明服务端没有返回TCP握手应答消息,但并不代表连接失败,我们需要将SocketChannel注册到多路复用器 Selector上, 注册SelectionKey.OP_CONNECT
			//当服务器端返回TCP syn-ack消息后,Selector就能够轮询到这个SocketChannel处于连接就绪状态
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
		}
	}

	private void doWrite(SocketChannel sc) throws IOException {
		byte[] req = "QUERY TIME ORDER".getBytes();
		ByteBuffer writeBuffer = ByteBuffer.allocate(req.length);
		writeBuffer.put(req);
		writeBuffer.flip();
		sc.write(writeBuffer);
		if(!writeBuffer.hasRemaining())
			//通过hasRemaining()方法对发送结果进行判断
			System.out.println("Send order 2 server succeed.");
	}

}
