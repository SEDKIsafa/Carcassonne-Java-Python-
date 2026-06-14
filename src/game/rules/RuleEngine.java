package game.rules;

import java.util.ArrayList;
import java.util.List;

import game.plateau.PlateauV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;

public class RuleEngine {
    private final List<RegleScore> reglesScore;

    public RuleEngine() {
        this.reglesScore = new ArrayList<>();

        final ScoreAbbayeV2 scoreAbbayeV2 = new ScoreAbbayeV2();
        this.reglesScore.add(new RegleScore() {
            @Override
            public int score(PlateauV2 plateau, TuileV2 tuile, Position pos) {
                return scoreAbbayeV2.calculerPlacement(plateau, tuile, pos).getPointsAbbaye();
            }

            @Override
            public int scoreFinPartie(PlateauV2 plateau, TuileV2 tuile, Position pos) {
                return scoreAbbayeV2.calculerFinPartie(plateau, tuile, pos).getPointsAbbaye();
            }
        });

        final ScoreRouteV2 scoreRouteV2 = new ScoreRouteV2();
        this.reglesScore.add(new RegleScore() {
            @Override
            public int score(PlateauV2 plateau, TuileV2 tuile, Position pos) {
                return scoreRouteV2.calculerPlacement(plateau, tuile, pos).getPointsRoute();
            }

            @Override
            public int scoreFinPartie(PlateauV2 plateau, TuileV2 tuile, Position pos) {
                return scoreRouteV2.calculerFinPartie(plateau, tuile, pos).getPointsRoute();
            }
        });

        final ScoreVilleV2 scoreVilleV2 = new ScoreVilleV2();
        this.reglesScore.add(new RegleScore() {
            @Override
            public int score(PlateauV2 plateau, TuileV2 tuile, Position pos) {
                return scoreVilleV2.calculerPlacement(plateau, tuile, pos).getPointsVille();
            }

            @Override
            public int scoreFinPartie(PlateauV2 plateau, TuileV2 tuile, Position pos) {
                return scoreVilleV2.calculerFinPartie(plateau, tuile, pos).getPointsVille();
            }
        });

        final ScoreChampV2 scoreChampV2 = new ScoreChampV2();
        this.reglesScore.add(new RegleScore() {
            @Override
            public int score(PlateauV2 plateau, TuileV2 tuile, Position pos) {
                return scoreChampV2.calculerPlacement(plateau, tuile, pos).getPointsChamp();
            }

            @Override
            public int scoreFinPartie(PlateauV2 plateau, TuileV2 tuile, Position pos) {
                return scoreChampV2.calculerFinPartie(plateau, tuile, pos).getPointsChamp();
            }
        });
    }


    public void ajouterRegleScore(RegleScore regle) {
        this.reglesScore.add(regle);
    }

    public int calculerScorePlacement(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        int total = 0;
        for (RegleScore regle : reglesScore) {
            if (regle.validate(plateau, tuile, pos)) {
                total += regle.score(plateau, tuile, pos);
            }
        }
        return total;
    }

    public int calculerScoreFinPartie(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        int total = 0;
        for (RegleScore regle : reglesScore) {
            if (regle.validate(plateau, tuile, pos)) {
                total += regle.scoreFinPartie(plateau, tuile, pos);
            }
        }
        return total;
    }
}
