package game.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
import game.plateau.tuiles.segment.SegmentType.SegmentRoute;

public class RegleMeepleTest {

    @Test
    public void testValidateAvecPlateauNull() {
        RegleMeeple regle = new RegleMeeple();
        Joueur joueur = new Joueur("Alice");
        Segment segment = new SegmentRoute("r");
        Position pos = new Position(10, 10);

        assertFalse(regle.validate(null, pos, joueur, segment));
    }

    @Test
    public void testValidateAvecPositionNull() {
        RegleMeeple regle = new RegleMeeple();
        PlateauV2 plateau = new PlateauV2();
        Joueur joueur = new Joueur("Alice");
        Segment segment = new SegmentRoute("r");

        assertFalse(regle.validate(plateau, null, joueur, segment));
    }

    @Test
    public void testValidateAvecSegmentNull() {
        RegleMeeple regle = new RegleMeeple();
        PlateauV2 plateau = new PlateauV2();
        Joueur joueur = new Joueur("Alice");
        Position pos = new Position(10, 10);

        assertFalse(regle.validate(plateau, pos, joueur, null));
    }

    @Test
    public void testValidateAvecJoueurNull() {
        RegleMeeple regle = new RegleMeeple();
        PlateauV2 plateau = new PlateauV2();
        Segment segment = new SegmentRoute("r");
        Position pos = new Position(10, 10);

        assertFalse(regle.validate(plateau, pos, null, segment));
    }

    @Test
    public void testValidateSansTuileAPosition() {
        RegleMeeple regle = new RegleMeeple();
        PlateauV2 plateau = new PlateauV2();
        Joueur joueur = new Joueur("Alice");
        Segment segment = new SegmentRoute("r");
        Position pos = new Position(10, 10);

        assertFalse(regle.validate(plateau, pos, joueur, segment));
    }

    @Test
    public void testValidateAvecSegmentAbsentDeLaTuile() {
        RegleMeeple regle = new RegleMeeple();
        PlateauV2 plateau = new PlateauV2();
        Joueur joueur = new Joueur("Alice");

        TuileV2 tuile = new TuileV2();
        Segment segmentInTile = new SegmentRoute("r-in");
        Segment segmentNotInTile = new SegmentRoute("r-out");

        tuile.addSegments(Direction.NORD, segmentInTile);

        Position pos = new Position(10, 10);
        plateau.Poser(tuile, pos);

        assertFalse(regle.validate(plateau, pos, joueur, segmentNotInTile));
    }

    @Test
    public void testValidateSansMeepleRestant() {
        RegleMeeple regle = new RegleMeeple();
        PlateauV2 plateau = new PlateauV2();
        Joueur joueur = new Joueur("Alice");

        TuileV2 tuile = new TuileV2();
        Segment segment = new SegmentRoute("r");
        tuile.addSegments(Direction.NORD, segment);

        Position pos = new Position(10, 10);
        plateau.Poser(tuile, pos);

        for (int i = 0; i < Joueur.NOMBRE_MEEPLES_DEPART; i++) {
            joueur.poserMeeple(segment);
        }

        assertFalse(regle.validate(plateau, pos, joueur, segment));
    }

    @Test
    public void testValidateSegmentCibleDejaOccupe() {
        RegleMeeple regle = new RegleMeeple();
        PlateauV2 plateau = new PlateauV2();

        Joueur alice = new Joueur("Alice");
        Joueur bob = new Joueur("Bob");

        TuileV2 tuile = new TuileV2();
        Segment segment = new SegmentRoute("r");
        tuile.addSegments(Direction.NORD, segment);

        Position pos = new Position(10, 10);
        plateau.Poser(tuile, pos);

        plateau.poserMeeple(pos, bob, segment);

        assertFalse(regle.validate(plateau, pos, alice, segment));
    }

    @Test
    public void testValidateRouteConnecteeDejaOccupee() {
        RegleMeeple regle = new RegleMeeple();
        PlateauV2 plateau = new PlateauV2();

        Joueur alice = new Joueur("Alice");
        Joueur bob = new Joueur("Bob");

        TuileV2 t0 = new TuileV2();
        TuileV2 t1 = new TuileV2();

        Segment r0 = new SegmentRoute("r0");
        Segment r1 = new SegmentRoute("r1");

        t0.addSegments(Direction.EST, r0);
        t1.addSegments(Direction.OUEST, r1);

        Position p0 = new Position(20, 20);
        Position p1 = new Position(21, 20);

        plateau.Poser(t0, p0);
        plateau.Poser(t1, p1);

        plateau.poserMeeple(p1, bob, r1);

        assertFalse(regle.validate(plateau, p0, alice, r0));
    }

    @Test
    public void testValidateRouteDifferenteMemeType() {
        RegleMeeple regle = new RegleMeeple();
        PlateauV2 plateau = new PlateauV2();

        Joueur alice = new Joueur("Alice");
        Joueur bob = new Joueur("Bob");

        TuileV2 t0 = new TuileV2();
        TuileV2 t1 = new TuileV2();

        Segment routeTarget = new SegmentRoute("r-target");
        Segment champTarget = new SegmentChamp("f-target");

        Segment champNeighbor = new SegmentChamp("f-neighbor");
        Segment routeNeighborWithMeeple = new SegmentRoute("r-neighbor");

       
        t0.addSegments(Direction.EST, routeTarget);
        t0.addSegments(Direction.EST, champTarget);


        t1.addSegments(Direction.OUEST, champNeighbor);
        t1.addSegments(Direction.OUEST, routeNeighborWithMeeple);

        Position p0 = new Position(30, 30);
        Position p1 = new Position(31, 30);

        plateau.Poser(t0, p0);
        plateau.Poser(t1, p1);

        plateau.poserMeeple(p1, bob, routeNeighborWithMeeple);

        // Le meeple voisin est sur une autre route (index different)
        assertTrue(regle.validate(plateau, p0, alice, routeTarget));
    }

    @Test
    public void testValidateAbbayeDejaOccupee() {
        RegleMeeple regle = new RegleMeeple();
        PlateauV2 plateau = new PlateauV2();

        Joueur alice = new Joueur("Alice");
        Joueur bob = new Joueur("Bob");

        TuileV2 tuile = new TuileV2();
        Segment abbaye = new SegmentAbbaye("A");
        tuile.addAbbaye(abbaye);

        Position pos = new Position(40, 40);
        plateau.Poser(tuile, pos);

        plateau.poserMeeple(pos, bob, abbaye);

        assertFalse(regle.validate(plateau, pos, alice, abbaye));
    }

    @Test
    public void testValidatePlacementValide() {
        RegleMeeple regle = new RegleMeeple();
        PlateauV2 plateau = new PlateauV2();

        Joueur alice = new Joueur("Alice");

        TuileV2 tuile = new TuileV2();
        Segment segment = new SegmentRoute("r");
        tuile.addSegments(Direction.NORD, segment);

        Position pos = new Position(50, 50);
        plateau.Poser(tuile, pos);

        assertTrue(regle.validate(plateau, pos, alice, segment));
    }
}