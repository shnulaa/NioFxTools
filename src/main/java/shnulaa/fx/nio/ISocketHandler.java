package shnulaa.fx.nio;

/**
 * 
 * @author liuyq
 *
 */
public interface ISocketHandler {

	void send(ChangeRequest request, byte[] data);

	void send(ChangeRequest request);

}
