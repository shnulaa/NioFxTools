package shnulaa.fx.message;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import shnulaa.fx.constant.Constant;
import shnulaa.fx.nio.base.ISocketHandler;
import shnulaa.fx.nio.clone.ChangeRequest;

@SuppressWarnings("restriction")
public class MessageOutputImpl {
	private TextArea textArea;
	private ChoiceBox<KeyValuePair> channelBox;
	private ISocketHandler iSocketHandler;

	public void setHandler(ISocketHandler iSocketHandler) {
		this.iSocketHandler = iSocketHandler;
	}

	public MessageOutputImpl(TextArea textArea) {
		this.textArea = textArea;
		this.textArea.setOnKeyPressed(new EventHandler<KeyEvent>() {
			final StringBuffer tmp = new StringBuffer();

			@Override
			public void handle(KeyEvent ke) {
				tmp.append(ke.getText());
				if (ke.getCode().equals(KeyCode.ENTER)) {
					KeyValuePair selected = channelBox.getSelectionModel().getSelectedItem();
					if (selected != null && iSocketHandler != null) {
						iSocketHandler.send(new ChangeRequest(selected.getKey(), ChangeRequest.CHANGE_SOCKET_OP,
								SelectionKey.OP_WRITE), tmp.toString().getBytes());
					}
					tmp.setLength(0);
				}
			}
		});

		this.textArea.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// textArea.setPrefSize(500d, 500d);
			}
		});

	}

	public MessageOutputImpl(TextArea textArea, ChoiceBox<KeyValuePair> channelBox) {
		this(textArea);
		this.channelBox = channelBox;

		this.channelBox.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
				KeyValuePair item = channelBox.getItems().get((Integer) number2);
				SocketChannel sc = item.getKey();
				if (sc != null && sc.isOpen() && sc.isConnected()) {
					String history = iSocketHandler.getHistory(sc);
					textArea.setText(history);
				} else {
					channelBox.getItems().remove(item);
				}

			}
		});
	}

	public void output(String message) {
		append(message);
	}

	public void output(String message, boolean withSplit) {
		append(withSplit ? Constant.SPLIT + Constant.BR + message + Constant.BR + Constant.SPLIT + Constant.BR
				: message);
	}

	private void append(String text) {
		javafx.application.Platform.runLater(() -> textArea.appendText(text));
	}

	public void accept(SocketChannel sc) {
		final KeyValuePair item = new KeyValuePair(sc, sc.toString().replaceAll("java.nio.channels.SocketChannel", ""));
		this.channelBox.getItems().add(item);
		// this.channelBox.getSelectionModel().selectFirst();
		// this.channelBox.getSelectionModel().select(item);
	}

	public static class KeyValuePair {
		private final SocketChannel key;
		private final String value;

		public KeyValuePair(SocketChannel key, String value) {
			this.key = key;
			this.value = value;
		}

		public SocketChannel getKey() {
			return key;
		}

		public String toString() {
			return value;
		}
	}

}
