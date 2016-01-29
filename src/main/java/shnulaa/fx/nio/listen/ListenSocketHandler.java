package shnulaa.fx.nio.listen;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import shnulaa.fx.nio.base.NioSocketHandler;
import shnulaa.fx.nio.clone.ChangeRequest;

public class ListenSocketHandler extends NioSocketHandler {

	@Override
	protected Selector initSelector() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean progressRequest(ChangeRequest changeRequest) throws IOException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void progressKey(SelectionKey key) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void stopServer() {
		// TODO Auto-generated method stub

	}

}
