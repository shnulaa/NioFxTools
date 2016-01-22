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
	private boolean isSendRemote;

	public PipeEvent() {
	}

	public PipeEvent(byte[] bytes, boolean isSendRemote) {
		this.setBytes(bytes);
		this.setSendRemote(isSendRemote);
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public boolean isSendRemote() {
		return isSendRemote;
	}

	public void setSendRemote(boolean isSendRemote) {
		this.isSendRemote = isSendRemote;
	}

}
