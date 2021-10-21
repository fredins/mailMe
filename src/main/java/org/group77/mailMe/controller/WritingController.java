package org.group77.mailMe.controller;

import javafx.application.*;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.*;
import javafx.scene.web.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.util.*;
import org.controlsfx.control.*;

import org.controlsfx.control.textfield.TextFields;

import org.group77.mailMe.Main;
import org.group77.mailMe.Control;
import org.group77.mailMe.model.*;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * controller for the writing view
 */

public class WritingController {
    @FXML
    private TextField toField;
    @FXML
    private Label fromLabel;
    @FXML
    private Button sendBtn;
    @FXML
    private Button attachBtn;
    @FXML
    private TextField subjectField;
    @FXML
    private HTMLEditor contentField;
    @FXML
    private HBox attachmentsHBox;
    private final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();

    private final List<File> attachments = new ArrayList<>();

    /**
     * normal init method when not replying
     *
     * @param control the model
     * @author Martin, Alexey, David
     */
    public void init(Control control) {
        init(control, null);
    }

    /**
     * 1. set initial values for nodes
     * 2. set event handlers for nodes and state fields
     *
     * @param control the model
     * @param to      the email address which the user is replying to
     * @author David Zamanian
     */

    public void init(Control control, String to) {
        //shows known recipients when typing in toField
        TextFields.bindAutoCompletion(toField, control.getKnownRecipients().get());
        if (to != null) {
            toField.setText(to);
        }
        fromLabel.setText(control.getActiveAccount().get().emailAddress());
        // input handlers
        sendBtn.setOnAction(inputEvent -> sendHandler(control, sendBtn));
        attachBtn.setOnAction(inputEvent -> attachFiles());
        // change handlers
        control.getActiveAccount().addObserver(newAccount -> fromLabel.setText(newAccount.emailAddress()));
        //remove attachments
        attachmentsHBox.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getTarget() == attachmentsHBox) {
                clearAllAttachments();
            }
        });
    }


    /**
     * show an error notification with information about error
     * @param e the thrown exception
     * @author Martin Fredin
     */
    private void showFailNotification(Exception e){
        Notifications.create()
          .position(Pos.TOP_CENTER)
          .hideAfter(Duration.seconds(2))
          .title("Failed")
          .text(e.getMessage())
          .showError();
    }

    /**
     * show success notification
     */
    private void showSuccessNotification(String message){
        Notifications.create()
          .position(Pos.TOP_CENTER)
          .hideAfter(Duration.seconds(2))
          .graphic(new Label(message))
          .show();
    }

    /**
     * try to send email, display visual feedback, move email to Sent Folder, Close window
     *
     * @param control the control layer
     * @param node    any node in active scene
     * @author Martin
     */
    private void sendHandler(Control control, Node node){
        String[] recipients = Arrays.stream(toField.getText()
          .split(";"))
          .distinct()
          .toArray(String[]::new);
        Email email = EmailFactory.createEmail(control,
                                 recipients,
                                 subjectField.getText(),
                                 contentField.getHtmlText(),
                                 attachments
        );

        threadExecutor.execute(() -> {
            try{
                control.send(email);
                control.addNewKnownRecipient(Arrays.stream(recipients).toList());
                Platform.runLater(() -> {
                    showSuccessNotification("Message sent successfully!");
                    control.addEmailToSent(email);
                });
            }catch (Exception e){
                Platform.runLater(() -> showFailNotification(e));
            }
        });
        ((Stage) node.getScene().getWindow()).close();
    }


    /**
     * @author Alexey Ryabov
     * Method creates file chooser and lets user to select multiple files and adds them to a list.
     */
    @FXML
    private void attachFiles() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            attachments.add(selectedFile);
            Button button = hBoxButtonSetup(selectedFile);
            attachmentsHBox.getChildren().add(button);
        }
    }

    /**
     * @param selectedFile - file that was added as an attachment.
     * @return button for the HBox.
     * @author Alexey Ryabov
     */
    private Button hBoxButtonSetup(File selectedFile) {
        //Creating image for attachments button
        ImageView icon = new ImageView(new Image(String.valueOf(Main.class.getResource("images_and_icons/attachment.png"))));
        icon.rotateProperty().set(-90);
        icon.setFitHeight(24);
        icon.setFitWidth(24);
        //Creating button for the HBox
        Button button = new Button();
        Tooltip tooltip = new Tooltip(); // Tooltip for the button with attachment's name.
        tooltip.setText(selectedFile.getName());
        button.setTooltip(tooltip);
        button.setGraphic(icon);
        button.setText(selectedFile.getName());
        button.setCursor(Cursor.HAND);
        button.setFont(Font.font("System", FontWeight.BOLD, 12));
        button.setStyle("-fx-background-color: #f4f4f4");

        return button;
    }

    /**
     * @author Alexey Ryabov
     * Removed all attachments from HBox and List of attachments
     */
    public void clearAllAttachments() {
        attachments.clear();
        attachmentsHBox.getChildren().removeAll(attachmentsHBox.getChildren());
    }
}
