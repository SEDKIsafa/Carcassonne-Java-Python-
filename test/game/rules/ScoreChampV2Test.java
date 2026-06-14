package game.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

public class ScoreChampV2Test {

    private TuileV2 creerTuileSimpleChamp() {
        TuileV2 tuile = new TuileV2();
        SegmentChamp champ = new SegmentChamp("f");
        tuile.addSegments(Direction.NORD, champ);
        tuile.addSegments(Direction.EST, champ);
        tuile.addSegments(Direction.SUD, champ);
        tuile.addSegments(Direction.OUEST, champ);
        return tuile;
    }

    private TuileV2 creerTuileVilleExtremite(Direction directionVille, boolean blason) {
        TuileV2 tuile = new TuileV2();
        tuile.addSegments(Direction.NORD, directionVille == Direction.NORD ? new SegmentVille("c") : new SegmentChamp("f"));
        tuile.addSegments(Direction.EST, directionVille == Direction.EST ? new SegmentVille("c") : new SegmentChamp("f"));
        tuile.addSegments(Direction.SUD, directionVille == Direction.SUD ? new SegmentVille("c") : new SegmentChamp("f"));
        tuile.addSegments(Direction.OUEST, directionVille == Direction.OUEST ? new SegmentVille("c") : new SegmentChamp("f"));
        tuile.setBlason(blason);
        return tuile;
    }

    private Segment premierChamp(TuileV2 tuile) {
        for (Direction direction : Direction.values()) {
            for (Segment segment : tuile.getBord(direction)) {
                if (segment instanceof SegmentChamp) {
                    return segment;
                }
            }
        }
        return null;
    }

    @Test
    void scorePlacement_champ_toujours_zero() {
        PlateauV2 plateau = new PlateauV2();
        ScoreChampV2 regle = new ScoreChampV2();

        Position p0 = new Position(0, 0);
        TuileV2 champ = creerTuileSimpleChamp();
        plateau.Poser(champ, p0);

        ScoreChampV2.ChampScoreResult resultat = regle.attribuerScorePlacement(plateau, champ, p0);

        assertEquals(0, resultat.getPointsChamp());
        assertEquals(0, resultat.getNbVillesCompletesAdjacentes());
    }

    @Test
    void scoreFinPartie_champ_avec_une_ville_complete() {
        PlateauV2 plateau = new PlateauV2();
        ScoreChampV2 regle = new ScoreChampV2();
        Joueur alice = new Joueur("Alice");

        Position champPos = new Position(0, 0);
        Position villeBas = new Position(1, 0);
        Position villeHaut = new Position(1, 1);

        TuileV2 champ = creerTuileSimpleChamp();
        TuileV2 ville0 = creerTuileVilleExtremite(Direction.NORD, false);
        TuileV2 ville1 = creerTuileVilleExtremite(Direction.SUD, false);

        plateau.Poser(champ, champPos);
        plateau.Poser(ville0, villeBas);
        plateau.Poser(ville1, villeHaut);

        Segment champMeeple = premierChamp(champ);
        assertTrue(plateau.poserMeeple(champPos, alice, champMeeple));

        ScoreChampV2.ChampScoreResult resultat = regle.attribuerScoreFinPartie(plateau, champ, champPos);

        assertEquals(1, resultat.getNbVillesCompletesAdjacentes());
        assertEquals(3, resultat.getPointsChamp());
        assertEquals(3, alice.getScore());
    }

    @Test
    void scoreFinPartie_champ_egalite_majorite() {
        PlateauV2 plateau = new PlateauV2();
        ScoreChampV2 regle = new ScoreChampV2();
        Joueur alice = new Joueur("Alice");
        Joueur bob = new Joueur("Bob");

        Position champ0Pos = new Position(2, 0);
        Position champ1Pos = new Position(2, 1);
        Position villeBas = new Position(3, 0);
        Position villeHaut = new Position(3, 1);

        TuileV2 champ0 = creerTuileSimpleChamp();
        TuileV2 champ1 = creerTuileSimpleChamp();
        TuileV2 ville0 = creerTuileVilleExtremite(Direction.NORD, false);
        TuileV2 ville1 = creerTuileVilleExtremite(Direction.SUD, false);

        plateau.Poser(champ0, champ0Pos);
        plateau.Poser(champ1, champ1Pos);
        plateau.Poser(ville0, villeBas);
        plateau.Poser(ville1, villeHaut);

        assertTrue(plateau.poserMeeple(champ0Pos, alice, premierChamp(champ0)));
        assertTrue(plateau.poserMeeple(champ1Pos, bob, premierChamp(champ1)));

        ScoreChampV2.ChampScoreResult resultat = regle.attribuerScoreFinPartie(plateau, champ0, champ0Pos);

        assertEquals(1, resultat.getNbVillesCompletesAdjacentes());
        assertEquals(3, resultat.getPointsChamp());
        assertEquals(3, alice.getScore());
        assertEquals(3, bob.getScore());
    }

    @Test
    void scoreFinPartie_champ_ne_compte_pas_ville_incomplete() {
        PlateauV2 plateau = new PlateauV2();
        ScoreChampV2 regle = new ScoreChampV2();
        Joueur alice = new Joueur("Alice");

        Position champPos = new Position(4, 0);
        Position villePos = new Position(5, 0);

        TuileV2 champ = creerTuileSimpleChamp();
        TuileV2 ville = creerTuileVilleExtremite(Direction.NORD, false);

        plateau.Poser(champ, champPos);
        plateau.Poser(ville, villePos);

        assertTrue(plateau.poserMeeple(champPos, alice, premierChamp(champ)));

        ScoreChampV2.ChampScoreResult resultat = regle.attribuerScoreFinPartie(plateau, champ, champPos);

        assertEquals(0, resultat.getNbVillesCompletesAdjacentes());
        assertEquals(0, resultat.getPointsChamp());
        assertEquals(0, alice.getScore());
    }
}
