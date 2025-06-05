package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

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
    private javafx.beans.property.ObjectProperty<? extends fr.umontpellier.iut.ptcgJavaFX.IJoueur> joueurActif;
    private javafx.scene.control.Label instructionLabel;
    private javafx.scene.control.Label nomDuJoueur;
    private javafx.scene.layout.HBox panneauMain;
    private Button boutonPasser;

    public VueDuJeu(IJeu jeu) {
        this.jeu = jeu;
        instructionLabel = new javafx.scene.control.Label();
        instructionLabel.setStyle("-fx-font-size: 18px;");
        //j'affiche le nom du joueur
        nomDuJoueur = new javafx.scene.control.Label();
        nomDuJoueur.setStyle("-fx-font-size: 18px;");
        //je cree un nouveau paneau
        panneauMain = new javafx.scene.layout.HBox();

        //j'ajoute tout
        boutonPasser = new Button("Passer");
        boutonPasser.setStyle("-fx-font-size: 18px;");
        boutonPasser.setOnAction(actionPasserParDefaut);
        this.getChildren().addAll(instructionLabel, nomDuJoueur, panneauMain, boutonPasser);

        // j'ajoute le listener pour chaque joueur
        for (IJoueur joueur : jeu.getJoueurs()) {
            setListenerChangementMain(joueur);
        }
    }

    public void creerBindings() {
        instructionLabel.textProperty().bind(jeu.instructionProperty());
         // j'appele le bind
        bindJoueurActif();
    }
     //je cree un bind pour le nom du joueur
    private void bindJoueurActif() {
        joueurActif = jeu.joueurActifProperty();
        setJoueurActifChangeListener();
        if (joueurActif.get() != null) {
            nomDuJoueur.setText(joueurActif.get().getNom());
            placerMain();
        } else {
            nomDuJoueur.setText("");
            panneauMain.getChildren().clear();
        }
    }

    private void setJoueurActifChangeListener() {
        joueurActif.addListener((source, ancien, nouveau) -> {
            if (nouveau != null) {
                nomDuJoueur.setText(nouveau.getNom());
                placerMain();
            } else {
                nomDuJoueur.setText("");
                panneauMain.getChildren().clear();
            }
        });
    }

    private void setListenerChangementMain(IJoueur joueur) {
        joueur.getMain().addListener((ListChangeListener<ICarte>) change -> placerMain());
    }

    private void placerMain() {
        panneauMain.getChildren().clear();
        // je verifie que le joueur n'est ^pas nul
        if (joueurActif.get() == null) return;
        //je cree une liste de cartes
        java.util.List<? extends ICarte> main = new java.util.ArrayList<>(joueurActif.get().getMain());
        main.sort(java.util.Comparator.comparing(ICarte::getNom));
        //je parcours la liste et cree
        for (ICarte carte : main) {
            Button btn = new Button(carte.getNom());
            btn.setStyle("-fx-font-size: 18px;");
            btn.setOnAction(e -> jeu.uneCarteDeLaMainAEteChoisie(carte.getId()));
            panneauMain.getChildren().add(btn);
        }
    }

    EventHandler<ActionEvent> actionPasserParDefaut = (event -> jeu.passerAEteChoisi());

}
