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
import game.plateau.tuiles.segment.SegmentType.SegmentChamp;
import game.plateau.tuiles.segment.SegmentType.SegmentVille;

public class ScoreVilleV2Test {

    private TuileV2 creerTuileVille(boolean nord, boolean est, boolean sud, boolean ouest) {
        TuileV2 tuile = new TuileV2();
        SegmentVille villePartagee = new SegmentVille("c");
        tuile.addSegments(Direction.NORD, nord ? villePartagee : new SegmentChamp("f"));
        tuile.addSegments(Direction.EST, est ? villePartagee : new SegmentChamp("f"));
        tuile.addSegments(Direction.SUD, sud ? villePartagee : new SegmentChamp("f"));
        tuile.addSegments(Direction.OUEST, ouest ? villePartagee : new SegmentChamp("f"));
        return tuile;
    }

    private Segment segmentVille(TuileV2 tuile, Direction direction) {
        for (Segment segment : tuile.getBord(direction)) {
            if (segment instanceof SegmentVille) {
                return segment;
            }
        }
        return null;
    }

    @Test
    void scorePlacement_villeComplete_majorite_simple() {
        PlateauV2 plateau = new PlateauV2();
        ScoreVilleV2 regle = new ScoreVilleV2();
        Joueur alice = new Joueur("Alice");

        Position p0 = new Position(0, 0);
        Position p1 = new Position(0, 1);

        TuileV2 t0 = creerTuileVille(true, false, false, false);
        TuileV2 t1 = creerTuileVille(false, false, true, false);

        plateau.Poser(t0, p0);
        plateau.Poser(t1, p1);

        Segment villeT0 = segmentVille(t0, Direction.NORD);
        assertNotNull(villeT0);
        assertTrue(plateau.poserMeeple(p0, alice, villeT0));

        ScoreVilleV2.VilleScoreResult resultat = regle.attribuerScorePlacement(plateau, t1, p1);

        assertTrue(resultat.isVilleComplete());
        assertEquals(2, resultat.getNbTuilesVille());
        assertEquals(0, resultat.getNbBlasons());
        assertEquals(4, resultat.getPointsVille());
        assertEquals(4, alice.getScore());
        assertEquals(7, alice.getMeeplesRestants());
        assertNull(villeT0.getMeeple());
    }

    @Test
    void scorePlacement_villeComplete_avecBlason() {
        PlateauV2 plateau = new PlateauV2();
        ScoreVilleV2 regle = new ScoreVilleV2();
        Joueur alice = new Joueur("Alice");

        Position p0 = new Position(2, 0);
        Position p1 = new Position(2, 1);

        TuileV2 t0 = creerTuileVille(true, false, false, false);
        TuileV2 t1 = creerTuileVille(false, false, true, false);
        t0.setBlason(true);

        plateau.Poser(t0, p0);
        plateau.Poser(t1, p1);

        Segment villeT0 = segmentVille(t0, Direction.NORD);
        assertNotNull(villeT0);
        assertTrue(plateau.poserMeeple(p0, alice, villeT0));

        ScoreVilleV2.VilleScoreResult resultat = regle.attribuerScorePlacement(plateau, t1, p1);

        assertTrue(resultat.isVilleComplete());
        assertEquals(2, resultat.getNbTuilesVille());
        assertEquals(1, resultat.getNbBlasons());
        assertEquals(6, resultat.getPointsVille());
        assertEquals(6, alice.getScore());
    }

    @Test
    void scorePlacement_villeComplete_egalite() {
        PlateauV2 plateau = new PlateauV2();
        ScoreVilleV2 regle = new ScoreVilleV2();
        Joueur alice = new Joueur("Alice");
        Joueur bob = new Joueur("Bob");

        Position p0 = new Position(4, 0);
        Position p1 = new Position(4, 1);

        TuileV2 t0 = creerTuileVille(true, false, false, false);
        TuileV2 t1 = creerTuileVille(false, false, true, false);

        plateau.Poser(t0, p0);
        plateau.Poser(t1, p1);

        Segment villeT0 = segmentVille(t0, Direction.NORD);
        Segment villeT1 = segmentVille(t1, Direction.SUD);
        assertTrue(plateau.poserMeeple(p0, alice, villeT0));
        assertTrue(plateau.poserMeeple(p1, bob, villeT1));

        ScoreVilleV2.VilleScoreResult resultat = regle.attribuerScorePlacement(plateau, t1, p1);

        assertTrue(resultat.isVilleComplete());
        assertEquals(4, resultat.getPointsVille());
        assertEquals(4, alice.getScore());
        assertEquals(4, bob.getScore());
    }

    @Test
    void scorePlacement_villeIncomplete_pasDePoint() {
        PlateauV2 plateau = new PlateauV2();
        ScoreVilleV2 regle = new ScoreVilleV2();
        Joueur alice = new Joueur("Alice");

        Position p0 = new Position(6, 0);
        TuileV2 t0 = creerTuileVille(true, false, false, false);
        plateau.Poser(t0, p0);

        Segment villeT0 = segmentVille(t0, Direction.NORD);
        assertNotNull(villeT0);
        assertTrue(plateau.poserMeeple(p0, alice, villeT0));

        ScoreVilleV2.VilleScoreResult resultat = regle.attribuerScorePlacement(plateau, t0, p0);

        assertFalse(resultat.isVilleComplete());
        assertEquals(1, resultat.getNbTuilesVille());
        assertEquals(0, resultat.getPointsVille());
        assertEquals(0, alice.getScore());
        assertEquals(6, alice.getMeeplesRestants());
        assertNotNull(villeT0.getMeeple());
    }

    @Test
    void scoreFinPartie_villeIncomplete_compte_tuiles_et_blasons() {
        PlateauV2 plateau = new PlateauV2();
        ScoreVilleV2 regle = new ScoreVilleV2();
        Joueur alice = new Joueur("Alice");

        Position p0 = new Position(8, 0);
        TuileV2 t0 = creerTuileVille(true, false, false, false);
        t0.setBlason(true);
        plateau.Poser(t0, p0);

        Segment villeT0 = segmentVille(t0, Direction.NORD);
        assertNotNull(villeT0);
        assertTrue(plateau.poserMeeple(p0, alice, villeT0));

        ScoreVilleV2.VilleScoreResult resultat = regle.attribuerScoreFinPartie(plateau, t0, p0);

        assertFalse(resultat.isVilleComplete());
        assertEquals(1, resultat.getNbTuilesVille());
        assertEquals(1, resultat.getNbBlasons());
        assertEquals(2, resultat.getPointsVille());
        assertEquals(2, alice.getScore());
        assertNotNull(villeT0.getMeeple());
    }
}
