package game.plateau.tuiles;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.RepresentationManager;

public class TuileManagerTest {

    private TuileManager tuileManager;
    private RepresentationManager repManager;

    @BeforeEach
    public void init() {
        tuileManager = new TuileManager();
        repManager = new RepresentationManager();
    }

    @Test
    public void testCreerSac() {
        SacTuiles sac = tuileManager.creerSac(repManager);

        assertNotNull(sac);
        assertFalse(sac.estVide());
        assertEquals(71, sac.getNombreTuilesRestante());
        
        TuileV2 pioche = sac.piocher();
        assertNotNull(pioche);
        assertEquals(70, sac.getNombreTuilesRestante());
    }
}