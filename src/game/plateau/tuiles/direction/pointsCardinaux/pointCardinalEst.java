package game.plateau.tuiles.direction.pointsCardinaux;

public class pointCardinalEst extends pointsCardinaux {
    
    /**
     * constructor of the cardinal point of the east
     */
    public pointCardinalEst() {
        super();
    }


    /**
     * @return a string representation of the cardinal point of the east, it is composed of the name of the cardinal point and the string representation of its segments
     */
    public String toString(){
        return "| EST : " + this.getSegments().get(0).toString() + this.getSegments().get(1).toString() + this.getSegments().get(2).toString() + " |";
    }
    
}
