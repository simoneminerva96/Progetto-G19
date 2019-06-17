package it.unipv.gui.manager;

import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unipv.DB.DBConnection;
import it.unipv.DB.HallOperations;
import it.unipv.gui.common.GUIUtils;
import it.unipv.gui.common.IPane;
import it.unipv.utils.ApplicationException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import javax.swing.*;

public class HallPanelController implements IPane {

    @FXML ScrollPane hallPanel;
    @FXML Label nuovaSalaButton;
    private GridPane grigliaSale = new GridPane();
    private static int rowCount = 0;
    private static int columnCount = 0;
    private int columnMax = 3;
    private List<String> hallNames = new ArrayList<>();
    private List<Image> previews = new ArrayList<>();
    private int hallNamesSize = 0;
    private ManagerHomeController managerHomeController;
    private HallEditor hallEditor;
    private HallOperations ho;
    private DBConnection dbConnection;

    public void init(ManagerHomeController managerHomeController, double initialWidth, DBConnection dbConnection) {
        this.managerHomeController = managerHomeController;
        this.dbConnection = dbConnection;
        ho = new HallOperations(dbConnection);
        initHallNameList();
        initPreview();
        columnMax = getColumnMaxFromPageWidth(initialWidth);
        Platform.runLater(this::createHallGrid);

        checkPageDimension();
    }

    private void initHallNameList() {
        hallNames = ho.retrieveHallNames();
        Collections.sort(hallNames);
        hallNamesSize = hallNames.size();
    }

    private void initPreview() {
        previews.clear();
        for(int i = 0; i<hallNamesSize; i++) {
            previews.add(ho.retrieveHallPreviewAsImage(hallNames.get(i), 150, 0, true, true));
        }
    }

    private void createHallGrid() {
        grigliaSale.getChildren().clear();

        for(int i = 0; i<hallNamesSize; i++) {
            createViewFromPreviews(hallNames.get(i), previews.get(i));
        }

        GUIUtils.setScaleTransitionOnControl(nuovaSalaButton);

        rowCount = 0;
        columnCount = 0;
    }

    private void createViewFromPreviews(String hallName, Image preview) {
        Label nomeSalaLabel = new Label(FilenameUtils.removeExtension(hallName));
        nomeSalaLabel.setFont(Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 15));
        hallNames.add(nomeSalaLabel.getText());
        nomeSalaLabel.setTextFill(Color.WHITE);

        grigliaSale.setHgap(80);
        grigliaSale.setVgap(80);

        ImageView snapHallView = new ImageView(preview);
        snapHallView.setFitWidth(150);

        Label deleteIcon = new Label();
        deleteIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        deleteIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Bin.png")));
        deleteIcon.setTooltip(new Tooltip("Elimina " + nomeSalaLabel.getText().trim()));
        GUIUtils.setFadeInOutOnControl(deleteIcon);

        Label renameIcon = new Label();
        renameIcon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        renameIcon.setGraphic(GUIUtils.getIconView(getClass().getResourceAsStream("/images/Edit.png")));
        renameIcon.setTooltip(new Tooltip("Rinomina " + nomeSalaLabel.getText().trim()));
        GUIUtils.setFadeInOutOnControl(renameIcon);

        AnchorPane pane = new AnchorPane();
        if (columnCount == columnMax) {
            columnCount = 0;
            rowCount++;
        }
        grigliaSale.add(pane, columnCount, rowCount);
        columnCount++;

        hallPanel.setContent(grigliaSale);
        GridPane.setMargin(pane, new Insets(15, 0, 0, 15));

        nomeSalaLabel.setLayoutY(snapHallView.getLayoutY() + 100);

        deleteIcon.setLayoutY(nomeSalaLabel.getLayoutY() - 2);
        deleteIcon.setLayoutX(nomeSalaLabel.getLayoutX() + 126);

        renameIcon.setLayoutY(nomeSalaLabel.getLayoutY() - 2);
        renameIcon.setLayoutX(nomeSalaLabel.getLayoutX() + 93);

        pane.getChildren().addAll(snapHallView, nomeSalaLabel, deleteIcon, renameIcon);

        snapHallView.setOnMouseClicked(event -> {
            hallEditor = new HallEditor(nomeSalaLabel.getText(), this, true, dbConnection);
            hallEditor.setAlwaysOnTop(true);
        });

        GUIUtils.setScaleTransitionOnControl(snapHallView);
        renameIcon.setOnMouseClicked(event -> renameHall(nomeSalaLabel.getText(), nomeSalaLabel, renameIcon, deleteIcon));
        deleteIcon.setOnMouseClicked(event -> removeHall(nomeSalaLabel.getText()));
    }

    private void removeHall(String hallName) {
        int reply = JOptionPane.showConfirmDialog(null, "Vuoi davvero eliminare la piantina " + hallName + "?");
        if(reply == JOptionPane.YES_OPTION) {
            ho.removeHallAndPreview(hallName);
            initHallNameList();
            initPreview();
            managerHomeController.triggerToHomeNewHallEvent();
            refreshUIandHallList();
        }
    }

    private void renameHall(String hallName, Label labelToModify, Label renameIcon, Label deleteIcon) {
        String newHallName = JOptionPane.showInputDialog(null, "Inserisci il nuovo nome della sala:");
        if(newHallName!=null) {
            if(!newHallName.trim().equalsIgnoreCase("")) {
                if(checkIfItIsFree(newHallName)) {
                        labelToModify.setText(newHallName);
                        renameIcon.setTooltip(new Tooltip("Rinomina " + newHallName));
                        deleteIcon.setTooltip(new Tooltip("Elimina " + newHallName));

                        ho.renameHallAndPreview(hallName, newHallName);
                        initHallNameList();
                        initPreview();

                        managerHomeController.triggerToHomeNewHallEvent();
                        GUIUtils.showAlert(Alert.AlertType.INFORMATION, "Informazione", "Operazione riuscita: ", "Sala rinominata con successo!");
                } else {
                    GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Esiste già una sala con questo nome!");
                }
            } else {
                GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Devi compilare il campo!");
            }
        }
    }

    private boolean checkIfItIsFree(String name) {
        boolean status = true;
        for(String s : hallNames) {
            if(name.trim().equalsIgnoreCase(s)) {
                status = false;
                break;
            }
        }
        return status;
    }

    private int rows;
    private int columns;
    private boolean canceled;

    @FXML public void newHallListener() {
        String nomeSala = JOptionPane.showInputDialog(null, "Inserisci il nome della sala");
        if(nomeSala!=null) {
            if(nomeSala.equalsIgnoreCase("") || nomeSala.trim().length()==0) {
                GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Devi inserire un nome!");
            } else if(!nomeSala.equalsIgnoreCase("")) {
                if(checkIfItIsFree(nomeSala)) {
                    int reply = JOptionPane.showConfirmDialog(null, "Vuoi creare una griglia preimpostata?","Scegli una opzione", JOptionPane.YES_NO_OPTION);
                    if(reply == JOptionPane.NO_OPTION) {
                        hallEditor = new HallEditor(nomeSala, this, false, dbConnection);
                        hallEditor.setAlwaysOnTop(true);
                    } else {
                        configureGridJOptionPaneMenu();
                        if(!canceled) {
                            if(rows<27) {
                                hallEditor = new HallEditor(nomeSala, this, rows, columns, dbConnection);
                                hallEditor.setAlwaysOnTop(true);
                            } else {
                                GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Numero massimo di righe 26!");
                            }
                        }
                    }
                } else {
                    GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Esiste già una sala con questo nome!");
                }
            }
        }
    }

    private void configureGridJOptionPaneMenu() {
        JTextField rows = new JTextField();
        JTextField columns = new JTextField();
        Object[] message = {
                "Righe:", rows,
                "Colonne:", columns
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Inserisci numero di righe e colonne", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            if(rows.getText().trim().equalsIgnoreCase("") || columns.getText().trim().equalsIgnoreCase("")) {
                GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Devi inserire entrambi i dati!");
                canceled = true;
            } else {
                this.rows = Integer.parseInt(rows.getText());
                this.columns = Integer.parseInt(columns.getText());
                canceled = false;
            }
        } else {
            canceled = true;
        }
    }

    void triggerModificationToHallList() {
        refreshUIandHallList();
        managerHomeController.triggerToHomeNewHallEvent();
    }

    private void refreshUIandHallList() {
        initHallNameList();
        initPreview();
        createHallGrid();
    }

    private void refreshUI() {
        createHallGrid();
    }

    private int temp = 0;
    private void checkPageDimension() {
        Platform.runLater(() -> {
            Stage stage = (Stage) nuovaSalaButton.getScene().getWindow();
            stage.widthProperty().addListener(e -> {
                columnMax = getColumnMaxFromPageWidth(stage.getWidth());
                if (temp != columnMax) {
                    temp = columnMax;
                    refreshUI();
                }
            });
        });
    }

    //Supporta fino ai 1080p
    private int getColumnMaxFromPageWidth(double width) {
        if(width<800) {
            return 2;
        } else if(width>=800 && width<=1200) {
            return 3;
        } else if(width>1200 && width<=1500) {
            return 4;
        } else if(width>1500 && width<=1700) {
            return 5;
        } else if(width>1700){
            return 6;
        } else {
            throw new ApplicationException("Impossibile settare numero colonne per width: " + width);
        }
    }

    @Override
    public void closeAllSubWindows() {
        if(hallEditor!=null) {
            hallEditor.dispose();
            hallEditor.dispatchEvent(new WindowEvent(hallEditor, WindowEvent.WINDOW_CLOSING));
        }
    }
}
