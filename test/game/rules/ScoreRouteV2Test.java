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
import game.plateau.tuiles.segment.SegmentType.SegmentRoute;

public class ScoreRouteV2Test {

    private TuileV2 creerTuileRoute(boolean nord, boolean est, boolean sud, boolean ouest) {
        TuileV2 tuile = new TuileV2();
        SegmentRoute routePartagee = new SegmentRoute("r");
        tuile.addSegments(Direction.NORD, nord ? routePartagee : new SegmentChamp("f"));
        tuile.addSegments(Direction.EST, est ? routePartagee : new SegmentChamp("f"));
        tuile.addSegments(Direction.SUD, sud ? routePartagee : new SegmentChamp("f"));
        tuile.addSegments(Direction.OUEST, ouest ? routePartagee : new SegmentChamp("f"));
        return tuile;
    }

    private Segment segmentRoute(TuileV2 tuile, Direction direction) {
        for (Segment segment : tuile.getBord(direction)) {
            if (segment.getRepresentation() != null && segment.getRepresentation().contains("r")) {
                return segment;
            }
        }
        return null;
    }

    private TuileV2 creerTuileRouteSlot(Direction direction, int slotRoute) {
        TuileV2 tuile = new TuileV2();
        for (Direction dir : Direction.values()) {
            if (dir == direction) {
                for (int i = 0; i < 3; i++) {
                    tuile.addSegments(dir, i == slotRoute ? new SegmentRoute("r") : new SegmentChamp("f"));
                }
            } else {
                tuile.addSegments(dir, new SegmentChamp("f"));
            }
        }
        return tuile;
    }

    @Test
    void scorePlacement_routeComplete_majorite_simple() {
        PlateauV2 plateau = new PlateauV2();
        ScoreRouteV2 regle = new ScoreRouteV2();
        Joueur alice = new Joueur("Alice");
        Joueur bob = new Joueur("Bob");

        Position p0 = new Position(0, 0);
        Position p1 = new Position(0, 1);

        TuileV2 t0 = creerTuileRoute(true, false, false, false);
        TuileV2 t1 = creerTuileRoute(false, false, true, false);

        plateau.Poser(t0, p0);
        plateau.Poser(t1, p1);

        Segment routeT0 = segmentRoute(t0, Direction.NORD);
        assertNotNull(routeT0);
        assertTrue(plateau.poserMeeple(p0, alice, routeT0));

        ScoreRouteV2.RouteScoreResult resultat = regle.attribuerScorePlacement(plateau, t1, p1);

        assertTrue(resultat.isRouteComplete());
        assertEquals(2, resultat.getNbTuilesRoute());
        assertEquals(2, resultat.getPointsRoute());
        assertEquals(2, alice.getScore());
        assertEquals(0, bob.getScore());
        assertEquals(7, alice.getMeeplesRestants());
        assertNull(routeT0.getMeeple());
    }

    @Test
    void scorePlacement_routeComplete_egalite() {
        PlateauV2 plateau = new PlateauV2();
        ScoreRouteV2 regle = new ScoreRouteV2();
        Joueur alice = new Joueur("Alice");
        Joueur bob = new Joueur("Bob");

        Position p0 = new Position(0, 0);
        Position p1 = new Position(0, 1);

        TuileV2 t0 = creerTuileRoute(true, false, false, false);
        TuileV2 t1 = creerTuileRoute(false, false, true, false);

        plateau.Poser(t0, p0);
        plateau.Poser(t1, p1);

        Segment routeT0 = segmentRoute(t0, Direction.NORD);
        Segment routeT1 = segmentRoute(t1, Direction.SUD);
        assertNotNull(routeT0);
        assertNotNull(routeT1);

        assertTrue(plateau.poserMeeple(p0, alice, routeT0));
        assertTrue(plateau.poserMeeple(p1, bob, routeT1));

        ScoreRouteV2.RouteScoreResult resultat = regle.attribuerScorePlacement(plateau, t1, p1);

        assertTrue(resultat.isRouteComplete());
        assertEquals(2, resultat.getPointsRoute());
        assertEquals(2, alice.getScore());
        assertEquals(2, bob.getScore());
    }

    @Test
    void scorePlacement_routeIncomplete_pasDePoint() {
        PlateauV2 plateau = new PlateauV2();
        ScoreRouteV2 regle = new ScoreRouteV2();
        Joueur alice = new Joueur("Alice");

        Position p0 = new Position(0, 0);
        TuileV2 t0 = creerTuileRoute(true, false, false, false);
        plateau.Poser(t0, p0);

        Segment routeT0 = segmentRoute(t0, Direction.NORD);
        assertNotNull(routeT0);
        assertTrue(plateau.poserMeeple(p0, alice, routeT0));

        ScoreRouteV2.RouteScoreResult resultat = regle.attribuerScorePlacement(plateau, t0, p0);

        assertFalse(resultat.isRouteComplete());
        assertEquals(1, resultat.getNbTuilesRoute());
        assertEquals(0, resultat.getPointsRoute());
        assertEquals(0, alice.getScore());
        assertEquals(6, alice.getMeeplesRestants());
        assertNotNull(routeT0.getMeeple());
    }

    @Test
    void scoreFinPartie_routeIncomplete_compte_tuiles_majorite() {
        PlateauV2 plateau = new PlateauV2();
        ScoreRouteV2 regle = new ScoreRouteV2();
        Joueur alice = new Joueur("Alice");

        Position p0 = new Position(0, 0);
        TuileV2 t0 = creerTuileRoute(true, false, false, false);
        plateau.Poser(t0, p0);

        Segment routeT0 = segmentRoute(t0, Direction.NORD);
        assertNotNull(routeT0);
        assertTrue(plateau.poserMeeple(p0, alice, routeT0));

        ScoreRouteV2.RouteScoreResult resultat = regle.attribuerScoreFinPartie(plateau, t0, p0);

        assertFalse(resultat.isRouteComplete());
        assertEquals(1, resultat.getPointsRoute());
        assertEquals(1, alice.getScore());
        assertNotNull(routeT0.getMeeple());
    }

    @Test
    void scorePlacement_boucle_est_complete() {
        PlateauV2 plateau = new PlateauV2();
        ScoreRouteV2 regle = new ScoreRouteV2();
        Joueur alice = new Joueur("Alice");

        Position p00 = new Position(0, 0);
        Position p10 = new Position(1, 0);
        Position p01 = new Position(0, 1);
        Position p11 = new Position(1, 1);

        TuileV2 t00 = creerTuileRoute(true, true, false, false);
        TuileV2 t10 = creerTuileRoute(true, false, false, true);
        TuileV2 t01 = creerTuileRoute(false, true, true, false);
        TuileV2 t11 = creerTuileRoute(false, false, true, true);

        plateau.Poser(t00, p00);
        plateau.Poser(t10, p10);
        plateau.Poser(t01, p01);
        plateau.Poser(t11, p11);

        Segment routeT00 = segmentRoute(t00, Direction.NORD);
        assertNotNull(routeT00);
        assertTrue(plateau.poserMeeple(p00, alice, routeT00));

        ScoreRouteV2.RouteScoreResult resultat = regle.attribuerScorePlacement(plateau, t11, p11);

        assertTrue(resultat.isRouteComplete());
        assertEquals(4, resultat.getNbTuilesRoute());
        assertEquals(4, resultat.getPointsRoute());
        assertEquals(4, alice.getScore());
    }

    @Test
    void voisin_present_mais_route_autre_slot_pas_connectee() {
        PlateauV2 plateau = new PlateauV2();
        ScoreRouteV2 regle = new ScoreRouteV2();
        Joueur alice = new Joueur("Alice");

        Position p0 = new Position(0, 0);
        Position p1 = new Position(0, 1);

        TuileV2 t0 = creerTuileRouteSlot(Direction.NORD, 1); // route au slot 1
        TuileV2 t1 = creerTuileRouteSlot(Direction.SUD, 0);  // route au slot 0 (different)

        plateau.Poser(t0, p0);
        plateau.Poser(t1, p1);

        Segment routeT0 = segmentRoute(t0, Direction.NORD);
        assertNotNull(routeT0);
        assertTrue(plateau.poserMeeple(p0, alice, routeT0));

        ScoreRouteV2.RouteScoreResult resultat = regle.attribuerScorePlacement(plateau, t0, p0);

        assertFalse(resultat.isRouteComplete());
        assertEquals(1, resultat.getNbTuilesRoute());
        assertEquals(0, resultat.getPointsRoute());
        assertEquals(0, alice.getScore());
    }

    @Test
    void deux_routes_differentes_sur_meme_tuile_ne_sont_pas_fusionnees() {
        PlateauV2 plateau = new PlateauV2();
        ScoreRouteV2 regle = new ScoreRouteV2();
        Joueur alice = new Joueur("Alice");
        Joueur bob = new Joueur("Bob");

        Position centre = new Position(0, 0);
        Position nord = new Position(0, 1);
        Position est = new Position(1, 0);

        TuileV2 tCentre = new TuileV2();
        Segment routeNordSud = new SegmentRoute("r");
        Segment routeEstOuest = new SegmentRoute("r");

        tCentre.addSegments(Direction.NORD, routeNordSud);
        tCentre.addSegments(Direction.SUD, routeNordSud);
        tCentre.addSegments(Direction.EST, routeEstOuest);
        tCentre.addSegments(Direction.OUEST, routeEstOuest);

        TuileV2 tNord = creerTuileRoute(false, false, true, false);
        TuileV2 tEst = creerTuileRoute(false, false, false, true);

        plateau.Poser(tCentre, centre);
        plateau.Poser(tNord, nord);
        plateau.Poser(tEst, est);

        assertTrue(plateau.poserMeeple(centre, alice, routeNordSud));
        assertTrue(plateau.poserMeeple(centre, bob, routeEstOuest));

        ScoreRouteV2.RouteScoreResult resultat = regle.attribuerScorePlacement(plateau, tCentre, centre);

        assertFalse(resultat.isRouteComplete());
        assertEquals(0, resultat.getPointsRoute());
        assertEquals(0, alice.getScore());
        assertEquals(0, bob.getScore());
    }
}
