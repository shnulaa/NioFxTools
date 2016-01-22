package shnulaa.fx.nio;

import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Queues;

import shnulaa.fx.config.Config;

/**
 * 
 * @author liuyq
 *
 */
public class PipeWorker implements Runnable {
	private static Logger log = LoggerFactory.getLogger(LocalClonePortServer.class);
	private ISocketHandler localHandler;
	private ISocketHandler remoteHandler;

	private SocketChannel localChannel;
	private SocketChannel remoteChannel;

	private BlockingQueue<PipeEvent> progressQueue;

	@SuppressWarnings("unused")
	private Config config;

	private volatile boolean requestClose = false;

	public PipeWorker(ISocketHandler localHandler, ISocketHandler remoteHandler, SocketChannel localChannel,
			SocketChannel remoteChannel, Config config) {
		this.localHandler = localHandler;
		this.remoteHandler = remoteHandler;

		this.localChannel = localChannel;
		this.remoteChannel = remoteChannel;

		this.config = config;
		this.progressQueue = Queues.newLinkedBlockingQueue();
	}

	public PipeWorker() {
	}

	public void progressEvent(byte[] bytes, boolean isSendRemote) {
		PipeEvent event = (bytes == null) ? new PipeEvent() : new PipeEvent(bytes, isSendRemote);
		progressQueue.add(event);

	}

	@Override
	public void run() {
		final Thread t = Thread.currentThread();
		while (!t.isInterrupted()) {
			if (progressQueue.isEmpty() && requestClose) {
				log.info("request close command is reached, shutdown the channel..");
				if (localChannel.isOpen()) {
					localHandler.send(new ChangeRequest(localChannel, ChangeRequest.CLOSE_CHANNEL));
				}
				if (remoteChannel.isOpen()) {
					remoteHandler.send(new ChangeRequest(remoteChannel, ChangeRequest.CLOSE_CHANNEL));
				}
				break;
			}

			try {
				final PipeEvent event = progressQueue.take();
				log.debug("catch pipe event successfully.");
				byte[] bytes = event.getBytes();
				if (bytes == null) {
					log.warn("bytes is null from the event..");
					continue;
				}

				final boolean isSendRemote = event.isSendRemote();
				ISocketHandler handler = (isSendRemote) ? localHandler : remoteHandler;
				SocketChannel channel = (isSendRemote) ? localChannel : remoteChannel;

				handler.send(new ChangeRequest(channel, ChangeRequest.CHANGE_SOCKET_OP), bytes);
			} catch (InterruptedException e) {
				log.error("InterruptedException is occurred when pipe the local and remote..", e);
			} catch (Exception e) {
				log.error("unknown Exception is occurred when pipe the local and remote, break..", e);
				break;
			}
		}

	}
}
