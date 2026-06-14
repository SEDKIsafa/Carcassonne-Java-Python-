package game.plateau.tuiles;

import java.util.*;

import game.plateau.tuiles.segment.Segment;
import game.plateau.tuiles.direction.Orientation;
import game.plateau.tuiles.direction.Position;
import game.joueur.Meeple;

public class TuileV3 {

    private Position position;
    //private Map<Direction,List<Segment>> bords;
    private Orientation orientation;
    private List<Segment> segments;

    private Segment abbaye;
    private boolean blason;
    //private boolean haveMeeple;
    private Meeple meeplePose;

    public TuileV3(Orientation orientation, boolean blason) {
        this.position = null;
        //this.bords = new HashMap<Direction,List<Segment>>();
        this.orientation = orientation;
        this.abbaye = null;
        this.blason = blason;
        this.meeplePose = null;
        
        /* 
        this.segments = new ArrayList<Segment>();
        for(Direction dir : Direction.values()){
            bords.put(dir, new ArrayList<Segment>());
        } 
        */
        
        //TODO, voir si on crée une tuile vide 
        //car dans cette logique la tuile est preremplis par orientation
        //orientation donne deja la liste des segments, on peut tout recup
        this.segments = new ArrayList<>();
        if (orientation != null) {
            this.segments.addAll(orientation.getNord().getSegments());
            this.segments.addAll(orientation.getEst().getSegments());
            this.segments.addAll(orientation.getSud().getSegments());
            this.segments.addAll(orientation.getOuest().getSegments());
        }
    }

    //getters
    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return this.position;
    }

    public Orientation getOrientation() {
        return this.orientation;
    }

    public boolean getBlason() {
        return this.blason;
    }

    public List<Segment> getSegments() {
        return this.segments;
    }

    //meeples
    public Meeple getMeeple() {
        return this.meeplePose;
    }

    public void setMeeple(Meeple meeple) {
        this.meeplePose = meeple;
    }

    public void removeMeeple() {
        this.meeplePose = null;
    }

    //orientation de la tuile
    public void orienterDroite() {
        this.orientation.orienterDroiteWithClass();
    }

    public void orienterGauche() {
        this.orientation.orienterGaucheWithClass();
    }

    //abbaye 
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

    //mise en commentaire car on a plus board dans cette logique
    // /**
    //  * link the given segment in parameter to the given direction, in boards
    //  * @param direction
    //  * @param segment
    //  */
    // public void addSegmentToBoards(Direction direction, Segment segment){
    //     this.bords.get(direction).add(segment);
    // }


    // /**
    //  * add the segment parameter in the segments sections 
    //  * @param segment
    //  */
    // public void addSegmentToSegments(Segment segment){
    //     this.segments.add(segment);
    // }

    // public void addSegments(Direction direction, Segment segment){
    //     this.addSegmentToBoards(direction, segment);
    //     this.addSegmentToSegments(segment);
    // }


    /**
     * Will build the string representation of a given list of segment
     * @param listeSegments the list of segment 
     * @return the string representation of the given list of segment
     */
    private String assemblerSegments(List<Segment> listeSegments) {
        //si pas de segment -> pas de representation
        if (listeSegments == null || listeSegments.isEmpty()) return "";

        StringBuilder res = new StringBuilder();

        for (Segment s : listeSegments) {
            res.append(s.getRepresentation());
        }

        return res.toString();
    }

    public String getNord() {
        return assemblerSegments(this.orientation.getNord().getSegments());
    }

    public String getEst() {
        return assemblerSegments(this.orientation.getEst().getSegments());
    }

    public String getSud() {
        return assemblerSegments(this.orientation.getSud().getSegments());
    }

    public String getOuest() {
        return assemblerSegments(this.orientation.getOuest().getSegments());
    }

    /**
     * @return the string representation of the tile
     */
    public String toString() {
        String finalString = "| " + this.getNord() + "-" + this.getEst() + "-" + this.getSud() + "-" + this.getOuest();
        if(abbaye!=null){
            finalString += ":A |";
        }
        else{
            finalString  += " |";
        }
        return finalString;
    }
}
