package shnulaa.fx.nio.base;

import java.nio.channels.SocketChannel;

import shnulaa.fx.nio.clone.ChangeRequest;

/**
 * 
 * @author liuyq
 *
 */
public interface ISocketHandler {

	void send(ChangeRequest request, byte[] data);

	void send(ChangeRequest request);

	String getHistory(SocketChannel sc);

}
