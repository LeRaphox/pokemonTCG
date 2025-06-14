package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox; // Ensure HBox is imported
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Pokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemon;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.IOException;
import java.io.InputStream;

public class VueDuJeu extends BorderPane {

    private IJeu jeu;

    // FXML existants
    @FXML private Label instructionLabel;
    @FXML private Button boutonPasser;
    @FXML private ImageView grandeCarteActiveView;
    @FXML private VBox conteneurBas;
    @FXML private ScrollPane scrollPanePourVueJoueur;
    @FXML private HBox energiesPokemonActifHBoxJeu;
    @FXML private VBox attaquesDisponiblesVBoxJeu;
    @FXML private Button boutonEchangerPokemon;
    @FXML private Label pvPokemonActifLabel;
    @FXML private Button boutonUtiliserTalent;
    @FXML private HBox bancJoueurActifCentreVBox;

    // Nouveaux FXML pour la zone adversaire
    @FXML private Label nomAdversaireLabel;
    @FXML private ImageView pokemonActifAdversaireView;
    @FXML private Label pvPokemonActifAdversaireLabel;
    @FXML private HBox bancAdversaireHBox;
    @FXML private HBox energiesPokemonActifAdversaireHBox;
    @FXML private VBox attaquesAdversaireVBox;
    @FXML private Label nbCartesMainAdversaireLabel;
    @FXML private Label nbCartesPiocheAdversaireLabel;
    @FXML private Label nbCartesDefausseAdversaireLabel;
    @FXML private Label nbRecompensesAdversaireLabel;

    // Nouveaux FXML pour les compteurs du JOUEUR ACTIF
    @FXML private Label nbCartesMainJoueurActifLabel;
    @FXML private Label nbCartesPiocheJoueurActifLabel;
    @FXML private Label nbCartesDefausseJoueurActifLabel;
    @FXML private Label nbRecompensesJoueurActifLabel;

    private VueJoueurActif vueJoueurActif;
    private String idPokemonActifCourant_PourGrandeCarte = null;
    private boolean estEnModeAttachementEnergie_Global = false;
    private MapChangeListener<String, List<String>> energiesPokemonActifListenerJeu;
    private javafx.collections.ListChangeListener<String> attaquesPokemonActifListenerJeu;
    private IPokemon pokemonActifObserveCourant = null;
    private boolean modeSelectionRemplacantApresRetraiteActif = false;
    private boolean modePaiementCoutRetraiteActif = false;
    private int coutRetraiteRestant = 0;
    private boolean modeSelectionBasePourEvolution = false;
    private fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemonEvolution carteEvolutionSelectionnee = null;
    private boolean modeDefausseEnergieAttaqueVue = false;

    private ObjectProperty<IJoueur> adversaireProperty = new SimpleObjectProperty<>(null);
    private Map<String, MapChangeListener<String, List<String>>> listenersEnergiesBancCentre = new HashMap<>();

    // Listener for bench changes for the central display
    private final ListChangeListener<IPokemon> bancCentreChangeListener = c -> {
        while (c.next()) {
            if (c.wasRemoved()) {
                for (IPokemon removedPokemon : c.getRemoved()) {
                    if (removedPokemon != null && removedPokemon.getCartePokemon() != null && removedPokemon.energieProperty() != null && listenersEnergiesBancCentre.containsKey(removedPokemon.getCartePokemon().getId())) {
                        MapChangeListener<String, List<String>> listener = listenersEnergiesBancCentre.remove(removedPokemon.getCartePokemon().getId());
                        if (listener != null) {
                            removedPokemon.energieProperty().removeListener(listener);
                        }
                    }
                }
            }
            // For additions, placerBancCentre() will handle setting up new energy listeners
            // when it re-renders the Pokémon.
        }
        placerBancCentre(); // Re-render the whole bench on any change
    };


    public VueDuJeu(IJeu jeu) {
        this.jeu = jeu;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VueDuJeu.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.vueJoueurActif = new VueJoueurActif(this, this.jeu, this.jeu.joueurActifProperty());
        if (scrollPanePourVueJoueur != null) {
            scrollPanePourVueJoueur.setContent(vueJoueurActif);
        } else {
            System.err.println("[VueDuJeu] Erreur: scrollpane est nul");
            if (conteneurBas != null) {
                conteneurBas.getChildren().add(0, vueJoueurActif);
            }
        }
        creerBindings();
    }

    private void mettreAJourGrandeCarteActive() {
        if (grandeCarteActiveView == null) {
            System.err.println("[VueDuJeu] ERREUR: gandecarteactive est null");
            idPokemonActifCourant_PourGrandeCarte = null;
        }

        IJoueur joueurCourant = this.jeu.joueurActifProperty().get();
        IPokemon pokemonPrecedent = pokemonActifObserveCourant;
        IPokemon pokemonCourantPourAffichage = null;

        if (joueurCourant != null && joueurCourant.pokemonActifProperty().get() != null) {
            pokemonCourantPourAffichage = joueurCourant.pokemonActifProperty().get();
            ICarte cartePokemon = pokemonCourantPourAffichage.getCartePokemon();
            idPokemonActifCourant_PourGrandeCarte = cartePokemon.getId();
            String imagePath = "/images/cartes/" + cartePokemon.getCode() + ".png";
            InputStream imageStream = getClass().getResourceAsStream(imagePath);

            if (imageStream == null) {
                System.err.println("[VueDuJeu] pas d'image trouver : " + imagePath);
                if(grandeCarteActiveView!=null) grandeCarteActiveView.setImage(null);
                idPokemonActifCourant_PourGrandeCarte = null;
            } else {
                if(grandeCarteActiveView!=null) grandeCarteActiveView.setImage(new Image(imageStream));
            }
        } else {
            if(grandeCarteActiveView!=null) grandeCarteActiveView.setImage(null);
            idPokemonActifCourant_PourGrandeCarte = null;
        }

        pokemonActifObserveCourant = pokemonCourantPourAffichage;

        if (pvPokemonActifLabel != null) {
            if (pokemonActifObserveCourant != null && pokemonActifObserveCourant.getCartePokemon() != null) {
                StringBinding pvBinding = Bindings.createStringBinding(() -> {
                    if (pokemonActifObserveCourant == null || pokemonActifObserveCourant.getCartePokemon() == null) {
                        return "--/-- PV";
                    }
                    CartePokemon cartePkm = (CartePokemon) pokemonActifObserveCourant.getCartePokemon();
                    return String.format("%d/%d PV",
                            pokemonActifObserveCourant.pointsDeVieProperty().get(),
                            cartePkm.getPointsVie());
                }, pokemonActifObserveCourant.pointsDeVieProperty(), pokemonActifObserveCourant.cartePokemonProperty());
                pvPokemonActifLabel.textProperty().bind(pvBinding);
                pvPokemonActifLabel.setVisible(true);
            } else {
                if (pvPokemonActifLabel.textProperty().isBound()) {
                    pvPokemonActifLabel.textProperty().unbind();
                }
                pvPokemonActifLabel.setText("--/-- PV");
                pvPokemonActifLabel.setVisible(false);
            }
        }

        if (boutonUtiliserTalent != null) {
            if (pokemonActifObserveCourant != null && jeu.joueurActifProperty().get() != null && !jeu.finDePartieProperty().get()) {
                Pokemon pokemonMecanique = (Pokemon) pokemonActifObserveCourant;
                boolean peutUtiliser = pokemonMecanique.peutUtiliserTalent();
                boutonUtiliserTalent.setDisable(!peutUtiliser);
            } else {
                boutonUtiliserTalent.setDisable(true);
            }
        }

        appliquerStyleGrandeCarteActive();

        if (pokemonPrecedent != pokemonActifObserveCourant) {
            if (pokemonPrecedent != null) {
                if (pokemonPrecedent.energieProperty() != null && energiesPokemonActifListenerJeu != null) {
                    pokemonPrecedent.energieProperty().removeListener(energiesPokemonActifListenerJeu);
                }
                if (pokemonPrecedent.attaquesProperty() != null && attaquesPokemonActifListenerJeu != null) {
                    pokemonPrecedent.attaquesProperty().removeListener(attaquesPokemonActifListenerJeu);
                }
            }

            if (pokemonActifObserveCourant != null) {
                afficherEnergiesGenerique(pokemonActifObserveCourant, energiesPokemonActifHBoxJeu, true);
                afficherAttaquesGenerique(pokemonActifObserveCourant, attaquesDisponiblesVBoxJeu, true);
                if (energiesPokemonActifListenerJeu == null) {
                    energiesPokemonActifListenerJeu = change -> {
                        afficherEnergiesGenerique(pokemonActifObserveCourant, energiesPokemonActifHBoxJeu, true);
                        afficherAttaquesGenerique(pokemonActifObserveCourant, attaquesDisponiblesVBoxJeu, true);
                    };
                }
                if (pokemonActifObserveCourant.energieProperty() != null) {
                    pokemonActifObserveCourant.energieProperty().addListener(energiesPokemonActifListenerJeu);
                }
                if (attaquesPokemonActifListenerJeu == null) {
                    attaquesPokemonActifListenerJeu = change -> afficherAttaquesGenerique(pokemonActifObserveCourant, attaquesDisponiblesVBoxJeu, true);
                }
                if (pokemonActifObserveCourant.attaquesProperty() != null) {
                    pokemonActifObserveCourant.attaquesProperty().addListener(attaquesPokemonActifListenerJeu);
                }
            } else {
                if (energiesPokemonActifHBoxJeu != null) energiesPokemonActifHBoxJeu.getChildren().clear();
                if (attaquesDisponiblesVBoxJeu != null) attaquesDisponiblesVBoxJeu.getChildren().clear();
            }
        } else if (pokemonActifObserveCourant != null) {
            afficherEnergiesGenerique(pokemonActifObserveCourant, energiesPokemonActifHBoxJeu, true);
            afficherAttaquesGenerique(pokemonActifObserveCourant, attaquesDisponiblesVBoxJeu, true);
        }
    }

    private Type getTypeFromLetter(String letter) {
        if (letter == null || letter.isEmpty()) return null;
        for (Type t : Type.values()) {
            if (t.asLetter().equalsIgnoreCase(letter)) return t;
        }
        return null;
    }

    private void peuplerConteneurEnergies(IPokemon pokemon, HBox conteneurEnergies) {
        if (conteneurEnergies == null) return;
        conteneurEnergies.getChildren().clear();
        if (pokemon == null || pokemon.getCartePokemon() == null || pokemon.energieProperty() == null) return;

        ObservableMap<String, List<String>> energiesMap = pokemon.energieProperty();
        if (energiesMap == null || energiesMap.isEmpty()) return;

        for (Map.Entry<String, List<String>> entry : energiesMap.entrySet()) {
            List<String> listeIdsEnergies = entry.getValue();
            int nombreEnergies = (listeIdsEnergies == null) ? 0 : listeIdsEnergies.size();
            if (nombreEnergies == 0) continue;

            Type typeEnum = getTypeFromLetter(entry.getKey());
            if (typeEnum == null) {
                Label errorTypeLabel = new Label(entry.getKey() + "?x" + nombreEnergies);
                errorTypeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: purple;");
                conteneurEnergies.getChildren().add(errorTypeLabel);
                continue;
            }
            String cheminImageEnergie = "/images/energie/" + typeEnum.asLetter() + ".png";
            InputStream imageStream = getClass().getResourceAsStream(cheminImageEnergie);

            if (imageStream == null) {
                Label errorImgLabel = new Label(typeEnum.asLetter() + "x" + nombreEnergies);
                errorImgLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: red;");
                conteneurEnergies.getChildren().add(errorImgLabel);
            } else {
                ImageView imgEnergieView = new ImageView(new Image(imageStream));
                imgEnergieView.setFitHeight(15);
                imgEnergieView.setFitWidth(15);
                Label lblNombre = new Label("x" + nombreEnergies);
                lblNombre.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 0 0 0 1px;");
                HBox energieGroupe = new HBox(imgEnergieView, lblNombre);
                energieGroupe.setSpacing(1);
                energieGroupe.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                conteneurEnergies.getChildren().add(energieGroupe);
            }
        }
    }


    private void afficherEnergiesGenerique(IPokemon pokemon, HBox conteneurEnergies, boolean cliquablePourDefausse) {
        if (conteneurEnergies == null) return;
        conteneurEnergies.getChildren().clear();
        if (pokemon == null || pokemon.getCartePokemon() == null || pokemon.energieProperty() == null) return;

        ObservableMap<String, List<String>> energiesMap = pokemon.energieProperty();
        if (energiesMap == null || energiesMap.isEmpty()) return;

        boolean energiesDefaussablesPourRetraite = cliquablePourDefausse && this.modePaiementCoutRetraiteActif && this.coutRetraiteRestant > 0 && pokemon == this.pokemonActifObserveCourant;
        boolean energiesDefaussablesPourAttaque = this.modeDefausseEnergieAttaqueVue && pokemon == this.pokemonActifObserveCourant;

        for (Map.Entry<String, List<String>> entry : energiesMap.entrySet()) {
            List<String> listeIdsEnergies = entry.getValue();
            int nombreEnergies = (listeIdsEnergies == null) ? 0 : listeIdsEnergies.size();
            if (nombreEnergies == 0) continue;

            Type typeEnum = getTypeFromLetter(entry.getKey());
            if (typeEnum == null) {
                Label errorTypeLabel = new Label(entry.getKey() + "?x" + nombreEnergies);
                errorTypeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: purple;");
                conteneurEnergies.getChildren().add(errorTypeLabel);
                continue;
            }
            String cheminImageEnergie = "/images/energie/" + typeEnum.asLetter() + ".png";

            if (energiesDefaussablesPourRetraite || energiesDefaussablesPourAttaque) {
                if (listeIdsEnergies != null) {
                    for (String idCarteEnergie : listeIdsEnergies) {
                        InputStream imgStreamIndiv = getClass().getResourceAsStream(cheminImageEnergie);
                        if (imgStreamIndiv == null) {
                            Label errorImgLabel = new Label(typeEnum.asLetter() + "!");
                            errorImgLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: orange;");
                            conteneurEnergies.getChildren().add(errorImgLabel);
                            continue;
                        }
                        ImageView imgEnergieViewClic = new ImageView(new Image(imgStreamIndiv));
                        imgEnergieViewClic.setFitHeight(20); imgEnergieViewClic.setFitWidth(20);
                        Button boutonEnergieIndividuelle = new Button();
                        boutonEnergieIndividuelle.setGraphic(imgEnergieViewClic);
                        boutonEnergieIndividuelle.setStyle("-fx-padding: 1px; -fx-background-color: transparent; -fx-border-color: gold; -fx-border-width: 1px;");
                        final String idPourAction = idCarteEnergie;

                        if (energiesDefaussablesPourRetraite) {
                            boutonEnergieIndividuelle.setOnAction(event -> energieDuPokemonActifChoisiePourDefausse(idPourAction));
                        } else {
                            boutonEnergieIndividuelle.setOnAction(event -> {
                                System.out.println("[VueDuJeu] Clic sur énergie " + idPourAction + " pour défausse d'attaque (modeDefausseEnergieAttaqueVue). Appel à jeu.uneCarteComplementaireAEteChoisie().");
                                this.jeu.uneCarteComplementaireAEteChoisie(idPourAction);
                            });
                        }
                        boutonEnergieIndividuelle.setOnMouseEntered(e -> boutonEnergieIndividuelle.setStyle("-fx-padding: 1px; -fx-background-color: lightgoldenrodyellow; -fx-border-color: darkgoldenrod; -fx-border-width: 2px;"));
                        boutonEnergieIndividuelle.setOnMouseExited(e -> boutonEnergieIndividuelle.setStyle("-fx-padding: 1px; -fx-background-color: transparent; -fx-border-color: gold; -fx-border-width: 1px;"));
                        conteneurEnergies.getChildren().add(boutonEnergieIndividuelle);
                    }
                }
            } else {
                InputStream imageStream = getClass().getResourceAsStream(cheminImageEnergie);
                if (imageStream == null) {
                    Label errorImgLabel = new Label(typeEnum.asLetter() + "x" + nombreEnergies);
                    errorImgLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: red;");
                    conteneurEnergies.getChildren().add(errorImgLabel);
                } else {
                    ImageView imgEnergieView = new ImageView(new Image(imageStream));
                    imgEnergieView.setFitHeight(20); imgEnergieView.setFitWidth(20);
                    Button boutonEnergieGroupe = new Button();
                    boutonEnergieGroupe.setGraphic(imgEnergieView);
                    boutonEnergieGroupe.setText("x" + nombreEnergies);
                    boutonEnergieGroupe.setStyle("-fx-padding: 2px;");
                    conteneurEnergies.getChildren().add(boutonEnergieGroupe);
                }
            }
        }
    }

    private void afficherAttaquesGenerique(IPokemon pokemon, VBox conteneurAttaques, boolean cliquable) {
        if (conteneurAttaques == null) return;
        conteneurAttaques.getChildren().clear();
        if (pokemon == null) return;

        javafx.collections.ObservableList<String> nomsAttaques = pokemon.attaquesProperty();
        if (nomsAttaques == null || nomsAttaques.isEmpty()) {
            Label pasDAttaqueLabel = new Label(cliquable ? "aucune attaque disponible" : "(aucune attaque)");
            pasDAttaqueLabel.setStyle("-fx-font-style: italic; -fx-font-size: 12px;");
            conteneurAttaques.getChildren().add(pasDAttaqueLabel);
            return;
        }

        for (String nomAttaque : nomsAttaques) {
            Button boutonAttaque = new Button(nomAttaque);
            boutonAttaque.setMaxWidth(Double.MAX_VALUE);
            if (cliquable) {
                boutonAttaque.setOnAction(event -> this.jeu.uneAttaqueAEteChoisie(nomAttaque));
            } else {
                boutonAttaque.setDisable(true);
                boutonAttaque.setStyle("-fx-font-size: 11px; -fx-opacity: 0.8;");
            }
            conteneurAttaques.getChildren().add(boutonAttaque);
        }
    }


    private void appliquerStyleGrandeCarteActive() {
        if (grandeCarteActiveView == null) return;
        String styleFinal = "";
        if (modeSelectionBasePourEvolution && carteEvolutionSelectionnee != null && idPokemonActifCourant_PourGrandeCarte != null && jeu.joueurActifProperty().get() != null) {
            fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur joueurMecanique = (fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur) jeu.joueurActifProperty().get();
            if (joueurMecanique != null && carteEvolutionSelectionnee != null) {
                List<String> ciblesValides = carteEvolutionSelectionnee.getChoixPossibles(joueurMecanique);
                if (ciblesValides.contains(idPokemonActifCourant_PourGrandeCarte)) {
                    styleFinal = "-fx-effect: dropshadow(gaussian, lawngreen, 20, 0.8, 0.0, 0.0); -fx-border-color: lawngreen; -fx-border-width: 4;";
                }
            }
        } else if (estEnModeAttachementEnergie_Global && idPokemonActifCourant_PourGrandeCarte != null) {
            IJoueur joueurCourant = jeu.joueurActifProperty().get();
            if (joueurCourant != null && joueurCourant.getChoixComplementaires().stream().anyMatch(c -> c.getId().equals(idPokemonActifCourant_PourGrandeCarte))) {
                styleFinal = "-fx-effect: dropshadow(gaussian, gold, 15, 0.7, 0.0, 0.0); -fx-border-color: gold; -fx-border-width: 3;";
            }
        }
        grandeCarteActiveView.setStyle(styleFinal);
    }

    private HBox findEnergiesHBoxForPokemonCentre(String pokemonId) {
        if (bancJoueurActifCentreVBox == null) return null;
        for (javafx.scene.Node node : bancJoueurActifCentreVBox.getChildren()) {
            if (node instanceof VBox) {
                VBox pokemonContainer = (VBox) node;
                if (pokemonContainer.getUserData() != null && pokemonContainer.getUserData().equals(pokemonId)) {
                    for(javafx.scene.Node subNode : pokemonContainer.getChildren()){
                        if(subNode instanceof HBox){
                            return (HBox) subNode;
                        }
                    }
                }
            }
        }
        return null;
    }


    public void creerBindings() {
        if (instructionLabel != null) {
            instructionLabel.textProperty().bind(jeu.instructionProperty());
        }

        this.jeu.instructionProperty().addListener((obs, oldInstruction, newInstruction) -> {
            // System.out.println("[VueDuJeu] Instruction changée: \"" + newInstruction + "\" (Ancienne: \"" + oldInstruction + "\")"); // Reduced verbosity
            String instructionLower = newInstruction.toLowerCase();

            boolean previousModeEvo = modeSelectionBasePourEvolution;
            boolean previousModePaiementRetraite = modePaiementCoutRetraiteActif;
            boolean previousModeSelectionRemplacant = modeSelectionRemplacantApresRetraiteActif;
            boolean previousModeAttachementEnergie = estEnModeAttachementEnergie_Global;

            modePaiementCoutRetraiteActif = false;
            modeSelectionRemplacantApresRetraiteActif = false;
            estEnModeAttachementEnergie_Global = false;
            modeDefausseEnergieAttaqueVue = false;

            if (instructionLower.startsWith("défaussez") && instructionLower.contains("énergie") && !instructionLower.equals("défaussez une énergie de ce pokémon.")) {
                // System.out.println("[VueDuJeu] Activation MODE PAIEMENT COUT RETRAITE.");
                modePaiementCoutRetraiteActif = true;
                modeSelectionBasePourEvolution = false;
                carteEvolutionSelectionnee = null;
                try {
                    String[] parts = instructionLower.split(" ");
                    coutRetraiteRestant = Integer.parseInt(parts[1]);
                    // System.out.println("[VueDuJeu] cout retraite restant: " + coutRetraiteRestant);
                } catch (Exception e) {
                    System.err.println("[VueDuJeu] erreur parsing cout retraite: " + newInstruction);
                    coutRetraiteRestant = 1;
                }
                if (boutonEchangerPokemon != null) boutonEchangerPokemon.setDisable(true);
                if (vueJoueurActif != null) {
                    vueJoueurActif.setModeSelectionPourRemplacantApresRetraite(false);
                    vueJoueurActif.setHandInteractionsActive(false);
                }

            } else if (instructionLower.equals("choisissez un nouveau pokémon actif.")) {
                // System.out.println("[VueDuJeu] Activation du mode retrait");
                modeSelectionRemplacantApresRetraiteActif = true;
                modeSelectionBasePourEvolution = false;
                carteEvolutionSelectionnee = null;
                coutRetraiteRestant = 0;
                if (boutonEchangerPokemon != null) boutonEchangerPokemon.setDisable(true);
                if (vueJoueurActif != null) {
                    vueJoueurActif.setModeSelectionPourRemplacantApresRetraite(true);
                }

            } else if (instructionLower.equals("choisissez un pokémon à faire évoluer")) {
            } else if (instructionLower.contains("choisissez un pokémon") && instructionLower.contains("énergie")) {
                estEnModeAttachementEnergie_Global = true;
                modeSelectionBasePourEvolution = false;
                carteEvolutionSelectionnee = null;
                if (vueJoueurActif != null) {
                    List<String> idsCiblesBancVueJoueurActif = List.of();
                    if (this.jeu.joueurActifProperty().get() != null) {
                        idsCiblesBancVueJoueurActif = this.jeu.joueurActifProperty().get().getBanc().stream()
                                .filter(p -> p != null)
                                .map(p -> p.getCartePokemon().getId())
                                .collect(Collectors.toList());
                    }
                    vueJoueurActif.informerModeAttachementEnergie(true, idsCiblesBancVueJoueurActif);
                }

            } else if (instructionLower.equals("défaussez une énergie de ce pokémon.")) {
                // System.out.println("[VueDuJeu] Activation MODE DEFAUSSE ENERGIE ATTAQUE.");
                modeDefausseEnergieAttaqueVue = true;
                modePaiementCoutRetraiteActif = false;
                modeSelectionRemplacantApresRetraiteActif = false;
                estEnModeAttachementEnergie_Global = false;
                modeSelectionBasePourEvolution = false;
                carteEvolutionSelectionnee = null;
                coutRetraiteRestant = 0;

                if (boutonEchangerPokemon != null) boutonEchangerPokemon.setDisable(true);

                if (vueJoueurActif != null) {
                    vueJoueurActif.setModeSelectionPourRemplacantApresRetraite(false);
                    vueJoueurActif.informerModeAttachementEnergie(false, List.of());
                    vueJoueurActif.rafraichirAffichagePourSelectionEvolution();
                    vueJoueurActif.setHandInteractionsActive(false);
                }
            } else {
                modeSelectionBasePourEvolution = false;
                carteEvolutionSelectionnee = null;
                coutRetraiteRestant = 0;
                modeDefausseEnergieAttaqueVue = false;

                IJoueur joueurCourant = jeu.joueurActifProperty().get();
                if (boutonEchangerPokemon != null && joueurCourant != null) {
                    boutonEchangerPokemon.setDisable(!joueurCourant.peutRetraiteProperty().get());
                } else if (boutonEchangerPokemon != null) {
                    boutonEchangerPokemon.setDisable(true);
                }

                if (vueJoueurActif != null) {
                    if(previousModeSelectionRemplacant) vueJoueurActif.setModeSelectionPourRemplacantApresRetraite(false);
                    if(previousModeAttachementEnergie) vueJoueurActif.informerModeAttachementEnergie(false, List.of());
                    if(previousModeEvo) {
                        vueJoueurActif.rafraichirAffichagePourSelectionEvolution();
                    }
                    if (!modePaiementCoutRetraiteActif && !modeDefausseEnergieAttaqueVue && !modeSelectionRemplacantApresRetraiteActif && !estEnModeAttachementEnergie_Global && !modeSelectionBasePourEvolution) {
                        vueJoueurActif.setHandInteractionsActive(true);
                    }
                }
            }
            mettreAJourGrandeCarteActive();
            placerBancCentre();
        });

        this.jeu.joueurActifProperty().addListener((obs, oldJ, newJ) -> {
            if (oldJ != null) {
                if (oldJ.getBanc() != null) {
                    oldJ.getBanc().removeListener(this.bancCentreChangeListener);
                    for (IPokemon p : oldJ.getBanc()) {
                        if (p != null && p.getCartePokemon() != null && p.energieProperty() != null) {
                            MapChangeListener<String, List<String>> listener = listenersEnergiesBancCentre.remove(p.getCartePokemon().getId());
                            if (listener != null) {
                                p.energieProperty().removeListener(listener);
                            }
                        }
                    }
                }
            }

            if (newJ != null) {
                adversaireProperty.set(newJ.getAdversaire());
                vueJoueurActif.preparerListenersPourJoueur(newJ);
                vueJoueurActif.placerMain();
                vueJoueurActif.placerBanc();

                newJ.pokemonActifProperty().addListener((obsPok, oldPok, newPok) -> {
                    mettreAJourGrandeCarteActive();
                });

                if (newJ.getBanc() != null) {
                    newJ.getBanc().addListener(this.bancCentreChangeListener);
                }


                final IJoueur joueurPourBindingActif = newJ;
                if (nbCartesMainJoueurActifLabel != null) {
                    if (nbCartesMainJoueurActifLabel.textProperty().isBound()) nbCartesMainJoueurActifLabel.textProperty().unbind();
                    StringBinding mainSizeBindingActif = Bindings.createStringBinding(() -> "Main: " + (joueurPourBindingActif != null && joueurPourBindingActif.getMain() != null ? joueurPourBindingActif.getMain().size() : "-"), joueurPourBindingActif.getMain());
                    nbCartesMainJoueurActifLabel.textProperty().bind(mainSizeBindingActif);
                }
                if (nbCartesPiocheJoueurActifLabel != null) {
                    if (nbCartesPiocheJoueurActifLabel.textProperty().isBound()) nbCartesPiocheJoueurActifLabel.textProperty().unbind();
                    StringBinding piocheSizeBindingActif = Bindings.createStringBinding(() -> "Pioche: " + (joueurPourBindingActif != null && joueurPourBindingActif.piocheProperty() != null ? joueurPourBindingActif.piocheProperty().size() : "--"), joueurPourBindingActif.piocheProperty());
                    nbCartesPiocheJoueurActifLabel.textProperty().bind(piocheSizeBindingActif);
                }
                if (nbCartesDefausseJoueurActifLabel != null) {
                    if (nbCartesDefausseJoueurActifLabel.textProperty().isBound()) nbCartesDefausseJoueurActifLabel.textProperty().unbind();
                    StringBinding defausseSizeBindingActif = Bindings.createStringBinding(() -> "Défausse: " + (joueurPourBindingActif != null && joueurPourBindingActif.defausseProperty() != null ? joueurPourBindingActif.defausseProperty().size() : "--"), joueurPourBindingActif.defausseProperty());
                    nbCartesDefausseJoueurActifLabel.textProperty().bind(defausseSizeBindingActif);
                }
                if (nbRecompensesJoueurActifLabel != null) {
                    if (nbRecompensesJoueurActifLabel.textProperty().isBound()) nbRecompensesJoueurActifLabel.textProperty().unbind();
                    StringBinding recompensesSizeBindingActif = Bindings.createStringBinding(() -> "Récompenses: " + (joueurPourBindingActif != null && joueurPourBindingActif.recompensesProperty() != null ? joueurPourBindingActif.recompensesProperty().size() : "-"), joueurPourBindingActif.recompensesProperty());
                    nbRecompensesJoueurActifLabel.textProperty().bind(recompensesSizeBindingActif);
                }


                if (boutonEchangerPokemon != null) {
                    newJ.peutRetraiteProperty().addListener((obsRetraite, oldValRetraite, newValRetraite) -> {
                        boutonEchangerPokemon.setDisable(!newValRetraite || modeSelectionRemplacantApresRetraiteActif);
                    });
                    boutonEchangerPokemon.setDisable(!newJ.peutRetraiteProperty().get() || modeSelectionRemplacantApresRetraiteActif);
                }
                placerBancCentre();
            } else {
                adversaireProperty.set(null);
                if (boutonEchangerPokemon != null) boutonEchangerPokemon.setDisable(true);
                if (boutonUtiliserTalent != null) boutonUtiliserTalent.setDisable(true);
                if (nbCartesMainJoueurActifLabel != null) { if(nbCartesMainJoueurActifLabel.textProperty().isBound()) nbCartesMainJoueurActifLabel.textProperty().unbind(); nbCartesMainJoueurActifLabel.setText("Main: -");}
                if (nbCartesPiocheJoueurActifLabel != null) { if(nbCartesPiocheJoueurActifLabel.textProperty().isBound()) nbCartesPiocheJoueurActifLabel.textProperty().unbind(); nbCartesPiocheJoueurActifLabel.setText("Pioche: --");}
                if (nbCartesDefausseJoueurActifLabel != null) { if(nbCartesDefausseJoueurActifLabel.textProperty().isBound()) nbCartesDefausseJoueurActifLabel.textProperty().unbind(); nbCartesDefausseJoueurActifLabel.setText("Défausse: --");}
                if (nbRecompensesJoueurActifLabel != null) { if(nbRecompensesJoueurActifLabel.textProperty().isBound()) nbRecompensesJoueurActifLabel.textProperty().unbind(); nbRecompensesJoueurActifLabel.setText("Récompenses: -");}

                placerBancCentre();
            }
            mettreAJourGrandeCarteActive();
            mettreAJourZoneAdversaire();
        });

        IJoueur premierJoueurActif = this.jeu.joueurActifProperty().get();
        if (premierJoueurActif != null) {
            if (premierJoueurActif.getAdversaire() != null) {
                adversaireProperty.set(premierJoueurActif.getAdversaire());
            }
            if (premierJoueurActif.getBanc() != null) {
                premierJoueurActif.getBanc().addListener(this.bancCentreChangeListener);
            }
            final IJoueur joueurInitialPourBinding = premierJoueurActif;
            if (nbCartesMainJoueurActifLabel != null) {
                if (nbCartesMainJoueurActifLabel.textProperty().isBound()) nbCartesMainJoueurActifLabel.textProperty().unbind();
                StringBinding mainSizeBindingInitial = Bindings.createStringBinding(() -> "Main: " + (joueurInitialPourBinding != null && joueurInitialPourBinding.getMain() != null ? joueurInitialPourBinding.getMain().size() : "-"), joueurInitialPourBinding.getMain());
                nbCartesMainJoueurActifLabel.textProperty().bind(mainSizeBindingInitial);
            }
            if (nbCartesPiocheJoueurActifLabel != null) {
                if (nbCartesPiocheJoueurActifLabel.textProperty().isBound()) nbCartesPiocheJoueurActifLabel.textProperty().unbind();
                StringBinding piocheSizeBindingInitial = Bindings.createStringBinding(() -> "Pioche: " + (joueurInitialPourBinding != null && joueurInitialPourBinding.piocheProperty() != null ? joueurInitialPourBinding.piocheProperty().size() : "--"), joueurInitialPourBinding.piocheProperty());
                nbCartesPiocheJoueurActifLabel.textProperty().bind(piocheSizeBindingInitial);
            }
            if (nbCartesDefausseJoueurActifLabel != null) {
                if (nbCartesDefausseJoueurActifLabel.textProperty().isBound()) nbCartesDefausseJoueurActifLabel.textProperty().unbind();
                StringBinding defausseSizeBindingInitial = Bindings.createStringBinding(() -> "Défausse: " + (joueurInitialPourBinding != null && joueurInitialPourBinding.defausseProperty() != null ? joueurInitialPourBinding.defausseProperty().size() : "--"), joueurInitialPourBinding.defausseProperty());
                nbCartesDefausseJoueurActifLabel.textProperty().bind(defausseSizeBindingInitial);
            }
            if (nbRecompensesJoueurActifLabel != null) {
                if (nbRecompensesJoueurActifLabel.textProperty().isBound()) nbRecompensesJoueurActifLabel.textProperty().unbind();
                StringBinding recompensesSizeBindingInitial = Bindings.createStringBinding(() -> "Récompenses: " + (joueurInitialPourBinding != null && joueurInitialPourBinding.recompensesProperty() != null ? joueurInitialPourBinding.recompensesProperty().size() : "-"), joueurInitialPourBinding.recompensesProperty());
                nbRecompensesJoueurActifLabel.textProperty().bind(recompensesSizeBindingInitial);
            }
        }

        mettreAJourGrandeCarteActive();
        mettreAJourZoneAdversaire();
        placerBancCentre();

        if (grandeCarteActiveView != null) {
            grandeCarteActiveView.setOnMouseClicked(event -> {
                if (isModeSelectionBasePourEvolution()) {
                    if (idPokemonActifCourant_PourGrandeCarte != null && carteEvolutionSelectionnee != null && jeu.joueurActifProperty().get() != null) {
                        fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur joueurMecanique = (fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur) jeu.joueurActifProperty().get();
                        if (joueurMecanique != null) {
                            List<String> ciblesValides = carteEvolutionSelectionnee.getChoixPossibles(joueurMecanique);
                            if (ciblesValides.contains(idPokemonActifCourant_PourGrandeCarte)) {
                                System.out.println("[VueDuJeu] Pokemon actif (ID: " + idPokemonActifCourant_PourGrandeCarte + ") choisi comme base pour evolution.");
                                pokemonDeBaseChoisiPourEvolution(idPokemonActifCourant_PourGrandeCarte);
                            } else {
                                System.out.println("[VueDuJeu] Pokemon actif (ID: " + idPokemonActifCourant_PourGrandeCarte + ") NON VALIDE pour evolution avec " + carteEvolutionSelectionnee.getNom());
                            }
                        } else {
                            System.err.println("[VueDuJeu] joueurMecanique est null apres cast dans le clic grande carte mode evolution.");
                        }
                    } else {
                        System.err.println("[VueDuJeu] Clic sur grande carte en mode evolution, mais infos manquantes (pokemon actif, carte evo, ou joueur).");
                    }
                } else if (estEnModeAttachementEnergie_Global) {
                    if (idPokemonActifCourant_PourGrandeCarte != null) {
                        IJoueur joueurCourant = this.jeu.joueurActifProperty().get();
                        if (joueurCourant != null && joueurCourant.getChoixComplementaires().stream().anyMatch(c -> c.getId().equals(idPokemonActifCourant_PourGrandeCarte))) {
                            this.jeu.uneCarteComplementaireAEteChoisie(idPokemonActifCourant_PourGrandeCarte);
                            afficherEnergiesGenerique(pokemonActifObserveCourant, energiesPokemonActifHBoxJeu, true);
                        } else {
                            System.out.println("[VueDuJeu] Clic sur grande carte Pokémon actif (ID: " + idPokemonActifCourant_PourGrandeCarte + ") en mode attachement énergie, mais NON VALIDE.");
                        }
                    }
                }
            });
        }


        jeu.finDePartieProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                String nomGagnant = jeu.getNomDuGagnant();
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Fin de la Partie");
                alert.setHeaderText("La partie est terminée !");
                alert.setContentText("Le gagnant est : " + nomGagnant + " ! Félicitations !");
                Platform.runLater(alert::showAndWait);

                if (boutonPasser != null) boutonPasser.setDisable(true);
                if (boutonEchangerPokemon != null) boutonEchangerPokemon.setDisable(true);
                if (boutonUtiliserTalent != null) boutonUtiliserTalent.setDisable(true);

                modePaiementCoutRetraiteActif = false;
                modeSelectionRemplacantApresRetraiteActif = false;
                estEnModeAttachementEnergie_Global = false;
                modeSelectionBasePourEvolution = false;
                carteEvolutionSelectionnee = null;
                coutRetraiteRestant = 0;
                modeDefausseEnergieAttaqueVue = false;

                mettreAJourGrandeCarteActive();
                placerBancCentre();
                if (vueJoueurActif != null) {
                    vueJoueurActif.setModeSelectionPourRemplacantApresRetraite(false);
                    vueJoueurActif.informerModeAttachementEnergie(false, java.util.List.of());
                    vueJoueurActif.rafraichirAffichagePourSelectionEvolution();
                    vueJoueurActif.setHandInteractionsActive(false);
                }
                jeu.instructionProperty().set("Partie terminée. Gagnant: " + nomGagnant);
            }
        });
    }

    // This replaces the previous placerBancCentre method
    private void placerBancCentre() {
        if (bancJoueurActifCentreVBox == null) { // Field name is still VBox due to @FXML, but it's an HBox
            System.err.println("[VueDuJeu] ERREUR: bancJoueurActifCentreVBox (HBox) is null in placerBancCentre!");
            return;
        }

        IJoueur joueurCourant = jeu.joueurActifProperty().get();

        Object previousPlayerUserData = bancJoueurActifCentreVBox.getUserData();
        if (joueurCourant == null || previousPlayerUserData != joueurCourant) {
            // Cleanup for previous player's listeners, more robustly handled by bancCentreChangeListener removal
            // and the loop in jeu.joueurActifProperty().addListener for oldJ.
            // For safety, clearing the map here if player context is truly new/gone.
            listenersEnergiesBancCentre.values().forEach(listener -> {
                // This is a generic clear; specific removal is preferred but complex without Pokemon instances.
            });
            listenersEnergiesBancCentre.clear();
        }
        bancJoueurActifCentreVBox.setUserData(joueurCourant);

        bancJoueurActifCentreVBox.getChildren().clear();

        if (joueurCourant == null) {
            return;
        }

        ObservableList<? extends IPokemon> banc = joueurCourant.getBanc();

        for (int i = 0; i < 5; i++) {
            if (i < banc.size() && banc.get(i) != null) {
                final IPokemon currentPokemon = banc.get(i);
                final ICarte cartePokemonInterface = currentPokemon.getCartePokemon();
                final String idCartePokemon = cartePokemonInterface.getId();

                VBox pokemonContainer = new VBox(3);
                pokemonContainer.setAlignment(javafx.geometry.Pos.CENTER);
                pokemonContainer.setUserData(idCartePokemon);

                Label pvLabel = new Label();
                pvLabel.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-background-color: rgba(255,255,255,0.7); -fx-padding: 1px 2px;");
                StringBinding pvBinding = Bindings.createStringBinding(() -> {
                    if (currentPokemon != null && currentPokemon.getCartePokemon() != null) {
                        CartePokemon cartePkm = (CartePokemon) currentPokemon.getCartePokemon();
                        return String.format("%d/%d PV", currentPokemon.pointsDeVieProperty().get(), cartePkm.getPointsVie());
                    }
                    return "--/-- PV";
                }, currentPokemon.pointsDeVieProperty(), currentPokemon.cartePokemonProperty());
                pvLabel.textProperty().bind(pvBinding);
                pokemonContainer.getChildren().add(pvLabel);

                ImageView imageView = new ImageView();
                String imagePath = "/images/cartes/" + cartePokemonInterface.getCode() + ".png";
                InputStream imageStream = getClass().getResourceAsStream(imagePath);
                StackPane cartePane;

                if (imageStream == null) {
                    Label errorLabel = new Label("Img: " + cartePokemonInterface.getNom().substring(0, Math.min(cartePokemonInterface.getNom().length(), 10)));
                    errorLabel.setStyle("-fx-font-size: 8px;");
                    cartePane = new StackPane(errorLabel);
                } else {
                    Image img = new Image(imageStream);
                    imageView.setImage(img);
                    imageView.setPreserveRatio(true);
                    imageView.setFitHeight(120);
                    cartePane = new StackPane(imageView);
                }
                pokemonContainer.getChildren().add(cartePane);

                HBox energiesHBox = new HBox(2);
                energiesHBox.setAlignment(javafx.geometry.Pos.CENTER);
                pokemonContainer.getChildren().add(energiesHBox);
                peuplerConteneurEnergies(currentPokemon, energiesHBox);


                String stylePourCartePane = "";
                boolean estCibleEvolution = this.isModeSelectionBasePourEvolution() && this.getCarteEvolutionSelectionnee() != null &&
                        joueurCourant instanceof Joueur &&
                        this.getCarteEvolutionSelectionnee().getChoixPossibles((Joueur)joueurCourant).contains(idCartePokemon);
                boolean estCibleRemplacement = this.isModeSelectionRemplacantApresRetraiteActif();
                boolean estCibleEnergie = this.estEnModeAttachementEnergie_Global &&
                        joueurCourant.getChoixComplementaires().stream().anyMatch(c -> c.getId().equals(idCartePokemon));

                if (estCibleEvolution) {
                    stylePourCartePane = "-fx-effect: dropshadow(gaussian, lawngreen, 15, 0.7, 0.0, 0.0); -fx-border-color: lawngreen; -fx-border-width: 3;";
                    cartePane.setOnMouseClicked(event -> {
                        System.out.println("[VueDuJeu] Bench Pokemon " + idCartePokemon + " clicked for EVOLUTION.");
                        this.pokemonDeBaseChoisiPourEvolution(idCartePokemon);
                    });
                } else if (estCibleRemplacement) {
                    stylePourCartePane = "-fx-effect: dropshadow(gaussian, orange, 15, 0.7, 0.0, 0.0); -fx-border-color: orange; -fx-border-width: 3;";
                    cartePane.setOnMouseClicked(event -> {
                        System.out.println("[VueDuJeu] Bench Pokemon " + idCartePokemon + " clicked for REPLACEMENT.");
                        this.pokemonDuBancChoisiPourRemplacer(idCartePokemon);
                    });
                } else if (estCibleEnergie) {
                    stylePourCartePane = "-fx-effect: dropshadow(gaussian, gold, 15, 0.7, 0.0, 0.0); -fx-border-color: gold; -fx-border-width: 3;";
                    cartePane.setOnMouseClicked(event -> {
                        System.out.println("[VueDuJeu] Bench Pokemon " + idCartePokemon + " clicked for ENERGY ATTACHMENT.");
                        this.jeu.uneCarteComplementaireAEteChoisie(idCartePokemon);
                    });
                } else {
                    cartePane.setOnMouseClicked(event -> System.out.println("[VueDuJeu] Clicked benched Pokemon in new display: " + cartePokemonInterface.getNom()));
                    if (stylePourCartePane.isEmpty()) {
                        cartePane.setOnMouseEntered(e -> cartePane.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0.3, 0.0, 0.0);"));
                        cartePane.setOnMouseExited(e -> cartePane.setStyle(""));
                    }
                }
                cartePane.setStyle(stylePourCartePane);
                bancJoueurActifCentreVBox.getChildren().add(pokemonContainer);

                if (currentPokemon.energieProperty() != null) {
                    MapChangeListener<String, List<String>> oldListener = listenersEnergiesBancCentre.remove(idCartePokemon);
                    if (oldListener != null) {
                        currentPokemon.energieProperty().removeListener(oldListener);
                    }
                    final HBox finalEnergiesHBox = energiesHBox;
                    MapChangeListener<String, List<String>> energyListener = change -> {
                        System.out.println("[VueDuJeu] Energy changed for benched Pokemon: " + idCartePokemon);
                        peuplerConteneurEnergies(currentPokemon, finalEnergiesHBox);
                    };
                    currentPokemon.energieProperty().addListener(energyListener);
                    listenersEnergiesBancCentre.put(idCartePokemon, energyListener);
                }

            } else {
                Button boutonVide = new Button("Vide");
                boutonVide.setPrefHeight(150);
                boutonVide.setUserData(String.valueOf(i));
                final int slotIndex = i;
                boutonVide.setOnAction(event -> this.jeu.unEmplacementVideDuBancAEteChoisi(String.valueOf(slotIndex)));
                boutonVide.setDisable(this.isModeSelectionBasePourEvolution() || this.isModeSelectionRemplacantApresRetraiteActif() || this.estEnModeAttachementEnergie_Global);
                bancJoueurActifCentreVBox.getChildren().add(boutonVide);
            }
        }
    }

    @FXML
    protected void actionPasser(ActionEvent event) {
        this.jeu.passerAEteChoisi();
    }

    private void afficherAttaquesDisponibles() {
        afficherAttaquesGenerique(pokemonActifObserveCourant, attaquesDisponiblesVBoxJeu, true);
    }
    private void afficherEnergiesPourPokemon(IPokemon pokemon, HBox conteneurEnergies) {
        afficherEnergiesGenerique(pokemon, conteneurEnergies, true);
    }


    @FXML
    private void echangerPokemon() {
        IJoueur joueurCourant = jeu.joueurActifProperty().get();
        if (joueurCourant == null || !joueurCourant.peutRetraiteProperty().get()) {
            System.err.println("[VueDuJeu] Tentative de retraite alors que ce n'est pas permis.");
            return;
        }

        if (modePaiementCoutRetraiteActif || modeSelectionRemplacantApresRetraiteActif) {
            modePaiementCoutRetraiteActif = false;
            modeSelectionRemplacantApresRetraiteActif = false;
            coutRetraiteRestant = 0;
            jeu.instructionProperty().set("retraite annulee.");
            if (boutonEchangerPokemon != null) {
                boutonEchangerPokemon.setDisable(!joueurCourant.peutRetraiteProperty().get());
            }
            if (vueJoueurActif != null) {
                vueJoueurActif.setModeSelectionPourRemplacantApresRetraite(false);
            }
            mettreAJourGrandeCarteActive();
            placerBancCentre();
        } else {
            this.jeu.retraiteAEteChoisie();
        }
    }

    @FXML
    private void actionUtiliserTalent() {
        if (pokemonActifObserveCourant != null && jeu.joueurActifProperty().get() != null) {
            Pokemon pokemonMecanique = (Pokemon) pokemonActifObserveCourant;
            if (pokemonMecanique.peutUtiliserTalent()) {
                System.out.println("[VueDuJeu] Bouton 'Utiliser Talent' cliqué pour: " + pokemonMecanique.getCartePokemon().getNom());
                this.jeu.uneCarteDeLaMainAEteChoisie(pokemonMecanique.getCartePokemon().getId());
            } else {
                System.out.println("Talent non utilisable ou Pokémon non valide.");
            }
        } else {
            System.out.println("Aucun Pokémon actif observé ou joueur actif non défini pour utiliser talent.");
        }
    }

    public void pokemonDuBancChoisiPourRemplacer(String idPokemonBanc) {
        if (!this.modeSelectionRemplacantApresRetraiteActif) {
            System.err.println("[VueDuJeu] Tentative de choisir un remplaçant alors que le mode n'est pas actif.");
            return;
        }
        this.jeu.uneCarteComplementaireAEteChoisie(idPokemonBanc);
        this.modeSelectionRemplacantApresRetraiteActif = false;
        if (boutonEchangerPokemon != null && jeu.joueurActifProperty().get() != null) {
            boutonEchangerPokemon.setDisable(!jeu.joueurActifProperty().get().peutRetraiteProperty().get());
        } else if (boutonEchangerPokemon != null) {
            boutonEchangerPokemon.setDisable(true);
        }
        if (vueJoueurActif != null) {
            vueJoueurActif.setModeSelectionPourRemplacantApresRetraite(false);
        }
        mettreAJourGrandeCarteActive();
        placerBancCentre();
    }

    public boolean isModeSelectionRemplacantApresRetraiteActif() {
        return modeSelectionRemplacantApresRetraiteActif;
    }

    public void energieDuPokemonActifChoisiePourDefausse(String idCarteEnergie) {
        if (modePaiementCoutRetraiteActif && coutRetraiteRestant > 0) {
            System.out.println("[VueDuJeu] Clic sur énergie " + idCarteEnergie + " pour défausse de retraite (modePaiementCoutRetraiteActif). Appel à jeu.defausserEnergieAEteChoisi() (NO-OP).");
            this.jeu.defausserEnergieAEteChoisi();
        } else {
            System.err.println("[VueDuJeu] Energie choisie pour défausse mais conditions non remplies (modePaiementCoutRetraiteActif=" + modePaiementCoutRetraiteActif + ", coutRetraiteRestant=" + coutRetraiteRestant + ")");
        }
    }

    public void activerModeSelectionBasePourEvolution(fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemonEvolution carteEvo) {
        this.modePaiementCoutRetraiteActif = false;
        this.modeSelectionRemplacantApresRetraiteActif = false;
        this.estEnModeAttachementEnergie_Global = false;
        this.modeDefausseEnergieAttaqueVue = false;
        this.carteEvolutionSelectionnee = carteEvo;
        this.modeSelectionBasePourEvolution = true;
        this.jeu.uneCarteDeLaMainAEteChoisie(carteEvo.getId());
        mettreAJourGrandeCarteActive();
        placerBancCentre();
        if (vueJoueurActif != null) {
            vueJoueurActif.rafraichirAffichagePourSelectionEvolution();
        }
    }

    public void pokemonDeBaseChoisiPourEvolution(String idPokemonBase) {
        if (!this.modeSelectionBasePourEvolution || this.carteEvolutionSelectionnee == null) {
            this.modeSelectionBasePourEvolution = false;
            this.carteEvolutionSelectionnee = null;
            mettreAJourGrandeCarteActive();
            placerBancCentre();
            if (vueJoueurActif != null) vueJoueurActif.rafraichirAffichagePourSelectionEvolution();
            return;
        }
        this.jeu.uneCarteComplementaireAEteChoisie(idPokemonBase);
        this.modeSelectionBasePourEvolution = false;
        this.carteEvolutionSelectionnee = null;
        mettreAJourGrandeCarteActive();
        placerBancCentre();
        if (vueJoueurActif != null) {
            vueJoueurActif.rafraichirAffichagePourSelectionEvolution();
        }
    }

    public boolean isModeSelectionBasePourEvolution() {
        return modeSelectionBasePourEvolution;
    }

    public fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemonEvolution getCarteEvolutionSelectionnee() {
        return carteEvolutionSelectionnee;
    }

    private void mettreAJourZoneAdversaire() {
        IJoueur joueurActifCourant = jeu.joueurActifProperty().get();
        IJoueur adversaire = adversaireProperty.get();

        if (adversaire == null) {
            if (nomAdversaireLabel != null) nomAdversaireLabel.setText("Adversaire: ---");
            if (pokemonActifAdversaireView != null) pokemonActifAdversaireView.setImage(null);
            if (pvPokemonActifAdversaireLabel != null) pvPokemonActifAdversaireLabel.setText("PV: --/--");
            if (bancAdversaireHBox != null) bancAdversaireHBox.getChildren().clear();
            if (energiesPokemonActifAdversaireHBox != null) energiesPokemonActifAdversaireHBox.getChildren().clear();
            if (attaquesAdversaireVBox != null) attaquesAdversaireVBox.getChildren().clear();

            if (nbCartesMainAdversaireLabel != null) { if(nbCartesMainAdversaireLabel.textProperty().isBound()) nbCartesMainAdversaireLabel.textProperty().unbind(); nbCartesMainAdversaireLabel.setText("Main: -");}
            if (nbCartesPiocheAdversaireLabel != null) { if(nbCartesPiocheAdversaireLabel.textProperty().isBound()) nbCartesPiocheAdversaireLabel.textProperty().unbind(); nbCartesPiocheAdversaireLabel.setText("Pioche: --");}
            if (nbCartesDefausseAdversaireLabel != null) { if(nbCartesDefausseAdversaireLabel.textProperty().isBound()) nbCartesDefausseAdversaireLabel.textProperty().unbind(); nbCartesDefausseAdversaireLabel.setText("Défausse: --");}
            if (nbRecompensesAdversaireLabel != null) { if(nbRecompensesAdversaireLabel.textProperty().isBound()) nbRecompensesAdversaireLabel.textProperty().unbind(); nbRecompensesAdversaireLabel.setText("Récompenses: -");}
            return;
        }


        if (nomAdversaireLabel != null) {
            nomAdversaireLabel.setText("Adversaire: " + adversaire.getNom());
        }

        IPokemon pokemonActifAdv = adversaire.pokemonActifProperty().get();
        if (pokemonActifAdversaireView != null && pvPokemonActifAdversaireLabel != null) {
            if (pokemonActifAdv != null && pokemonActifAdv.getCartePokemon() != null) {
                String imagePath = "/images/cartes/" + pokemonActifAdv.getCartePokemon().getCode() + ".png";
                InputStream imageStream = getClass().getResourceAsStream(imagePath);
                pokemonActifAdversaireView.setImage(imageStream == null ? null : new Image(imageStream));

                CartePokemon cartePkmAdv = (CartePokemon) pokemonActifAdv.getCartePokemon();

                if (pvPokemonActifAdversaireLabel.textProperty().isBound()) {
                    pvPokemonActifAdversaireLabel.textProperty().unbind();
                }
                pvPokemonActifAdversaireLabel.textProperty().bind(Bindings.createStringBinding(() ->
                                String.format("%d/%d PV", pokemonActifAdv.pointsDeVieProperty().get(), cartePkmAdv.getPointsVie()),
                        pokemonActifAdv.pointsDeVieProperty(), pokemonActifAdv.cartePokemonProperty()
                ));

                pvPokemonActifAdversaireLabel.setVisible(true);
                pokemonActifAdversaireView.setVisible(true);

                if (energiesPokemonActifAdversaireHBox != null) {
                    afficherEnergiesGenerique(pokemonActifAdv, energiesPokemonActifAdversaireHBox, false);
                }
                if (attaquesAdversaireVBox != null) {
                    afficherAttaquesGenerique(pokemonActifAdv, attaquesAdversaireVBox, false);
                }

            } else {
                pokemonActifAdversaireView.setImage(null);
                pokemonActifAdversaireView.setVisible(false);
                if (pvPokemonActifAdversaireLabel.textProperty().isBound()) {
                    pvPokemonActifAdversaireLabel.textProperty().unbind();
                }
                pvPokemonActifAdversaireLabel.setText("--/-- PV");
                pvPokemonActifAdversaireLabel.setVisible(false);
                if (energiesPokemonActifAdversaireHBox != null) energiesPokemonActifAdversaireHBox.getChildren().clear();
                if (attaquesAdversaireVBox != null) attaquesAdversaireVBox.getChildren().clear();
            }
        }

        if (bancAdversaireHBox != null) {
            bancAdversaireHBox.getChildren().clear();
            for (IPokemon pokemonBancAdv : adversaire.getBanc()) {
                if (pokemonBancAdv != null && pokemonBancAdv.getCartePokemon() != null) {
                    ImageView imgBancAdv = new ImageView();
                    String pathImgBanc = "/images/cartes/" + pokemonBancAdv.getCartePokemon().getCode() + ".png";
                    InputStream streamImgBanc = getClass().getResourceAsStream(pathImgBanc);
                    if (streamImgBanc != null) {
                        imgBancAdv.setImage(new Image(streamImgBanc));
                        imgBancAdv.setFitHeight(60);
                        imgBancAdv.setPreserveRatio(true);
                        bancAdversaireHBox.getChildren().add(imgBancAdv);
                    } else {
                        bancAdversaireHBox.getChildren().add(new Label("Err"));
                    }
                }
            }
        }

        final IJoueur adversairePourBinding = adversaire;
        if (nbCartesMainAdversaireLabel != null) {
            if (nbCartesMainAdversaireLabel.textProperty().isBound()) nbCartesMainAdversaireLabel.textProperty().unbind();
            StringBinding mainSizeBindingAdv = Bindings.createStringBinding(() ->
                            "Main: " + (adversairePourBinding != null && adversairePourBinding.getMain() != null ? adversairePourBinding.getMain().size() : "-"),
                    adversairePourBinding.getMain()
            );
            nbCartesMainAdversaireLabel.textProperty().bind(mainSizeBindingAdv);
        }
        if (nbCartesPiocheAdversaireLabel != null) {
            if (nbCartesPiocheAdversaireLabel.textProperty().isBound()) nbCartesPiocheAdversaireLabel.textProperty().unbind();
            StringBinding piocheSizeBindingAdv = Bindings.createStringBinding(() ->
                            "Pioche: " + (adversairePourBinding != null && adversairePourBinding.piocheProperty() != null ? adversairePourBinding.piocheProperty().size() : "--"),
                    adversairePourBinding.piocheProperty()
            );
            nbCartesPiocheAdversaireLabel.textProperty().bind(piocheSizeBindingAdv);
        }
        if (nbCartesDefausseAdversaireLabel != null) {
            if (nbCartesDefausseAdversaireLabel.textProperty().isBound()) nbCartesDefausseAdversaireLabel.textProperty().unbind();
            StringBinding defausseSizeBindingAdv = Bindings.createStringBinding(() ->
                            "Défausse: " + (adversairePourBinding != null && adversairePourBinding.defausseProperty() != null ? adversairePourBinding.defausseProperty().size() : "--"),
                    adversairePourBinding.defausseProperty()
            );
            nbCartesDefausseAdversaireLabel.textProperty().bind(defausseSizeBindingAdv);
        }
        if (nbRecompensesAdversaireLabel != null) {
            if (nbRecompensesAdversaireLabel.textProperty().isBound()) nbRecompensesAdversaireLabel.textProperty().unbind();
            StringBinding recompensesSizeBindingAdv = Bindings.createStringBinding(() ->
                            "Récompenses: " + (adversairePourBinding != null && adversairePourBinding.recompensesProperty() != null ? adversairePourBinding.recompensesProperty().size() : "-"),
                    adversairePourBinding.recompensesProperty()
            );
            nbRecompensesAdversaireLabel.textProperty().bind(recompensesSizeBindingAdv);
        }
    }
}
