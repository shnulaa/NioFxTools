package shnulaa.fx.constant;

import java.nio.charset.Charset;

/**
 * 
 * @author liuyq
 *
 */
public class Constant {
	public static final String BR = System.getProperty("line.separator");
	public static final String LOGIN_SUCCESS = "login successfully.." + BR + ">>  ";
	public static final String CHAT = ">>  ";
	public static final int BUFFER_SIZE = 163840;
	public static final String SPLIT = "=====================";
	public static final String SPLIT2 = "---------------------------------------------------------";
	public static final Charset CHARSET = Charset.forName("gb2312");
	public static final String TITLE = "Nio Fx Demo";
	public static final int DEFAULT_PORT = 1234;
	public static final String DEFAULT_HOST = "127.0.0.1";

	public static final String PROPMT = SPLIT2 + BR + "             Welcome to Nio demo Service                   " + BR
			+ SPLIT2 + BR + "Please input name to login:";
}
