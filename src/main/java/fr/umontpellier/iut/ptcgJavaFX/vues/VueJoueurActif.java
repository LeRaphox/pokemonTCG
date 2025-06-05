package fr.umontpellier.iut.ptcgJavaFX.vues;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.collections.ListChangeListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Comparator.comparing;


/**
 * Cette classe présente les éléments appartenant au joueur Actif.
 * On y définit les bindings sur le joueur Actif, ainsi que le listener à exécuter lorsque ce joueur change
 */
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;

public class VueJoueurActif extends VBox {
    @FXML private Label nomDuJoueur;
    @FXML private Button pokemonActif;
    @FXML private HBox panneauMain;
    @FXML private HBox panneauBanc;
    private ObjectProperty<? extends IJoueur> joueurActif;
    private IJeu jeu;

    public void bindJeu(IJeu jeu) {
        this.jeu = jeu;
        bindJoueurActif();
        for (IJoueur joueur : jeu.getJoueurs()) {
            setListenerChangementMain(joueur);
            setListenerChangementBanc(joueur);
        }
    }

    private void bindJoueurActif() {
        joueurActif = jeu.joueurActifProperty();
        setJoueurActifChangeListener();
        if (joueurActif.get() != null) {
            nomDuJoueur.setText(joueurActif.get().getNom());
            placerMain();
        } else {
            nomDuJoueur.setText("");
            panneauMain.getChildren().clear();
            panneauBanc.getChildren().clear();
            pokemonActif.setText("");
        }
    }

    private void setJoueurActifChangeListener() {
        joueurActif.addListener((obs, oldJoueur, newJoueur) -> {
            if (newJoueur != null) {
                nomDuJoueur.setText(newJoueur.getNom());
                placerMain();
                placerBanc();
                placerPokemonActif();
            } else {
                nomDuJoueur.setText("");
                panneauMain.getChildren().clear();
                panneauBanc.getChildren().clear();
                pokemonActif.setText("");
            }
        });
    }

    private void setListenerChangementMain(IJoueur joueur) {
        joueur.getMain().addListener((ListChangeListener<ICarte>) change -> placerMain());
    }

    private void placerMain() {
        panneauMain.getChildren().clear();

        if (joueurActif.get() == null) return;
        List<? extends ICarte> main = new ArrayList<>(joueurActif.get().getMain());
        main.sort(comparing(ICarte::getNom));
        System.out.println("main " + main.size());
        for (ICarte carte : main) {
            Button btn = new Button(carte.getNom());
            btn.setStyle("-fx-font-size: 18px;");
            btn.setOnAction(e -> jeu.uneCarteDeLaMainAEteChoisie(carte.getId()));
            panneauMain.getChildren().add(btn);
        }
    }

    private void placerBanc() {
        panneauBanc.getChildren().clear();
        if (joueurActif.get() == null) return;
        List<? extends IPokemon> banc = new ArrayList<>(joueurActif.get().getBanc());
        //pour debug

        System.out.println("banc " + banc.size());
        for (int i = 0; i < banc.size(); i++) {
            IPokemon pokemon = banc.get(i);
            
            //ssi le pokemon n'existe pas on continue sinon ça pla,nte meme avec le if d'en bas n'enleve pas cette ligne
            if (pokemon == null) continue;

            String nomPokemon = "";
            if (pokemon.getCartePokemon() != null && pokemon.getCartePokemon().getNom() != null) {
                nomPokemon = pokemon.getCartePokemon().getNom();
            }
            Button btn = new Button(nomPokemon);
            btn.setStyle("-fx-font-size: 16px;");
            int idx = i;
            btn.setOnAction(e -> jeu.unEmplacementVideDuBancAEteChoisi(String.valueOf(idx)));
            panneauBanc.getChildren().add(btn);
        }
    }

    private void placerPokemonActif() {
        if (joueurActif.get() != null && joueurActif.get().pokemonActifProperty().get() != null) {
            IPokemon poke = joueurActif.get().pokemonActifProperty().get();
            String nomPokemon = "";
            if (poke.getCartePokemon() != null && poke.getCartePokemon().getNom() != null) {
                nomPokemon = poke.getCartePokemon().getNom();
            }
            pokemonActif.setText(nomPokemon);

        } else {
            pokemonActif.setText("");
            pokemonActif.setOnAction(null); // enlever l'action s'il n'y a pas de pokemon ne pas enelvre
        }
    }

    public VueJoueurActif() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/VueJoueurActif.fxml"));
        fxmlLoader.setController(this);
        try {
            VBox root = fxmlLoader.load();
            this.getChildren().addAll(root.getChildren());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.getStylesheets().add(getClass().getResource("/css/vuejoueuractif.css").toExternalForm());
    }

    private void setListenerChangementBanc(IJoueur joueur) {
        joueur.getBanc().addListener((ListChangeListener<? super IPokemon>) c -> {
            if (joueur == joueurActif.get()) placerBanc();
        });
        joueur.pokemonActifProperty().addListener((source, ancien, nouveau) -> {
            if (joueur == joueurActif.get()) placerPokemonActif();
        });
    }
}
