package game.plateau.tuiles.direction.pointsCardinaux;

public class pointCardinalSud extends pointsCardinaux {
    
    /**
     * constructor of the cardinal point of the south
     */
    public pointCardinalSud() {
        super();
    }

    /**
     * @return a string representation of the cardinal point, it is used to display the cardinal point in the console
     */
    public String toString(){
        return "| SUD : " + this.getSegments().get(0).toString() + this.getSegments().get(1).toString() + this.getSegments().get(2).toString() + " |";
    }
    
}
