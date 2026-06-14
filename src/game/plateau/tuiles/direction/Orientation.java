package game.plateau.tuiles.direction;

import java.util.List;

import game.plateau.tuiles.direction.pointsCardinaux.pointCardinalEst;
import game.plateau.tuiles.direction.pointsCardinaux.pointCardinalNord;
import game.plateau.tuiles.direction.pointsCardinaux.pointCardinalOuest;
import game.plateau.tuiles.direction.pointsCardinaux.pointCardinalSud;
import game.plateau.tuiles.segment.Segment;

public class Orientation {
    

    private pointCardinalNord nord;
    private pointCardinalEst est;
    private pointCardinalSud sud;
    private pointCardinalOuest ouest;

    //TODO revoir le constructeur, il ne faudrait pas plutot mettre des liste de segments au lieu de point cardinal ? 
    public Orientation(pointCardinalNord nord, pointCardinalEst est,pointCardinalSud sud ,pointCardinalOuest ouest){
        this.est=est;
        this.nord=nord;
        this.ouest=ouest;
        this.sud=sud;
    }


    // public void orienterDroite(){
    //     String swap = this.nord;
    //     this.nord = this.ouest;
    //     this.ouest = this.sud;
    //     this.sud = this.est;
    //     this.est = swap;

    // }

    public void orienterDroiteWithClass(){
        List<Segment> segmentNord = this.nord.getSegments();
        List<Segment> segmentEst = this.est.getSegments();
        List<Segment> segmentSud = this.sud.getSegments();
        List<Segment> segmentOuest = this.ouest.getSegments(); 

        this.nord.setSegments(segmentOuest);
        this.est.setSegments(segmentNord);
        this.sud.setSegments(segmentEst);
        this.ouest.setSegments(segmentSud);        
    }

    // public void orienterGauche(){
    //     String swap = this.nord;
    //     this.nord=this.est;
    //     this.est=this.sud;
    //     this.sud=this.ouest;
    //     this.ouest=swap;
    // }

    public void orienterGaucheWithClass(){
        List<Segment> segmentNord = this.nord.getSegments();
        List<Segment> segmentEst = this.est.getSegments();
        List<Segment> segmentSud = this.sud.getSegments();
        List<Segment> segmentOuest = this.ouest.getSegments(); 

        this.nord.setSegments(segmentEst);
        this.est.setSegments(segmentSud);
        this.sud.setSegments(segmentOuest);
        this.ouest.setSegments(segmentNord);        
    }

    public pointCardinalNord getNord(){
        return this.nord;
    }

    public pointCardinalEst getEst(){
        return this.est;
    }

    public pointCardinalSud getSud(){
        return this.sud;
    }

    public pointCardinalOuest getOuest(){
        return this.ouest;
    }


}
