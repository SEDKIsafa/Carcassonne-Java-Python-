package game.plateau.tuiles;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SacTuilesTest {

    private SacTuiles sac;
    private TuileV2 tuile1;
    private TuileV2 tuile2;
    private TuileV2 tuile3;

    @BeforeEach
    public void init() {
        sac = new SacTuiles();
        tuile1 = new TuileV2();
        tuile2 = new TuileV2();
        tuile3 = new TuileV2();
    }

    @Test
    public void testInit() {
        assertTrue(sac.estVide());
        assertEquals(0, sac.getNombreTuilesRestante());
        assertNotNull(sac.getTuiles());
    }

    @Test
    public void testAddTuile() {
        sac.addTuile(tuile1);
        assertFalse(sac.estVide());
        assertEquals(1, sac.getNombreTuilesRestante());
        assertEquals(tuile1, sac.getTuiles().get(0));
    }

    @Test
    public void testPiocher() {
        // Pioche dans un sac vide
        assertNull(sac.piocher());

        sac.addTuile(tuile1);
        sac.addTuile(tuile2);        
        assertEquals(2, sac.getNombreTuilesRestante());

        TuileV2 pioche = sac.piocher();
        assertEquals(tuile1, pioche);
        assertEquals(1, sac.getNombreTuilesRestante());

        assertEquals(tuile2, sac.piocher());
        assertTrue(sac.estVide());
    }

    @Test
    public void testMelanger() {
        sac.addTuile(tuile1);
        sac.addTuile(tuile2);
        sac.addTuile(tuile3);
        sac.melanger();
        
        assertEquals(3, sac.getNombreTuilesRestante());
        
        List<TuileV2> contenu = sac.getTuiles();
        assertTrue(contenu.contains(tuile1));
        assertTrue(contenu.contains(tuile2));
        assertTrue(contenu.contains(tuile3));
    }
}