package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.vues.VueJoueurActif;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Cette classe correspond à la fenêtre principale de l'application.
 * Elle est initialisée avec une référence sur la partie en cours (IJeu).
 * On y définit les bindings sur les éléments internes qui peuvent changer
 * (l'instruction à donner au joueur, les éléments du joueur actif
 * et les informations utiles sur son adversaire, le moyen de passer...)
 * ainsi que les listeners à exécuter lorsque ces éléments changent
 */
public class VueDuJeu extends javafx.scene.layout.VBox {

    private IJeu jeu;
    @FXML private VueAdversaire panneauAdversaire;
    @FXML private javafx.scene.control.Label instructionLabel;
    @FXML private VueJoueurActif panneauDuJoueurActif;
    @FXML private Button boutonPasser;

    public VueDuJeu(IJeu jeu) {
        this.jeu = jeu;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/VueDuJeu.fxml"));
        fxmlLoader.setController(this);
        try {
            VBox root = fxmlLoader.load();
            this.getChildren().addAll(root.getChildren());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        boutonPasser.setOnAction(actionPasserParDefaut);
    }

    public void creerBindings() {
        instructionLabel.textProperty().bind(jeu.instructionProperty());
        panneauDuJoueurActif.bindJeu(jeu);

    }

    EventHandler<ActionEvent> actionPasserParDefaut = (event -> jeu.passerAEteChoisi());

}
