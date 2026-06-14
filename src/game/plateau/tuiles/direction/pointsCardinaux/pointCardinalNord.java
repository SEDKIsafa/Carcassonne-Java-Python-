package game.plateau.tuiles.direction.pointsCardinaux;

public class pointCardinalNord extends pointsCardinaux {
    

    /**
     * constructor of the cardinal point north
     */
    public pointCardinalNord() {
        super();
    }

    /**
     * @return a string representation of the cardinal point north, it is composed of the name of the cardinal point and the string representation of its segments
     */
    public String toString(){
        return "| NORD : " + this.getSegments().get(0).toString() + this.getSegments().get(1).toString() + this.getSegments().get(2).toString() + " |";
    }
    
}
