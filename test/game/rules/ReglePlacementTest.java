package game.rules;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import game.plateau.PlateauV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.direction.Direction;
import game.plateau.tuiles.segment.SegmentType.SegmentRoute;
import game.plateau.tuiles.segment.SegmentType.SegmentChamp;
import game.plateau.tuiles.segment.SegmentType.SegmentVille;
import game.plateau.tuiles.segment.Segment;

public class ReglePlacementTest {

    @Test
    public void testValidateAvecParametresNull() {
        ReglePlacement regle = new ReglePlacement();
        PlateauV2 plateau = new PlateauV2();

        assertFalse(regle.validate(null, new TuileV2(), new Position(0, 0)));
        assertFalse(regle.validate(plateau, null, new Position(0, 0)));
        assertFalse(regle.validate(plateau, new TuileV2(), null));
    }

    @Test
    public void testValidateCaseDejaOccupee() {
        ReglePlacement regle = new ReglePlacement();
        PlateauV2 plateau = new PlateauV2();

      
        assertFalse(regle.validate(plateau, new TuileV2(), new Position(36, 36)));
    }

    @Test
    public void testValidateSansVoisin() {
        ReglePlacement regle = new ReglePlacement();
        PlateauV2 plateau = new PlateauV2();
        TuileV2 tuile = new TuileV2();
        Position pos = new Position(20, 20); 

        assertFalse(regle.validate(plateau, tuile, pos));
    }

    @Test
    public void testValidateAvecVoisinCompatible() {
        ReglePlacement regle = new ReglePlacement();
        PlateauV2 plateau = new PlateauV2();

        Position pos = new Position(36, 37); 
        TuileV2 tuile = new TuileV2();

        TuileV2 tuileInitiale = plateau.getTuile(new Position(36, 36));
        for (Segment s : tuileInitiale.getNordSegments()) {
            tuile.addSegments(Direction.SUD, cloneByType(s));
        }

        assertTrue(regle.validate(plateau, tuile, pos));
    }

    @Test
    public void testValidateAvecVoisinIncompatible() {
        ReglePlacement regle = new ReglePlacement();
        PlateauV2 plateau = new PlateauV2();

        Position pos = new Position(36, 37);
        TuileV2 tuile = new TuileV2();

        
        tuile.addSegments(Direction.SUD, new SegmentRoute("r"));

        assertFalse(regle.validate(plateau, tuile, pos));
    }

    private Segment cloneByType(Segment original) {
        if (original instanceof SegmentRoute) return new SegmentRoute("r");
        if (original instanceof SegmentChamp) return new SegmentChamp("f");
        if (original instanceof SegmentVille) return new SegmentVille("c");
        
        return new SegmentRoute("r");
    }
}