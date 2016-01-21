package shnulaa.fx.exception;

/**
 * 
 * @author liuyq
 *
 */
public class NioException extends RuntimeException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -3474151484165173122L;

	public NioException() {
	}

	public NioException(String error) {
		super(error);
	}

	public NioException(String error, Exception ex) {
		super(error, ex);
	}
}
