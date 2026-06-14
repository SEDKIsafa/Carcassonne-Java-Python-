package game.plateau.tuiles.direction.pointsCardinaux;

import java.util.ArrayList;
import java.util.List;

import game.plateau.tuiles.segment.Segment;

public class pointsCardinaux {

    /**
     * the segments of the cardinal point, it is an array list of 3 segments because each cardinal point is composed of 3 segments
     */
    private List<Segment>  segments;


    /**
     * constructor of the cardinal point, it initializes the segments of the cardinal point as an empty array list
     */
    public pointsCardinaux(){
        this.segments = new ArrayList<Segment>(3);
    }
    
    /**
     * 
     * @return the segments of the cardinal point
     */
    public List<Segment> getSegments(){
        return this.segments;
    }

    /**
     * change the segments of the cardinal point by the given segments
     * @param segments the new segments of the cardinal point
    */
    public void setSegments(List<Segment> segments){
        this.segments = segments;
    }

}
