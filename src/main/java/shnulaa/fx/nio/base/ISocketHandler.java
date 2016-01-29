package shnulaa.fx.nio.base;

import shnulaa.fx.nio.clone.ChangeRequest;

/**
 * 
 * @author liuyq
 *
 */
public interface ISocketHandler {

	void send(ChangeRequest request, byte[] data);

	void send(ChangeRequest request);

}
