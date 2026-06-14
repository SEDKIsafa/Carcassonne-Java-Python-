package game.plateau.tuiles.segment;

import game.joueur.Meeple;

public class Segment {
    
    private String representation;
    private Meeple meeplePose;

    public Segment(String representation){
        this.representation = representation;
        this.meeplePose = null;
    }

    public String getRepresentation(){
        return this.representation;
    }

    public Meeple getMeeple(){
        return this.meeplePose;
    }   
    
    public void setMeeple(Meeple meeplePose) {
        this.meeplePose = meeplePose;
    }

    public void removeMeeple(){
        this.meeplePose = null;
    }

    @Override
    public String toString() {
        //si un meeple est present sur le segment on l'affiche 
        if (this.meeplePose != null && this.meeplePose.getOwner() != null) {
            String nomJoueur = this.meeplePose.getOwner().getNom().toUpperCase();
            // on prend les deux premieres lettres du prenom
            String initiale = nomJoueur.length() >= 2 ? nomJoueur.substring(0, 2) : nomJoueur;
            
            return this.representation + "(M_" + initiale + ")";
        }
        
        // si pas meeple, representation normale
        return this.representation;
    }
}
