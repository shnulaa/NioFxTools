package shnulaa.fx.nio.clone;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shnulaa.fx.config.Config;
import shnulaa.fx.exception.NioException;
import shnulaa.fx.message.MessageOutputImpl;
import shnulaa.fx.nio.base.NioSocketHandler;
import shnulaa.fx.pool.Executor;
import shnulaa.fx.util.Collections3;
import shnulaa.fx.worker.PipeWorker;

/**
 * 
 * @author liuyq
 *
 */
public class LocalSocketHandler extends NioSocketHandler {

	private static Logger log = LoggerFactory.getLogger(LocalSocketHandler.class);

	private ServerSocketChannel server;
	private RemoteSocketHandler remoteServer;
	private final Executor executor;

	public LocalSocketHandler(MessageOutputImpl output, Config config) {
		super(output, config);
		this.executor = Executor.getInstance();
		this.remoteServer = new RemoteSocketHandler(output, config);
		this.executor.execute(this.remoteServer);
	}

	@Override
	protected Selector initSelector() {
		try {
			this.server = SelectorProvider.provider().openServerSocketChannel();
			server.bind(new InetSocketAddress(config.getLocalPort()));
			server.configureBlocking(false);
			Selector selector = Selector.open();
			server.register(selector, SelectionKey.OP_ACCEPT);
			return selector;
		} catch (IOException e) {
			String error = "IOException occurred when init selector..";
			log.error(error, e);
			throw new NioException(error);
		}
	}

	@Override
	protected boolean progressRequest(ChangeRequest changeRequest) throws IOException {
		switch (changeRequest.type) {
		case ChangeRequest.CHANGE_SOCKET_OP:
			SelectionKey key = changeRequest.socket.keyFor(selector);
			key.interestOps(changeRequest.op);
			break;
		case ChangeRequest.CLOSE_CHANNEL:
			cleanUp(changeRequest.socket);
			break;
		default:
			log.error("change request type is not support..");
			break;
		}
		return true;
	}

	@Override
	protected void progressKey(SelectionKey key) throws IOException {
		if (key.isAcceptable()) {
			accept(key);
		} else if (key.isReadable()) {
			read(key);
		} else if (key.isWritable()) {
			write(key);
		} else {
			log.error("key type is not support..");
		}
	}

	private void accept(SelectionKey key) throws IOException {
		log.info("Ready to accept the client with local Clone Server selectKey..");
		ServerSocketChannel server = (ServerSocketChannel) key.channel();
		SocketChannel sc = server.accept();
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ);

		createWriteBuffer(sc);

		// create PipeWorker instance and
		// create socket channel to connect remote
		PipeWorker worker = remoteServer.createPipeWorker(this, sc);
		pipes.put(sc, worker);
		executor.execute(worker);
	}

	private void read(SelectionKey key) throws IOException {
		log.info("Ready to read from client SocketChannel with local Clone Server selectKey..");
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

		pipe.progressEvent(bytes, readCount, true);
	}

	private void write(SelectionKey key) throws IOException {
		log.info("Ready to write into client SocketChannel with local Clone Server selectKey..");
		SocketChannel sc = (SocketChannel) key.channel();
		List<ByteBuffer> queue = pendingData.get(sc);
		if (queue != null) {
			synchronized (queue) {
				while (!queue.isEmpty()) {
					ByteBuffer buffer = Collections3.getFirst(queue);
					sc.write(buffer);
					// log.info(decode(buffer, true));
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

	@Override
	protected void stopServer() {
		if (remoteServer != null) {
			remoteServer.stop();
		}

		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				log.error("IOException occurred when close the server..");
			}
		}

	}

}
