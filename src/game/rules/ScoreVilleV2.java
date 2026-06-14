package game.rules;

import game.joueur.Joueur;
import game.joueur.Meeple;
import game.plateau.PlateauV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Direction;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.Segment;
import game.plateau.tuiles.segment.SegmentType.SegmentVille;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Regle de score des villes pour le modele V2 (PlateauV2 / TuileV2).
 *
 * Regles implementees:
 * - En cours de partie: une ville complete vaut 2 points par tuile + 2 points par blason
 * - Fin de partie: une ville incomplete vaut 1 point par tuile + 1 point par blason
 * - Majorite/egalite geree par composante de ville
 * - Recuperation des meeples uniquement si la ville est complete en cours de partie
 */
public class ScoreVilleV2 {

    public static final class VilleScoreResult {
        private final boolean villeComplete;
        private final int nbTuilesVille;
        private final int nbBlasons;
        private final int pointsVille;
        private final Map<Joueur, Integer> meeplesParJoueur;
        private final Set<Joueur> joueursMajoritaires;
        private final List<Segment> segmentsVilleAvecMeeple;

        private VilleScoreResult(
            boolean villeComplete,
            int nbTuilesVille,
            int nbBlasons,
            int pointsVille,
            Map<Joueur, Integer> meeplesParJoueur,
            Set<Joueur> joueursMajoritaires,
            List<Segment> segmentsVilleAvecMeeple
        ) {
            this.villeComplete = villeComplete;
            this.nbTuilesVille = nbTuilesVille;
            this.nbBlasons = nbBlasons;
            this.pointsVille = pointsVille;
            this.meeplesParJoueur = Collections.unmodifiableMap(new HashMap<>(meeplesParJoueur));
            this.joueursMajoritaires = Collections.unmodifiableSet(new LinkedHashSet<>(joueursMajoritaires));
            this.segmentsVilleAvecMeeple = Collections.unmodifiableList(new ArrayList<>(segmentsVilleAvecMeeple));
        }

        public boolean isVilleComplete() {
            return villeComplete;
        }

        public int getNbTuilesVille() {
            return nbTuilesVille;
        }

        public int getNbBlasons() {
            return nbBlasons;
        }

        public int getPointsVille() {
            return pointsVille;
        }

        public Map<Joueur, Integer> getMeeplesParJoueur() {
            return meeplesParJoueur;
        }

        public Set<Joueur> getJoueursMajoritaires() {
            return joueursMajoritaires;
        }

        public List<Segment> getSegmentsVilleAvecMeeple() {
            return segmentsVilleAvecMeeple;
        }
    }

    private static final class Endpoint {
        private final Position position;
        private final Direction direction;
        private final int slot;

        private Endpoint(Position position, Direction direction, int slot) {
            this.position = position;
            this.direction = direction;
            this.slot = slot;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Endpoint)) {
                return false;
            }
            Endpoint endpoint = (Endpoint) o;
            return slot == endpoint.slot
                && Objects.equals(position, endpoint.position)
                && direction == endpoint.direction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(position, direction, slot);
        }
    }

    private static final class ComponentStats {
        private final boolean complete;
        private final int points;
        private final int blasons;
        private final Set<Position> positions;
        private final Set<Segment> villeSegments;
        private final Set<Segment> villeSegmentsAvecMeeple;
        private final Map<Joueur, Integer> meeplesParJoueur;
        private final Set<Joueur> majoritaires;

        private ComponentStats(
            boolean complete,
            int points,
            int blasons,
            Set<Position> positions,
            Set<Segment> villeSegments,
            Set<Segment> villeSegmentsAvecMeeple,
            Map<Joueur, Integer> meeplesParJoueur,
            Set<Joueur> majoritaires
        ) {
            this.complete = complete;
            this.points = points;
            this.blasons = blasons;
            this.positions = positions;
            this.villeSegments = villeSegments;
            this.villeSegmentsAvecMeeple = villeSegmentsAvecMeeple;
            this.meeplesParJoueur = meeplesParJoueur;
            this.majoritaires = majoritaires;
        }
    }

    public VilleScoreResult calculerPlacement(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        return calculer(plateau, tuile, pos, false);
    }

    public VilleScoreResult calculerFinPartie(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        return calculer(plateau, tuile, pos, true);
    }

    public VilleScoreResult attribuerScorePlacement(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        return attribuer(plateau, tuile, pos, false);
    }

    public VilleScoreResult attribuerScoreFinPartie(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        return attribuer(plateau, tuile, pos, true);
    }

    private VilleScoreResult attribuer(PlateauV2 plateau, TuileV2 tuile, Position pos, boolean finPartie) {
        if (plateau == null || tuile == null || pos == null || !hasAnyVille(tuile)) {
            return emptyResult();
        }

        List<ComponentStats> composantes = analyserComposantes(plateau, tuile, pos, finPartie);

        int totalPointsPotentiels = 0;
        int totalTuilesComptabilisees = 0;
        int totalBlasons = 0;
        boolean toutComplete = true;

        Map<Joueur, Integer> meeplesParJoueurGlobal = new HashMap<>();
        Set<Joueur> majoritairesGlobal = new LinkedHashSet<>();
        Set<Segment> segmentsAvecMeepleGlobal = new LinkedHashSet<>();

        for (ComponentStats composante : composantes) {
            totalPointsPotentiels += composante.points;
            totalTuilesComptabilisees += composante.positions.size();
            totalBlasons += composante.blasons;
            toutComplete = toutComplete && composante.complete;

            mergeCounts(meeplesParJoueurGlobal, composante.meeplesParJoueur);
            majoritairesGlobal.addAll(composante.majoritaires);
            segmentsAvecMeepleGlobal.addAll(composante.villeSegmentsAvecMeeple);

            if (composante.points > 0 && !composante.majoritaires.isEmpty()) {
                for (Joueur joueur : composante.majoritaires) {
                    joueur.ajouterScore(composante.points);
                }
            }

            if (!finPartie && composante.complete) {
                for (Segment segment : composante.villeSegmentsAvecMeeple) {
                    Meeple meeple = segment.getMeeple();
                    if (meeple == null || meeple.getOwner() == null) {
                        continue;
                    }

                    Joueur owner = meeple.getOwner();
                    owner.recupererMeeple(segment);
                    segment.removeMeeple();
                }
            }
        }

        return new VilleScoreResult(
            toutComplete,
            totalTuilesComptabilisees,
            totalBlasons,
            totalPointsPotentiels,
            meeplesParJoueurGlobal,
            majoritairesGlobal,
            new ArrayList<>(segmentsAvecMeepleGlobal)
        );
    }

    private VilleScoreResult calculer(PlateauV2 plateau, TuileV2 tuile, Position pos, boolean finPartie) {
        if (plateau == null || tuile == null || pos == null || !hasAnyVille(tuile)) {
            return emptyResult();
        }

        List<ComponentStats> composantes = analyserComposantes(plateau, tuile, pos, finPartie);

        int totalPointsPotentiels = 0;
        int totalTuilesComptabilisees = 0;
        int totalBlasons = 0;
        boolean toutComplete = true;

        Map<Joueur, Integer> meeplesParJoueurGlobal = new HashMap<>();
        Set<Joueur> majoritairesGlobal = new LinkedHashSet<>();
        Set<Segment> segmentsAvecMeepleGlobal = new LinkedHashSet<>();

        for (ComponentStats composante : composantes) {
            totalPointsPotentiels += composante.points;
            totalTuilesComptabilisees += composante.positions.size();
            totalBlasons += composante.blasons;
            toutComplete = toutComplete && composante.complete;

            mergeCounts(meeplesParJoueurGlobal, composante.meeplesParJoueur);
            majoritairesGlobal.addAll(composante.majoritaires);
            segmentsAvecMeepleGlobal.addAll(composante.villeSegmentsAvecMeeple);
        }

        return new VilleScoreResult(
            toutComplete,
            totalTuilesComptabilisees,
            totalBlasons,
            totalPointsPotentiels,
            meeplesParJoueurGlobal,
            majoritairesGlobal,
            new ArrayList<>(segmentsAvecMeepleGlobal)
        );
    }

    private List<ComponentStats> analyserComposantes(PlateauV2 plateau, TuileV2 tuileDepart, Position posDepart, boolean finPartie) {
        List<Endpoint> departs = endpointsVille(tuileDepart, posDepart);
        List<ComponentStats> composantes = new ArrayList<>();
        Set<Endpoint> dejaVisitesGlobal = new HashSet<>();

        for (Endpoint depart : departs) {
            if (dejaVisitesGlobal.contains(depart)) {
                continue;
            }

            ComponentStats composante = explorerComposante(plateau, depart, finPartie);
            composantes.add(composante);

            for (Endpoint endpoint : endpointsFromSegments(plateau, composante.positions, composante.villeSegments)) {
                dejaVisitesGlobal.add(endpoint);
            }
        }

        return composantes;
    }

    private ComponentStats explorerComposante(PlateauV2 plateau, Endpoint depart, boolean finPartie) {
        ArrayDeque<Endpoint> aVisiter = new ArrayDeque<>();
        Set<Endpoint> visitees = new HashSet<>();

        Set<Position> positions = new HashSet<>();
        Set<Segment> villeSegments = new HashSet<>();

        boolean complete = true;

        aVisiter.add(depart);

        while (!aVisiter.isEmpty()) {
            Endpoint courant = aVisiter.pollFirst();
            if (!visitees.add(courant)) {
                continue;
            }

            TuileV2 tuileCourante = plateau.getTuile(courant.position);
            Segment segmentCourant = getSegmentAt(tuileCourante, courant.direction, courant.slot);
            if (!isVilleSegment(segmentCourant)) {
                continue;
            }

            positions.add(courant.position);
            villeSegments.add(segmentCourant);

            Endpoint voisin = neighborEndpoint(plateau, courant);
            if (voisin == null) {
                complete = false;
            } else if (!visitees.contains(voisin)) {
                aVisiter.add(voisin);
            }

            for (Endpoint interne : internalLinkedEndpoints(tuileCourante, courant.position, segmentCourant)) {
                if (!visitees.contains(interne)) {
                    aVisiter.add(interne);
                }
            }
        }

        Set<Segment> villeSegmentsAvecMeeple = new LinkedHashSet<>();
        Map<Joueur, Integer> meeplesParJoueur = new HashMap<>();

        for (Segment segment : villeSegments) {
            Meeple meeple = segment.getMeeple();
            if (meeple == null || meeple.getOwner() == null) {
                continue;
            }
            villeSegmentsAvecMeeple.add(segment);
            Joueur owner = meeple.getOwner();
            meeplesParJoueur.put(owner, meeplesParJoueur.getOrDefault(owner, 0) + 1);
        }

        int nbBlasons = compterBlasons(plateau, positions);
        Set<Joueur> majoritaires = joueursMajoritaires(meeplesParJoueur);

        int multiplicateur = (complete && !finPartie) ? 2 : (finPartie ? 1 : 0);
        int points = multiplicateur == 0 ? 0 : (positions.size() + nbBlasons) * multiplicateur;

        return new ComponentStats(
            complete,
            points,
            nbBlasons,
            positions,
            villeSegments,
            villeSegmentsAvecMeeple,
            meeplesParJoueur,
            majoritaires
        );
    }

    private int compterBlasons(PlateauV2 plateau, Set<Position> positions) {
        int blasons = 0;

        for (Position position : positions) {
            TuileV2 tuile = plateau.getTuile(position);
            if (tuile != null && tuile.hasBlason()) {
                blasons++;
            }
        }

        return blasons;
    }

    private Set<Endpoint> endpointsFromSegments(PlateauV2 plateau, Set<Position> positions, Set<Segment> segments) {
        Set<Endpoint> endpoints = new HashSet<>();

        for (Position position : positions) {
            TuileV2 tuile = plateau.getTuile(position);
            if (tuile == null) {
                continue;
            }

            for (Direction direction : Direction.values()) {
                List<Segment> bord = tuile.getBord(direction);
                if (bord == null) {
                    continue;
                }

                for (int i = 0; i < bord.size(); i++) {
                    Segment segment = bord.get(i);
                    if (isVilleSegment(segment) && segments.contains(segment)) {
                        endpoints.add(new Endpoint(position, direction, i));
                    }
                }
            }
        }

        return endpoints;
    }

    private List<Endpoint> endpointsVille(TuileV2 tuile, Position position) {
        List<Endpoint> endpoints = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            List<Segment> bord = tuile.getBord(direction);
            if (bord == null) {
                continue;
            }

            for (int i = 0; i < bord.size(); i++) {
                if (isVilleSegment(bord.get(i))) {
                    endpoints.add(new Endpoint(position, direction, i));
                }
            }
        }

        return endpoints;
    }

    private Endpoint neighborEndpoint(PlateauV2 plateau, Endpoint endpoint) {
        Position posVoisine = voisin(endpoint.position, endpoint.direction);
        TuileV2 tuileVoisine = plateau.getTuile(posVoisine);
        if (tuileVoisine == null) {
            return null;
        }

        Direction directionOpposee = opposee(endpoint.direction);
        Segment segmentVoisin = getSegmentAt(tuileVoisine, directionOpposee, endpoint.slot);
        if (!isVilleSegment(segmentVoisin)) {
            return null;
        }

        return new Endpoint(posVoisine, directionOpposee, endpoint.slot);
    }

    private List<Endpoint> internalLinkedEndpoints(TuileV2 tuile, Position position, Segment segment) {
        List<Endpoint> linked = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            List<Segment> bord = tuile.getBord(direction);
            if (bord == null) {
                continue;
            }

            for (int i = 0; i < bord.size(); i++) {
                if (bord.get(i) == segment) {
                    linked.add(new Endpoint(position, direction, i));
                }
            }
        }

        return linked;
    }

    private Segment getSegmentAt(TuileV2 tuile, Direction direction, int slot) {
        if (tuile == null) {
            return null;
        }

        List<Segment> bord = tuile.getBord(direction);
        if (bord == null || slot < 0 || slot >= bord.size()) {
            return null;
        }

        return bord.get(slot);
    }

    private void mergeCounts(Map<Joueur, Integer> target, Map<Joueur, Integer> source) {
        for (Map.Entry<Joueur, Integer> entry : source.entrySet()) {
            Joueur joueur = entry.getKey();
            int valeur = entry.getValue();
            target.put(joueur, target.getOrDefault(joueur, 0) + valeur);
        }
    }

    private Set<Joueur> joueursMajoritaires(Map<Joueur, Integer> meeplesParJoueur) {
        if (meeplesParJoueur.isEmpty()) {
            return Set.of();
        }

        int max = 0;
        for (int nb : meeplesParJoueur.values()) {
            if (nb > max) {
                max = nb;
            }
        }

        Set<Joueur> majoritaires = new LinkedHashSet<>();
        for (Map.Entry<Joueur, Integer> entry : meeplesParJoueur.entrySet()) {
            if (entry.getValue() == max && max > 0) {
                majoritaires.add(entry.getKey());
            }
        }

        return majoritaires;
    }

    private VilleScoreResult emptyResult() {
        return new VilleScoreResult(false, 0, 0, 0, Map.of(), Set.of(), List.of());
    }

    private boolean hasAnyVille(TuileV2 tuile) {
        return hasVilleOnSide(tuile, Direction.NORD)
            || hasVilleOnSide(tuile, Direction.EST)
            || hasVilleOnSide(tuile, Direction.SUD)
            || hasVilleOnSide(tuile, Direction.OUEST);
    }

    private boolean hasVilleOnSide(TuileV2 tuile, Direction direction) {
        List<Segment> segments = tuile.getBord(direction);
        if (segments == null) {
            return false;
        }

        for (Segment segment : segments) {
            if (isVilleSegment(segment)) {
                return true;
            }
        }

        return false;
    }

    private boolean isVilleSegment(Segment segment) {
        return segment instanceof SegmentVille;
    }

    private Position voisin(Position courante, Direction direction) {
        switch (direction) {
            case NORD:
                return new Position(courante.getX(), courante.getY() + 1);
            case EST:
                return new Position(courante.getX() + 1, courante.getY());
            case SUD:
                return new Position(courante.getX(), courante.getY() - 1);
            case OUEST:
                return new Position(courante.getX() - 1, courante.getY());
            default:
                throw new IllegalArgumentException("Direction inconnue: " + direction);
        }
    }

    private Direction opposee(Direction direction) {
        switch (direction) {
            case NORD:
                return Direction.SUD;
            case EST:
                return Direction.OUEST;
            case SUD:
                return Direction.NORD;
            case OUEST:
                return Direction.EST;
            default:
                throw new IllegalArgumentException("Direction inconnue: " + direction);
        }
    }
}
