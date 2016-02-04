package shnulaa.fx.nio.base;

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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import shnulaa.fx.config.Config;
import shnulaa.fx.constant.Constant;
import shnulaa.fx.exception.NioException;
import shnulaa.fx.message.MessageOutputImpl;
import shnulaa.fx.nio.clone.ChangeRequest;
import shnulaa.fx.util.Lists;
import shnulaa.fx.util.Maps;
import shnulaa.fx.worker.PipeWorker;

/**
 * NioServerBase
 * 
 * @author liuyq
 *
 */
public abstract class NioSocketHandler implements ISocketHandler, IServer {

	/** the instance of log **/
	private static Logger log = LoggerFactory.getLogger(NioSocketHandler.class);
	protected Selector selector;
	protected Config config;

	protected MessageOutputImpl messageOutputImpl;
	protected ByteBuffer readBuffer = ByteBuffer.allocate(Constant.BUFFER_SIZE);
	private volatile boolean isRunning = true;

	protected Map<SocketChannel, List<ByteBuffer>> pendingData = Maps.newConcurrentMap();

	protected Map<SocketChannel, StringBuffer> historyData = Maps.newConcurrentMap();

	protected List<ChangeRequest> pendingRequests = Lists.newArrayList();

	protected Map<SocketChannel, PipeWorker> pipes = Maps.newConcurrentMap();

	protected abstract Selector initSelector();

	protected abstract boolean progressRequest(ChangeRequest changeRequest) throws IOException;

	protected abstract void progressKey(SelectionKey key) throws IOException;

	protected abstract void stopServer();

	public NioSocketHandler() {
	}

	public NioSocketHandler(MessageOutputImpl output, Config config) {
		this();
		this.config = config;
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

				selector.select();

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

	protected void createWriteBuffer(SocketChannel socketChannel) {
		List<ByteBuffer> queue = Lists.newArrayList();
		Object put;
		put = pendingData.putIfAbsent(socketChannel, queue);
		if (put != null) {
			log.warn("Dup write buffer creation: " + socketChannel);
		}
	}

	protected String decode(ByteBuffer readBuffer, boolean ignoreBr) {
		String packet = new String(decode(readBuffer), Constant.CHARSET);
		return ignoreBr ? packet.replaceAll("\n", StringUtils.EMPTY) : packet;
	}

	protected byte[] decode(ByteBuffer readBuffer) {
		try {
			readBuffer.flip(); // flip the buffer for reading
			byte[] bytes = new byte[readBuffer.remaining()];
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
		isRunning = false;

		for (PipeWorker worker : pipes.values()) {
			worker.close();
		}
		pipes.clear();

		try {
			selector.close();
		} catch (IOException e) {
			log.error("IOException occurred when close the selector..");
		}

		stopServer();
	}

	@Override
	public void send(ChangeRequest request, byte[] data) {
		switch (request.type) {
		case ChangeRequest.CHANGE_SOCKET_OP:
			SocketChannel sc = request.socket;
			List<ByteBuffer> queue = pendingData.get(sc);
			if (queue != null) {
				synchronized (queue) {
					queue.add(ByteBuffer.wrap(data));
				}
			} else {
				log.warn("pendingData is not ready to send..");
			}
			break;
		default:
			log.warn("ChangeRequest operation is not support for send..");
			break;
		}

		synchronized (pendingRequests) {
			pendingRequests.add(request);
		}

		selector.wakeup();
	}

	@Override
	public void send(ChangeRequest request) {

	}

	protected void addHistory(SocketChannel sc, String message) {
		StringBuffer history = historyData.get(sc);
		if (history != null) {
			history.append(message);
		} else {
			log.warn("save history failed..");
		}
	}

	public String getHistory(SocketChannel sc) {
		StringBuffer history = historyData.get(sc);
		return (history != null) ? history.toString() : "No history!!";
	}

}
