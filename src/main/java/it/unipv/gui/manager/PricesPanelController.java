package it.unipv.gui.manager;

import it.unipv.conversion.CSVToPrices;
import it.unipv.conversion.PricesToCSV;
import it.unipv.gui.common.GUIUtils;
import it.unipv.gui.common.Prices;
import it.unipv.utils.DataReferences;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javax.swing.JOptionPane;

public class PricesPanelController implements Initializable {

    private Prices prices = null;
    
    @FXML TextField baseTextField;
    @FXML TextField vipTextField;
    @FXML TextField threeDTextField;
    @FXML TextField reducedTextField;

    @FXML Label saveButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initPricesIfExists();
        setComponentIfPricesExists();
        GUIUtils.setScaleTransitionOnControl(saveButton);
    }    
    
    private void initPricesIfExists(){
        prices = CSVToPrices.getPricesFromCSV(DataReferences.PRICESFILEPATH);
    }

    private void setComponentIfPricesExists(){
        if(prices!=null){
            baseTextField.setText(""+prices.getBase());
            vipTextField.setText(""+prices.getVip());
            threeDTextField.setText(""+prices.getThreed());
            reducedTextField.setText(""+prices.getReduced());
        }
    }

    @FXML
    public void doSave() throws NumberFormatException {
        if( baseTextField.getText().trim().equalsIgnoreCase("")
                || vipTextField.getText().trim().equalsIgnoreCase("")
                || threeDTextField.getText().trim().equalsIgnoreCase("")
                || reducedTextField.getText().trim().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(null, "Devi compilare tutti i campi!");
        } else {
            if(prices!=null) {
                prices.setBase(Double.parseDouble(baseTextField.getText()));
                prices.setVip(Double.parseDouble(vipTextField.getText()));
                prices.setThreed(Double.parseDouble(threeDTextField.getText()));
                prices.setReduced(Double.parseDouble(reducedTextField.getText()));
            } else {
                prices = new Prices( Double.parseDouble(baseTextField.getText())
                        , Double.parseDouble(vipTextField.getText())
                        , Double.parseDouble(threeDTextField.getText())
                        , Double.parseDouble(reducedTextField.getText()));
            }
            PricesToCSV.createCSVFromPrices(prices, DataReferences.PRICESFILEPATH, false);
            JOptionPane.showMessageDialog(null, "Salvataggio prezzi riuscito con successo!");
        }
    }
    
}
