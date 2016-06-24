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

	public Config(int listenPort, boolean bindShell) {
		this.listenPort = listenPort;
		this.setBindShell(bindShell);
	}

	private String localIp;
	private int localPort;

	private String remoteIp;
	private int remotePort;

	private int listenPort;
	private boolean bindShell;

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

	public int getListenPort() {
		return listenPort;
	}

	public void setListenPort(int listenPort) {
		this.listenPort = listenPort;
	}

	public boolean isBindShell() {
		return bindShell;
	}

	public void setBindShell(boolean bindShell) {
		this.bindShell = bindShell;
	}

}
