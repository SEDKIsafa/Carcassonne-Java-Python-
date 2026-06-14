package game.plateau.tuiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**classe pour le sachet de tuiles */
public class SacTuiles {
    private List<TuileV2> tuiles;

    /**
     * Constructeur pour le sachet de tuiles
     */
    public SacTuiles(){
        this.tuiles = new ArrayList<>();
    }

    /**
     * Getter des tuiles
     * @return toute les tuiles du sac
     */
    public List<TuileV2> getTuiles(){
        return this.tuiles;
    }

    /**
     * @return true si le sachet est vide, false sinon.
     */
    public boolean estVide() {
        return this.tuiles.isEmpty();
    }

    /**
     * @return Le nombre de tuiles restantes dans le sachet.
     */
    public int getNombreTuilesRestante() {
        return this.tuiles.size();
    }

    /**
     * Va ajouter une tuile dans le sachet
     * @param tuile la tuile a ajouter
     */
    public void addTuile(TuileV2 tuile) {
        this.tuiles.add(tuile);
    }

    /**
     * Mélange les tuiles dans le sachet
     */
    public void melanger() {
        Collections.shuffle(this.tuiles);
    }

    /**
     * Pioche la première tuile du sachet et la retire du sac.
     * @return La tuile piochée, null si le sachet est vide.
     */
    public TuileV2 piocher() {
        if (estVide()) {
            return null;
        }
        // piocher la tuile en tete de pile
        return this.tuiles.remove(0);
    }
}
