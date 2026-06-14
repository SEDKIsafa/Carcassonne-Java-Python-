package game.plateau.tuiles;

import java.util.ArrayList;
import java.util.List;

import game.plateau.tuiles.segment.Segment;
import game.plateau.tuiles.segment.SegmentType.SegmentChamp;
import game.plateau.tuiles.segment.SegmentType.SegmentVille;

/**
 * Will create a tile depending of the representation of the tile
 */
public class TuileFactory {
    
    /**
     * Empty builder
     */
    public TuileFactory(){}

    /**
     * Will create a tile depending on the orientation given
     * @param orientations the list of orientation
     * @return a tile
     */
    public static Tuile buildTuile(String orientations){
        // if(orientations == null || orientations.isEmpty()){
        //     return null;
        // }

        // //boolean abbaye
        // Boolean haveAbbaye = false;
        // Boolean haveBlason = false;
        // String initOrientations = orientations;

        // //detecter abbaye + enlever de la string si c'est la cas
        // if(initOrientations.endsWith(":A")){
        //     haveAbbaye = true;
        //     initOrientations = orientations.substring(0,orientations.length() -2);
        // }

        // // detecter le blason (C)
        // if(initOrientations.contains("C")){
        //     haveBlason = true;
        // }
        
        // String[] bords = initOrientations.split("-");

        // if(bords.length !=4){
        //     System.out.println("La tuile est non valide");
        //     return null;
        // }


        // Orientation orientation = new Orientation(bords[0], bords[1], bords[2], bords[3]);
        // Tuile nouvelleTuille = new Tuile(orientation, haveAbbaye, haveBlason);

        // return nouvelleTuille;


        Boolean haveAbbaye = false;
        Boolean haveBlason = false;
        String initOrientations = orientations;

        List<Segment> segmentNord = new ArrayList<Segment>(3);
        List<Segment> segmentEst = new ArrayList<Segment>(3);
        List<Segment> segmentSud = new ArrayList<Segment>(3);
        List<Segment> segmentOuest = new ArrayList<Segment>(3);

        Segment segment1 = null;
        Segment segment2 = null;
        Segment segment3 = null;

        //detecter abbaye + enlever de la string si c'est la cas
        if(initOrientations.endsWith(":A")){
            haveAbbaye = true;
            initOrientations = orientations.substring(0,orientations.length() -2);
        }

        // detecter le blason (C)
        if(initOrientations.contains("C")){
            haveBlason = true;
        }

        String[] bords = initOrientations.split("-");

        for(int i=0;i<4;i++){
            if(bords[i].contains("r")){
                // TODO : cas complexe
            }
            else{
                if(bords[i].contains("c")){
                    segment1 = new SegmentVille("c");
                    segment2 = new SegmentVille("c");
                    segment3 = new SegmentVille("c");
                }
                else{
                    segment1 = new SegmentChamp("c");
                   segment2 = new SegmentChamp("c");
                   segment3 = new SegmentChamp("c");
                }
            }

            switch(i){
                case 0:
                    segmentNord.add(segment1);
                    segmentNord.add(segment2);
                    segmentNord.add(segment3);
                    break;
                case 1:
                    segmentEst.add(segment1);
                    segmentEst.add(segment2);
                    segmentEst.add(segment3);
                    break;
                case 2:
                    segmentSud.add(segment1);
                    segmentSud.add(segment2);
                    segmentSud.add(segment3);
                    break;
                case 3:
                    segmentOuest.add(segment1);
                    segmentOuest.add(segment2);
                    segmentOuest.add(segment3);
                    break;
            }

            
        }



        return null;

    }




}
