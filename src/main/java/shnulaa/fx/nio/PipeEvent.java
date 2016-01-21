package shnulaa.fx.nio;

import java.io.Serializable;

/**
 * 
 * @author liuyq
 *
 */
public class PipeEvent implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -408680655594266957L;

	private transient byte[] bytes;

	public PipeEvent(byte[] bytes) {
		this.setBytes(bytes);
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

}
