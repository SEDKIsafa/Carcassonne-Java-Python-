package game.plateau.tuiles;

import java.util.ArrayList;
import java.util.List;

import game.plateau.tuiles.segment.SegmentType.SegmentRoute;
import game.plateau.tuiles.direction.Direction;
import game.plateau.tuiles.segment.Segment;
import game.plateau.tuiles.segment.SegmentType.SegmentAbbaye;
import game.plateau.tuiles.segment.SegmentType.SegmentChamp;
import game.plateau.tuiles.segment.SegmentType.SegmentVille;

/**
 * Will create a tile depending of the representation of the tile
 */
public class TuileFactoryV2 {

    /**
     * Empty builder
     */
    public TuileFactoryV2() {
    }

    /**
     * Will create a tile depending on the orientation given
     * 
     * @param orientations the list of orientation
     * @return a tile
     */
    // public static TuileV2 buildTuile(String orientations) {

    //     Boolean haveAbbaye = false;
    //     Boolean haveBlason = false;
    //     String initOrientations = orientations;

    //     List<Segment> segmentNord = new ArrayList<Segment>();
    //     List<Segment> segmentEst = new ArrayList<Segment>();
    //     List<Segment> segmentSud = new ArrayList<Segment>();
    //     List<Segment> segmentOuest = new ArrayList<Segment>();

    //     Segment segment1 = null;
    //     Segment segment2 = null;
    //     Segment segment3 = null;

    //     // detecter abbaye + enlever de la string si c'est la cas
    //     if (initOrientations.endsWith(":A")) {
    //         haveAbbaye = true;
    //         initOrientations = orientations.substring(0, orientations.length() - 2);
    //     }

    //     // detecter le blason (C)
    //     if (initOrientations.contains("C")) {
    //         haveBlason = true;
    //     }

    //     String[] bords = initOrientations.split("-");
    //     Map<String, SegmentRoute> routesPartagees = new HashMap<>();
    //     TuileV2 tuile = new TuileV2();

    //     for (int i = 0; i < 4; i++) {
    //         /*
    //          * if (bords[i].contains("r")) {
    //          * SegmentRoute route = routesPartagees.get(bords[i]);
    //          * if (route == null) {
    //          * route = new SegmentRoute("r");
    //          * routesPartagees.put(bords[i], route);
    //          * }
    //          * segment1 = route;
    //          * segment2 = route;
    //          * segment3 = route;
    //          * }
    //          */

    //         int indexChar = 0;
    //         String s = bords[i];

    //         for (int j = 0; i < s.length(); j++) {
    //             char c = s.charAt(j);
    //             String fullString = "";
    //             String stringWithoutR = "";
    //             if (c == 'r') {

    //                 if (fullString.contains("c")) {
    //                     segment1 = new SegmentVille(fullString);
    //                 }

    //                 else if (fullString.contains("f")) {
    //                     segment1 = new SegmentChamp(fullString);
    //                 }

    //                 segment2 = new SegmentRoute("r");

    //                 stringWithoutR = s.substring(indexChar + 1, s.length());
    //             }

    //             else if (stringWithoutR != "") {
    //                 if (fullString.contains("c")) {
    //                     segment3 = new SegmentVille(stringWithoutR);
    //                 }

    //                 else if (fullString.contains("f")) {
    //                     segment3 = new SegmentChamp(stringWithoutR);
    //                 }
    //             }

    //             else {
    //                 fullString += c;
    //             }
    //         }

    //         if (bords[i].contains("c")) {
    //             segment1 = new SegmentVille("c");
    //             segment2 = new SegmentVille("c");
    //             segment3 = new SegmentVille("c");
    //         } 
    //         else {
    //             segment1 = new SegmentChamp("f");
    //             segment2 = new SegmentChamp("f");
    //             segment3 = new SegmentChamp("f");
    //         }

    //         switch (i) {
    //             case 0:
    //                 segmentNord.add(segment1);
    //                 segmentNord.add(segment2);
    //                 segmentNord.add(segment3);
    //                 tuile.addSegments(Direction.NORD, segment2);

    //                 break;
    //             case 1:
    //                 segmentEst.add(segment1);
    //                 segmentEst.add(segment2);
    //                 segmentEst.add(segment3);
    //                 tuile.addSegments(Direction.EST, segment2);
    //                 break;
    //             case 2:
    //                 segmentSud.add(segment1);
    //                 segmentSud.add(segment2);
    //                 segmentSud.add(segment3);
    //                 tuile.addSegments(Direction.SUD, segment2);
    //                 break;
    //             case 3:
    //                 segmentOuest.add(segment1);
    //                 segmentOuest.add(segment2);
    //                 segmentOuest.add(segment3);
    //                 tuile.addSegments(Direction.OUEST, segment2);
    //                 break;
    //         }

    //     }

    //     return tuile;

    // }


        public static TuileV2 buildTuileV2(String orientations) {

        TuileV2 tuile = new TuileV2();
        String initOrientations = orientations;

        // detecter abbaye + enlever de la string si c'est la cas
        if (initOrientations.endsWith(":A")) {
            tuile.addAbbaye(new SegmentAbbaye("A"));
            initOrientations = orientations.substring(0, orientations.length() - 2);
        }

        // detecter le blason (C)
        if (initOrientations.contains("C")) {
            tuile.setBlason(true);
        }

        String[] bords = initOrientations.split("-");
        

        // pour chaque bord
        for(int i = 0;i<4;i++){

            List<Segment> segments = new ArrayList<Segment>();

            if(bords[i].contains("r")){

                int idxRoad = 0;
                while(bords[i].charAt(idxRoad)!='r') idxRoad++;

                segments.add(giveSegmentWithTheGoodType(bords[i].substring(0, idxRoad)));

                int idxRoadEnd = idxRoad;
                for(;idxRoadEnd<bords[i].length();idxRoadEnd++){
                 
                    switch (bords[i].charAt(idxRoadEnd)) { 
                        case 'c' : 
                        break;
                        case 'f' :
                         break;
                        case 'C' : 
                         break;
                    }
                }
                
                segments.add( new SegmentRoute((String) bords[i].subSequence(idxRoad, idxRoadEnd-2)));
                    

                segments.add(giveSegmentWithTheGoodType(bords[i].substring(idxRoadEnd-2)));
            
            }
            else{
                segments.add(giveSegmentWithTheGoodType(bords[i]));
            } 


            addSegmentsToTuile(segments,tuile,i);

        }


        return tuile;
    }

    private static void addSegmentsToTuile(List<Segment> segments, TuileV2 tuile,int i){

        switch (i){
                    case 0 : 
                        for (Segment segment : segments) {
                            tuile.addSegments(Direction.NORD, segment);
                        }
                        break;
                    case 1 : 
                        for (Segment segment : segments) {
                            tuile.addSegments(Direction.EST, segment);
                        }
                        break;
                    case 2 : 
                        for (Segment segment : segments) {
                        tuile.addSegments(Direction.SUD, segment);
                        }
                        break;
                    case 3 : 
                        for (Segment segment : segments) {
                        tuile.addSegments(Direction.OUEST, segment);
                        }
                        break;
                }
    }

    private static Segment giveSegmentWithTheGoodType(String représentation){
        if(représentation.contains("f")) {return new SegmentChamp(représentation);}
        else {return new SegmentVille(représentation);}
    }

}
