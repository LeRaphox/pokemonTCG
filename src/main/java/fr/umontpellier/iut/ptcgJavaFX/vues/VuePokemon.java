package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class VuePokemon extends Pane {
    private final IPokemon pokemon;
    private final boolean estActif;
    private final boolean highlight;
    private final Runnable onClick;
    private ImageView imageView;
    private Label nom;

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

        conteneur.getChildren().addAll(this.imageView, this.nom, pv);
        
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
