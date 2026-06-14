package game.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import game.plateau.PlateauV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Direction;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.SegmentType.SegmentAbbaye;
import game.plateau.tuiles.segment.SegmentType.SegmentChamp;
import game.plateau.tuiles.segment.SegmentType.SegmentRoute;

public class RuleEngineTest {

    private TuileV2 creerTuileSimple() {
        TuileV2 tuile = new TuileV2();
        tuile.addSegments(Direction.NORD, new SegmentChamp("f"));
        tuile.addSegments(Direction.EST, new SegmentChamp("f"));
        tuile.addSegments(Direction.SUD, new SegmentChamp("f"));
        tuile.addSegments(Direction.OUEST, new SegmentChamp("f"));
        return tuile;
    }

    @Test
    void calculeScorePlacement_abbaye_complete() {
        PlateauV2 plateau = new PlateauV2();
        RuleEngine engine = new RuleEngine();

        Position centre = new Position(10, 10);
        TuileV2 abbaye = creerTuileSimple();
        abbaye.addAbbaye(new SegmentAbbaye("A"));
        plateau.Poser(abbaye, centre);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                plateau.Poser(creerTuileSimple(), new Position(10 + dx, 10 + dy));
            }
        }

        int total = engine.calculerScorePlacement(plateau, abbaye, centre);
        assertEquals(9, total);
    }

    @Test
    void calculeScoreFinPartie_abbaye_incomplete() {
        PlateauV2 plateau = new PlateauV2();
        RuleEngine engine = new RuleEngine();

        Position centre = new Position(12, 12);
        TuileV2 abbaye = creerTuileSimple();
        abbaye.addAbbaye(new SegmentAbbaye("A"));
        plateau.Poser(abbaye, centre);

        plateau.Poser(creerTuileSimple(), new Position(13, 12));
        plateau.Poser(creerTuileSimple(), new Position(12, 13));

        int total = engine.calculerScoreFinPartie(plateau, abbaye, centre);
        assertEquals(3, total); // 1 + 2 voisins
    }

    @Test
    void calculeScorePlacement_route_complete() {
        PlateauV2 plateau = new PlateauV2();
        RuleEngine engine = new RuleEngine();

        Position p0 = new Position(20, 20);
        Position p1 = new Position(20, 21);

        TuileV2 t0 = new TuileV2();
        t0.addSegments(Direction.NORD, new SegmentRoute("r"));
        t0.addSegments(Direction.EST, new SegmentChamp("f"));
        t0.addSegments(Direction.SUD, new SegmentChamp("f"));
        t0.addSegments(Direction.OUEST, new SegmentChamp("f"));

        TuileV2 t1 = new TuileV2();
        t1.addSegments(Direction.NORD, new SegmentChamp("f"));
        t1.addSegments(Direction.EST, new SegmentChamp("f"));
        t1.addSegments(Direction.SUD, new SegmentRoute("r"));
        t1.addSegments(Direction.OUEST, new SegmentChamp("f"));

        plateau.Poser(t0, p0);
        plateau.Poser(t1, p1);

        int total = engine.calculerScorePlacement(plateau, t0, p0);
        assertEquals(2, total);
    }

    @Test
    void calculeScoreFinPartie_route_incomplete() {
        PlateauV2 plateau = new PlateauV2();
        RuleEngine engine = new RuleEngine();

        Position p0 = new Position(30, 30);

        TuileV2 t0 = new TuileV2();
        t0.addSegments(Direction.NORD, new SegmentRoute("r"));
        t0.addSegments(Direction.EST, new SegmentChamp("f"));
        t0.addSegments(Direction.SUD, new SegmentChamp("f"));
        t0.addSegments(Direction.OUEST, new SegmentChamp("f"));

        plateau.Poser(t0, p0);

        int total = engine.calculerScoreFinPartie(plateau, t0, p0);
        assertEquals(1, total);
    }
}
