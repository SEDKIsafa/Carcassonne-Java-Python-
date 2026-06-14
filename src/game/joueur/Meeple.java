package game.joueur;

import game.plateau.tuiles.segment.Segment;

/**
 * The class for the meeples
 */
public class Meeple {
    private Joueur owner;
    private Segment zone;

    /**
     * The builder of the meeple class
     * @param owner the owner of the meeple 
     * @param zone the zone where the meeple will be place
     */

    public Meeple(Joueur owner, Segment zone){
        this.owner = owner;
        this.zone = zone;
    }

    /**
     * @return the owner of the meeple
     */
    public Joueur getOwner(){
        return this.owner;
    } 

    /**
     * @return the zone of the meeple
     */
    public Segment getZone(){
        return this.zone;
    }
}
