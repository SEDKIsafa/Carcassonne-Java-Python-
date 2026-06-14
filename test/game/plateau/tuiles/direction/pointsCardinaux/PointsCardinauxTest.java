package game.plateau.tuiles.direction.pointsCardinaux;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import game.plateau.tuiles.segment.Segment;

public class PointsCardinauxTest {

    private pointsCardinaux pointBase;

    // methode interne pour créer une liste de 3 segments
    private List<Segment> createList(String s1, String s2, String s3) {
        List<Segment> list = new ArrayList<>();
        list.add(new Segment(s1));
        list.add(new Segment(s2));
        list.add(new Segment(s3));
        return list;
    }

    @BeforeEach
    public void init() {
        pointBase = new pointsCardinaux();
    }

    @Test
    public void testBaseGetAndSetSegments() {
        assertNotNull(pointBase.getSegments());
        assertEquals(0, pointBase.getSegments().size());

        List<Segment> testList = createList("f0", "r", "f1");
        pointBase.setSegments(testList);

        assertEquals(testList, pointBase.getSegments());
        assertEquals(3, pointBase.getSegments().size());
    }

    @Test
    public void testToStringNord() {
        pointCardinalNord nord = new pointCardinalNord();
        nord.setSegments(createList("f0", "c", "f1"));
        assertEquals("| NORD : f0cf1 |", nord.toString());
    }

    @Test
    public void testToStringSud() {
        pointCardinalSud sud = new pointCardinalSud();
        sud.setSegments(createList("f", "f", "f"));
        assertEquals("| SUD : fff |", sud.toString());
    }

    @Test
    public void testToStringEst() {
        pointCardinalEst est = new pointCardinalEst();
        est.setSegments(createList("f0", "c", "f1"));
        assertEquals("| EST : f0cf1 |", est.toString());
    }

    @Test
    public void testToStringOuest() {
        pointCardinalOuest ouest = new pointCardinalOuest();
        ouest.setSegments(createList("f", "f", "f"));
        assertEquals("| OUEST : fff |", ouest.toString());
    }
}