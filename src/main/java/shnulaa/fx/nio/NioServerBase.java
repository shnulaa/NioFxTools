package shnulaa.fx.nio;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import shnulaa.fx.constant.Constant;
import shnulaa.fx.exception.NioException;
import shnulaa.fx.message.MessageOutputImpl;

/**
 * NioServerBase
 * 
 * @author liuyq
 *
 */
public abstract class NioServerBase implements ISocketHandler, IServer {

	/** the instance of log **/
	private static Logger log = LoggerFactory.getLogger(NioServerBase.class);
	protected int port;
	protected Selector selector;

	protected MessageOutputImpl messageOutputImpl;
	protected ByteBuffer readBuffer = ByteBuffer.allocate(Constant.BUFFER_SIZE);
	private volatile boolean isRunning = true;

	private Map<SocketChannel, Queue<PipeEvent>> pendingData = Maps.newConcurrentMap();

	private List<ChangeRequest> pendingRequests = Lists.newArrayList();

	protected abstract Selector initSelector();

	protected abstract boolean progressRequest(ChangeRequest changeRequest) throws IOException;

	protected abstract void progressKey(SelectionKey key) throws IOException;

	protected abstract void stopServer();

	public NioServerBase() {
	}

	public NioServerBase(MessageOutputImpl output, int port) {
		this();
		this.port = port;
		this.messageOutputImpl = output;
		this.selector = initSelector();
	}

	@Override
	public void run() {
		Thread t = Thread.currentThread();
		while (!t.isInterrupted() && isRunning) {
			try {
				// handle change request list
				synchronized (pendingRequests) {
					Iterator<ChangeRequest> i = pendingRequests.iterator();
					while (i.hasNext()) {
						ChangeRequest request = (ChangeRequest) i.next();
						if (!progressRequest(request)) {
							break;
						}
						i.remove();
					}
				}

				int r = selector.select();
				if (r <= 0) {
					log.warn("selector.select() ret <= 0");
					continue;
				}

				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
					if (!key.isValid()) {
						log.warn("key is not valid..");
						continue;
					}

					try {
						progressKey(key);
					} catch (IOException ex) {
						key.cancel();
						log.error("IOException occurred when progress the selectKey..", ex);
					}
				}
			} catch (ClosedChannelException ex) {
				log.error("ClosedChannelException occurred when Accept, Read, Write, break..", ex);
				break;
			} catch (ClosedSelectorException ex) {
				log.error("ClosedSelectorException occurred when Accept, Read, Write, break..", ex);
				break;
			} catch (Exception e) {
				log.error("Exception occurred when Accept, Read, Write..", e);
			}
		}
	}

	protected void outPut(String message) {
		this.messageOutputImpl.output(message);
	}

	protected void outPut(String message, boolean withSplit) {
		this.messageOutputImpl.output(message, withSplit);
	}

	protected String decode(ByteBuffer readBuffer, boolean ignoreBr) {
		try {
			readBuffer.flip(); // flip the buffer for reading
			byte[] bytes = new byte[readBuffer.remaining()]; // create a byte
																// array
																// the length of
																// the
																// number of
																// bytes
																// written to
																// the
																// buffer
			readBuffer.get(bytes); // read the bytes that were written
			String packet = new String(bytes, Constant.CHARSET);
			return ignoreBr ? packet.replaceAll("\n", StringUtils.EMPTY) : packet;
		} catch (Exception ex) {
			log.error("decode ByteBuffer error..", ex);
			throw new NioException("decode ByteBuffer error..", ex);
		}
	}

	protected byte[] decode(ByteBuffer readBuffer) {
		try {
			readBuffer.flip(); // flip the buffer for reading
			byte[] bytes = new byte[readBuffer.remaining()]; // create a byte
																// array
																// the length of
																// the
																// number of
																// bytes
																// written to
																// the
																// buffer
			readBuffer.get(bytes); // read the bytes that were written
			return bytes;
		} catch (Exception ex) {
			log.error("decode ByteBuffer error..", ex);
			throw new NioException("decode ByteBuffer error..", ex);
		}
	}

	protected void cleanUp(SocketChannel socketChannel) {
		try {
			socketChannel.close();
		} catch (IOException e) {
			log.info("IOException occurred when close socketChannel.", e);
		}
		SelectionKey key = socketChannel.keyFor(selector);
		if (key != null) {
			key.cancel();
		}

		if (pendingData.containsKey(socketChannel)) {
			pendingData.remove(socketChannel);
		}
	}

	@Override
	public void stop() {

	}

}
