package fr.umontpellier.iut.ptcgJavaFX.vues;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
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

import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
//JE RE PUSH LE TRAVAIL DE RAPHAEKL CAR J AI FAIT UNE BETISE
public class VueJoueurActif extends VBox {
    @FXML private Label nomDuJoueur;
    @FXML private Button pokemonActif;
    @FXML private HBox panneauMain;
    @FXML private HBox panneauBanc;
    @FXML private HBox panneauEnergies;
    private ObjectProperty<? extends IJoueur> joueurActif;
    private IJeu jeu;

    private String getCouleurEnergie(String type) {
        if (type == null) {
            return "#cccccc";
        }
        return switch (type) {
            case "Feu" -> "#ff9999";
            case "Eau" -> "#99ccff";
            case "Plante" -> "#99ff99";
            case "Électrique" -> "#ffff99";
            case "Psy" -> "#ff99ff";
            case "Combat" -> "#ffcc99";
            default -> "#cccccc";
        };
    }

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
        if (joueurActif.get() == null) {
            System.out.println("Aucun joueur actif");
            return;
        }

        // creation d'une copie
        List<? extends ICarte> main = new ArrayList<>(joueurActif.get().getMain());
        main.sort(comparing(ICarte::getNom));

        System.out.println("=== MAIN DU JOUEUR ===");
        System.out.println("Nombre de cartes dans la main: " + main.size());
        main.forEach(c -> System.out.println("- " + c.getNom() + " (Type: " +
                (c.getTypeEnergie() != null ? c.getTypeEnergie().name() : "Non-Énergie") + ")"));
        System.out.println("=====================");

        // creation d'un bouton pour chaque carte
        main.forEach(carte -> {
            if (carte != null) {
                Button btn = new Button(carte.getNom());


                String style = "-fx-font-size: 14px;" +
                        "-fx-padding: 5px 10px;" +
                        "-fx-margin: 0 2px;" +
                        "-fx-background-radius: 10;";

                //style que pour les cartes energies
                if (carte.getTypeEnergie() != null) {
                    String typeEnergie = carte.getTypeEnergie().name();
                    style += "-fx-background-color: " + getCouleurEnergie(typeEnergie) + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;";

                    // pour les cartes energies on appele uneCarteEnergieAeteChosiie
                    btn.setOnAction(e -> {
                        if (joueurActif.get().pokemonActifProperty().get() != null) {
                            jeu.uneCarteEnergieAEteChoisie(carte.getId());
                            // metre a jour l'affichage
                            placerPokemonActif();
                        } else {
                            // s'il y a aucun pokmeon actif on affiche un message
                            jeu.instructionProperty().set("Aucun pokémon actif !!");
                        }
                    });
                } else {
                    // pour les autres cartes rien
                    btn.setOnAction(e -> jeu.uneCarteDeLaMainAEteChoisie(carte.getId()));
                }

                btn.setStyle(style);
                panneauMain.getChildren().add(btn);
            }
        });
    }

    private void placerBanc() {
        panneauBanc.getChildren().clear();
        if (joueurActif.get() == null) {
            System.out.println("Aucun joueur actif pour afficher le banc");
            return;
        }

        // Nombre d'emplacements de banc disponibles (5 dans Pokémon TCG)
        int nombreEmplacementsBanc = 5;

        // Créer un bouton pour chaque emplacement de banc
        for (int i = 0; i < nombreEmplacementsBanc; i++) {
            VBox emplacement = new VBox();
            emplacement.setAlignment(Pos.CENTER);
            emplacement.setStyle(
                    "-fx-border-color: #999;" +
                            "-fx-border-radius: 5;" +
                            "-fx-padding: 5px;" +
                            "-fx-min-width: 80px;" +
                            "-fx-min-height: 40px;"
            );

            // verfie qu'il n'y a pas de pokemon a cet emplacement
            List<? extends IPokemon> banc = new ArrayList<>(joueurActif.get().getBanc());
            if (i < banc.size() && banc.get(i) != null) {
                IPokemon pokemon = banc.get(i);
                String nomPokemon = (pokemon.getCartePokemon() != null) ?
                        pokemon.getCartePokemon().getNom() : "Pokémon " + (i + 1);

                Label lblPokemon = new Label(nomPokemon);
                lblPokemon.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                emplacement.getChildren().add(lblPokemon);
            } else {
                // Afficher un emplacement vide cliquable
                Button btnEmplacement = new Button("Emplacement " + (i + 1));
                btnEmplacement.setStyle(
                        "-fx-font-size: 10px;" +
                                "-fx-text-fill: #666;" +
                                "-fx-background-color: #f0f0f0;" +
                                "-fx-border-color: #ccc;" +
                                "-fx-border-radius: 3;"
                );

                int idx = i;
                btnEmplacement.setOnAction(e -> {
                    System.out.println("Emplacement de banc choisi: " + idx);
                    jeu.unEmplacementVideDuBancAEteChoisi(String.valueOf(idx));
                });

                emplacement.getChildren().add(btnEmplacement);
            }


            panneauBanc.getChildren().add(emplacement);
        }
    }

    private void placerPokemonActif() {
        panneauEnergies.getChildren().clear();

        if (joueurActif.get() == null || joueurActif.get().pokemonActifProperty().get() == null) {
            pokemonActif.setText("Aucun Pokémon actif");
            pokemonActif.setStyle(
                    "-fx-font-size: 14px;" +
                            "-fx-text-fill: #666;"
            );
            return;
        }

        IPokemon poke = joueurActif.get().pokemonActifProperty().get();
        String nomPokemon = (poke.getCartePokemon() != null && poke.getCartePokemon().getNom() != null) ?
                poke.getCartePokemon().getNom() : "Pokémon Actif";

        pokemonActif.setText(nomPokemon);
        pokemonActif.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;"
        );



        //affiche les energies
        if (poke.energieProperty() != null) {
            poke.energieProperty().forEach((type, energies) -> {
                if (energies != null && !energies.isEmpty()) {
                    Label lblEnergie = new Label(type + " (" + energies.size() + ")");
                    lblEnergie.setStyle(
                            "-fx-background-color: " + getCouleurEnergie(type) + ";" +
                                    "-fx-padding: 2px 8px;" +
                                    "-fx-margin: 0 3px;" +
                                    "-fx-border-radius: 10px;" +
                                    "-fx-background-radius: 10px;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-size: 14px;" +
                                    "-fx-font-weight: bold;"
                    );
                    panneauEnergies.getChildren().add(lblEnergie);
                }
            });
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