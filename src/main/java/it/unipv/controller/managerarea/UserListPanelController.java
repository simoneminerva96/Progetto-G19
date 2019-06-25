package it.unipv.controller.managerarea;

import java.util.*;

import it.unipv.db.*;
import it.unipv.dao.PrenotationDao;
import it.unipv.dao.UserDao;
import it.unipv.dao.PrenotationDaoImpl;
import it.unipv.dao.UserDaoImpl;
import it.unipv.controller.common.GUIUtils;
import it.unipv.model.User;
import it.unipv.model.Prenotation;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.StringUtils;

public class UserListPanelController {

    @FXML ScrollPane userListPanel;
    @FXML TextField searchBarTextfield;
    @FXML Label searchButton;
    private GridPane grigliaUser = new GridPane();
    private static int rowCount = 0;
    private static int columnCount = 0;
    private List<User> users = new ArrayList<>();
    private PrenotationDao prenotationDao;
    private UserDao userDao;

    public void init(DBConnection dbConnection) {
        this.prenotationDao = new PrenotationDaoImpl(dbConnection);
        this.userDao = new UserDaoImpl(dbConnection);
        createUserListGrid();
    }

    private void initUserList() {
        users = userDao.retrieveUserList();
        Collections.sort(users);
    }

    private void createUserListGrid() {
        grigliaUser.getChildren().clear();
        GUIUtils.setScaleTransitionOnControl(searchButton);

        initUserList();

        for (User user : users) {
            if(!user.getNome().equalsIgnoreCase("Admin")) {
                createGridCellFromUser(user);
            }
        }

        initRowAndColumnCount();
    }

    private void createGridCellFromUser(User user) {
        Label userLabel = new Label(StringUtils.abbreviate(user.getNome(), 30));
        userLabel.setFont(Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 20));
        userLabel.setTextFill(Color.WHITE);

        grigliaUser.setHgap(15);
        grigliaUser.setVgap(15);

        Label deleteIcon = new Label();
        deleteIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        deleteIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Bin.png")));
        deleteIcon.setTooltip(new Tooltip("Elimina " + user.getNome()));
        GUIUtils.setFadeInOutOnControl(deleteIcon);

        Label editIcon = new Label();
        editIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        editIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Edit.png")));
        editIcon.setTooltip(new Tooltip("Modifica password a " + user.getNome()));
        GUIUtils.setFadeInOutOnControl(editIcon);

        AnchorPane pane = new AnchorPane();
        if(columnCount==1) {
            columnCount=0;
            rowCount++;
        }
        grigliaUser.add(pane, columnCount, rowCount);
        columnCount++;

        userListPanel.setContent(grigliaUser);
        GridPane.setMargin(pane, new Insets(5,5,5,5));

        editIcon.setLayoutY(userLabel.getLayoutY());
        editIcon.setLayoutX(userLabel.getLayoutX()+270);
        editIcon.setOnMouseClicked( event -> {
            String password = GUIUtils.showInputAlert("Modifica password", "Stai modificando la password di " + user.getNome(), "Inserisci la nuova password").orElse(null);
            if(password!=null) {
                if(!Objects.requireNonNull(password).trim().equalsIgnoreCase("") || !(password.trim().length() ==0)) {
                    user.setPassword(password);
                    userDao.updateUser(user);
                    refreshUI();
                    GUIUtils.showAlert(Alert.AlertType.INFORMATION, "Informazione", "Operazione riuscita: ", "La password dell'utente " + user.getNome() + " è stata correttamente cambiata in: " + password);
                } else {
                    GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore", "Non puoi inserire una password di soli spazi!");
                }
            }
        });

        deleteIcon.setLayoutY(userLabel.getLayoutY());
        deleteIcon.setLayoutX(userLabel.getLayoutX()+305);
        deleteIcon.setOnMouseClicked(e -> {
            Optional<ButtonType> option =
                    GUIUtils.showConfirmationAlert( "Attenzione"
                                                  , "Richiesta conferma:"
                                                  , "Sei sicuro di voler eliminare questo utente e le sue relative prenotazioni?");
            if(option.orElse(null)==ButtonType.YES) {
                removeConcerningPrenotations(user);
                userDao.deleteUser(user);
                refreshUI();
            }
        });

        pane.getChildren().addAll(userLabel, editIcon, deleteIcon);
    }

    private void removeConcerningPrenotations(User user) {
        List<Prenotation> prenotations = prenotationDao.retrievePrenotationList();
        for(Prenotation p : prenotations) {
            if(p.getNomeUtente().equalsIgnoreCase(user.getNome())) {
                prenotationDao.deletePrenotation(p);
            }
        }
    }

    private void refreshUI() {
        grigliaUser.getChildren().clear();
        createUserListGrid();
    }

    @FXML public void searchButtonListener() {
        String searchedUserName = searchBarTextfield.getText();
        if(searchedUserName!=null) {
            grigliaUser.getChildren().clear();
            for(User u : users) {
                if(u.getNome().toLowerCase().trim().contains(searchedUserName.toLowerCase())){
                    if(!u.getNome().trim().equalsIgnoreCase("Admin")) {
                        createGridCellFromUser(u);
                    }
                }
            }
            initRowAndColumnCount();
        } else {
            refreshUI();
        }
    }

    private void initRowAndColumnCount() {
        rowCount=0;
        columnCount=0;
    }

}