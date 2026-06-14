package game.plateau.tuiles;

import java.util.*;

import game.plateau.tuiles.segment.Segment;

import game.joueur.Meeple;
import game.plateau.tuiles.direction.Direction;

public class TuileV2 {

    private Map<Direction, List<Segment>> bords;
    private List<Segment> segments;
    private Segment abbaye;
    private boolean blason;

    public TuileV2() {
        this.bords = new HashMap<Direction, List<Segment>>();
        this.segments = new ArrayList<Segment>();
        this.abbaye = null;
        this.blason = false;
        for (Direction dir : Direction.values()) {
            bords.put(dir, new ArrayList<Segment>());
        }
    }

    public void addSegmentToBoards(Direction direction, Segment segment) {
        this.bords.get(direction).add(segment);
    }

    public List<Segment> getSegments() {
        return this.segments;
    }

    public void addSegmentToSegments(Segment segment) {
        this.segments.add(segment);
    }

    public void addSegments(Direction direction, Segment segment) {
        this.addSegmentToBoards(direction, segment);
        this.addSegmentToSegments(segment);
    }

    public void addAbbaye(Segment abbaye) {
        this.abbaye = abbaye;
        this.segments.add(abbaye);
    }

    public Segment getAbbaye() {
        return this.abbaye;
    }

    public boolean hasAbbaye() {
        return this.abbaye != null;
    }

    public boolean hasBlason() {
        return this.blason;
    }

    public void setBlason(boolean blason) {
        this.blason = blason;
    }

    public String toString() {
        String finalString = "| " + this.getNord() + "-" + this.getEst() + "-" + this.getSud() + "-" + this.getOuest();
        if (abbaye != null) {
            // gestion de meeple sur l'abbaye
            finalString += ":" + abbaye.toString() + " |";
        } else {
            finalString += " |";
        }
        return finalString;
    }

    private String assemblerBord(List<Segment> bord) {
        if (bord == null || bord.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        for (Segment s : bord) {
            sb.append(s.getRepresentation());
        }
        return sb.toString();
    }

    public String getNord() {
        return assemblerBord(this.bords.get(Direction.NORD));
    }

    public String getEst() {
        return assemblerBord(this.bords.get(Direction.EST));
    }

    public String getSud() {
        return assemblerBord(this.bords.get(Direction.SUD));
    }

    public String getOuest() {
        return assemblerBord(this.bords.get(Direction.OUEST));
    }

    // mise en forme de la representation reseau
    private String assemblerBordReseau(List<Segment> bord) {

        if (bord == null || bord.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();

        // parcours segments
        for (Segment s : bord) {
            // representation normale de la tuile
            sb.append(s.getRepresentation());

            // si on a un meeple, on le met dans la representation
            if (s.getMeeple() != null && s.getMeeple().getOwner() != null) {

                String nom = s.getMeeple().getOwner().getNom().toUpperCase();
                String initiale = nom.length() >= 2 ? nom.substring(0, 2) : nom;
                sb.append("_M_").append(initiale);
            }
        }
        return sb.toString();
    }

    // formatte le plateau pour l'envoyer en brut sur le reseau
    public String getRepresentationReseau() {
        String res = assemblerBordReseau(this.bords.get(Direction.NORD)) + "-"
                + assemblerBordReseau(this.bords.get(Direction.EST)) + "-"
                + assemblerBordReseau(this.bords.get(Direction.SUD)) + "-"
                + assemblerBordReseau(this.bords.get(Direction.OUEST));

        // cas abbaye
        if (this.abbaye != null) {
            res += ":A";

            // meeple dans abbaye
            if (this.abbaye.getMeeple() != null && this.abbaye.getMeeple().getOwner() != null) {
                String nom = this.abbaye.getMeeple().getOwner().getNom().toUpperCase();
                String initiale = nom.length() >= 2 ? nom.substring(0, 2) : nom;
                res += "_M_" + initiale;
            }
        }
        return res;
    }

    public Meeple getMeeple(Segment segment) {
        return segment.getMeeple();
    }

    public void setMeeple(Meeple meeple, Segment segment) {
        segment.setMeeple(meeple);
    }

    public List<Segment> getBord(Direction direction) {
        return this.bords.get(direction);
    }

    /**
     * @return la liste des segments nord
     */
    public List<Segment> getNordSegments() {
        return this.bords.get(Direction.NORD);
    }
    /**
     * @return la liste des segments est
     */
    public List<Segment> getEstSegments() {
        return this.bords.get(Direction.EST);
    }

    /**
     * @return la liste des segments sud
     */
    public List<Segment> getSudSegments() {
        return this.bords.get(Direction.SUD);
    }

    /**
     * @return la liste des segments ouest
     */
    public List<Segment> getOuestSegments() {
        return this.bords.get(Direction.OUEST);
    }

    /**
     * Fait pivoter logiquement la tuile a droite
     */
    public void orienterDroite() {
        List<Segment> temp = this.bords.get(Direction.NORD);
        this.bords.put(Direction.NORD, this.bords.get(Direction.OUEST));
        this.bords.put(Direction.OUEST, this.bords.get(Direction.SUD));
        this.bords.put(Direction.SUD, this.bords.get(Direction.EST));
        this.bords.put(Direction.EST, temp);
    }

    /**
     * Fait pivoter la tuile a gauche
     */
    public void orienterGauche() {
        List<Segment> temp = this.bords.get(Direction.NORD);
        this.bords.put(Direction.NORD, this.bords.get(Direction.EST));
        this.bords.put(Direction.EST, this.bords.get(Direction.SUD));
        this.bords.put(Direction.SUD, this.bords.get(Direction.OUEST));
        this.bords.put(Direction.OUEST, temp);
    }
}
