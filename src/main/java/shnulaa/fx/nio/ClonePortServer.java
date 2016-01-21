package shnulaa.fx.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

/**
 * 
 * @author liuyq
 *
 */
public class ClonePortServer extends NioServerBase {

	@Override
	protected Selector initSelector() {
		return null;
	}

	@Override
	protected boolean progressRequest(ChangeRequest changeRequest) throws IOException {
		return false;
	}

	@Override
	protected void progressKey(SelectionKey key) throws IOException {
	}

	@Override
	protected void stopServer() {
	}

	@Override
	public void stop() {
	}
}
