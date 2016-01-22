package shnulaa.fx.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shnulaa.fx.config.Config;
import shnulaa.fx.exception.NioException;

/**
 * 
 * @author liuyq
 *
 */
public class RemoteConnectionServer extends NioServerBase {
	private static Logger log = LoggerFactory.getLogger(RemoteConnectionServer.class);
	private final Config config;

	public RemoteConnectionServer(Config config) {
		this.config = config;
	}

	@Override
	public void send(ChangeRequest request, byte[] data) {

	}

	@Override
	public void send(ChangeRequest request) {
	}

	@Override
	protected Selector initSelector() {
		return null;
	}

	@Override
	protected boolean progressRequest(ChangeRequest changeRequest) throws IOException {
		return false;
	}

	@Override
	protected void progressKey(SelectionKey key) throws IOException {
	}

	@Override
	protected void stopServer() {
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
			PipeWorker pipeWorker = new PipeWorker(localHandler, this, localChannel, sc, config);
			return pipeWorker;
		} catch (IOException e) {
			String error = "IOException occurred when createPipeWorker..";
			log.error(error, e);
			throw new NioException(error, e);
		}
	}

}
