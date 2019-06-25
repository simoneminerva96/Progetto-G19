package it.unipv.controller.common;

import it.unipv.utils.CloseableUtils;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.Optional;

public class GUIUtils {

    public static void setFadeInOutOnControl(Node control) {
        final FadeTransition fadeIn = new FadeTransition(Duration.millis(100));
        fadeIn.setNode(control);
        fadeIn.setToValue(1);
        control.setOnMouseEntered(e -> {
            fadeIn.playFromStart();
            control.setCursor(Cursor.HAND);
        });

        final FadeTransition fadeOut = new FadeTransition(Duration.millis(100));
        fadeOut.setNode(control);
        fadeOut.setToValue(0.5);
        control.setOnMouseExited(e -> {
            fadeOut.playFromStart();
            control.setCursor(Cursor.DEFAULT);
        } );

        control.setOpacity(0.5);
    }

    public static void setScaleTransitionOnControl(Node control) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), control);
        scaleTransition.setCycleCount(1);
        scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
        DoubleProperty expandToMaxProperty = new SimpleDoubleProperty(1.2);

        control.setOnMouseEntered(event -> {
            control.setCursor(Cursor.HAND);
            scaleTransition.setFromX(control.getScaleX());
            scaleTransition.setFromY(control.getScaleY());
            scaleTransition.setToX(expandToMaxProperty.get());
            scaleTransition.setToY(expandToMaxProperty.get());
            scaleTransition.playFromStart();
        });


        control.setOnMouseExited(event -> {
            control.setCursor(Cursor.DEFAULT);
            scaleTransition.setFromX(control.getScaleX());
            scaleTransition.setFromY(control.getScaleY());
            scaleTransition.setToX(1);
            scaleTransition.setToY(1);
            scaleTransition.playFromStart();
        } );
    }

    public static ImageView getIconView(InputStream iconInputStream) {
        ImageView view = new ImageView(new Image(iconInputStream));
        view.setFitWidth(25);
        view.setFitHeight(25);

        CloseableUtils.close(iconInputStream);

        return view;
    }

    public static void showAlert(Alert.AlertType type, String title, String headerText, String message) {
        Alert alert = new Alert(type);
        Stage s = (Stage) alert.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image(GUIUtils.class.getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static Optional<ButtonType> showConfirmationAlert(String title, String headerText, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.NO, ButtonType.YES);
        Stage s = (Stage) alert.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image(GUIUtils.class.getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    public static Optional<String> showInputAlert(String title, String header, String message) {
        TextInputDialog inputDialog = new TextInputDialog();
        Stage s = (Stage) inputDialog.getDialogPane().getScene().getWindow();
        s.getIcons().add(new Image(GUIUtils.class.getResourceAsStream("/images/GoldenMovieStudioIcon.png")));
        inputDialog.setTitle(title);
        inputDialog.setHeaderText(header);
        inputDialog.setContentText(message);
        return inputDialog.showAndWait();
    }
}