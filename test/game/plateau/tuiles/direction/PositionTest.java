package game.plateau.tuiles.direction;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PositionTest {
    
    private Position p1;
    private Position p2;
    private Position p3;

    @BeforeEach
    public void init() {
        p1 = new Position(1, 2);
        p2 = new Position(1, 2); 
        p3 = new Position(3, 4);
    }

    @Test
    public void testGetters() {
        assertEquals(1, p1.getX());
        assertEquals(2, p1.getY());
    }

    @Test
    public void testEquals() {
        //identiques
        assertTrue(p1.equals(p2));
        assertTrue(p1.equals(p1));
        //pas identiques
        assertFalse(p1.equals(p3));
    }

    @Test
    public void testHashCode() {
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1.hashCode(), p3.hashCode());
    }

    @Test
    public void testToString() {
        assertEquals("(1,2)", p1.toString());
    }
}