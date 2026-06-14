package game.joueur;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.plateau.tuiles.segment.Segment;

public class JoueurTest {
    
    private Joueur joueur;
    private Segment segment;

    @BeforeEach
    public void init() {
        joueur = new Joueur("Alice");
        segment = null;
    }

    @Test
    public void testInit() {
        assertEquals("Alice", joueur.getNom());
        assertEquals(0, joueur.getScore());
        assertEquals(Joueur.NOMBRE_MEEPLES_DEPART, joueur.getMeeplesRestants());
    }

    @Test
    public void testAjouterScore() {
        joueur.ajouterScore(10);
        assertEquals(10, joueur.getScore());

        joueur.ajouterScore(-5);
        assertEquals(10, joueur.getScore());
    }

    @Test
    public void testPoserMeeple() {
        assertTrue(joueur.poserMeeple(segment));
        assertEquals(6, joueur.getMeeplesRestants());

        for (int i = 0; i < 6; i++) {
            assertTrue(joueur.poserMeeple(segment));
        }

        assertEquals(0, joueur.getMeeplesRestants());
        assertFalse(joueur.poserMeeple(segment));
    }

    @Test
    public void testRecupererMeeple() {
        joueur.poserMeeple(segment);
        assertEquals(6, joueur.getMeeplesRestants());

        joueur.recupererMeeple(segment);
        assertEquals(7, joueur.getMeeplesRestants());
    }
}