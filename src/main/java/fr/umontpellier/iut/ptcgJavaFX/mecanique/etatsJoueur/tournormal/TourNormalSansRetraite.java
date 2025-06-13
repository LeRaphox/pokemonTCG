package fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.tournormal;

import fr.umontpellier.iut.ptcgJavaFX.mecanique.Joueur;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.Pokemon;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.cartes.Carte;
import fr.umontpellier.iut.ptcgJavaFX.mecanique.etatsJoueur.EtatJoueur;

import java.util.List;

public class TourNormalSansRetraite extends EtatJoueur {

    public TourNormalSansRetraite(Joueur joueur) {
        super(joueur);
        getJeu().instructionProperty().setValue("Choisissez une action ou passez");
    }

    @Override
    public void passer() {
        getJeu().controlePokemon();
        joueur.onFinTour();
        if (!getJeu().estTermine())
            passerAuJoueurSuivant();
        else
            joueur.setEtatCourant(new FinPartie(joueur));
    }

    @Override
    public void carteChoisie(String numCarte) {
        List<String> choixPossibles = joueur.getCartesEnMainJouables();
        if (!choixPossibles.isEmpty() && choixPossibles.contains(numCarte)) {
            List<String> pokemonsEnJeuAvecTalent = joueur.getListePokemonEnJeu().stream().filter(Pokemon::peutUtiliserTalent).map(Pokemon::getCartePokemon).map(Carte::getId).toList();
            if (pokemonsEnJeuAvecTalent.contains(numCarte)) {
                joueur.getPokemon(Carte.get(numCarte)).utiliserTalent(joueur);
            } else
                joueur.jouerCarteEnMain(numCarte);
        } else passerALEtatSuivant();
    }

    public void passerAuJoueurSuivant() {
        getJeu().passeAuJoueurSuivant();
        joueur = getJeu().getJoueurActif();
        passerALEtatSuivant();
        joueur.jouerTour();
    }

    public void passerALEtatSuivant() {
        joueur.setEtatCourant(new TourNormalSansRetraite(joueur));
    }

    @Override
    public void bancChoisi(String s) { // s est l'index du banc en string
        if (joueur.getPokemonActif() == null) {
            // Normalment ça ne devrais jamais arriver mais on verifie quand meme
            return;
        }

        int index;
        try {
            index = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            // ici si la transformation est pas possible
            System.err.println("Format d'index du banc invalide : " + s);
            return;
        }

        // je verifie si l'index est valide
        if (index >= 0 && index < joueur.getBanc().size()) {
            Pokemon pokemonToAdvance = (Pokemon) joueur.getBanc().get(index);
            if (pokemonToAdvance != null) {
                // j'echange
                joueur.avancerPokemonDeBanc(pokemonToAdvance);

                // j'autorise pas les autres actions
                // je pars du principe qu'un echange ne fini pas un tour
                joueur.setEtatCourant(new TourNormal(joueur));
            } else {

                // innutile car ça risque de surcharger l'ecran mais bon : getJeu().instructionProperty().setValue("t'as cliquer sur un emplacement vide");
            }
        } else {
            //on ^peux aussi faire un petit message de debug mais pas necessaire
        }
    }
}
