package shnulaa.fx.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shnulaa.fx.exception.NioException;

/**
 * 
 * @author liuyq
 *
 */
public class ClonePortServer extends NioServerBase {

	private static Logger log = LoggerFactory.getLogger(NioServerBase.class);

	private int port;

	public ClonePortServer(int port) {
		this.port = port;
	}

	@Override
	protected Selector initSelector() {
		try {
			ServerSocketChannel server = SelectorProvider.provider().openServerSocketChannel();
			server.bind(new InetSocketAddress(port));
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

			break;
		default:
			log.error("");
			break;
		}
		return false;
	}

	@Override
	protected void progressKey(SelectionKey key) throws IOException {
	}

	@Override
	protected void stopServer() {
	}

	@Override
	public void stop() {
	}
}
