package game.plateau.tuiles.direction;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import game.plateau.tuiles.direction.pointsCardinaux.*;
import game.plateau.tuiles.segment.Segment;
import game.plateau.tuiles.segment.SegmentType.SegmentChamp;
import game.plateau.tuiles.segment.SegmentType.SegmentRoute;
import game.plateau.tuiles.segment.SegmentType.SegmentVille;

public class OrientationTest {

    private Orientation orientation;
    private pointCardinalNord nord;
    private pointCardinalEst est;
    private pointCardinalSud sud;
    private pointCardinalOuest ouest;

    private List<Segment> listeNord;
    private List<Segment> listeEst;
    private List<Segment> listeSud;
    private List<Segment> listeOuest;

    @BeforeEach
    public void init() {
        nord = new pointCardinalNord();
        est = new pointCardinalEst();
        sud = new pointCardinalSud();
        ouest = new pointCardinalOuest();

        listeNord = new ArrayList<>();
        listeEst = new ArrayList<>();
        listeSud = new ArrayList<>();
        listeOuest = new ArrayList<>();

        //voir photo pour les tuiles de tests
        // NORD 
        listeNord.add(new SegmentVille("c1"));
        listeNord.add(new SegmentChamp("r"));
        listeNord.add(new SegmentVille("c2"));

        // EST 
        listeEst.add(new SegmentChamp("c2"));
        listeEst.add(new SegmentRoute("c2"));
        listeEst.add(new SegmentChamp("c2"));

        // SUD 
        listeSud.add(new SegmentChamp("c2"));
        listeSud.add(new SegmentChamp("r"));
        listeSud.add(new SegmentChamp("c1"));

        // OUEST
        listeOuest.add(new SegmentVille("c1"));
        listeOuest.add(new SegmentVille("c1"));
        listeOuest.add(new SegmentVille("c1"));

        nord.setSegments(listeNord);
        est.setSegments(listeEst);  
        sud.setSegments(listeSud);
        ouest.setSegments(listeOuest);

        orientation = new Orientation(nord, est, sud, ouest);
    }

    @Test
    public void testInit() {
        assertEquals(nord, orientation.getNord());
        assertEquals(est, orientation.getEst());
        assertEquals(sud, orientation.getSud());
        assertEquals(ouest, orientation.getOuest());
        
        assertEquals(3, orientation.getNord().getSegments().size());
        assertEquals(3, orientation.getEst().getSegments().size());
        assertEquals(3, orientation.getSud().getSegments().size());
        assertEquals(3, orientation.getOuest().getSegments().size());
    }

    @Test
    public void testOrienterDroiteWithClass() {
        orientation.orienterDroiteWithClass();

        //on check que tout les segments tournent bien vers la droite
        assertEquals(listeOuest, orientation.getNord().getSegments());
        assertEquals(listeNord, orientation.getEst().getSegments());
        assertEquals(listeEst, orientation.getSud().getSegments());
        assertEquals(listeSud, orientation.getOuest().getSegments());
    }

    @Test
    public void testOrienterGaucheWithClass() {
        orientation.orienterGaucheWithClass();

        //on check que tout les segments tournent bien vers la gauche
        assertEquals(listeEst, orientation.getNord().getSegments());
        assertEquals(listeSud, orientation.getEst().getSegments());
        assertEquals(listeOuest, orientation.getSud().getSegments());
        assertEquals(listeNord, orientation.getOuest().getSegments());
    }
}