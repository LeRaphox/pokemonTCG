package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import javafx.beans.InvalidationListener;
import javafx.collections.MapChangeListener;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.List;

public class VuePokemon extends Pane {
    private final IPokemon pokemon;
    private final boolean estActif;
    private final boolean highlight;
    private final Runnable onClick;
    private ImageView imageView;
    private Label nom;

    private static String getCouleurEnergie(String type) {
        if (type == null) {
            return "#cccccc";
        }
        return switch (type) {
            case "FEU" -> "#ff9999";
            case "EAU" -> "#99ccff";
            case "PLANTE" -> "#99ff99";
            case "ELECTRIQUE" -> "#ffff99";
            case "PSY" -> "#ff99ff";
            case "COMBAT" -> "#ffcc99";
            default -> "#cccccc";
        };
    }

    public VuePokemon(IPokemon pokemon, boolean estActif, boolean highlight, Runnable onClick) {
        this.pokemon = pokemon;
        this.estActif = estActif;
        this.highlight = highlight;
        this.onClick = onClick;
        
        // conteneur principale
        VBox conteneur = new VBox(3);
        conteneur.setAlignment(Pos.CENTER);

        // initialisation
        this.imageView = new ImageView();
        this.nom = new Label();

        // l'image du pokemon
        this.imageView.setFitHeight(70);
        this.imageView.setPreserveRatio(true);
        String initialImagePath = "/images/pokemons/" + (pokemon.getCartePokemon() != null ? pokemon.getCartePokemon().getCode() : "back") + ".png";
        try {
            Image initialImg = new Image(getClass().getResourceAsStream(initialImagePath));
            this.imageView.setImage(initialImg);
        } catch (Exception e) {
            this.imageView.setImage(null); 
        }
        
        // nom et pv
        this.nom.setText(pokemon.getCartePokemon() != null ? pokemon.getCartePokemon().getNom() : "?");
        int pvValue = pokemon.pointsDeVieProperty() != null ? pokemon.pointsDeVieProperty().get() : 0;
        Label pv = new Label("PV: " + pvValue);
        
        // j'ajoute un listenr pour les points de vie
        if (pokemon.pointsDeVieProperty() != null) {
            pokemon.pointsDeVieProperty().addListener((obs, oldVal, newVal) -> {
                pv.setText("PV: " + newVal);
            });
        }

        // j'ajoute un listener pour le changement de carte
        if (pokemon.cartePokemonProperty() != null) {
            pokemon.cartePokemonProperty().addListener((obs, oldCarte, newCarte) -> {
                if (newCarte != null) {
                    String imagePath = "/images/pokemons/" + newCarte.getCode() + ".png";
                    try {
                        Image img = new Image(getClass().getResourceAsStream(imagePath));
                        this.imageView.setImage(img); 
                    } catch (Exception e) {
                        System.err.println("Erreur lors du charegment de l'image " + newCarte.getCode() + " - " + e.getMessage());
                        this.imageView.setImage(null); 
                    }
                    this.nom.setText(newCarte.getNom()); 
                } else {
                    this.imageView.setImage(null);
                    this.nom.setText("?");
                }
            });
        }

        // j'ajoute un listener pour les énergies
        if (pokemon.energieProperty() != null) {
            pokemon.energieProperty().addListener((MapChangeListener<? super String, ? super List<String>>) (change) -> {
                // Mettre à jour l'affichage des énergies
                VBox energieBox = new VBox(2);
                energieBox.setAlignment(Pos.CENTER);
                
                pokemon.energieProperty().forEach((type, energies) -> {
                    if (energies != null && !energies.isEmpty()) {
                        Label lblEnergie = new Label(type + " (" + energies.size() + ")");
                        lblEnergie.setStyle(
                            "-fx-background-color: " + getCouleurEnergie(type) + ";" +
                            "-fx-padding: 2px 8px;" +
                            "-fx-margin: 0 3px;" +
                            "-fx-border-radius: 10px;" +
                            "-fx-background-radius: 10px;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;"
                        );
                        energieBox.getChildren().add(lblEnergie);
                    }
                });
                
                // Remplacer l'ancienne boîte d'énergies
                VBox container = (VBox) getChildren().get(0);
                if (container.getChildren().size() > 3) {
                    container.getChildren().remove(3);
                }
                container.getChildren().add(energieBox);
            });
        }

        // ajouter les énergies
        VBox energieBox = new VBox(2);
        energieBox.setAlignment(Pos.CENTER);
        
        if (pokemon.energieProperty() != null) {
            pokemon.energieProperty().forEach((type, energies) -> {
                if (energies != null && !energies.isEmpty()) {
                    Label lblEnergie = new Label(type + " (" + energies.size() + ")");
                    lblEnergie.setStyle(
                        "-fx-background-color: " + getCouleurEnergie(type) + ";" +
                        "-fx-padding: 2px 8px;" +
                        "-fx-margin: 0 3px;" +
                        "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;"
                    );
                    energieBox.getChildren().add(lblEnergie);
                }
            });
        }

        conteneur.getChildren().addAll(this.imageView, this.nom, pv, energieBox);
        
        // creeer le conteneur avec le style
        StackPane stack = new StackPane();
        stack.getChildren().add(conteneur);
        
        // ajout du contour
        if (highlight) {
            Rectangle rect = new Rectangle(80, 100);
            rect.setArcWidth(8);
            rect.setArcHeight(8);
            rect.setFill(Color.TRANSPARENT);
            rect.setStroke(Color.web("#ffcc00"));
            rect.setStrokeWidth(3);
            stack.getChildren().add(rect);
        }
        
        // styke soecifique pour le pokemon actif
        if (estActif) {
            stack.setStyle("-fx-border-width:2px;-fx-border-color:#00ccee;-fx-border-radius:5px;");
        }
        
        // j'ajoute le conteneur au pane
        getChildren().add(stack);
        
        // gestionnaire du click
        setOnMouseClicked(e -> {
            if (onClick != null) onClick.run();
        });
    }
}
