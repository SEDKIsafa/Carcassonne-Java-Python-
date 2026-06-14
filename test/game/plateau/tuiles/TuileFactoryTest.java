/* 
package game.plateau.tuiles;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TuileFactoryTest {
    @Test
    public void testBuildTuileDontWorkWithNullOrEmptyString() {
        Tuile tuileNull = TuileFactory.buildTuile(null);
        assertNull(tuileNull);

        Tuile tuileVide = TuileFactory.buildTuile("");
        assertNull(tuileVide, "");
    }

    @Test
    public void testCantBuildTuileWithInvalidFormat() {
        Tuile tuileInvalide = TuileFactory.buildTuile("f-f-c");
        assertNull(tuileInvalide, "La chaine doit etre bien contruite");
        
        Tuile tuileInvalide2 = TuileFactory.buildTuile("c-f-r-s-f");
        assertNull(tuileInvalide2, "La chaine doit etre bien contruite");

        Tuile tuileInvalide3 = TuileFactory.buildTuile("c");
        assertNull(tuileInvalide3, "La chaine doit etre bien contruite");
    }

    @Test 
    public testBuildNormalTuileOK() {
        Tuile tuile = TuileFactory.buildTuile("c-f-r-s");

        assertNotNull(tuile);
        assertFalse(tuile.getAbbaye(), "Pas abbaye dans le build");
        assertFalse(tuile.getBlason(), "Pas blason dans le build");

        // verif positions
        assertEquals("c", tuile.getNord());
        assertEquals("f", tuile.getEst());
        assertEquals("r", tuile.getSud());
        assertEquals("s", tuile.getOuest());
    }

    public testBuildTuileWithTuile() {
        Tuile tuile = TuileFactory.buildTuile("f-f-f-f:A");

        assertNotNull(tuile);
        assertTrue(tuile.getAbbaye());
        assertFalse(tuile.getBlason(), "Pas blason dans le build");

        // verif positions
        assertEquals("f", tuile.getNord());
        assertEquals("f", tuile.getEst());
        assertEquals("f", tuile.getSud());
        assertEquals("f", tuile.getOuest());
    }

    
}
*/