package shnulaa.fx.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import shnulaa.fx.constant.Constant;
import shnulaa.fx.message.MessageOutputImpl;
import shnulaa.fx.nio.IServer;

@SuppressWarnings("restriction")
public class MainLayoutController {

    private static Logger log = LoggerFactory.getLogger(MainLayoutController.class);

    @FXML
    private TextField listenPort;

    @FXML
    private Label listenLab;

    @FXML
    private Button listen;

    @FXML
    private Button stop;

    @FXML
    private Button clear;

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
        listenPort.setText(String.valueOf(Constant.DEFAULT_PORT));
        listen.setDisable(false);
        stop.setDisable(true);
    }

    /**
     * the action for handle the button of listen
     */
    @FXML
    private void handleListen() {
        String portText = listenPort.getText();
        if (StringUtils.isEmpty(portText)) {
            showAlert(Constant.TITLE, "Invalid Port", Alert.AlertType.ERROR);
            return;
        }

        try {
            int port = Integer.valueOf(portText);
            if (port <= 0 || port > 65535) {
                showAlert(Constant.TITLE, "Port outOf range", Alert.AlertType.ERROR);
                return;
            }

            log.debug("Start Local service and ready to listen port: {}..", port);
            service = Executors.newSingleThreadExecutor();
			// base = new LocalNioServer(listenArea, port);
			// service.execute(base);

            listen.setDisable(true);
            stop.setDisable(false);

        } catch (Exception ex) {
            log.error("Exception occurred when execute the LocalNioServer..", ex);
            showAlert(Constant.TITLE, (ex instanceof NumberFormatException) ? "Port is not number.." : ex.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * the action for handle the button of stop
     */
    @FXML
    private void handleStop() {
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

}
