package shnulaa.fx.nio.listen;

import java.io.IOException;
import java.nio.channels.AcceptPendingException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shnulaa.fx.exception.NioException;
import shnulaa.fx.nio.base.NioSocketHandler;
import shnulaa.fx.nio.clone.ChangeRequest;

public class ListenSocketHandler extends NioSocketHandler {

	private static Logger log = LoggerFactory.getLogger(ListenSocketHandler.class);
	private ServerSocketChannel server;

	@Override
	protected Selector initSelector() {
		try {
			server = SelectorProvider.provider().openServerSocketChannel();
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
		return false;
	}

	@Override
	protected void progressKey(SelectionKey key) throws IOException {
		if (key.isAcceptable()) {
			log.info("");
		} else if (key.isReadable()) {

		} else if (key.isWritable()) {

		}
	}

	private void accept(SelectionKey key)  {
		try {
			ServerSocketChannel channel = (ServerSocketChannel) key.channel();
			channel.accept();
			
			
			
		} catch (IOException ex) {
			
		}


	}

	private void read(SelectionKey key) {

	}

	@Override
	protected void stopServer() {
		// TODO Auto-generated method stub

	}

}
