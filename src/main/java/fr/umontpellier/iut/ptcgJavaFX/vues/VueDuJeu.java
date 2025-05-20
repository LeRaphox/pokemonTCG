package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * Cette classe correspond à la fenêtre principale de l'application.
  * Elle est initialisée avec une référence sur la partie en cours (Jeu).
  * On y définit les bindings sur les éléments internes qui peuvent changer
 * (l'instruction à donner au joueur, les éléments du joueur actif
 * et les informations utiles sur son adversaire, le moyen de passer...)
 * ainsi que les listeners à exécuter lorsque ces éléments changent
 */
public class VueDuJeu extends Pane {

    private IJeu jeu;

    public VueDuJeu(IJeu jeu) {
        this.jeu = jeu;
    }

    public void creerBindings() {
    }

    EventHandler<? super MouseEvent> actionPasserParDefaut = (mouseEvent -> System.out.println("Vous avez choisi Passer"));

}
