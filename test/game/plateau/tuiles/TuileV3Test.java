package game.plateau.tuiles;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import game.joueur.Joueur;
import game.joueur.Meeple;
import game.plateau.tuiles.direction.Orientation;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.direction.pointsCardinaux.*;
import game.plateau.tuiles.segment.Segment;
import game.plateau.tuiles.segment.SegmentType.SegmentAbbaye;

public class TuileV3Test {
    private TuileV3 tuile;
    private Orientation orientation;

    private List<Segment> creerBord(String c1, String c2, String c3) {
        List<Segment> bord = new ArrayList<>();
        bord.add(new Segment(c1));
        bord.add(new Segment(c2));
        bord.add(new Segment(c3));
        return bord;
    }
    
    @BeforeEach
    public void init() {
        pointCardinalNord nord = new pointCardinalNord();
        pointCardinalEst est = new pointCardinalEst();
        pointCardinalSud sud = new pointCardinalSud();
        pointCardinalOuest ouest = new pointCardinalOuest();

        nord.setSegments(creerBord("c1", "r", "c2"));
        est.setSegments(creerBord("c2", "c2", "c2"));
        sud.setSegments(creerBord("c2", "r", "c1"));
        ouest.setSegments(creerBord("c1", "c1", "c1"));

        orientation = new Orientation(nord, est, sud, ouest);
        tuile = new TuileV3(orientation, true);
    }

    @Test
    public void testInit() {
        assertTrue(tuile.getBlason());
        assertNull(tuile.getPosition());
        assertNull(tuile.getMeeple());
        assertFalse(tuile.hasAbbaye());
        assertNotNull(tuile.getOrientation());
        assertEquals(12, tuile.getSegments().size());
    }

    @Test
    public void testPosition() {
        Position pos = new Position(5, 4);
        tuile.setPosition(pos);
        
        assertEquals(pos, tuile.getPosition());
        assertEquals(5, tuile.getPosition().getX());
        assertEquals(4, tuile.getPosition().getY());
    }

    @Test
    public void testMeepleLogic() {
        Joueur joueur = new Joueur("Alice");
        Segment segmentTest = new Segment("c");
        Meeple meeple = new Meeple(joueur, segmentTest); 
        
        tuile.setMeeple(meeple);
        assertEquals(meeple, tuile.getMeeple());
        assertEquals("Alice", tuile.getMeeple().getOwner().getNom());
        
        tuile.removeMeeple();
        assertNull(tuile.getMeeple());
    }

    @Test
    public void testAbbayeLogic() {
        SegmentAbbaye abbaye = new SegmentAbbaye("A");
        tuile.addAbbaye(abbaye);

        assertTrue(tuile.hasAbbaye());
        assertEquals(abbaye, tuile.getAbbaye());
        assertEquals(13, tuile.getSegments().size());
        assertTrue(tuile.getSegments().contains(abbaye));
    }

    @Test
    public void testAssemblerSegment() {
        assertEquals("c1rc2", tuile.getNord());
        assertEquals("c2c2c2", tuile.getEst());
        assertEquals("c2rc1", tuile.getSud());
        assertEquals("c1c1c1", tuile.getOuest());
    }

    @Test
    public void testToString() {
        assertEquals("| c1rc2-c2c2c2-c2rc1-c1c1c1 |", tuile.toString());

        tuile.addAbbaye(new SegmentAbbaye("A"));
        assertEquals("| c1rc2-c2c2c2-c2rc1-c1c1c1:A |", tuile.toString());
    }

    @Test
    public void testRotations() {
        tuile.orienterDroite();
        assertEquals("c1c1c1", tuile.getNord());
        assertEquals("c1rc2", tuile.getEst());
        assertEquals("c2c2c2", tuile.getSud());
        assertEquals("c2rc1", tuile.getOuest());

        tuile.orienterGauche();
        assertEquals("c1rc2", tuile.getNord());
        assertEquals("c2c2c2", tuile.getEst());
        assertEquals("c2rc1", tuile.getSud());
        assertEquals("c1c1c1", tuile.getOuest());
    }
}