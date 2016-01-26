package shnulaa.fx.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import shnulaa.fx.config.Config;
import shnulaa.fx.constant.Constant;
import shnulaa.fx.message.MessageOutputImpl;
import shnulaa.fx.nio.IServer;
import shnulaa.fx.nio.LocalClonePortServer;

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
	private Button listen; // the clone button

	@FXML
	private Button stop; // stop button

	@FXML
	private Button clear; // the clear button

	@FXML
	private TextArea listenArea;

	/** the service create a new thread for receive the connection **/
	private ExecutorService service;

	/** the worker of local Nio server, use for shutdown the server **/
	private IServer base;

	/** use for output the message in textArea **/
	private MessageOutputImpl outputImpl;

	/**
	 * constructor
	 */
	public MainLayoutController() {
	}

	@FXML
	private void initialize() {
		log.debug("Initialize the Controller..");
		this.outputImpl = new MessageOutputImpl(listenArea);

		localIp.setText(Constant.DEFAULT_HOST);
		localPort.setText(String.valueOf(Constant.DEFAULT_PORT));

		remoteIp.setText("192.168.1.38");
		remotePort.setText("22");

		listen.setDisable(false);
		stop.setDisable(true);
	}

	/**
	 * the action for handle the button of listen
	 */
	@FXML
	private void handleListen() {
		try {
			Config config = createConfig();
			if (config == null) {
				throw new RuntimeException("");
			}
			// log.debug("Start Local service and ready to listen port: {}..",
			// port);
			service = Executors.newSingleThreadExecutor();

			base = new LocalClonePortServer(outputImpl, config);
			service.execute(base);

			listen.setDisable(true);
			stop.setDisable(false);

		} catch (Exception ex) {
			log.error("Exception occurred when execute the LocalNioServer..", ex);
			showAlert(Constant.TITLE, (ex instanceof NumberFormatException) ? "Port is not number.." : ex.getMessage(),
					Alert.AlertType.ERROR);
			return;
		}

		outputImpl.output("Clone port successfully..", true);
	}

	/**
	 * the action for handle the button of stop
	 */
	@FXML
	private void handleStop() {
		stop();
	}

	/**
	 * stop
	 */
	public void stop() {
		try {
			log.debug("Read to Shutdown the service..");
			if (base != null) {
				base.stop();
			}

			if (service != null) {
				service.shutdown();
			}
			outputImpl.output("Shutdown service successfully..", true);

			listen.setDisable(false);
			stop.setDisable(true);

			log.debug("Shutdown the service successfully..");

		} catch (Exception ex) {
			log.error("Exception occurred when handleStop", ex);
		} finally {
		}
	}

	/**
	 * the action for handle the button of clear
	 */
	@FXML
	private void handleClear() {
		if (listenArea != null) {
			log.debug("Clear the information in textarea..");
			listenArea.clear();
		}
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
