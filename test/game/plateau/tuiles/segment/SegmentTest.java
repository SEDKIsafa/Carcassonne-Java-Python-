package game.plateau.tuiles.segment;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.joueur.Joueur;
import game.joueur.Meeple;
import game.plateau.tuiles.segment.SegmentType.*;

public class SegmentTest {

    private Joueur joueur;
    private Meeple meeple;
    private Segment segmentCible;

    @BeforeEach
    public void init() {
        joueur = new Joueur("Alice");
        segmentCible = new Segment("c");
        meeple = new Meeple(joueur, segmentCible); 
    }

    @Test
    public void testSegmentBase() {
        Segment segment = new Segment("c");
        assertEquals("c", segment.getRepresentation());

        assertNull(segment.getMeeple());
        segment.setMeeple(meeple);
        assertEquals(meeple, segment.getMeeple());
    }

    @Test
    public void testToStringAvecMeeple() {
        Segment segment = new Segment("c");
        assertEquals("c", segment.toString());
        
        segment.setMeeple(meeple);
        assertEquals("c(M_AL)", segment.toString());
    }

    @Test
    public void testSegmentTypes() {
        SegmentAbbaye abbaye = new SegmentAbbaye("A");
        assertTrue(abbaye instanceof Segment);
        assertEquals("A", abbaye.getRepresentation());

        SegmentChamp champ = new SegmentChamp("f");
        assertTrue(champ instanceof Segment);
        assertEquals("f", champ.getRepresentation());

        SegmentRoute route = new SegmentRoute("r");
        assertTrue(route instanceof Segment);
        assertEquals("r", route.getRepresentation());

        SegmentVille ville = new SegmentVille("c");
        assertTrue(ville instanceof Segment);
        assertEquals("c", ville.getRepresentation());
    }
}