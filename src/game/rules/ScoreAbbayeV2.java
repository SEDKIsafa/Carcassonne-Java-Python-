package game.rules;

import game.joueur.Joueur;
import game.joueur.Meeple;
import game.plateau.PlateauV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.Segment;

/**
 * Regle de score abbaye pour le modele V2 (PlateauV2 / TuileV2).
 *
 * Regles implementees:
 * - En cours de partie: une abbaye complete (8 voisins) vaut 9 points
 * - Fin de partie: une abbaye incomplete vaut 1 + nombre de voisins
 * - Les points vont au proprietaire du meeple pose sur l'abbaye
 * - En cours de partie, si complete: recuperation du meeple
 */
public class ScoreAbbayeV2 {

    public static final class AbbayeScoreResult {
        private final boolean hasAbbaye;
        private final boolean abbayeComplete;
        private final int nbVoisins;
        private final int pointsAbbaye;
        private final Joueur proprietaire;

        private AbbayeScoreResult(
            boolean hasAbbaye,
            boolean abbayeComplete,
            int nbVoisins,
            int pointsAbbaye,
            Joueur proprietaire
        ) {
            this.hasAbbaye = hasAbbaye;
            this.abbayeComplete = abbayeComplete;
            this.nbVoisins = nbVoisins;
            this.pointsAbbaye = pointsAbbaye;
            this.proprietaire = proprietaire;
        }

        public boolean hasAbbaye() {
            return hasAbbaye;
        }

        public boolean isAbbayeComplete() {
            return abbayeComplete;
        }

        public int getNbVoisins() {
            return nbVoisins;
        }

        public int getPointsAbbaye() {
            return pointsAbbaye;
        }

        public Joueur getProprietaire() {
            return proprietaire;
        }
    }

    public AbbayeScoreResult calculerPlacement(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        return calculer(plateau, tuile, pos, false);
    }

    public AbbayeScoreResult calculerFinPartie(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        return calculer(plateau, tuile, pos, true);
    }

    public AbbayeScoreResult attribuerScorePlacement(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        AbbayeScoreResult resultat = calculerPlacement(plateau, tuile, pos);
        attribuer(resultat);

        if (resultat.isAbbayeComplete()) {
            recupererMeeple(tuile);
        }

        return resultat;
    }

    public AbbayeScoreResult attribuerScoreFinPartie(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        AbbayeScoreResult resultat = calculerFinPartie(plateau, tuile, pos);
        attribuer(resultat);
        return resultat;
    }

    private AbbayeScoreResult calculer(PlateauV2 plateau, TuileV2 tuile, Position pos, boolean finPartie) {
        if (plateau == null || tuile == null || pos == null || !tuile.hasAbbaye()) {
            return new AbbayeScoreResult(false, false, 0, 0, null);
        }

        int nbVoisins = compterVoisins(plateau, pos);
        boolean complete = nbVoisins == 8;

        int points;
        if (complete) {
            points = 9;
        } else if (finPartie) {
            points = 1 + nbVoisins;
        } else {
            points = 0;
        }

        Joueur proprietaire = proprietaireAbbaye(tuile);

        return new AbbayeScoreResult(true, complete, nbVoisins, points, proprietaire);
    }

    private int compterVoisins(PlateauV2 plateau, Position pos) {
        int x = pos.getX();
        int y = pos.getY();
        int voisins = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }

                Position voisine = new Position(x + dx, y + dy);
                if (plateau.getTuile(voisine) != null) {
                    voisins++;
                }
            }
        }

        return voisins;
    }

    private Joueur proprietaireAbbaye(TuileV2 tuile) {
        Segment abbaye = tuile.getAbbaye();
        if (abbaye == null) {
            return null;
        }

        Meeple meeple = abbaye.getMeeple();
        if (meeple == null) {
            return null;
        }

        return meeple.getOwner();
    }

    private void attribuer(AbbayeScoreResult resultat) {
        if (resultat.getPointsAbbaye() <= 0 || resultat.getProprietaire() == null) {
            return;
        }

        resultat.getProprietaire().ajouterScore(resultat.getPointsAbbaye());
    }

    private void recupererMeeple(TuileV2 tuile) {
        Segment abbaye = tuile.getAbbaye();
        if (abbaye == null) {
            return;
        }

        Meeple meeple = abbaye.getMeeple();
        if (meeple == null || meeple.getOwner() == null) {
            return;
        }

        Joueur owner = meeple.getOwner();
        owner.recupererMeeple(abbaye);
        abbaye.removeMeeple();
    }
}
