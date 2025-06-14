package fr.umontpellier.iut.ptcgJavaFX.vues;

import fr.umontpellier.iut.ptcgJavaFX.ICarte;
import fr.umontpellier.iut.ptcgJavaFX.IJeu;
import fr.umontpellier.iut.ptcgJavaFX.IJoueur;
import fr.umontpellier.iut.ptcgJavaFX.IPokemon;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.MapChangeListener;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Type;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
// import java.util.Set; // Inutilise?
import java.util.stream.Collectors;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.event.EventHandler; // Ajout pour EventHandler
import javafx.scene.input.MouseEvent; // Ajout pour MouseEvent

import java.io.IOException;
import java.io.InputStream;

// imports pour la mecanique
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemonEvolution;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.pokemon.CartePokemon; // deja la
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;


public class VueJoueurActif extends VBox {

    @FXML
    private Label nomDuJoueur;
    @FXML
    private HBox panneauMain;
    @FXML
    private HBox panneauEnergiesEnMain;
    @FXML
    private HBox panneauBanc;

    private ObjectProperty<? extends IJoueur> joueurActif;
    private IJeu jeu;
    private VueDuJeu vueDuJeu;

    private ChangeListener<IJoueur> joueurActifChangeListener;
    private ListChangeListener<ICarte> changementMainJoueurListener;
    private ListChangeListener<IPokemon> changementBancListener;
    private boolean enModeSelectionCibleEnergie = false;
    private List<String> idsCartesChoisissables = List.of();
    private Map<String, MapChangeListener<String, List<String>>> listenersEnergiesBanc = new HashMap<>();
    private boolean handInteractionsActive = true;

    public VueJoueurActif(VueDuJeu vueDuJeu, IJeu jeu, ObjectProperty<? extends IJoueur> joueurActifProperty) {
        this.vueDuJeu = vueDuJeu;
        this.jeu = jeu;
        this.joueurActif = joueurActifProperty;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VueJoueurActif.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initialiserListenersEtBindings();
    }

    private void initialiserListenersEtBindings() {
        joueurActifChangeListener = (observable, oldJoueur, newJoueur) -> {
            if (newJoueur != null) {
                preparerListenersPourJoueur(newJoueur);
                placerMain();
                // placerBanc(); // Call to old bench rendering is now disabled by placerBanc itself
            } else {
                if (panneauMain != null) panneauMain.getChildren().clear();
                if (panneauEnergiesEnMain != null) panneauEnergiesEnMain.getChildren().clear();
                if (nomDuJoueur != null) nomDuJoueur.setText("en attente de joueur...");
                if (panneauBanc != null) panneauBanc.getChildren().clear(); // Clear old bench UI
                informerModeAttachementEnergie(false, List.of());
                listenersEnergiesBanc.clear(); // Clear listeners map when player is null
            }
            placerBanc(); // This will now clear and return, effectively disabling the old bench
        };

        changementMainJoueurListener = change -> placerMain();
        // The changementBancListener will still fire, but placerBanc() will handle it by returning early.
        changementBancListener = change -> placerBanc();
        bindJoueurActif();
    }

    public void setHandInteractionsActive(boolean active) {
        boolean changed = this.handInteractionsActive != active;
        this.handInteractionsActive = active;
        if (changed) {
            placerMain();
        }
    }

    private void bindJoueurActif() {
        if (nomDuJoueur != null) {
            StringBinding nomJoueurBinding = Bindings.createStringBinding(() ->
                            (joueurActif.get() != null) ? joueurActif.get().getNom() : "en attente de joueur...",
                    joueurActif
            );
            nomDuJoueur.textProperty().bind(nomJoueurBinding);
        }
        setJoueurActifChangeListener();
        IJoueur joueurInitial = joueurActif.get();
        if (joueurInitial != null) {
            preparerListenersPourJoueur(joueurInitial);
            placerMain();
            placerBanc(); // Initial call, will clear and return
        } else {
            if (panneauMain != null) panneauMain.getChildren().clear();
            if (panneauEnergiesEnMain != null) panneauEnergiesEnMain.getChildren().clear();
            if (panneauBanc != null) panneauBanc.getChildren().clear();
        }
    }

    private void setJoueurActifChangeListener() {
        this.joueurActif.addListener(joueurActifChangeListener);
    }

    public void placerMain() {
        if (panneauMain == null) {
            System.err.println("[VueJoueurActif] panneauMain est null dans placerMain().");
            return;
        }
        panneauMain.getChildren().clear();
        if (panneauEnergiesEnMain != null) panneauEnergiesEnMain.getChildren().clear();

        if (joueurActif.get() == null) return;

        for (ICarte carte : joueurActif.get().getMain()) {
            String imagePath = "/images/cartes/" + carte.getCode() + ".png";
            ImageView imageView = new ImageView();
            InputStream imageStream = getClass().getResourceAsStream(imagePath);
            if (imageStream == null) {
                System.err.println("[VueJoueurActif] image non trouvee: " + imagePath);
                Label errorLabel = new Label("Img: " + carte.getNom());
                panneauMain.getChildren().add(errorLabel);
                continue;
            }
            Image img = new Image(imageStream);
            imageView.setImage(img);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(120);

            StackPane cartePane = new StackPane(imageView);
            cartePane.setOnMouseEntered(event -> cartePane.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 10, 0.5, 0.0, 0.0);"));
            cartePane.setOnMouseExited(event -> cartePane.setStyle(""));

            if (!this.handInteractionsActive) {
                cartePane.setOpacity(0.6);
            } else {
                cartePane.setOpacity(1.0);
            }

            boolean estEnergie = carte.getNom().toLowerCase().contains("énergie") || carte.getCode().startsWith("TEUEner");
            final String idCarte = carte.getId();

            cartePane.setOnMouseClicked(event -> {
                if (this.handInteractionsActive) {
                    Carte carteMecaniqueInterne = Carte.get(idCarte);
                    if (carteMecaniqueInterne instanceof CartePokemonEvolution) {
                        CartePokemonEvolution carteEvo = (CartePokemonEvolution) carteMecaniqueInterne;
                        Joueur joueurMecanique = (Joueur) joueurActif.get();
                        if (carteEvo.peutJouer(joueurMecanique)) {
                            if (vueDuJeu != null) {
                                vueDuJeu.activerModeSelectionBasePourEvolution(carteEvo);
                            } else {
                                System.err.println("[VueJoueurActif] vueDuJeu est null, ne peut pas activer mode évolution.");
                            }
                        } else {
                            this.jeu.uneCarteDeLaMainAEteChoisie(idCarte);
                        }
                    } else {
                        this.jeu.uneCarteDeLaMainAEteChoisie(idCarte);
                    }
                } else {
                    System.out.println("[VueJoueurActif] Clic sur carte en main ignoré (ID: " + idCarte + "); interactions désactivées.");
                }
            });

            if (estEnergie && panneauEnergiesEnMain != null) {
                panneauEnergiesEnMain.getChildren().add(cartePane);
            } else {
                panneauMain.getChildren().add(cartePane);
            }
        }
    }

    public void preparerListenersPourJoueur(IJoueur joueur) {
        if (joueur != null) {
            joueur.getMain().addListener(this.changementMainJoueurListener);
            // Detach old listener if any from old player's bench, though it might be complex
            // if old player reference isn't readily available here.
            // For now, new listener is added. If player instance changes, old listener might become inactive.
            joueur.getBanc().addListener(this.changementBancListener);
            rafraichirAffichageCibles(); // This will call placerBanc which now returns early
        }
    }

    public void informerModeAttachementEnergie(boolean estActif, List<String> idsCiblesDuBanc) {
        this.enModeSelectionCibleEnergie = estActif;
        this.idsCartesChoisissables = estActif ? idsCiblesDuBanc : List.of();
        rafraichirAffichageCibles(); // This will call placerBanc which now returns early
    }

    private Type getTypeFromLetter(String letter) {
        if (letter == null || letter.isEmpty()) return null;
        for (Type t : Type.values()) {
            if (t.asLetter().equalsIgnoreCase(letter)) return t;
        }
        return null;
    }

    // This method is now largely unused as placerBanc returns early.
    // Kept for structural integrity or if old bench needs to be re-enabled later.
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
                errorTypeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: purple;");
                conteneurEnergies.getChildren().add(errorTypeLabel);
                continue;
            }
            String cheminImageEnergie = "/images/energie/" + typeEnum.asLetter() + ".png";
            InputStream imageStream = getClass().getResourceAsStream(cheminImageEnergie);

            if (imageStream == null) {
                Label errorImgLabel = new Label(typeEnum.asLetter() + "x" + nombreEnergies);
                errorImgLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: red;");
                conteneurEnergies.getChildren().add(errorImgLabel);
            } else {
                ImageView imgEnergieView = new ImageView(new Image(imageStream));
                imgEnergieView.setFitHeight(15);
                imgEnergieView.setFitWidth(15);
                Label lblNombre = new Label("x" + nombreEnergies);
                lblNombre.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 0 0 0 1px;");
                HBox energieGroupe = new HBox(imgEnergieView, lblNombre);
                energieGroupe.setSpacing(1);
                energieGroupe.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                conteneurEnergies.getChildren().add(energieGroupe);
            }
        }
    }

    private void rafraichirAffichageCibles() {
        // This method used to call placerBanc. Now placerBanc will handle its own early exit.
        // System.out.println("[VueJoueurActif] Appel de rafraichirAffichageCibles(). Mode sélection énergie: " + enModeSelectionCibleEnergie);
        placerBanc(); // Call will clear and return
    }

    public void placerBanc() {
        if (panneauBanc != null) {
            panneauBanc.getChildren().clear(); // Clear any existing children
        }
        // Clear listeners associated with THIS VueJoueurActif's bench display
        // It's important to iterate over a copy or manage removal carefully if modifying the map during iteration.
        // However, since no new listeners are added if we return, just clearing is fine.
        listenersEnergiesBanc.clear();

        return; // Exit the method before any rendering logic for the old bench is executed.

        // All the code below this line is now effectively disabled for the old bench display.
        // It's kept for reference or if the old bench needs to be re-enabled.
        /*
        if (panneauBanc == null) {
            System.err.println("[VueJoueurActif] ERREUR: panneauBanc est null!");
            return;
        }
        if (joueurActif.get() == null) {
            panneauBanc.getChildren().clear();
            return;
        }

        // ... (original content of placerBanc was here) ...
        */
    }

    public void setModeSelectionPourRemplacantApresRetraite(boolean estActif) {
        // This method used to call placerBanc. Now placerBanc will handle its own early exit.
        placerBanc(); // Call will clear and return
    }

    public void rafraichirAffichagePourSelectionEvolution() {
        // This method used to call placerBanc. Now placerBanc will handle its own early exit.
        placerBanc(); // Call will clear and return
    }
}
