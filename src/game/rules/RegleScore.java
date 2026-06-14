
package game.rules;

import game.plateau.PlateauV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;

public abstract class RegleScore extends RegleGenerale {

    /**
     * Valide de base l'application de la règle.
     * @param plateau Le plateau de jeu.
     * @param tuile La tuile.
     * @param pos La position.
     * @return true si la regle est validée, faux sinon
     */
    @Override
    public boolean validate(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        return true;
    }

    /**
     * Score gagné immédiatement après avoir posé une tuile
     * @param plateau Le plateau de jeu
     * @param tuile La tuile posée
     * @param pos La position
     * @return Le nombre de points générés
     */
    public abstract int score(PlateauV2 plateau, TuileV2 tuile, Position pos);

    /**
     * Score gagné en fin de partie.
     * Par défaut: 0, chaque règle peut redéfinir si nécessaire.
     * @param plateau Le plateau de jeu.
     * @param tuile La tuile posée.
     * @param pos La position.
     * @return Le nombre de points de fin de partie.
     */
    public int scoreFinPartie(PlateauV2 plateau, TuileV2 tuile, Position pos) {
        return 0;
    }
}
