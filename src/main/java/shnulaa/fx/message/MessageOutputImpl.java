package shnulaa.fx.message;

import javafx.scene.control.TextArea;
import shnulaa.fx.constant.Constant;

@SuppressWarnings("restriction")
public class MessageOutputImpl {
	private TextArea textArea;

	public MessageOutputImpl(TextArea textArea) {
		this.textArea = textArea;
	}

	public void output(String message) {
		append(message);
	}

	public void output(String message, boolean withSplit) {
		String text = message;
		if (withSplit) {
			text = Constant.SPLIT + Constant.BR + message + Constant.BR + Constant.SPLIT;
		}
		append(text);
	}

	private void append(String text) {
		javafx.application.Platform.runLater(() -> textArea.appendText(text + Constant.BR));
	}

}
