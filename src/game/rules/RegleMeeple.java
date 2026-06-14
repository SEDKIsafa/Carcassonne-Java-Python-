package game.rules;
import game.joueur.Joueur;
import game.plateau.PlateauV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.Segment;
import java.util.*;
/**
 * Regle de validation pour la pose d'un meeple.
 *
 * Cette classe verifie qu'un joueur peut poser un meeple sur un segment cible:
 * - parametres non nuls (plateau, position, joueur, segment)
 * - presence d'une tuile a la position donnee
 * - segment cible appartenant bien a la tuile
 * - joueur disposant encore d'au moins un meeple
 * - segment cible non deja occupe
 * - absence de meeple sur la meme zone connectee
 *
 * La recherche de zone connectee est realisee a partir d'un couple
 * (position, segment) afin de distinguer des segments de meme type
 * mais representant des zones differentes.
 *
  * Cas particulier abbaye:
 * contrairement aux routes/champs/villes, une abbaye est traitee localement.
 * La verification se fait uniquement sur l'abbaye cible, sans propagation sur les voisins.
 */
public class RegleMeeple {

  public boolean validate (PlateauV2 plateau, Position position, Joueur joueur, Segment segment){
    // on vérifie que les paramètres ne sont pas nuls
    if (plateau== null||position==null||segment==null || joueur==null){
        return false; 
    }
    TuileV2 tuile=plateau.getTuile(position);
    // on ne peut pas poser de meeple si il n'y a pas de tuile à cet emplacement
    if(tuile==null){
        return false;
    }
    // le segment choisi doit faire partie de la tuile sur laquelle le joueur veut placer son meeple
    if(!tuile.getSegments().contains(segment)){
        return false; 
    }
    // on vérifie que le joueur a des meeples restants à poser
    if(joueur.getMeeplesRestants()<=0){
        return false;
    }
    // on vérifie que le segment choisi n'a pas déjà un meeple dessus
    if(tuile.getMeeple(segment)!=null){
        return false;
    }
     
    return isZoneLibre(plateau, position, segment);
  
    
  }
  // vérifie  qu'aucun segment connecté n'a de meeple dessus
    private boolean isZoneLibre(PlateauV2 plateau, Position startPos, Segment startSeg) {
        Set<Node> connected = getConnectedSegments(plateau, startPos, startSeg);

        for (Node n : connected) {
            if (n.segment.getMeeple() != null) {
                return false;
            }
        }
        return true;
    }

/**
 Récupère tous les segments connectés du même type (ville/route/champ/abbaye).
 */
private Set<Node> getConnectedSegments(PlateauV2 plateau, Position startPos, Segment startSeg) {
    Set<Node> visited = new HashSet<>();
    Queue<Node> queue = new LinkedList<>();

    Node start = new Node(startPos, startSeg);
    visited.add(start);
    queue.add(start);

    while (!queue.isEmpty()) {
        Node current = queue.poll();
        TuileV2 currentTuile = plateau.getTuile(current.position);
        if (currentTuile == null) {
            continue;
        }

        if (current.segment instanceof game.plateau.tuiles.segment.SegmentType.SegmentAbbaye) {
            continue;
        }

        int x = current.position.getX();
        int y = current.position.getY();

        if (currentTuile.getNordSegments() != null) {
            int idx = currentTuile.getNordSegments().indexOf(current.segment);
            if (idx >= 0) {
                Position northPos = new Position(x, y + 1);
                TuileV2 northTuile = plateau.getTuile(northPos);
                if (northTuile != null && northTuile.getSudSegments() != null && idx < northTuile.getSudSegments().size()) {
                    Segment candidat = northTuile.getSudSegments().get(idx);
                    if (candidat != null && candidat.getClass().equals(current.segment.getClass())) {
                        Node next = new Node(northPos, candidat);
                        if (visited.add(next)) {
                            queue.add(next);
                        }
                    }
                }
            }
        }

      
        if (currentTuile.getSudSegments() != null) {
            int idx = currentTuile.getSudSegments().indexOf(current.segment);
            if (idx >= 0) {
                Position southPos = new Position(x, y - 1);
                TuileV2 southTuile = plateau.getTuile(southPos);
                if (southTuile != null && southTuile.getNordSegments() != null && idx < southTuile.getNordSegments().size()) {
                    Segment candidat = southTuile.getNordSegments().get(idx);
                    if (candidat != null && candidat.getClass().equals(current.segment.getClass())) {
                        Node next = new Node(southPos, candidat);
                        if (visited.add(next)) {
                            queue.add(next);
                        }
                    }
                }
            }
        }

    
        if (currentTuile.getEstSegments() != null) {
            int idx = currentTuile.getEstSegments().indexOf(current.segment);
            if (idx >= 0) {
                Position eastPos = new Position(x + 1, y);
                TuileV2 eastTuile = plateau.getTuile(eastPos);
                if (eastTuile != null && eastTuile.getOuestSegments() != null && idx < eastTuile.getOuestSegments().size()) {
                    Segment candidat = eastTuile.getOuestSegments().get(idx);
                    if (candidat != null && candidat.getClass().equals(current.segment.getClass())) {
                        Node next = new Node(eastPos, candidat);
                        if (visited.add(next)) {
                            queue.add(next);
                        }
                    }
                }
            }
        }

     
        if (currentTuile.getOuestSegments() != null) {
            int idx = currentTuile.getOuestSegments().indexOf(current.segment);
            if (idx >= 0) {
                Position westPos = new Position(x - 1, y);
                TuileV2 westTuile = plateau.getTuile(westPos);
                if (westTuile != null && westTuile.getEstSegments() != null && idx < westTuile.getEstSegments().size()) {
                    Segment candidat = westTuile.getEstSegments().get(idx);
                    if (candidat != null && candidat.getClass().equals(current.segment.getClass())) {
                        Node next = new Node(westPos, candidat);
                        if (visited.add(next)) {
                            queue.add(next);
                        }
                    }
                }
            }
        }
    }

    return visited;
}
// Classe interne pour représenter une position + segment de manière unique dans les ensembles
private static final class Node {
    private final Position position;
    private final Segment segment;

    private Node(Position position, Segment segment) {
        this.position = position;
        this.segment = segment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node other = (Node) o;
        return this.position.equals(other.position) && this.segment == other.segment;
    }

    @Override
    public int hashCode() {
        return 31 * position.hashCode() + System.identityHashCode(segment);
    }
}}