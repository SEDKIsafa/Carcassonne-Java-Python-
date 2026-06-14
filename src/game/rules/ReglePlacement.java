package game.rules;

import game.plateau.PlateauV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.Segment;
import java.util.List;

/**src
 * Règle vérifiant si la tuile peut physiquement être posée sur le plateau.
 * Vérifie que la tuile est posée à côté d'au moins une tuile déjà posée et que les segments de la tuile sont compatibles avec ceux des tuiles voisines.
 * Exemple : une route doit être posée à côté d'une route, un champ à côté d'un champ, etc.
 */
public class ReglePlacement extends RegleGenerale {

   @Override
public boolean validate(PlateauV2 plateau,TuileV2 tuile,Position pos){

    if (plateau==null || tuile==null || pos==null){
        return false ;// on ne peut pas poser une tuile sur un plateau inexistant ou à une position nulle
    }
    if(!plateau.peutPoser(pos)){
        return false ;// on ne peut pas poser une tuile sur une case déjà occupée
    }
    return verifierVoisins(plateau, tuile, pos);
}

private boolean verifierVoisins(PlateauV2 plateau, TuileV2 tuile, Position pos){
    int nbVoisins=0;
    int x=pos.getX();
    int y=pos.getY();

    TuileV2 voisinNord= plateau.getTuile(new Position(x,y+1));
    if(voisinNord!=null){
        nbVoisins++;
        if (!sontCompatibles(tuile.getNordSegments(),voisinNord.getSudSegments())){
            return false;
        }}
    TuileV2 voisinSud= plateau.getTuile(new Position(x,y-1));
    if(voisinSud!=null){
        nbVoisins++;
        if (!sontCompatibles(tuile.getSudSegments(),voisinSud.getNordSegments())){
            return false;
        }  }  
    TuileV2 voisinEst= plateau.getTuile(new Position(x+1,y));
    if(voisinEst!=null){
        nbVoisins++;
        if (!sontCompatibles(tuile.getEstSegments(),voisinEst.getOuestSegments())){
            return false;
        }}
    TuileV2 voisinOuest= plateau.getTuile(new Position(x-1,y));
    if(voisinOuest!=null){
        nbVoisins++;
        if (!sontCompatibles(tuile.getOuestSegments(),voisinOuest.getEstSegments())){
            return false;
        }
    }    
    return nbVoisins>0; // on doit poser la tuile à côté d'au moins une tuile déjà posée
    }
     
    private boolean sontCompatibles(List<Segment> seg1, List<Segment> seg2){
     if (seg1 == null || seg2==null){
        return false;
     }
     if(seg1.isEmpty() && seg2.isEmpty()){
        return false;
     }
     if (seg1.size()!=seg2.size()){
        return false;
     }
     for (int i=0;i< seg1.size() ; i++){
        Segment s1=seg1.get(i);
        Segment s2=seg2.get(i);
        if (s1==null || s2==null){
            return false;
        }
       if (s1.getClass() != s2.getClass()){
            return false;
    }
     }
  return true;
    }

}
   