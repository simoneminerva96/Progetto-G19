package it.unipv.gui.manager;

import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import it.unipv.gui.common.GUIUtils;
import it.unipv.gui.common.IPane;
import it.unipv.utils.ApplicationException;
import it.unipv.utils.ApplicationUtils;
import it.unipv.utils.CloseableUtils;
import it.unipv.utils.DataReferences;
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
    private File[] listOfPreviews;
    private ManagerHomeController managerHomeController;
    private HallEditor hallEditor;

    public void init(ManagerHomeController managerHomeController, double initialWidth) {
        this.managerHomeController = managerHomeController;
        initListOfPreviews();
        columnMax = getColumnMaxFromPageWidth(initialWidth);
        createHallGrid();
        checkPageDimension();
    }

    private void initListOfPreviews() {
        listOfPreviews = new File(DataReferences.PIANTINEPREVIEWSFOLDERPATH).listFiles();
    }

    private void createHallGrid() {
        hallNames.clear();
        grigliaSale.getChildren().clear();

        for (File file : Objects.requireNonNull(listOfPreviews)) {
            createViewFromPreviews(file);
        }

        GUIUtils.setScaleTransitionOnControl(nuovaSalaButton);

        rowCount = 0;
        columnCount = 0;
    }

    private void createViewFromPreviews(File file) {
        try{
            Label nomeSalaLabel = new Label(FilenameUtils.removeExtension(file.getName()));
            nomeSalaLabel.setFont(Font.font("system", FontWeight.NORMAL, FontPosture.REGULAR, 15));
            hallNames.add(nomeSalaLabel.getText());
            nomeSalaLabel.setTextFill(Color.WHITE);

            grigliaSale.setHgap(80);
            grigliaSale.setVgap(80);

            FileInputStream fis = new FileInputStream(file);
            ImageView snapHallView = new ImageView(new Image(fis, 150, 0, true, true));
            snapHallView.setFitWidth(150);
            CloseableUtils.close(fis);

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
            if(columnCount==columnMax) {
                columnCount=0;
                rowCount++;
            }
            grigliaSale.add(pane, columnCount, rowCount);
            columnCount++;

            hallPanel.setContent(grigliaSale);
            GridPane.setMargin(pane, new Insets(15,0,0,15));

            nomeSalaLabel.setLayoutY(snapHallView.getLayoutY() + 100);

            deleteIcon.setLayoutY(nomeSalaLabel.getLayoutY()-2);
            deleteIcon.setLayoutX(nomeSalaLabel.getLayoutX()+126);

            renameIcon.setLayoutY(nomeSalaLabel.getLayoutY()-2);
            renameIcon.setLayoutX(nomeSalaLabel.getLayoutX()+93);

            pane.getChildren().addAll(snapHallView, nomeSalaLabel, deleteIcon, renameIcon);

            snapHallView.setOnMouseClicked(event -> {
                hallEditor = new HallEditor(nomeSalaLabel.getText(), this, true);
                hallEditor.setAlwaysOnTop(true);
            });

            GUIUtils.setScaleTransitionOnControl(snapHallView);
            renameIcon.setOnMouseClicked(event -> renameHall(nomeSalaLabel.getText(), nomeSalaLabel, renameIcon, deleteIcon));
            deleteIcon.setOnMouseClicked(event -> removeHall(nomeSalaLabel.getText()));


        } catch(FileNotFoundException ex) {
            throw new ApplicationException(ex);
        }
    }

    private void removeHall(String hallName) {
        int reply = JOptionPane.showConfirmDialog(null, "Vuoi davvero eliminare la piantina " + hallName + "?");
        if(reply == JOptionPane.YES_OPTION) {
            ApplicationUtils.removeFileFromPath(DataReferences.PIANTINEFOLDERPATH + hallName + ".csv");
            ApplicationUtils.removeFileFromPath(DataReferences.PIANTINEPREVIEWSFOLDERPATH + hallName + ".jpg");
            hallNames.remove(hallName);
            managerHomeController.triggerToHomeNewHallEvent();
            refreshUIandHallList();
        }
    }

    private void renameHall(String hallName, Label labelToModify, Label renameIcon, Label deleteIcon) {
        String newFileName = JOptionPane.showInputDialog(null, "Inserisci il nuovo nome della sala:");
        if(newFileName!=null) {
            if(!newFileName.trim().equalsIgnoreCase("")) {
                if(checkIfItIsFree(newFileName)) {
                    if( ApplicationUtils.renameFile( DataReferences.PIANTINEFOLDERPATH + hallName+".csv"
                                                   , DataReferences.PIANTINEFOLDERPATH + newFileName+".csv")
                     && ApplicationUtils.renameFile( DataReferences.PIANTINEPREVIEWSFOLDERPATH + hallName + ".jpg"
                                                   , DataReferences.PIANTINEPREVIEWSFOLDERPATH + newFileName + ".jpg" ) ) {
                        labelToModify.setText(newFileName);
                        renameIcon.setTooltip(new Tooltip("Rinomina " + newFileName));
                        deleteIcon.setTooltip(new Tooltip("Elimina " + newFileName));

                        hallNames.add(newFileName);
                        hallNames.remove(hallName);

                        managerHomeController.triggerToHomeNewHallEvent();
                        GUIUtils.showAlert(Alert.AlertType.INFORMATION, "Informazione", "Operazione riuscita: ", "Sala rinominata con successo!");
                    } else {
                        GUIUtils.showAlert(Alert.AlertType.ERROR, "Errore", "Si è verificato un errore:", "Si è verificato un errore durante la procedura!");
                    }
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
                        hallEditor = new HallEditor(nomeSala, this, false);
                        hallEditor.setAlwaysOnTop(true);
                    } else {
                        configureGridJOptionPaneMenu();
                        if(!canceled) {
                            if(rows<27) {
                                hallEditor = new HallEditor(nomeSala, this, rows, columns);
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
        grigliaSale.getChildren().clear();
        initListOfPreviews();
        createHallGrid();
    }

    private void refreshUI() {
        grigliaSale.getChildren().clear();
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
