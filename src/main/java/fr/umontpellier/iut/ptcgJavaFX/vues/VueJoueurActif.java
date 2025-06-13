package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.beans.property.ObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.collections.ListChangeListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import static java.util.Comparator.comparing;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemonEvolution;

public class VueJoueurActif extends VBox {
    private String idCarteEnergieSelectionnee = null;
    private List<String> cartesEnergieCompatibles = new ArrayList<>();
    private String carteIdEvolution = null;
    private String evoltionBN = null;
    private boolean evoliutionEnCours = false;
    @FXML private Label nomDuJoueur;
    @FXML private Button pokemonActif;
    @FXML private HBox panneauMain;
    @FXML private HBox panneauBanc;
    @FXML private HBox panneauEnergies;
    private ObjectProperty<? extends IJoueur> joueurActif;
    private IJeu jeu;

    private VueResultats vueResultat; // a initialiser avec le bon PokemonTCGIHM

    public void setJoueur(IJoueur joueur) {
        this.joueurActif = jeu.joueurActifProperty();
        if (jeu != null) {
            vueResultat = new VueResultats((PokemonTCGIHM) jeu);
            this.getChildren().add(vueResultat);
        }
    }

    public void setJeu(IJeu jeu) {
        this.jeu = jeu;
    }

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
            return true; // simplification: on suppose que tout Pokémon peut recevoir n'importe quelle énergie
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

        // mise a jour de la liste des energies compatibles
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

            // verifier si l'énergie est compatible avec au moins un Pokémon
            boolean estCompatible = cartesEnergieCompatibles.contains(carte.getId());

            // Appliquer un style différent pour compatibilité
            if (estCompatible) {
                imageView.setStyle("-fx-cursor: hand; -fx-opacity: 1.0;");
                final ICarte carteFinale = carte;
                imageView.setOnMouseClicked(e -> {
                    idCarteEnergieSelectionnee = carteFinale.getId();
                    jeu.instructionProperty().set("Cliquez sur un Pokémon pour attacher l'énergie");

                    // Mettre en couleur les Pokémon cibles valides
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

                    // mettre en couleur les Pokémon du banc
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
        List<Node> cartesAAjouter = new ArrayList<>();
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

            // verifier si la carte est une carte evolution
            boolean estEvolutionJouable = false;
            if (carte instanceof CartePokemonEvolution evoCarte) {
                String baseName = evoCarte.getEvolutionDe();
                // actif
                IPokemon act = joueurActif.get().pokemonActifProperty().get();
                if (act != null && act.getCartePokemon() != null && baseName.equals(act.getCartePokemon().getNom()) && act.getPeutEvoluer()) {
                    estEvolutionJouable = true;
                }
                // banc
                if (!estEvolutionJouable) {
                    for (IPokemon p : joueurActif.get().getBanc()) {
                        if (p != null && p.getCartePokemon() != null && baseName.equals(p.getCartePokemon().getNom()) && p.getPeutEvoluer()) {
                            estEvolutionJouable = true;
                            break;
                        }
                    }
                }
            }

            if (estEvolutionJouable) {
                // colorier les cartes evolution disponible
                imageView.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, #00ff00, 10, 0.5, 0, 0);");
            } else {
                imageView.setStyle("-fx-cursor: not-allowed; -fx-opacity: 0.5;");
            }

            final ICarte carteFinale = carte;
            final boolean estEvolutionJouableFinale = estEvolutionJouable;
            imageView.setOnMouseClicked(evt -> {
                if (carteFinale instanceof CartePokemonEvolution) {
                    if (!estEvolutionJouableFinale) {
                        jeu.instructionProperty().set("Aucun pokemon ne peut evoluer avec cette carte");
                        return;
                    }
                    // demarrer le processus d'evolution
                    startEvolution((CartePokemonEvolution) carteFinale);
                } else {
                    // autre carte jouee directement
                    jeu.uneCarteDeLaMainAEteChoisie(carteFinale.getId());
                    placerMain();
                    placerBanc();
                }
            });

            imageView.setOnMouseEntered(evt -> {
                if (!(carteFinale instanceof CartePokemonEvolution && !estEvolutionJouableFinale)) {
                    imageView.setOpacity(0.7);
                }
            });
            imageView.setOnMouseExited(evt -> imageView.setOpacity(1.0));

            // ajouter un indicateur visuel pour les cartes d'évolution
            if (carte instanceof CartePokemonEvolution) {
                VBox carteAvecIndicateur = new VBox();
                carteAvecIndicateur.setAlignment(Pos.CENTER);

                // Ajouter un indicateur "Évolution" en haut de la carte
                Label indicateur = new Label("Évolution");
                indicateur.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 2px 5px; -fx-font-size: 10px; -fx-font-weight: bold; -fx-background-radius: 5px;");

                StackPane stackPane = new StackPane(imageView);
                stackPane.setAlignment(Pos.TOP_CENTER);
                stackPane.getChildren().add(indicateur);

                carteAvecIndicateur.getChildren().add(stackPane);
                cartesAAjouter.add(carteAvecIndicateur);
            } else {
                cartesAAjouter.add(imageView);
            }
        });

        panneauAutresCartes.getChildren().addAll(cartesAAjouter);
        if (!cartesAAjouter.isEmpty()) {
            panneauMain.getChildren().add(conteneurAutresCartes);
        }
        panneauMain.getChildren().add(conteneurEnergies);
    }

    // lance la phase de choix du pokemon a evoluer
    private void startEvolution(CartePokemonEvolution carte) {
        evoliutionEnCours = true;
        carteIdEvolution = carte.getId();
        evoltionBN = carte.getEvolutionDe();
        jeu.instructionProperty().set("choisissez le pokemon a evoluer");
        highlightEligiblePokemon();
    }

    // met en surbrillance les pokemons compatibles et gère la sélection
    private void highlightEligiblePokemon() {
        // actif
        if (pokemonActif.getGraphic() instanceof Pane pane && joueurActif.get() != null) {
            IPokemon p = joueurActif.get().pokemonActifProperty().get();
            if (p != null && p.getCartePokemon() != null && p.getCartePokemon().getNom().equals(evoltionBN) && p.getPeutEvoluer()) {
                pane.setStyle("-fx-border-width:2px;-fx-border-color:#ffcc00;-fx-border-radius:5px;");
                // pane.setOnMouseClicked(e -> {
                //     joueurActif.get().getEtatCourant().carteChoisie(p.getCartePokemon().getId());
                //     resetEvolutionSelection();
                // });
            }
        }
        // banc
        for (Node node : panneauBanc.getChildren()) {
            if (node instanceof VBox vb && vb.getUserData() instanceof IPokemon p) {
                if (p.getCartePokemon() != null && p.getCartePokemon().getNom().equals(evoltionBN) && p.getPeutEvoluer()) {
                    vb.setStyle("-fx-border-width:2px;-fx-border-color:#ffcc00;-fx-border-radius:5px;");
                }
            }
        }
    }

    // reset selection et styles
    private void resetEvolutionSelection() {
        evoliutionEnCours = false;
        carteIdEvolution = null;
        evoltionBN = null;
        reinitialiserStyles();
    }

    private void placerBanc() {
        panneauBanc.getChildren().clear();
        if (joueurActif.get() == null) {
            System.out.println("Aucun joueur actif pour afficher le banc");
            return;
        }

        // nombre max d'emplacements
        int nombreEmplacementsBanc = 5;
        List<? extends IPokemon> banc = new ArrayList<>(joueurActif.get().getBanc());

        // créer un emplacement pour chaque position
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

            // ajouter le Pokémon s'il existe à cette position
            if (i < banc.size() && banc.get(i) != null) {
                IPokemon pokemon = banc.get(i);
                boolean highlight = pokemon.getCartePokemon() != null &&
                        pokemon.getCartePokemon().getNom().equals(evoltionBN);

                int index = i; // Capture de l'index dans une variable finale
                VuePokemon vue = new VuePokemon(pokemon, false, highlight, () -> {
                    // si on est en mode évolution
                    if (evoliutionEnCours) {
                        // ssi c'est un Pokémon du banc
                        if (pokemon != joueurActif.get().pokemonActifProperty().get()) {

                            joueurActif.get().getEtatCourant().carteChoisie(pokemon.getCartePokemon().getId());
                            resetEvolutionSelection();
                        }
                    }
                    // Si on est en mode pour attacher les energies
                    else if (idCarteEnergieSelectionnee != null) {
                        try {
                            jeu.uneCarteEnergieAEteChoisie(idCarteEnergieSelectionnee);
                            jeu.instructionProperty().set("Énergie attachée avec succès !");
                        } catch (Exception ex) {
                            System.err.println("Erreur lors de l'attachement de l'énergie: " + ex.getMessage());
                            jeu.instructionProperty().set("Erreur lors de l'attachement de l'énergie !");
                        } finally {
                            idCarteEnergieSelectionnee = null;
                            placerMain();
                            placerBanc();
                        }
                    }
                    // si on est en mode échange
                    else if (joueurActif.get() != null && joueurActif.get().pokemonActifProperty().get() != null) {

                        jeu.unEmplacementVideDuBancAEteChoisi(String.valueOf(index));


                    }
                });
                emplacement.setUserData(pokemon);
                emplacement.getChildren().add(vue);
            } else {
                emplacement.setUserData(null);

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

                // vérifier si l'énergie est toujours compatible (au cas où l'état a changé)
                if (!cartesEnergieCompatibles.contains(idCarteEnergieSelectionnee)) {
                    jeu.instructionProperty().set("Cette énergie n'est plus compatible !");
                    idCarteEnergieSelectionnee = null;
                    return;
                }

                System.out.println("Tentative d'attachement de l'énergie: " + idCarteEnergieSelectionnee);

                try {
                    jeu.uneCarteEnergieAEteChoisie(idCarteEnergieSelectionnee);
                    jeu.instructionProperty().set("Énergie attachée avec succès !");
                } catch (Exception ex) {
                    System.err.println("Erreur lors de l'attachement de l'énergie: " + ex.getMessage());
                    jeu.instructionProperty().set("Erreur lors de l'attachement de l'énergie !");
                } finally {
                    idCarteEnergieSelectionnee = null;
                    placerMain();
                }
            } else if (evoliutionEnCours && joueurActif.get() != null && joueurActif.get().pokemonActifProperty().get() != null) {
                IPokemon p = joueurActif.get().pokemonActifProperty().get();
                if (p.getCartePokemon() != null && p.getCartePokemon().getNom().equals(evoltionBN)) {
                    jeu.uneCarteDeLaMainAEteChoisie(p.getCartePokemon().getId());
                    resetEvolutionSelection();
                    placerMain();
                    placerBanc();
                }
            } else if (joueurActif.get() != null && joueurActif.get().pokemonActifProperty().get() != null) {
                // Mettre en mode échange
                jeu.instructionProperty().set("Cliquez sur un Pokémon du banc pour échanger");

                // Ajouter un style temporaire pour indiquer le mode échange
                if (pokemonActif.getGraphic() instanceof Pane pane) {
                    pane.setStyle("-fx-border-width:2px;-fx-border-color:#4CAF50;-fx-border-radius:5px;");
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
        String nomPokemon = (poke.getCartePokemon() != null) ?
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
                    // verifier si l'attaque existe
                    if (poke.attaquesProperty() != null && poke.attaquesProperty().contains(attaque)) {
                        System.out.println("Attaque utilisée : " + attaque);
                        jeu.uneAttaqueAEteChoisie(attaque);
                    } else {
                        jeu.instructionProperty().set("Cette attaque n'est pas disponible !");
                    }
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