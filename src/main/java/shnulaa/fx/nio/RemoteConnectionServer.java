package shnulaa.fx.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shnulaa.fx.config.Config;
import shnulaa.fx.exception.NioException;
import shnulaa.fx.message.MessageOutputImpl;
import shnulaa.fx.util.Collections3;

/**
 * 
 * @author liuyq
 *
 */
public class RemoteConnectionServer extends NioServerBase {
	private static Logger log = LoggerFactory.getLogger(RemoteConnectionServer.class);

	public RemoteConnectionServer(MessageOutputImpl output, Config config) {
		super(output, config);
	}

	@Override
	protected Selector initSelector() {
		try {
			return SelectorProvider.provider().openSelector();
		} catch (IOException e) {
			throw new NioException("IOException occurred when initSelector..", e);
		}
	}

	@Override
	protected boolean progressRequest(ChangeRequest request) throws IOException {
		SocketChannel sc = request.socket;

		switch (request.type) {
		case ChangeRequest.REGISTER_CHANNEL:
			sc.register(selector, request.op);

			break;
		case ChangeRequest.CHANGE_SOCKET_OP:
			SelectionKey key = sc.keyFor(selector);
			if (key != null && key.isValid()) {
				key.interestOps(request.op);
			} else {
				log.warn("the channel:{} is not currently registered with that selector:{}..", sc, selector);
			}
			break;
		case ChangeRequest.CLOSE_CHANNEL:
			log.info("the CLOSE_CHANNEL request is reached, clean up the channel..");
			cleanUp(sc);
			break;
		default:
			log.error("ChangeRequest type is not support..");
			break;
		}
		return true;
	}

	@Override
	protected void progressKey(SelectionKey key) throws IOException {
		if (key.isConnectable()) {
			finishConnection(key);
		} else if (key.isReadable()) {
			read(key);
		} else if (key.isWritable()) {
			write(key);
		} else {
			log.warn("key type is not support..");
		}
	}

	private void finishConnection(SelectionKey key) {
		log.info("Ready to finsish the remote connection with remote selectKey..");
		SocketChannel sc = (SocketChannel) key.channel();
		try {
			sc.finishConnect();
		} catch (IOException e) {
			log.error("IOException occurred when finish connection of SocketChannel..", e);
			cleanUp(sc);
			return;
		}

		key.interestOps(SelectionKey.OP_WRITE);
	}

	private void read(SelectionKey key) throws IOException {
		log.info("Ready to read from the remote connection with remote selectKey..");
		SocketChannel sc = (SocketChannel) key.channel();

		PipeWorker pipe = pipes.get(sc);
		if (pipe == null) {
			log.error("fetch null from pipes map..");
			cleanUp(sc);
			return;
		}

		// clear read buffer for new data
		readBuffer.clear();

		int readCount = sc.read(readBuffer);
		if (readCount == -1) {
			log.warn("read from channel result less than 0..");
			cleanUp(sc);
			return;
		}

		byte[] bytes = decode(readBuffer);
		if (bytes == null || bytes.length <= 0) {
			log.error("decode the BytesBuffer result is incorrect..");
			cleanUp(sc);
			return;
		}

		pipe.progressEvent(bytes, readCount, false);
	}

	private void write(SelectionKey key) throws IOException {
		log.info("Ready to write into the remote connection with remote selectKey..");
		SocketChannel sc = (SocketChannel) key.channel();
		List<ByteBuffer> queue = pendingData.get(sc);
		if (queue != null) {
			synchronized (queue) {
				while (!queue.isEmpty()) {
					ByteBuffer buffer = Collections3.getFirst(queue);
					sc.write(buffer);
					if (buffer.hasRemaining()) {
						break;
					}
					queue.remove(buffer);
				}
				if (queue.isEmpty()) {
					key.interestOps(SelectionKey.OP_READ);
				}
			}
		} else {
			log.warn("queue is null while write the ByteBuffer to channel..");
		}

	}

	/**
	 * 
	 * @param localHandler
	 * @param localChannel
	 * @return
	 */
	public PipeWorker createPipeWorker(ISocketHandler localHandler, SocketChannel localChannel) {
		SocketChannel sc;
		try {
			sc = SocketChannel.open();
			sc.configureBlocking(false);
			sc.connect(new InetSocketAddress(config.getRemoteIp(), config.getRemotePort()));

			createWriteBuffer(sc);

			PipeWorker pipeWorker = new PipeWorker(localHandler, this, localChannel, sc, config);

			pipes.put(sc, pipeWorker);

			synchronized (pendingRequests) {
				pendingRequests.add(new ChangeRequest(sc, ChangeRequest.REGISTER_CHANNEL, SelectionKey.OP_CONNECT));
			}
			selector.wakeup();

			return pipeWorker;
		} catch (IOException e) {
			String error = "IOException occurred when createPipeWorker..";
			log.error(error, e);
			throw new NioException(error, e);
		}
	}

	@Override
	protected void stopServer() {
	}

}
