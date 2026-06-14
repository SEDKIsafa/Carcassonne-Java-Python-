package game.plateau.tuiles.direction;

import java.util.Objects;

public class Position {

    // On met final : une Position ne doit pas changer une fois créée.
    private final int x;
    private final int y;

    public Position(int x, int y){
        this.x = x;
        this.y =y;
    }

    public int getX(){
        return this.x;
    }

    public int getY(){
        return this.y;
    }

    // equals() : dire quand deux Position sont "égales"
    // ici elles sont égales si elles ont le même x et le même y.
    @Override
    public boolean equals(Object o) {
        // Si c'est exactement le même objet en mémoire -> vrai
        if (this == o) return true;

        // Si o n'est pas une Position -> faux
        if (!(o instanceof Position)) return false;

        // On convertit o en Position pour accéder à x et y
        Position p = (Position) o;

        // Deux positions sont égales si elles ont le même x et le même y
        return this.x == p.x && this.y == p.y;
    }

    // hashCode()  donne un numéro basé sur x et y
    //  si equals() dit que deux objets sont égaux, alors hashCode() doit donner le même résultat.
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

   
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}
