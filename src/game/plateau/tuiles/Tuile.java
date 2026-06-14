package game.plateau.tuiles;


import game.joueur.Meeple;
import game.plateau.tuiles.direction.Orientation;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.direction.pointsCardinaux.pointCardinalEst;
import game.plateau.tuiles.direction.pointsCardinaux.pointCardinalNord;
import game.plateau.tuiles.direction.pointsCardinaux.pointCardinalOuest;
import game.plateau.tuiles.direction.pointsCardinaux.pointCardinalSud;

public class Tuile {

    private Position position;
    private Orientation orientation;
    private Meeple meeplePose;
    private boolean abbaye;
    private boolean blason;

    public Tuile(Orientation orientation,boolean abbaye, boolean blason){
        this.position = null;
        this.orientation = orientation;
        this.meeplePose = null;
        this.abbaye = abbaye;
        this.blason = blason;
    }

    public boolean getAbbaye(){
        return this.abbaye;
    }

    public boolean getBlason(){
        return this.blason;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public void orienterDroite() {
        this.orientation.orienterDroiteWithClass();
    }

    public void orienterGauche() {
        this.orientation.orienterGaucheWithClass();
    }

    public Position getPosition() {
        return this.position;
    }

    public Orientation getOrientation() {
        return this.orientation;
    }

    public Meeple getMeeple(){
        return this.meeplePose;
    }

    public void setMeeple(Meeple meeple){
        this.meeplePose = meeple;
    }


    public void removeMeeple(){
        this.meeplePose = null;
    }


    public pointCardinalNord getNord() {
        return this.orientation.getNord();
    }


    public pointCardinalEst getEst() {
        return this.orientation.getEst();
    }

    public pointCardinalSud getSud() {
        return this.orientation.getSud();
    }


    public pointCardinalOuest getOuest() {
        return this.orientation.getOuest();
    }

    public String toString() {
        String finalString = "| " + this.getNord() + "-" + this.getEst() + "-" + this.getSud() + "-" + this.getOuest();
        if(abbaye){
            finalString += ":A |";
        }
        else{
            finalString  += " |";
        }
        return finalString;
    }
}
