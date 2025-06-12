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
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import static java.util.Comparator.comparing;

import fr.umontpellier.iut.ptcgJavaFX.IPokemon;

public class VueJoueurActif extends VBox {
    private String idCarteEnergieSelectionnee = null;
    private List<String> cartesEnergieCompatibles = new ArrayList<>();

    @FXML private Label nomDuJoueur;
    @FXML private Button pokemonActif;
    @FXML private HBox panneauMain;
    @FXML private HBox panneauBanc;
    @FXML private HBox panneauEnergies;
    private ObjectProperty<? extends IJoueur> joueurActif;


    //ImageView i = new ImageView("/resources/images/test.png");


    private IJeu jeu;



    private String getCouleurEnergie(String type) {
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
            placerPokemonActif();
            placerBanc();
        } else {
            nomDuJoueur.setText("");
            panneauMain.getChildren().clear();
            panneauBanc.getChildren().clear();
            pokemonActif.setGraphic(null);
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

    private boolean estEnergieCompatible(ICarte carteEnergie) {
        if (carteEnergie.getTypeEnergie() == null) return false;
        
        // verifier la compatibilité avec le Pokémon actif
        if (joueurActif.get().pokemonActifProperty().get() != null) {
            return true; // Simplification: on suppose que tout Pokémon peut recevoir n'importe quelle énergie
        }
        
        // verifier la compatibilité avec les Pokémon sur le banc
        for (IPokemon pokemon : joueurActif.get().getBanc()) {
            if (pokemon != null) {
                return true; // on suppose que tout Pokémon peut recevoir n'importe quelle énergie
            }
        }
        
        return false;
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

        // séparer les autres cartes et les cartes énergie
        List<ICarte> cartesEnergie = main.stream()
                .filter(c -> c.getTypeEnergie() != null)
                .collect(Collectors.toList());

        List<ICarte> autresCartes = main.stream()
                .filter(c -> c.getTypeEnergie() == null)
                .collect(Collectors.toList());
                
        // Mettre à jour la liste des cartes énergie compatibles
        cartesEnergieCompatibles = cartesEnergie.stream()
                .filter(this::estEnergieCompatible)
                .map(ICarte::getId)
                .collect(Collectors.toList());
        // creer un contener avec les energies
        VBox conteneurEnergies = new VBox(5);
        conteneurEnergies.setStyle("-fx-padding: 5px; -fx-background-color: #ffffff; -fx-border-color: #999; -fx-border-radius: 5;");
        Label titreEnergies = new Label("Énergies disponibles:");
        titreEnergies.setStyle("-fx-font-weight: bold;");
        conteneurEnergies.getChildren().add(titreEnergies);

        HBox panneauEnergies = new HBox(5);
        panneauEnergies.setAlignment(Pos.CENTER_LEFT);
        conteneurEnergies.getChildren().add(panneauEnergies);

        // ajouter les energies
        cartesEnergie.forEach(carte -> {
            String imagePath = "/images/cartes/" + carte.getCode() + ".png";
            ImageView imageView = new ImageView();
            imageView.setFitHeight(80);
            imageView.setPreserveRatio(true);
            imageView.setPickOnBounds(true);
            imageView.setMouseTransparent(false);
            imageView.setFocusTraversable(true);

            try {
                java.io.InputStream stream = getClass().getResourceAsStream(imagePath);
                if (stream == null) {
                    System.err.println("Resource not found: " + imagePath);
                    return;
                }
                imageView.setImage(new Image(stream));
            } catch (Exception e) {
                System.err.println("Error loading image: " + imagePath);
                e.printStackTrace();
                return;
            }

            // Vérifier si l'énergie est compatible avec au moins un Pokémon
            boolean estCompatible = cartesEnergieCompatibles.contains(carte.getId());
            
            // Appliquer un style différent selon la compatibilité
            if (estCompatible) {
                imageView.setStyle("-fx-cursor: hand; -fx-opacity: 1.0;");
                imageView.setOnMouseClicked(e -> {
                    idCarteEnergieSelectionnee = carte.getId();
                    jeu.instructionProperty().set("Cliquez sur un Pokémon pour attacher l'énergie");
                    
                    // Mettre en surbrillance les Pokémon cibles valides
                    if (pokemonActif.getGraphic() != null) {
                        Node graphic = pokemonActif.getGraphic();
                        if (graphic instanceof Pane) {
                            graphic.setStyle(
                                "-fx-border-width: 2px; " +
                                "-fx-border-color: #ffcc00; " +
                                "-fx-border-radius: 5px;"
                            );
                        }
                    }
                    
                    // Mettre en surbrillance les Pokémon du banc
                    for (Node node : panneauBanc.getChildren()) {
                        if (node instanceof VBox) {
                            node.setStyle(
                                "-fx-border-width: 2px; " +
                                "-fx-border-color: #ffcc00; " +
                                "-fx-border-radius: 5px;"
                            );
                        }
                    }
                });
            } else {
                imageView.setStyle("-fx-cursor: not-allowed; -fx-opacity: 0.5;");
                imageView.setOnMouseClicked(e -> {
                    jeu.instructionProperty().set("Cette énergie n'est pas compatible avec vos Pokémon !");
                });
            }

            imageView.setOnMouseEntered(e -> imageView.setOpacity(0.7));
            imageView.setOnMouseExited(e -> imageView.setOpacity(1.0));

            panneauEnergies.getChildren().add(imageView);
        });

        //conteneur pour els autres cartes
        VBox conteneurAutresCartes = new VBox(5);
        conteneurAutresCartes.setStyle("-fx-padding: 5px; -fx-background-color: #f8f8f8; -fx-border-color: #999; -fx-border-radius: 5;");
        Label titreAutresCartes = new Label("Autres cartes:");
        titreAutresCartes.setStyle("-fx-font-weight: bold;");
        conteneurAutresCartes.getChildren().add(titreAutresCartes);

        HBox panneauAutresCartes = new HBox(5);
        panneauAutresCartes.setAlignment(Pos.CENTER_LEFT);
        conteneurAutresCartes.getChildren().add(panneauAutresCartes);

        // ajouter les cartes
        autresCartes.forEach(carte -> {
            String imagePath = "/images/cartes/" + carte.getCode() + ".png";
            ImageView imageView = new ImageView();
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            imageView.setPickOnBounds(true);

            try {
                java.io.InputStream stream = getClass().getResourceAsStream(imagePath);
                if (stream == null) {
                    System.err.println("Resource not found: " + imagePath);
                    Button btn = new Button(carte.getNom());
                    btn.setOnAction(e -> {
                        jeu.uneCarteDeLaMainAEteChoisie(carte.getId());
                        placerMain();
                        placerBanc();
                    });
                    panneauAutresCartes.getChildren().add(btn);
                    return;
                }
                imageView.setImage(new Image(stream));
            } catch (Exception ex) {
                System.err.println("Error loading image: " + imagePath);
                ex.printStackTrace();
                Button btn = new Button(carte.getNom());
                btn.setOnAction(evt -> {
                    jeu.uneCarteDeLaMainAEteChoisie(carte.getId());
                    placerMain();
                    placerBanc();
                });
                panneauAutresCartes.getChildren().add(btn);
                return;
            }

            imageView.setStyle("-fx-cursor: hand;");
            imageView.setOnMouseClicked(evt -> {
                jeu.uneCarteDeLaMainAEteChoisie(carte.getId());
                placerMain();
                placerBanc();
            });

            imageView.setOnMouseEntered(evt -> imageView.setOpacity(0.7));
            imageView.setOnMouseExited(evt -> imageView.setOpacity(1.0));

            panneauAutresCartes.getChildren().add(imageView);
        });

        // aout des conteneurs au panneau principal
        if (!cartesEnergie.isEmpty()) {
            panneauMain.getChildren().add(conteneurEnergies);
        }
        if (!autresCartes.isEmpty()) {
            panneauMain.getChildren().add(conteneurAutresCartes);
        }
    }

    private void placerBanc() {
        panneauBanc.getChildren().clear();
        if (joueurActif.get() == null) {
            System.out.println("Aucun joueur actif pour afficher le banc");
            return;
        }

        // nombre max empalcement
        int nombreEmplacementsBanc = 5;

        // un boutton pour chaque emplacement
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

                // afficher le pv avec le
                String pvText = String.format("%s\nPV: %d",
                        nomPokemon,
                        pokemon.pointsDeVieProperty() != null ? pokemon.pointsDeVieProperty().get() : 0);

                Label lblPokemon = new Label(pvText);
                lblPokemon.setStyle(
                        "-fx-font-size: 12px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-text-alignment: center;"
                );
                lblPokemon.setWrapText(true);
                lblPokemon.setMaxWidth(80);
                emplacement.getChildren().add(lblPokemon);

                // metre a jour avec un listener( si les pv changent le texte aussi)
                if (pokemon.pointsDeVieProperty() != null) {
                    pokemon.pointsDeVieProperty().addListener((obs, oldVal, newVal) -> {
                        lblPokemon.setText(String.format("%s\nPV: %d", nomPokemon, newVal));
                    });
                }
            } else {
                // un boutton clickeable
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

    private void configurerEcouteurPokemonActif() {
        // configuration de l'écouteur pour le Pokémon actif
        pokemonActif.setOnMouseClicked(e -> {
            if (idCarteEnergieSelectionnee != null) {
                if (joueurActif.get() == null || joueurActif.get().pokemonActifProperty().get() == null) {
                    jeu.instructionProperty().set("Aucun Pokémon actif sélectionné !");
                    return;
                }
                
                // verifier si l'énergie est toujours compatible (au cas où l'état a changé)
                if (!cartesEnergieCompatibles.contains(idCarteEnergieSelectionnee)) {
                    jeu.instructionProperty().set("Cette énergie n'est plus compatible !");
                    idCarteEnergieSelectionnee = null;
                    reinitialiserStyles();
                    return;
                }

                System.out.println("Tentative d'attachement de l'énergie: " + idCarteEnergieSelectionnee);

                try {
                    // appeler la méthode pour attacher l'énergie
                    jeu.uneCarteEnergieAEteChoisie(idCarteEnergieSelectionnee);

                    // mettre à jour le message de succès
                    jeu.instructionProperty().set("Énergie attachée avec succès !");

                    // mettre en surbrillance le Pokémon pour confirmer l'action
                    if (pokemonActif.getGraphic() != null) {
                        Node graphic = pokemonActif.getGraphic();
                        if (graphic instanceof Pane) {
                            graphic.setStyle(
                                    "-fx-border-width: 2px; " +
                                            "-fx-border-color: #00cc00; " +
                                            "-fx-border-radius: 5px;"
                            );

                            // reinitialiser le style après 1 seconde
                            new java.util.Timer().schedule(
                                    new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            javafx.application.Platform.runLater(() -> {
                                                graphic.setStyle("-fx-border-width: 0;");
                                            });
                                        }
                                    },
                                    1000
                            );
                        }
                    }

                } catch (Exception ex) {
                    System.err.println("Erreur lors de l'attachement de l'énergie: " + ex.getMessage());
                    jeu.instructionProperty().set("Erreur lors de l'attachement de l'énergie !");
                } finally {
                    // rnitialiser la sélection dans tous les cas
                    idCarteEnergieSelectionnee = null;

                    // update l'interface
                    placerPokemonActif();
                    placerMain();
                }
            }
        });
    }

    private void placerPokemonActif() {
        panneauEnergies.getChildren().clear();
        pokemonActif.getStyleClass().clear();
        pokemonActif.getStyleClass().add("pokemon-actif");
        pokemonActif.setGraphic(null);
        pokemonActif.setText("");

        // reset le style du Pokémon actif
        if (pokemonActif.getGraphic() != null) {
            Node graphic = pokemonActif.getGraphic();
            if (graphic instanceof Pane) {
                graphic.setStyle("-fx-border-width: 0;");
            }
        }

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

        // creer un conteneur pour l'image et les infos du Pokémon
        VBox pokemonContainer = new VBox(5);
        pokemonContainer.setAlignment(Pos.CENTER);

        // ajouter l'image du Pokémon
        try {
            String imagePath = "/images/cartes/" + poke.getCartePokemon().getCode() + ".png";
            java.io.InputStream stream = getClass().getResourceAsStream(imagePath);
            if (stream != null) {
                ImageView pokemonImage = new ImageView(new Image(stream));
                pokemonImage.setFitHeight(150);
                pokemonImage.setPreserveRatio(true);
                pokemonContainer.getChildren().add(pokemonImage);
            }
        } catch (Exception e) {
            System.err.println("er chargmt: " + e.getMessage());
        }

        // afficher la vie et le nom du pokemon

        Label nomLabel = new Label(nomPokemon);
        nomLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        int pv = (poke.pointsDeVieProperty() != null) ? poke.pointsDeVieProperty().get() : 0;
        String pvText = String.format("PV: %d", pv);

        Label pvLabel = new Label(pvText);
        pvLabel.setStyle(
                "-fx-font-size: 13px;" +
                        (pv <= 0 ? " -fx-text-fill: red;" : "")
        );

        pokemonContainer.getChildren().addAll(nomLabel, pvLabel);
        pokemonActif.setGraphic(pokemonContainer);

        // affiche les attaques disponible
        if (poke.attaquesProperty() != null && !poke.attaquesProperty().isEmpty()) {
            VBox attaquesBox = new VBox(5);
            attaquesBox.setStyle("-fx-padding: 5px; -fx-background-color: #f0f0f0; -fx-border-color: #999; -fx-border-radius: 5;");

            Label titreAttaques = new Label("Attaques :");
            titreAttaques.setStyle("-fx-font-weight: bold;");
            attaquesBox.getChildren().add(titreAttaques);

            poke.attaquesProperty().forEach(attaque -> {
                Button btnAttaque = new Button(attaque);
                btnAttaque.setStyle(
                        "-fx-font-size: 12px;" +
                                "-fx-padding: 2px 5px;" +
                                "-fx-background-color: #ffcc99;" +
                                "-fx-border-radius: 3;"
                );
                btnAttaque.setOnAction(e -> {
                    System.out.println("Attaque utilisée : " + attaque);
                    jeu.uneAttaqueAEteChoisie(attaque);
                });
                attaquesBox.getChildren().add(btnAttaque);
            });

            this.getChildren().removeIf(node -> node instanceof VBox && ((VBox)node).getChildren().stream()
                    .anyMatch(n -> n instanceof Label && ((Label)n).getText() != null && ((Label)n).getText().startsWith("Attaques :")));
            this.getChildren().add(attaquesBox);
        }

        // affiche les energies
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VueJoueurActif.fxml"));
            loader.setController(this);
            VBox root = loader.load();
            getChildren().add(root);

            // Configurer l'écouteur du Pokémon actif une seule fois
            configurerEcouteurPokemonActif();

            // Cvharger les styles
            getStylesheets().add(getClass().getResource("/css/vuejoueuractif.css").toExternalForm());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("ee", e);
        }
    }

    private void reinitialiserStyles() {
        // Réinitialiser le style du Pokémon actif
        if (pokemonActif.getGraphic() != null) {
            Node graphic = pokemonActif.getGraphic();
            if (graphic instanceof Pane) {
                graphic.setStyle("-fx-border-width: 0;");
            }
        }
        
        // Réinitialiser le style des Pokémon du banc
        for (Node node : panneauBanc.getChildren()) {
            if (node instanceof VBox) {
                node.setStyle(
                    "-fx-border-color: #999;" +
                    "-fx-border-radius: 5;" +
                    "-fx-padding: 5px;" +
                    "-fx-min-width: 80px;" +
                    "-fx-min-height: 40px;"
                );
            }
        }
    }

    private void setListenerChangementBanc(IJoueur joueur) {
        joueur.getBanc().addListener((ListChangeListener<? super IPokemon>) c -> {
            if (joueur == joueurActif.get()) {
                placerBanc();
                placerMain(); // update la compatibilité des énergies
            }
        });
        joueur.pokemonActifProperty().addListener((source, ancien, nouveau) -> {
            if (joueur == joueurActif.get()) {
                placerPokemonActif();
                placerMain(); // update la compatibilité des énergies
            }
        });
    }

    @FXML
    private void piocherCarte(javafx.event.ActionEvent event) {
        if (jeu != null && joueurActif != null && joueurActif.get() != null) {
            jeu.passerAEteChoisi();

            placerMain();
        }
    }
}