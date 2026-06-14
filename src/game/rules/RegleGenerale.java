package game.rules;
import game.plateau.PlateauV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;

public abstract class RegleGenerale {
    public abstract boolean validate(PlateauV2 plateau, TuileV2 tuile, Position pos);
}
