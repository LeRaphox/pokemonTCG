package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.PokemonTCGIHM;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.scene.text.Font;

public class VueResultats extends VBox {
    private final PokemonTCGIHM ihm;

    public VueResultats(PokemonTCGIHM ihm) {
        this.ihm = ihm;

        // Style général
        setSpacing(10);
        setAlignment(Pos.CENTER);
        setStyle("-fx-padding: 20px; -fx-background-color: #f5f5f5; -fx-border-radius: 10px; " +
                "-fx-border-color: #ddd; -fx-border-width: 1px;");

        // Titre
        Label titre = new Label("Résultat du tour");
        titre.setFont(new Font("Arial", 20));
        titre.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        getChildren().add(titre);

        // informations du joueur
        HBox infoJoueur = new HBox(10);
        infoJoueur.setAlignment(Pos.CENTER);

        Label nomJoueur = new Label("Joueur: " + ihm.getJeu().getJoueurActif().getNom());
        nomJoueur.setFont(new Font("Arial", 14));
        nomJoueur.setStyle("-fx-text-fill: #333;");

        Label nombreCartes = new Label("Cartes en main: " + ihm.getJeu().getJoueurActif().getMain().size());
        nombreCartes.setFont(new Font("Arial", 14));
        nombreCartes.setStyle("-fx-text-fill: #666;");

        infoJoueur.getChildren().addAll(nomJoueur, nombreCartes);
        getChildren().add(infoJoueur);

        // pokemon actif
        if (ihm.getJeu().getJoueurActif().pokemonActifProperty().get() != null) {
            Label pokemonActif = new Label("Pokémon actif: " +
                    ihm.getJeu().getJoueurActif().pokemonActifProperty().get().getCartePokemon().getNom());
            pokemonActif.setFont(new Font("Arial", 14));
            pokemonActif.setStyle("-fx-text-fill: #333;");
            getChildren().add(pokemonActif);
        }

        // les actions possibles
        Label actions = new Label("Actions possibles:");
        actions.setFont(new Font("Arial", 14));
        actions.setStyle("-fx-font-weight: bold; -fx-text-fill: #333;");
        getChildren().add(actions);

        // pour les actions
        HBox boutons = new HBox(10);
        boutons.setAlignment(Pos.CENTER);

        // bouton pour attaquer
        Label attaquer = new Label("Attaquer");
        attaquer.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-padding: 5px 10px; -fx-border-radius: 5px; " +
                "-fx-font-weight: bold; -fx-cursor: hand;");
        attaquer.setOnMouseClicked(e -> {
            // logique attaque
        });

        // bouton pour jouer une carte
        Label jouerCarte = new Label("Jouer une carte");
        jouerCarte.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-padding: 5px 10px; -fx-border-radius: 5px; " +
                "-fx-font-weight: bold; -fx-cursor: hand;");
        jouerCarte.setOnMouseClicked(e -> {
            // logique du jeu de carte
        });

        boutons.getChildren().addAll(attaquer, jouerCarte);
        getChildren().add(boutons);
    }
}
