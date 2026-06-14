package game.rules;

import game.joueur.Joueur;
import game.plateau.PlateauV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Direction;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.Segment;
import game.plateau.tuiles.segment.SegmentType.SegmentChamp;

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
 * Regle de score des champs pour le modele V2 (PlateauV2 / TuileV2).
 *
 * Regles implementees:
 * - Aucun point en cours de partie
 * - Fin de partie: 3 points par ville complete adjacente au champ
 * - Majorite/egalite geree par composante de champ
 * - Une meme ville complete n'est comptabilisee qu'une seule fois par champ
 *
 * Remarque de modele:
 * avec la representation actuelle des tuiles, l'adjacence champ/ville est
 * approchée au niveau de la tuile: un champ est considere adjacent aux
 * composantes de ville presentes sur les tuiles du champ.
 */
public class ScoreChampV2 {

    public static final class ChampScoreResult {
        private final int nbVillesCompletesAdjacentes;
        private final int pointsChamp;
        private final Map<Joueur, Integer> meeplesParJoueur;
        private final Set<Joueur> joueursMajoritaires;
        private final List<Segment> segmentsChampAvecMeeple;

        private ChampScoreResult(
            int nbVillesCompletesAdjacentes,
            int pointsChamp,
            Map<Joueur, Integer> meeplesParJoueur,
            Set<Joueur> joueursMajoritaires,
            List<Segment> segmentsChampAvecMeeple
        ) {
            this.nbVillesCompletesAdjacentes = nbVillesCompletesAdjacentes;
            this.pointsChamp = pointsChamp;
            this.meeplesParJoueur = Collections.unmodifiableMap(new HashMap<>(meeplesParJoueur));
            this.joueursMajoritaires = Collections.unmodifiableSet(new LinkedHashSet<>(joueursMajoritaires));
            this.segmentsChampAvecMeeple = Collections.unmodifiableList(new ArrayList<>(segmentsChampAvecMeeple));
        }

        public int getNbVillesCompletesAdjacentes() {
            return nbVillesCompletesAdjacentes;
        }

        public int getPointsChamp() {
            return pointsChamp;
        }

        public Map<Joueur, Integer> getMeeplesParJoueur() {
            return meeplesParJoueur;
        }

        public Set<Joueur> getJoueursMajoritaires() {
            return joueursMajoritaires;
        }

        public List<Segment> getSegmentsChampAvecMeeple() {
            return segmentsChampAvecMeeple;
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

    private static final class ChampComponentStats {
        private final Set<Position> positions;
        private final Set<Segment> champSegments;
        private final Set<Segment> champSegmentsAvecMeeple;
        private final Map<Joueur, Integer> meeplesParJoueur;
        private final Set<Joueur> majoritaires;
        private final int villesCompletesAdjacentes;
        private final int points;

        private ChampComponentStats(
            Set<Position> positions,
            Set<Segment> champSegments,
            Set<Segment> champSegmentsAvecMeeple,
            Map<Joueur, Integer> meeplesParJoueur,
            Set<Joueur> majoritaires,
            int villesCompletesAdjacentes,
            int points
        ) {
            this.positions = positions;
            this.champSegments = champSegments;
            this.champSegmentsAvecMeeple = champSegmentsAvecMeeple;
            this.meeplesParJoueur = meeplesParJoueur;
            this.majoritaires = majoritaires;
            this.villesCompletesAdjacentes = villesCompletesAdjacentes;
            this.points = points;
        }
    }

    private final ScoreVilleV2 scoreVilleV2;

    public ScoreChampV2() {
        this.scoreVilleV2 = new ScoreVilleV2();
    }

    public ChampScoreResult calculerPlacement(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        return emptyResult();
    }

    public ChampScoreResult calculerFinPartie(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        if (plateau == null || tuile == null || pos == null || !hasAnyChamp(tuile)) {
            return emptyResult();
        }

        List<ChampComponentStats> composantes = analyserComposantes(plateau, tuile, pos);

        int totalVillesCompletesAdjacentes = 0;
        int totalPoints = 0;

        Map<Joueur, Integer> meeplesParJoueurGlobal = new HashMap<>();
        Set<Joueur> majoritairesGlobal = new LinkedHashSet<>();
        Set<Segment> segmentsAvecMeepleGlobal = new LinkedHashSet<>();

        for (ChampComponentStats composante : composantes) {
            totalVillesCompletesAdjacentes += composante.villesCompletesAdjacentes;
            totalPoints += composante.points;
            mergeCounts(meeplesParJoueurGlobal, composante.meeplesParJoueur);
            majoritairesGlobal.addAll(composante.majoritaires);
            segmentsAvecMeepleGlobal.addAll(composante.champSegmentsAvecMeeple);
        }

        return new ChampScoreResult(
            totalVillesCompletesAdjacentes,
            totalPoints,
            meeplesParJoueurGlobal,
            majoritairesGlobal,
            new ArrayList<>(segmentsAvecMeepleGlobal)
        );
    }

    public ChampScoreResult attribuerScorePlacement(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        return emptyResult();
    }

    public ChampScoreResult attribuerScoreFinPartie(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        if (plateau == null || tuile == null || pos == null || !hasAnyChamp(tuile)) {
            return emptyResult();
        }

        List<ChampComponentStats> composantes = analyserComposantes(plateau, tuile, pos);

        int totalVillesCompletesAdjacentes = 0;
        int totalPoints = 0;

        Map<Joueur, Integer> meeplesParJoueurGlobal = new HashMap<>();
        Set<Joueur> majoritairesGlobal = new LinkedHashSet<>();
        Set<Segment> segmentsAvecMeepleGlobal = new LinkedHashSet<>();

        for (ChampComponentStats composante : composantes) {
            totalVillesCompletesAdjacentes += composante.villesCompletesAdjacentes;
            totalPoints += composante.points;
            mergeCounts(meeplesParJoueurGlobal, composante.meeplesParJoueur);
            majoritairesGlobal.addAll(composante.majoritaires);
            segmentsAvecMeepleGlobal.addAll(composante.champSegmentsAvecMeeple);

            if (composante.points > 0 && !composante.majoritaires.isEmpty()) {
                for (Joueur joueur : composante.majoritaires) {
                    joueur.ajouterScore(composante.points);
                }
            }
        }

        return new ChampScoreResult(
            totalVillesCompletesAdjacentes,
            totalPoints,
            meeplesParJoueurGlobal,
            majoritairesGlobal,
            new ArrayList<>(segmentsAvecMeepleGlobal)
        );
    }

    private List<ChampComponentStats> analyserComposantes(PlateauV2 plateau, TuileV2 tuileDepart, Position posDepart) {
        List<Endpoint> departs = endpointsChamp(tuileDepart, posDepart);
        List<ChampComponentStats> composantes = new ArrayList<>();
        Set<Endpoint> dejaVisitesGlobal = new HashSet<>();

        for (Endpoint depart : departs) {
            if (dejaVisitesGlobal.contains(depart)) {
                continue;
            }

            ChampComponentStats composante = explorerComposante(plateau, depart);
            composantes.add(composante);

            for (Endpoint endpoint : endpointsFromSegments(plateau, composante.positions, composante.champSegments)) {
                dejaVisitesGlobal.add(endpoint);
            }
        }

        return composantes;
    }

    private ChampComponentStats explorerComposante(PlateauV2 plateau, Endpoint depart) {
        ArrayDeque<Endpoint> aVisiter = new ArrayDeque<>();
        Set<Endpoint> visitees = new HashSet<>();

        Set<Position> positions = new HashSet<>();
        Set<Segment> champSegments = new HashSet<>();

        aVisiter.add(depart);

        while (!aVisiter.isEmpty()) {
            Endpoint courant = aVisiter.pollFirst();
            if (!visitees.add(courant)) {
                continue;
            }

            TuileV2 tuileCourante = plateau.getTuile(courant.position);
            Segment segmentCourant = getSegmentAt(tuileCourante, courant.direction, courant.slot);
            if (!isChampSegment(segmentCourant)) {
                continue;
            }

            positions.add(courant.position);
            champSegments.add(segmentCourant);

            Endpoint voisin = neighborEndpoint(plateau, courant);
            if (voisin != null && !visitees.contains(voisin)) {
                aVisiter.add(voisin);
            }

            for (Endpoint interne : internalLinkedEndpoints(tuileCourante, courant.position, segmentCourant)) {
                if (!visitees.contains(interne)) {
                    aVisiter.add(interne);
                }
            }
        }

        Set<Segment> champSegmentsAvecMeeple = new LinkedHashSet<>();
        Map<Joueur, Integer> meeplesParJoueur = new HashMap<>();

        for (Segment segment : champSegments) {
            if (segment.getMeeple() == null || segment.getMeeple().getOwner() == null) {
                continue;
            }
            champSegmentsAvecMeeple.add(segment);
            Joueur owner = segment.getMeeple().getOwner();
            meeplesParJoueur.put(owner, meeplesParJoueur.getOrDefault(owner, 0) + 1);
        }

        int villesCompletesAdjacentes = compterVillesCompletesAdjacentes(plateau, positions);
        int points = villesCompletesAdjacentes * 3;
        Set<Joueur> majoritaires = joueursMajoritaires(meeplesParJoueur);

        return new ChampComponentStats(
            positions,
            champSegments,
            champSegmentsAvecMeeple,
            meeplesParJoueur,
            majoritaires,
            villesCompletesAdjacentes,
            points
        );
    }

    private int compterVillesCompletesAdjacentes(PlateauV2 plateau, Set<Position> positionsChamp) {
        Set<Position> positionsVilleDejaComptees = new HashSet<>();
        int nbVillesCompletes = 0;

        for (Position positionChamp : positionsChamp) {
            for (Position positionCandidate : positionsVilleCandidates(positionChamp)) {
                TuileV2 tuile = plateau.getTuile(positionCandidate);
                if (tuile == null) {
                    continue;
                }

                Set<Segment> villesSurTuile = new LinkedHashSet<>();
                for (Direction direction : Direction.values()) {
                    List<Segment> bord = tuile.getBord(direction);
                    if (bord == null) {
                        continue;
                    }
                    for (Segment segment : bord) {
                        if (segment != null && isVilleSegment(segment)) {
                            villesSurTuile.add(segment);
                        }
                    }
                }

                for (Segment villeSegment : villesSurTuile) {
                    Position positionVille = trouverPositionDepartVille(tuile, positionCandidate, villeSegment);
                    if (positionVille == null || positionsVilleDejaComptees.contains(positionVille)) {
                        continue;
                    }

                    ScoreVilleV2.VilleScoreResult resultatVille = scoreVilleV2.calculerFinPartie(plateau, tuile, positionVille);
                    if (resultatVille.isVilleComplete()) {
                        nbVillesCompletes++;
                        positionsVilleDejaComptees.addAll(extrairePositionsVille(plateau, tuile, positionVille));
                    }
                }
            }
        }

        return nbVillesCompletes;
    }

    private List<Position> positionsVilleCandidates(Position centre) {
        List<Position> positions = new ArrayList<>();
        positions.add(centre);
        positions.add(new Position(centre.getX(), centre.getY() + 1));
        positions.add(new Position(centre.getX() + 1, centre.getY()));
        positions.add(new Position(centre.getX(), centre.getY() - 1));
        positions.add(new Position(centre.getX() - 1, centre.getY()));
        return positions;
    }

    private Set<Position> extrairePositionsVille(PlateauV2 plateau, TuileV2 tuileDepart, Position posDepart) {
        Set<Position> positions = new HashSet<>();
        ArrayDeque<ScoreVilleV2EndpointProxy> aVisiter = new ArrayDeque<>();
        Set<ScoreVilleV2EndpointProxy> visitees = new HashSet<>();

        for (Direction direction : Direction.values()) {
            List<Segment> bord = tuileDepart.getBord(direction);
            if (bord == null) {
                continue;
            }
            for (int i = 0; i < bord.size(); i++) {
                if (isVilleSegment(bord.get(i))) {
                    aVisiter.add(new ScoreVilleV2EndpointProxy(posDepart, direction, i));
                }
            }
        }

        while (!aVisiter.isEmpty()) {
            ScoreVilleV2EndpointProxy courant = aVisiter.pollFirst();
            if (!visitees.add(courant)) {
                continue;
            }

            TuileV2 tuileCourante = plateau.getTuile(courant.position);
            Segment segmentCourant = getSegmentAt(tuileCourante, courant.direction, courant.slot);
            if (!isVilleSegment(segmentCourant)) {
                continue;
            }

            positions.add(courant.position);

            ScoreVilleV2EndpointProxy voisin = neighborVilleEndpoint(plateau, courant);
            if (voisin != null && !visitees.contains(voisin)) {
                aVisiter.add(voisin);
            }

            for (ScoreVilleV2EndpointProxy interne : internalLinkedVilleEndpoints(tuileCourante, courant.position, segmentCourant)) {
                if (!visitees.contains(interne)) {
                    aVisiter.add(interne);
                }
            }
        }

        return positions;
    }

    private Position trouverPositionDepartVille(TuileV2 tuile, Position position, Segment villeSegment) {
        for (Direction direction : Direction.values()) {
            List<Segment> bord = tuile.getBord(direction);
            if (bord == null) {
                continue;
            }
            for (int i = 0; i < bord.size(); i++) {
                if (bord.get(i) == villeSegment) {
                    return position;
                }
            }
        }
        return null;
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
                    if (isChampSegment(segment) && segments.contains(segment)) {
                        endpoints.add(new Endpoint(position, direction, i));
                    }
                }
            }
        }

        return endpoints;
    }

    private List<Endpoint> endpointsChamp(TuileV2 tuile, Position position) {
        List<Endpoint> endpoints = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            List<Segment> bord = tuile.getBord(direction);
            if (bord == null) {
                continue;
            }

            for (int i = 0; i < bord.size(); i++) {
                if (isChampSegment(bord.get(i))) {
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
        if (!isChampSegment(segmentVoisin)) {
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

    private ChampScoreResult emptyResult() {
        return new ChampScoreResult(0, 0, Map.of(), Set.of(), List.of());
    }

    private boolean hasAnyChamp(TuileV2 tuile) {
        return hasChampOnSide(tuile, Direction.NORD)
            || hasChampOnSide(tuile, Direction.EST)
            || hasChampOnSide(tuile, Direction.SUD)
            || hasChampOnSide(tuile, Direction.OUEST);
    }

    private boolean hasChampOnSide(TuileV2 tuile, Direction direction) {
        List<Segment> segments = tuile.getBord(direction);
        if (segments == null) {
            return false;
        }

        for (Segment segment : segments) {
            if (isChampSegment(segment)) {
                return true;
            }
        }

        return false;
    }

    private boolean isChampSegment(Segment segment) {
        return segment instanceof SegmentChamp;
    }

    private boolean isVilleSegment(Segment segment) {
        return segment != null && !(segment instanceof SegmentChamp) && segment.getRepresentation() != null
            && segment.getRepresentation().indexOf('c') >= 0;
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

    private static final class ScoreVilleV2EndpointProxy {
        private final Position position;
        private final Direction direction;
        private final int slot;

        private ScoreVilleV2EndpointProxy(Position position, Direction direction, int slot) {
            this.position = position;
            this.direction = direction;
            this.slot = slot;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof ScoreVilleV2EndpointProxy)) {
                return false;
            }
            ScoreVilleV2EndpointProxy that = (ScoreVilleV2EndpointProxy) o;
            return slot == that.slot
                && Objects.equals(position, that.position)
                && direction == that.direction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(position, direction, slot);
        }
    }

    private ScoreVilleV2EndpointProxy neighborVilleEndpoint(PlateauV2 plateau, ScoreVilleV2EndpointProxy endpoint) {
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

        return new ScoreVilleV2EndpointProxy(posVoisine, directionOpposee, endpoint.slot);
    }

    private List<ScoreVilleV2EndpointProxy> internalLinkedVilleEndpoints(TuileV2 tuile, Position position, Segment segment) {
        List<ScoreVilleV2EndpointProxy> linked = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            List<Segment> bord = tuile.getBord(direction);
            if (bord == null) {
                continue;
            }

            for (int i = 0; i < bord.size(); i++) {
                if (bord.get(i) == segment) {
                    linked.add(new ScoreVilleV2EndpointProxy(position, direction, i));
                }
            }
        }

        return linked;
    }
}
