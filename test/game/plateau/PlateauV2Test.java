package game.plateau;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.joueur.Joueur;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.Segment;

public class PlateauV2Test {

    private PlateauV2 plateau;
    private TuileV2 tuile;
    private Position position;
    private Joueur joueur;
    private Segment segment;

    @BeforeEach
    public void init() {
        plateau = new PlateauV2();
        tuile = new TuileV2();
        position = new Position(37, 36);
        joueur = new Joueur("Alice");
        segment = new Segment("r");
        tuile.addSegmentToSegments(segment);
    }

    @Test
    public void testInitPlateau() {
        // La tuile de départ doit être en 36:36
        assertNotNull(plateau.getTuile(new Position(36, 36)));
        assertFalse(plateau.peutPoser(new Position(36, 36)));
        assertTrue(plateau.peutPoser(new Position(37, 36)));
    }

    @Test
    public void testPoserTuileEtLimites() {
        plateau.Poser(tuile, position);
        
        assertEquals(tuile, plateau.getTuile(position));
        assertFalse(plateau.peutPoser(position));
        assertNotNull(plateau.getTuile(new Position(37, 36)));
    }

    @Test
    public void testPoserEtRecupererMeeple() {
        plateau.Poser(tuile, position);
        
        // Pose
        assertTrue(plateau.poserMeeple(position, joueur, segment));
        assertEquals("Alice", segment.getMeeple().getOwner().getNom());
        assertEquals(6, joueur.getMeeplesRestants());
        
        // Récupération
        plateau.recupererMeeple(position, segment);
        assertNull(segment.getMeeple());
        assertEquals(7, joueur.getMeeplesRestants());
    }
}