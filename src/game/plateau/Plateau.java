package game.plateau;

import java.util.HashMap;

import game.joueur.Joueur;
import game.joueur.Meeple;
import game.plateau.tuiles.Tuile;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.Segment;

public class Plateau {

    private HashMap<Position,Tuile> cases;
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;

    public Plateau(){
        this.cases = new HashMap<Position,Tuile>();
        this.xMin=1;
        this.xMax=1;
        this.yMin=1;
        this.yMax=1;
    }


    public void init(){/*
        pointCardinalNord nord = new pointCardinalNord();
        ArrayList segmentNord = new ArrayList<Segment>();
        segmentNord.add(new SegmentVille("c"));
        segmentNord.add(new SegmentVille("c"));
        segmentNord.add(new SegmentVille("c"));
        nord.setSegments();
        Orientation o = new Orientation("c", "f0rf", "f", "frf0");
        Tuile t = new Tuile(o,false,false);
        this.Poser(t,new Position(0, 0));*/
    }

    public int getMax(int n,int m){
        return n>m?n:m;
    }

    public int getMin(int n,int m){
        return n<m?n:m;
    }

    public boolean peutPoser(Position position){
        return this.cases.get(position)==null;
    }
    
    public void Poser(Tuile tuile, Position position){
        this.cases.put(position, tuile);
        tuile.setPosition(position);
        this.yMax=getMax(yMax, position.getY());
        this.yMin=getMin(yMin, position.getY());
        this.xMax=getMax(xMax, position.getX());
        this.xMin=getMin(xMin, position.getX());
    }

    /**
     * Va essayer de poser un meeple sur le plateau
     * @param pos la position de la tuile 
     * @param joueur le joueur qui pose le meeple
     * @param zone le segment cible de la tuile
     * @return true si le meeple a été posé, faux sinon 
     */
    public boolean poserMeeple(Position pos, Joueur joueur, Segment zone){
        Tuile tuileCible = this.getTuile(pos);

        boolean tuileInexistante = tuileCible == null;
        boolean meepleSurTuile = tuileCible.getMeeple() !=null;

        if(tuileInexistante || meepleSurTuile){
            return false;
        }

        if(joueur.poserMeeple(zone)){
            Meeple meeple = new Meeple(joueur, zone);
            tuileCible.setMeeple(meeple);
            return true;
        }
        return false;
    }

    /**
     * On recupere le meeple a une position donnée
     * @param pos la position a laquelle recupere le meeple
     */
    public void recupererMeeple(Position pos){
        Tuile tuileCible = this.getTuile(pos);

        if(tuileCible != null && tuileCible.getMeeple() != null){
            Joueur owner = tuileCible.getMeeple().getOwner();
            //voir si on ajoute juste un meeple au compteur 
            //ou si on recupere literallement l'objet
            owner.recupererMeeple(tuileCible.getMeeple().getZone());

            tuileCible.removeMeeple();
        }
    }

    public Tuile getTuile(Position pos){
        return this.cases.get(pos);
    }

    public void displayPlateau(){
        // System.out.print("         ");
        // for (int j = this.xMin; j < this.xMax; j++) {
        //     System.out.print(j + "           ");
        // }
        // System.out.println();
    
        // // Ligne de séparation
        // System.out.print("    ");
        // for (int j = this.xMin; j < this.xMax; j++) {
        //     System.out.print("------------");
        // }
        // System.out.println();
    
        // // Affichage des lignes avec les numéros de ligne
        // for (int i = this.xMin; i < this.xMax; i++) {
        //     // Affichage du numéro de ligne
        //     System.out.print(i + " | "); 
        //     for (int j = yMin; j < this.yMax; j++) {
        //         if (this.getTuile(new Position(i, j)) != null) {
        //             System.out.print(this.getTuile(new Position(i, j)).toString());
        //         }
        //     }
        //     System.out.println();
        // }
        for (Tuile tuile : this.cases.values()) {
            System.out.println(tuile.toString());
        }
    }

}
