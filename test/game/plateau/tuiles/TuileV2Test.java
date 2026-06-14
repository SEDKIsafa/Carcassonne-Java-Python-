package game.plateau.tuiles;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.joueur.Joueur;
import game.joueur.Meeple;
import game.plateau.tuiles.direction.Direction;
import game.plateau.tuiles.segment.Segment;

public class TuileV2Test {

    private TuileV2 tuile;
    private Segment nord1, est1, sud1, ouest1;

    @BeforeEach
    public void init() {
        tuile = new TuileV2();
        
        nord1 = new Segment("c");
        est1 = new Segment("f");
        sud1 = new Segment("r");
        ouest1 = new Segment("f");

        tuile.addSegments(Direction.NORD, nord1);
        tuile.addSegments(Direction.EST, est1);
        tuile.addSegments(Direction.SUD, sud1);
        tuile.addSegments(Direction.OUEST, ouest1);
    }

    @Test
    public void testInitEtSegments() {
        assertFalse(tuile.hasAbbaye());
        assertFalse(tuile.hasBlason());
        assertEquals(4, tuile.getSegments().size());
        
        assertEquals("c", tuile.getNord());
        assertEquals("f", tuile.getEst());
        assertEquals("r", tuile.getSud());
        assertEquals("f", tuile.getOuest());
    }

    @Test
    public void testRotations() {
        // Départ: N=c, E=f, S=r, O=f
        // Rotation Droite -> N=f, E=c, S=f, O=r
        tuile.orienterDroite();
        assertEquals("f", tuile.getNord());
        assertEquals("c", tuile.getEst());
        assertEquals("f", tuile.getSud());
        assertEquals("r", tuile.getOuest());

        tuile.orienterGauche();
        assertEquals("c", tuile.getNord());
        assertEquals("f", tuile.getEst());
        assertEquals("r", tuile.getSud());
        assertEquals("f", tuile.getOuest());
    }

    @Test
    public void testAbbaye() {
        Segment abbaye = new Segment("A");
        tuile.addAbbaye(abbaye);
        
        assertTrue(tuile.hasAbbaye());
        assertEquals(abbaye, tuile.getAbbaye());
        assertTrue(tuile.getSegments().contains(abbaye));
        assertEquals(5, tuile.getSegments().size());
    }

    @Test
    public void testRepresentationReseauEtMeeple() {
        assertEquals("c-f-r-f", tuile.getRepresentationReseau());

        Joueur alice = new Joueur("Alice");
        Meeple meeple = new Meeple(alice, nord1);
        tuile.setMeeple(meeple, nord1);

        // on doit avoir le meeple
        assertEquals("c_M_AL-f-r-f", tuile.getRepresentationReseau());

        Segment abbaye = new Segment("A");
        tuile.addAbbaye(abbaye);
        Meeple meepleAbbaye = new Meeple(new Joueur("Bob"), abbaye);
        abbaye.setMeeple(meepleAbbaye);

        // ajout meeple
        assertEquals("c_M_AL-f-r-f:A_M_BO", tuile.getRepresentationReseau());
    }
}