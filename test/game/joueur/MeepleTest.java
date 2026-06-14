package game.joueur;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import game.plateau.tuiles.segment.Segment;

public class MeepleTest {

    private Joueur joueur;
    private Meeple meeple;
    private Segment segment;

    @BeforeEach
    public void init() {
        joueur = new Joueur("Bob");
        segment = null;
        meeple = new Meeple(joueur, segment);
    }

    @Test
    public void testGetOwner() {
        assertEquals(joueur, meeple.getOwner());
        assertEquals("Bob", meeple.getOwner().getNom());
    }

    @Test
    public void testGetZone() {
        assertEquals(segment, meeple.getZone());
    }
}