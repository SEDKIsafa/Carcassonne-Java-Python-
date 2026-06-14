package game.plateau;

import java.util.HashMap;

import game.joueur.Joueur;
import game.joueur.Meeple;
import game.plateau.tuiles.TuileFactoryV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.*;

public class PlateauV2 {

    private HashMap<Position,TuileV2> cases;
    private int xMin;
    private int xMax;
    private int yMin;
    private int yMax;

    public PlateauV2(){
        this.cases = new HashMap<Position,TuileV2>();
        this.xMin=36;
        this.xMax=36;
        this.yMin=36;
        this.yMax=36;
        TuileV2 t = TuileFactoryV2.buildTuileV2("c-f0rf1-f1-f1rf0");

        this.cases.put(new Position(36, 36),t);
    }

    public int getMax(int n,int m){
        return n>m?n:m;
    }

    public int getMin(int n,int m){
        return n<m?n:m;
    }

    /**
     * @return les cases du plateau
     */
    public HashMap<Position, TuileV2> getCases() {
        return this.cases;
    }

    public boolean peutPoser(Position position){
        return this.cases.get(position)==null;
    }
    
    public void Poser(TuileV2 t, Position position){
        this.cases.put(position, t);
        this.yMax=getMax(yMax, position.getY());
        this.yMin=getMin(yMin, position.getY());
        this.xMax=getMax(xMax, position.getX());
        this.xMin=getMin(xMin, position.getX());
    }

    /**
     * Va essayer de poser un meeple sur le plateau
     * @param pos la position de la tuile 
     * @param joueur le joueur qui pose le meeple
     * @param zone la zone de la tuile(c,f,r,a)
     * @return true si le meeple a été posé, faux sinon 
     */
    public boolean poserMeeple(Position pos, Joueur joueur, Segment zone){
        TuileV2 tuileCible = this.getTuile(pos);

        boolean tuileInexistante = tuileCible == null;
        boolean meepleSurTuile = tuileCible.getMeeple(zone) !=null;

        //cas ou on ne peut pas poser
        if(tuileInexistante || meepleSurTuile){
            return false;
        }

        if(joueur.poserMeeple(zone)){
            Meeple meeple = new Meeple(joueur, zone);
            tuileCible.setMeeple(meeple,zone);
            return true;
        }
        return false;
    }

    /**
     * On recupere le meeple a une position donnée
     * @param pos la position a laquelle recupere le meeple
     * @param segment le segment sur lequel recuperer le meeple
     */
    public void recupererMeeple(Position pos,Segment segment){
        TuileV2 tuileCible = this.getTuile(pos);

        if(tuileCible.getMeeple(segment) != null){
            Joueur owner = tuileCible.getMeeple(segment).getOwner();
            owner.recupererMeeple(segment);
            segment.removeMeeple();
        }
    }

    public TuileV2 getTuile(Position pos){
        return this.cases.get(pos);
    }

    public void displayPlateau() {

    // ── CELL_WIDTH calculé dynamiquement ─────────────────────
    int CELL_WIDTH = 10; // minimum par défaut
    for (int j = yMax; j >= yMin; j--) {
        for (int i = xMin; i <= xMax; i++) {
            TuileV2 tuile = this.getTuile(new Position(i, j));
            if (tuile != null)
                CELL_WIDTH = Math.max(CELL_WIDTH, tuile.toString().length());
        }
    }
    // On s'assure que la largeur est paire pour le centrage
    if (CELL_WIDTH % 2 != 0) CELL_WIDTH++;

    final String EMPTY_CELL = "| " + " ".repeat(CELL_WIDTH - 2);
    final String SEP_CELL   = "-".repeat(CELL_WIDTH);
    final String ROW_LABEL  = "      "; // padding devant les numéros de ligne

    // ── En-tête des colonnes ──────────────────────────────────
    System.out.print(ROW_LABEL + "     ");
    for (int i = xMin; i <= xMax; i++) {
        String num = String.valueOf(i);
        int padLeft  = (CELL_WIDTH - num.length()) / 2;
        int padRight = CELL_WIDTH - num.length() - padLeft;
        System.out.print(" ".repeat(padLeft) + num + " ".repeat(padRight));
    }
    System.out.println();

    // ── Ligne de séparation ───────────────────────────────────
    System.out.print(ROW_LABEL + "+----");
    for (int i = xMin; i <= xMax; i++)
        System.out.print("+" + SEP_CELL);
    System.out.println("+");

    // ── Lignes du plateau ─────────────────────────────────────
    for (int j = yMax; j >= yMin; j--) {
        System.out.printf("%5d | ", j);

        for (int i = xMin; i <= xMax; i++) {
            TuileV2 tuile = this.getTuile(new Position(i, j));
            if (tuile != null) {
                // Padding à droite si plus court que CELL_WIDTH
                String cell = tuile.toString();
                System.out.printf("%-" + CELL_WIDTH + "s", cell);
            } else {
                System.out.print(EMPTY_CELL);
            }
        }
        System.out.println();

        // ── Séparateur de ligne ───────────────────────────────
        System.out.print(ROW_LABEL + "+----");
        for (int i = xMin; i <= xMax; i++)
            System.out.print("+" + SEP_CELL);
        System.out.println("+");
    }
    }
}
