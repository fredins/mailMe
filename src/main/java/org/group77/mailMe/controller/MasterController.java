package org.group77.mailMe.controller;

import javafx.application.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.*;
import org.controlsfx.control.*;
import org.group77.mailMe.*;
import org.group77.mailMe.controller.utils.*;
import org.group77.mailMe.model.*;
import org.group77.mailMe.Control;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class MasterController implements SearchControl {
    @FXML
    private Button refreshBtn; // refresh button
    @FXML
    private Button writeBtn; // write button: starts writing view
    @FXML
    private FlowPane foldersFlow; // flow of folders
    @FXML
    private Pane readingPane; // pane for displaying emails that are being read
    @FXML
    private ComboBox<Account> accountsCombo; // box with accounts to select from
    @FXML
    private TextField searchField; // search field used for searching
    @FXML
    private Button applySearchButton; // button for applying the search
    @FXML
    private Button clearSearchButton; // button for clearing the search field
    @FXML
    private Button filterButton; // button for opening the filter view
    @FXML
    private Button addAccountBtn; // button for adding
    @FXML
    private FlowPane emailsFlow; // flow of emails in the current folder
    @FXML
    private FlowPane filterFlowPane; // window for the filter alternatives
    @FXML
    private AnchorPane progressPane; // progress bar
    @FXML
    private Label progressLabel; // text for the progress bar
    private final ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
    private FilterControl filterControl; // component responsible for filtering


    /**
     * 1. load/display folders in flowPane
     * 2. populate account comboBox
     * 3. add event handlers to nodes and state fields
     *
     * @param control the model
     * @author Martin
     * @author David
     * @author Hampus Jernkrook (added sorting)
     */
    public void init(Control control) {
        loadFolders(control.getFolders().get(), control);
        if (control.getAccounts() != null) {
            populateAccountCombo(control.getAccounts().get(), control);
        }
        if (control.getActiveAccount().get() != null) {
            accountsCombo.setValue(control.getActiveAccount().get());
        }


        //previous version: // filterButton.setOnAction(i -> WindowOpener.openFilter(control));
        // open filter view in background without displaying to user.
        loadFilterView(control);
        // upon pressing the filter button, show filter view if not shown, or hide it if currently shown.
        filterButton.setOnAction(i -> {
            filterFlowPane.setVisible(!filterFlowPane.isVisible());
        });
        //Add ToolTips for every button
        Tooltip t1 = new Tooltip("Search");
        Tooltip.install(applySearchButton, t1);
        Tooltip t2 = new Tooltip("Clear Search");
        Tooltip.install(clearSearchButton, t2);
        Tooltip t3 = new Tooltip("Refresh");
        Tooltip.install(refreshBtn, t3);
        Tooltip t4 = new Tooltip("Open Filter");
        Tooltip.install(filterButton, t4);
        Tooltip t5 = new Tooltip("Write New Mail");
        Tooltip.install(writeBtn, t5);
        Tooltip t6 = new Tooltip("Add New Account");
        Tooltip.install(addAccountBtn, t6);

        // attach event handlers
        refreshBtn.setOnAction(i -> refresh(control));
        writeBtn.setOnAction(i -> WindowOpener.openWriting(control));
        addAccountBtn.setOnAction(inputEvent -> WindowOpener.openAddAccount(control, node -> ((Stage) node.getScene().getWindow()).close()));
        control.getActiveAccount().addObserver(newAccount -> accountsCombo.setValue(newAccount));
        control.getFolders().addObserver(newFolders -> handleFoldersChange(newFolders, control));
        control.getActiveEmails().addObserver(newEmails -> loadEmails(newEmails, control));
        control.getAccounts().addObserver(newEmails -> accountsCombo.setItems(FXCollections.observableList(newEmails)));
        control.getActiveEmail().addObserver(newEmail -> handleActiveEmailChange(newEmail, control));
        control.getActiveFolder().addObserver(newFolder -> handleActiveFolderChange(newFolder, control));
        applySearchButton.setOnAction(i -> applySearch(control));
        searchField.setOnAction(i -> applySearch(control)); // allows for direct search using 'ENTER' instead of pressing the button.
        clearSearchButton.setOnAction(i -> clearSearch(control));
    }

    /**
     * display progressbar, refresh new emails without blocking the application thread, remove progressbar and give display feedback
     *
     * @param control the control layer
     * @author Martin
     * @author Hampus Jernkrook (added sorting)
     */
    private void refresh(Control control) {
        progressPane.toFront();
        progressLabel.setText(control.getActiveAccount().get().emailAddress() + ": downloading new messages...");

        Notifications notification = Notifications.create()
                .position(Pos.TOP_CENTER)
                .hideAfter(Duration.seconds(2));


        threadExecutor.execute(() -> {
            try {
                List<Email> newEmails = control.refresh();
                Platform.runLater(() -> {
                    try {
                        control.updateFolder("Inbox", newEmails);
                        notification
                                .graphic(new Label(newEmails.isEmpty() ? "No new messages" : newEmails.size() + " new messages"))
                                .show();
                        // sort the result of the refresh by the chosen sorting strategy
                        filterControl.applySorting(control);
                    } catch (FolderNotFoundException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressLabel.setText("failure");
                    notification
                            .title("Failure!")
                            .text(e.getMessage())
                            .showError();
                });
                e.printStackTrace();
            }
            Platform.runLater(() -> progressPane.toBack());
        });
    }

    /**
     * when folders change:
     * load folders if not empty, set active folder to inbox
     *
     * @param control    the control layer
     * @param newFolders the new folders
     * @author Martin
     * @author Hampus Jernkrook (added sorting)
     */
    private void handleFoldersChange(List<Folder> newFolders, Control control) {
        if (!newFolders.isEmpty()) {
            loadFolders(newFolders, control);
            control.getActiveFolder().set(newFolders.get(0));
            filterControl.applySorting(control); // keep current sorting
        }
    }

    /**
     * when active email change: load email in reading pane or clear it
     *
     * @param newEmail the new Email
     * @param control  the control layer
     * @author Martin
     */
    private void handleActiveEmailChange(Email newEmail, Control control) {
        if (newEmail != null) {
            loadReading(newEmail, control);
        } else {
            readingPane.getChildren().clear();
        }
    }

    /**
     * when active folder change: clear reading pane
     *
     * @param control the control layer
     * @author Martin
     * @author Hampus Jernkrook (added sorting)
     */
    private void handleActiveFolderChange(Folder newFolder, Control control) {
        if (control.getActiveEmail() != null) {
            readingPane.getChildren().clear();
        }
        control.getActiveEmails().replaceAll(newFolder.emails());
        filterControl.applySorting(control); // keep current sorting
    }

    /**
     * Populates the comboBox with the emailAddresses of the accounts in model's accounts.
     * Adds an Account with emailAddress "Add New Account" that can be clicked on to open AddAccountView.
     *
     * @param accounts all accounts in model's accounts.
     * @param control  holds the state of the application
     * @author Martin
     * @author Elin Hagman
     * @author David Ågren Zamanian
     */

    private void populateAccountCombo(List<? extends Account> accounts, Control control) {
        accountsCombo.getItems().addAll(accounts);
        accountsCombo.setOnAction(inputEvent -> control.getActiveAccount().set(accountsCombo.getValue()));
        accountsCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Account account) {
                return account != null ? account.emailAddress() : null;
            }

            @Override
            public Account fromString(String string) {
                Account account = null;
                try {
                    account = control.getAccounts().stream()
                            .filter(acc -> acc.emailAddress().equals(string))
                            .findAny()
                            .orElseThrow(Exception::new);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return account;
            }
        });
        accountsCombo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                if (control.getActiveAccount().get() != null) {
                    setText(control.getActiveAccount().get().emailAddress());
                }
            }
        });
    }

    /**
     * loads/display email
     *
     * @param email   the email to be displayed
     * @param control the model
     * @author David
     * @author Martin
     */
    private void loadReading(Email email, Control control) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("View/Reading.fxml"));
            Pane pane = fxmlLoader.load();
            pane.setPrefWidth(readingPane.getWidth());
            pane.setPrefHeight(readingPane.getHeight());
            ((ReadingController) fxmlLoader.getController()).init(control, email);
            readingPane.getChildren().clear();
            readingPane.getChildren().add(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *  initialize every folder in state field folders, display every folder in the folder flowPane
     *
     * @param folders all the folders to be loaded
     * @param control the model
     * @author Martin
     */
    private void loadFolders(List<Folder> folders, Control control) {
        foldersFlow.getChildren().clear();
        foldersFlow.getChildren().addAll(folders
                .stream()
                .map(folder -> {
                    Pane pane = null;
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("View/FolderItem.fxml"));
                        pane = fxmlLoader.load();
                        ((FolderItemController) fxmlLoader.getController()).init(control, folder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return pane;
                })
                .collect(Collectors.toList()));
    }

    /**
     * initialize every email in state field visibleEmails, display every email in the emails flowPane
     *
     * @param emails  the emails to be displayed
     * @param control the model
     * @author David
     * @author Martin
     */
    private void loadEmails(List<Email> emails, Control control) {
        emailsFlow.getChildren().clear();
        emailsFlow.getChildren().addAll(emails
                .stream()
                .map(email -> {
                    Pane pane = null;
                    try {
                        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("View/EmailItem.fxml"));
                        pane = fxmlLoader.load();
                        ((EmailItemController) fxmlLoader.getController()).init(control, email);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return pane;
                })
                .collect(Collectors.toList())
        );
    }

    /**
     * Load the FilterView in separate flowpane. Hide it upon initialisation (do not cover MainView).
     *
     * @param control the control class to pass as argument to the FilterControl.
     * @author Hampus Jernkrook
     * @author David Zamanian
     */
    private void loadFilterView(Control control) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("View/Filter.fxml"));
            Pane pane = fxmlLoader.load();
            filterControl = fxmlLoader.getController();
            filterControl.init(control, this);
            filterFlowPane.getChildren().clear();
            filterFlowPane.getChildren().add(pane);
            filterFlowPane.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Apply the search by searching for the search word in the search field.
     *
     * @param control - the class to delegate on to towards the backend.
     * @author Hampus Jernkrook
     */
    public void applySearch(Control control) {
        control.clearSearchResult(); // clear the search result first.
        filterControl.applyFilter(control); // re-apply filters via filter control
        control.search(searchField.getText()); // search for the submitted search word.
    }

    /**
     * Clear the search field and clear search results by restoring to showing all emails.
     *
     * @param control - the class to delegate on to towards the backend.
     * @author Hampus Jernkrook
     */
    private void clearSearch(Control control) {
        searchField.setText(""); // clear search field
        control.clearSearchResult(); // restore to original emails shown.
        filterControl.applyFilter(control); // let filter re-apply its filters
    }
}
