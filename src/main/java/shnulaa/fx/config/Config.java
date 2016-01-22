package shnulaa.fx.config;

import java.io.Serializable;

public class Config implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 3904180412790846756L;

	/**
	 * 
	 */
	public Config() {
	}

	/**
	 * 
	 * @param localIp
	 * @param localPort
	 * @param cloneIp
	 * @param clonePort
	 */
	public Config(String localIp, int localPort, String cloneIp, int clonePort) {
		this.localIp = localIp;
		this.localPort = localPort;
		this.remoteIp = cloneIp;
		this.remotePort = clonePort;
	}

	private String localIp;
	private int localPort;

	private String remoteIp;
	private int remotePort;

	public String getLocalIp() {
		return localIp;
	}

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public String getRemoteIp() {
		return remoteIp;
	}

	public void setRemoteIp(String remoteIp) {
		this.remoteIp = remoteIp;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

}