package shnulaa.fx.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shnulaa.fx.config.Config;
import shnulaa.fx.exception.NioException;
import shnulaa.fx.pool.Executor;

/**
 * 
 * @author liuyq
 *
 */
public class LocalClonePortServer extends NioServerBase {

	private static Logger log = LoggerFactory.getLogger(LocalClonePortServer.class);

	private final Config config;
	private ServerSocketChannel server;
	private RemoteConnectionServer remoteServer;
	private final Executor executor;

	public LocalClonePortServer(Config config) {
		this.config = config;
		this.remoteServer = new RemoteConnectionServer(config);
		this.executor = Executor.getInstance();
	}

	@Override
	protected Selector initSelector() {
		try {
			this.server = SelectorProvider.provider().openServerSocketChannel();
			server.bind(new InetSocketAddress(config.getLocalPort()));
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

	private void accept(SelectionKey key) {
		try {
			ServerSocketChannel server = (ServerSocketChannel) key.channel();
			server.configureBlocking(false);
			SocketChannel sc = server.accept();
			sc.register(selector, SelectionKey.OP_READ);
			// create PipeWorker instance and
			// create socket channel to connect remote
			PipeWorker worker = remoteServer.createPipeWorker(this, sc);
			executor.execute(worker);
		} catch (IOException e) {
			log.error("IOException occurred when accept the SocketChannel", e);
		}
	}

	private void read(SelectionKey key) {
		try {
			SocketChannel sc = (SocketChannel) key.channel();

			int read = sc.read(readBuffer);
			if (read <= 0) {
				log.warn("");
				cleanUp(sc);
				return;
			}

			byte[] bytes = decode(readBuffer);
			if (bytes == null) {
				log.error("");
				cleanUp(sc);
				return;
			}

		} catch (IOException e) {
			log.error("IOException occurred when read data from local..", e);
		}

	}

	private void write(SelectionKey key) {

	}

	@Override
	protected void stopServer() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void send(ChangeRequest request, byte[] data) {

	}

	@Override
	public void send(ChangeRequest request) {

	}
}
