package shnulaa.fx.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import shnulaa.fx.config.Config;
import shnulaa.fx.constant.Constant;
import shnulaa.fx.message.MessageOutputImpl;
import shnulaa.fx.message.MessageOutputImpl.KeyValuePair;
import shnulaa.fx.nio.base.IServer;
import shnulaa.fx.nio.base.ISocketHandler;
import shnulaa.fx.nio.clone.LocalSocketHandler;
import shnulaa.fx.nio.listen.ListenSocketHandler;

@SuppressWarnings("restriction")
public class MainLayoutController {

	private static Logger log = LoggerFactory.getLogger(MainLayoutController.class);

	@FXML
	private TextField localIp; // local IP address

	@FXML
	private TextField localPort; // local port

	@FXML
	private TextField remoteIp; // remote IP address

	@FXML
	private TextField remotePort; // remote port

	@FXML
	private Button clone; // the clone button

	@FXML
	private Button stop; // stop button

	@FXML
	private Button clear; // the clear button

	@FXML
	private TextArea cloneArea;

	@FXML
	private Button listen; // the listen button

	@FXML
	private Button stopListen;

	@FXML
	private TextField listenPort; // listen port

	@FXML
	private TextArea listenArea;

	@FXML
	private CheckBox bindshell;

	@FXML
	private ChoiceBox<KeyValuePair> channelBox;

	/** the service create a new thread for receive the connection **/
	private ExecutorService cloneService;

	private ExecutorService listenService;

	/** the worker of local Nio server, use for shutdown the server **/
	private IServer cloneServer;

	/** the worker of local Nio server, use for shutdown the server **/
	private IServer listenServer;

	/** use for output the message in textArea **/
	private MessageOutputImpl cloneOutput;

	/** use for output the message in textArea **/
	private MessageOutputImpl listenOutput;

	/**
	 * constructor
	 */
	public MainLayoutController() {
	}

	@FXML
	private void initialize() {
		log.debug("Initialize the Controller..");
		this.cloneOutput = new MessageOutputImpl(cloneArea);
		this.listenOutput = new MessageOutputImpl(listenArea, channelBox);

		localIp.setText(Constant.DEFAULT_HOST);
		localPort.setText(String.valueOf(Constant.DEFAULT_PORT));

		remoteIp.setText("192.168.1.38");
		remotePort.setText("22");

		listenPort.setText("1234");

		clone.setDisable(false);
		stop.setDisable(true);

		listen.setDisable(false);
		stopListen.setDisable(true);

		// final KeyValuePair item = new KeyValuePair(null, " ");
		// this.channelBox.getItems().add(item);

	}

	/**
	 * the action for handle the button of listen
	 */
	@FXML
	private void handleClone() {
		try {
			Config config = createConfig();
			if (config == null) {
				throw new RuntimeException("input is incorrect..");
			}
			cloneService = Executors.newSingleThreadExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setDaemon(true);
					t.setName("Clone-Port-Thread");
					return t;
				}
			});

			cloneServer = new LocalSocketHandler(cloneOutput, config);
			cloneService.execute(cloneServer);

			clone.setDisable(true);
			stop.setDisable(false);

		} catch (Exception ex) {
			log.error("Exception occurred when execute the LocalNioServer..", ex);
			showAlert(Constant.TITLE, (ex instanceof NumberFormatException) ? "Port is not number.." : ex.getMessage(),
					Alert.AlertType.ERROR);
			return;
		}
		cloneOutput.output("Clone port successfully..", true);
	}

	/**
	 * the action for handle the button of stop
	 */
	@FXML
	private void handleStop() {
		stop();
	}

	/**
	 * the action for handle the button of clear
	 */
	@FXML
	private void handleClear() {
		if (cloneArea != null) {
			log.debug("Clear the information in textarea..");
			cloneArea.clear();
		}
	}

	@FXML
	private void handleListen() {
		try {
			listenService = Executors.newSingleThreadExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r);
					t.setDaemon(true);
					t.setName("Listen-Port-Thread");
					return t;
				}
			});

			String portTxt = listenPort.getText();
			if (StringUtils.isEmpty(portTxt)) {
				showAlert(Constant.TITLE, "Port must be specified..", Alert.AlertType.ERROR);
				return;
			}

			int port = Integer.valueOf(portTxt);

			listenServer = new ListenSocketHandler(listenOutput, new Config(port, bindshell.isSelected()));
			listenOutput.setHandler((ISocketHandler) listenServer);
			listenService.submit(listenServer);

			listen.setDisable(true);
			stopListen.setDisable(false);
			listenOutput.output("listen port " + portTxt + " successfully..", true);
		} catch (Exception ex) {
			log.error("Exception occurred when Listen a port..");
			showAlert(Constant.TITLE, (ex instanceof NumberFormatException) ? "Port is not number.." : ex.getMessage(),
					Alert.AlertType.ERROR);
			return;
		}
	}

	@FXML
	private void handleStopListen() {
		stopListen();
	}

	/**
	 * 
	 * @param title
	 * @param message
	 * @param type
	 */
	private void showAlert(String title, String message, Alert.AlertType type) {
		Alert a = new Alert(type);
		a.setTitle(title);
		a.setHeaderText(type.name());
		a.setResizable(false);
		a.setContentText(message);
		a.showAndWait();
	}

	public void stopListen() {
		try {
			log.debug("Read to Shutdown the service..");
			if (listenServer != null) {
				listenServer.stop();
			}

			if (listenService != null) {
				listenService.shutdown();
			}

			listenOutput.output("Close port successfully..", true);

			listen.setDisable(false);
			stopListen.setDisable(true);
			//
			// try {
			// // if (!channelBox.getSelectionModel().isEmpty()) {
			// // channelBox.getSelectionModel().clearSelection();
			// channelBox.getItems().clear();
			// // }
			//
			// } catch (Exception sex) {
			// log.error("Exception occurred when clearSelection");
			// }
			log.debug("Shutdown the service successfully..");
		} catch (Exception ex) {
			log.error("Exception occurred when handleStop", ex);
		} finally {
		}
	}

	/**
	 * stop
	 */
	public void stop() {
		try {
			log.debug("Read to Shutdown the service..");
			if (cloneServer != null) {
				cloneServer.stop();
			}

			if (cloneService != null) {
				cloneService.shutdown();
			}
			cloneOutput.output("Shutdown service successfully..", true);

			clone.setDisable(false);
			stop.setDisable(true);

			log.debug("Shutdown the service successfully..");
		} catch (Exception ex) {
			log.error("Exception occurred when handleStop", ex);
		} finally {
		}
	}

	/**
	 * 
	 * @return
	 */
	private Config createConfig() {
		String localIpText = localIp.getText();
		if (StringUtils.isEmpty(localIpText)) {
			showAlert(Constant.TITLE, "Invalid localIp", Alert.AlertType.ERROR);
			return null;
		}

		String localPortText = localPort.getText();
		if (StringUtils.isEmpty(localPortText)) {
			showAlert(Constant.TITLE, "Invalid localPort", Alert.AlertType.ERROR);
			return null;
		}

		String remoteIpText = remoteIp.getText();
		if (StringUtils.isEmpty(remoteIpText)) {
			showAlert(Constant.TITLE, "Invalid remote", Alert.AlertType.ERROR);
			return null;
		}

		String remotePortText = remotePort.getText();
		if (StringUtils.isEmpty(remotePortText)) {
			showAlert(Constant.TITLE, "Invalid remotePort", Alert.AlertType.ERROR);
			return null;
		}

		try {
			int lPort = Integer.valueOf(localPortText);
			if (lPort <= 0 || lPort > 65535) {
				showAlert(Constant.TITLE, "local Port outOf range", Alert.AlertType.ERROR);
				return null;
			}

			int rPort = Integer.valueOf(remotePortText);
			if (rPort <= 0 || rPort > 65535) {
				showAlert(Constant.TITLE, "local Port outOf range", Alert.AlertType.ERROR);
				return null;
			}

			return new Config(localIpText, lPort, remoteIpText, rPort);
		} catch (Exception ex) {
			log.error("Exception occurred when pase port to int..");
			return null;
		}
	}

}
