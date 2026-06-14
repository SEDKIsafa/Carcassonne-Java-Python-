package game.joueur;

import java.util.ArrayList;
import java.util.List;

import game.plateau.tuiles.segment.Segment;
public class Joueur {

    private String nom;
    private int score;
    private int meeplesRestants;
    private List<Segment> segmentJoueur;

    public static final int NOMBRE_MEEPLES_DEPART = 7;

    public Joueur(String nom) {
        this.nom = nom;
        this.score = 0;
        this.meeplesRestants = NOMBRE_MEEPLES_DEPART;
        this.segmentJoueur = new ArrayList<>();
    }

    public String getNom() {
        return nom;
    }
    public int getScore() {
        return score;
    }
    public int getMeeplesRestants() {
        return meeplesRestants;
    }
    public void ajouterScore(int points) {
        if (points > 0) {
            this.score += points;
        }

    }

    public boolean poserMeeple(Segment segment) {
        if (this.meeplesRestants > 0) {
            this.meeplesRestants--;
            this.segmentJoueur.add(segment);
            return true;
        }
        return false;
    }
    
    public void recupererMeeple(Segment segment) {
        this.meeplesRestants++;
        this.segmentJoueur.remove(segment);
    }

}
