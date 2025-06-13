package fr.umontpellier.iut.ptcgJavaFX.vues;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import java.io.IOException;

/**
 * Cette classe présente les éléments de l'adversaire,
 * en cachant ceux que le joueur actif n'a pas à connaitre.
 * <p>
 * On y définit les bindings sur le joueur actif, ainsi que le listener à exécuter lorsque ce joueur change
 */
public class VueAdversaire extends VBox {
    @FXML private javafx.scene.control.Label nomAdversaire;

    public VueAdversaire() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/VueAdversaire.fxml"));
        fxmlLoader.setController(this);
        try {
            VBox root = fxmlLoader.load();
            this.getChildren().addAll(root.getChildren());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
