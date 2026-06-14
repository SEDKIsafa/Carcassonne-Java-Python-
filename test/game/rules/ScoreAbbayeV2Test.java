package game.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import game.joueur.Joueur;
import game.plateau.PlateauV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Direction;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.Segment;
import game.plateau.tuiles.segment.SegmentType.SegmentAbbaye;
import game.plateau.tuiles.segment.SegmentType.SegmentChamp;

public class ScoreAbbayeV2Test {

    private TuileV2 creerTuileSimple() {
        TuileV2 tuile = new TuileV2();
        tuile.addSegments(Direction.NORD, new SegmentChamp("f"));
        tuile.addSegments(Direction.EST, new SegmentChamp("f"));
        tuile.addSegments(Direction.SUD, new SegmentChamp("f"));
        tuile.addSegments(Direction.OUEST, new SegmentChamp("f"));
        return tuile;
    }

    private TuileV2 creerTuileAbbaye() {
        TuileV2 tuile = creerTuileSimple();
        tuile.addAbbaye(new SegmentAbbaye("A"));
        return tuile;
    }

    @Test
    void placement_abbaye_complete_9_points_et_recup_meeple() {
        PlateauV2 plateau = new PlateauV2();
        ScoreAbbayeV2 regle = new ScoreAbbayeV2();
        Joueur alice = new Joueur("Alice");

        Position centre = new Position(10, 10);
        TuileV2 abbaye = creerTuileAbbaye();
        plateau.Poser(abbaye, centre);

        Segment segmentAbbaye = abbaye.getAbbaye();
        assertNotNull(segmentAbbaye);
        assertTrue(plateau.poserMeeple(centre, alice, segmentAbbaye));

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                plateau.Poser(creerTuileSimple(), new Position(10 + dx, 10 + dy));
            }
        }

        ScoreAbbayeV2.AbbayeScoreResult resultat = regle.attribuerScorePlacement(plateau, abbaye, centre);

        assertTrue(resultat.hasAbbaye());
        assertTrue(resultat.isAbbayeComplete());
        assertEquals(8, resultat.getNbVoisins());
        assertEquals(9, resultat.getPointsAbbaye());
        assertEquals(9, alice.getScore());
        assertEquals(7, alice.getMeeplesRestants());
        assertNull(segmentAbbaye.getMeeple());
    }

    @Test
    void placement_abbaye_incomplete_pas_de_points() {
        PlateauV2 plateau = new PlateauV2();
        ScoreAbbayeV2 regle = new ScoreAbbayeV2();
        Joueur alice = new Joueur("Alice");

        Position centre = new Position(12, 12);
        TuileV2 abbaye = creerTuileAbbaye();
        plateau.Poser(abbaye, centre);

        Segment segmentAbbaye = abbaye.getAbbaye();
        assertNotNull(segmentAbbaye);
        assertTrue(plateau.poserMeeple(centre, alice, segmentAbbaye));

        plateau.Poser(creerTuileSimple(), new Position(13, 12));
        plateau.Poser(creerTuileSimple(), new Position(12, 13));

        ScoreAbbayeV2.AbbayeScoreResult resultat = regle.attribuerScorePlacement(plateau, abbaye, centre);

        assertFalse(resultat.isAbbayeComplete());
        assertEquals(2, resultat.getNbVoisins());
        assertEquals(0, resultat.getPointsAbbaye());
        assertEquals(0, alice.getScore());
        assertEquals(6, alice.getMeeplesRestants());
        assertNotNull(segmentAbbaye.getMeeple());
    }

    @Test
    void fin_partie_abbaye_incomplete_score_progressif() {
        PlateauV2 plateau = new PlateauV2();
        ScoreAbbayeV2 regle = new ScoreAbbayeV2();
        Joueur alice = new Joueur("Alice");

        Position centre = new Position(14, 14);
        TuileV2 abbaye = creerTuileAbbaye();
        plateau.Poser(abbaye, centre);

        Segment segmentAbbaye = abbaye.getAbbaye();
        assertNotNull(segmentAbbaye);
        assertTrue(plateau.poserMeeple(centre, alice, segmentAbbaye));

        plateau.Poser(creerTuileSimple(), new Position(15, 14));
        plateau.Poser(creerTuileSimple(), new Position(14, 15));

        ScoreAbbayeV2.AbbayeScoreResult resultat = regle.attribuerScoreFinPartie(plateau, abbaye, centre);

        assertFalse(resultat.isAbbayeComplete());
        assertEquals(2, resultat.getNbVoisins());
        assertEquals(3, resultat.getPointsAbbaye());
        assertEquals(3, alice.getScore());
        assertNotNull(segmentAbbaye.getMeeple());
    }

    @Test
    void abbaye_complete_sans_meeple_pas_attribution() {
        PlateauV2 plateau = new PlateauV2();
        ScoreAbbayeV2 regle = new ScoreAbbayeV2();
        Joueur alice = new Joueur("Alice");

        Position centre = new Position(16, 16);
        TuileV2 abbaye = creerTuileAbbaye();
        plateau.Poser(abbaye, centre);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                plateau.Poser(creerTuileSimple(), new Position(16 + dx, 16 + dy));
            }
        }

        ScoreAbbayeV2.AbbayeScoreResult resultat = regle.attribuerScorePlacement(plateau, abbaye, centre);

        assertTrue(resultat.isAbbayeComplete());
        assertEquals(9, resultat.getPointsAbbaye());
        assertNull(resultat.getProprietaire());
        assertEquals(0, alice.getScore());
    }
}
