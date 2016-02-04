package shnulaa.fx.nio.listen;

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
import shnulaa.fx.nio.clone.ChangeRequest;
import shnulaa.fx.util.Collections3;
import shnulaa.fx.util.Lists;
import shnulaa.fx.worker.PipeWorker;

/**
 * 
 * @author liuyq
 *
 */
public class ListenSocketHandler extends NioSocketHandler {

	private static Logger log = LoggerFactory.getLogger(ListenSocketHandler.class);
	private ServerSocketChannel server;

	public ListenSocketHandler(MessageOutputImpl output, Config config) {
		super(output, config);
	}

	@Override
	protected Selector initSelector() {
		try {
			server = SelectorProvider.provider().openServerSocketChannel();
			server.bind(new InetSocketAddress(config.getListenPort()));
			server.configureBlocking(false);
			Selector selector = SelectorProvider.provider().openSelector();
			server.register(selector, SelectionKey.OP_ACCEPT);
			return selector;
		} catch (IOException e) {
			throw new NioException("IOException occurred when init the Selector", e);
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
			log.info("progressKey key:{}.", key);
		}
	}

	private void accept(SelectionKey key) {
		SocketChannel sc = null;
		try {
			messageOutputImpl.output("accept key " + key + " successfully..", true);
			log.info("accept key..");
			ServerSocketChannel channel = (ServerSocketChannel) key.channel();
			sc = (SocketChannel) channel.accept();
			sc.configureBlocking(false);
			sc.register(selector, SelectionKey.OP_READ);

			if (!pendingData.containsKey(sc)) {
				pendingData.put(sc, Lists.newArrayList());
				messageOutputImpl.accept(sc);
			}

			if (!historyData.containsKey(sc)) {
				historyData.put(sc, new StringBuffer());
			}

			pipes.put(sc, new PipeWorker(this, null, sc, null, config));
		} catch (IOException ex) {
			log.error("IOException occurred when accept.", ex);
			if (sc != null) {
				cleanUp(sc);
			}
		}
	}

	private void read(SelectionKey key) {
		SocketChannel sc = null;
		try {
			sc = (SocketChannel) key.channel();

			readBuffer.clear();

			int readCount = sc.read(readBuffer);
			if (readCount < 0) {
				cleanUp(sc);
				return;
			}

			String message = decode(readBuffer, false);
			log.info(message);
			messageOutputImpl.output(message);

			addHistory(sc, message);

			sc.register(selector, SelectionKey.OP_WRITE);

		} catch (IOException ex) {
			log.error("IOException occurred when read.", ex);
			if (sc != null) {
				cleanUp(sc);
			}
		}
	}

	private void write(SelectionKey key) {
		SocketChannel sc = null;
		try {
			log.info("Writable key..");
			sc = (SocketChannel) key.channel();

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

		} catch (IOException ex) {
			log.error("IOException occurred when write.", ex);
			if (sc != null) {
				cleanUp(sc);
			}
		}
	}

	@Override
	protected void stopServer() {
		if (server != null) {
			try {
				server.close();
			} catch (IOException e) {
				log.error("IOException occurred when close the ServerSocketChannel..", e);
			}
		}
	}
}
