package game.plateau.tuiles.direction;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DirectionTest {

    @Test
    public void testEnumValues() {
        Direction[] directions = Direction.values();
        assertEquals(4, directions.length);
        
        assertEquals(Direction.NORD, Direction.valueOf("NORD"));
        assertEquals(Direction.SUD, Direction.valueOf("SUD"));
        assertEquals(Direction.EST, Direction.valueOf("EST"));
        assertEquals(Direction.OUEST, Direction.valueOf("OUEST"));
    }
}