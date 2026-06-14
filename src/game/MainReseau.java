package game;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;

import game.plateau.PlateauV2;
import game.plateau.tuiles.SacTuiles;
import game.plateau.tuiles.TuileManager;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;
import game.rules.ReglePlacement;
import game.joueur.Joueur;
import game.plateau.tuiles.segment.Segment;
import game.rules.RegleMeeple;
import game.rules.ScoreAbbayeV2;
import game.rules.ScoreRouteV2;
import game.rules.ScoreVilleV2;
import game.rules.ScoreChampV2;

public class MainReseau {
    
    // Fonction pour envoyer un message au Réflecteur via l'arbitre
    private static void envoyerReseau(String message) {
        System.out.println("MSG: Arbitre " + message);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Initialisation de l'Arbitre...");
        PlateauV2 plateau = new PlateauV2();
        
        TuileManager tuileManager = new TuileManager();
        SacTuiles sac = tuileManager.creerSac(new RepresentationManager());

        // roles arbirtre + joueurs
        envoyerReseau("ELECTS referee Arbitre");
        envoyerReseau("ELECTS player Alice Bob");

        Joueur alice = new Joueur("Alice");
        Joueur bob = new Joueur("Bob");
        List<Joueur> listeJoueurs = Arrays.asList(alice, bob);

        //init des regles
        ScoreRouteV2 scoreRouteV2 = new ScoreRouteV2();
        ScoreAbbayeV2 scoreAbbayeV2 = new ScoreAbbayeV2();
        ScoreVilleV2 scoreVilleV2 = new ScoreVilleV2();
        ScoreChampV2 scoreChampV2 = new ScoreChampV2();

        int tour = 1;
        int rangGlobal = 0; // Compteur pour suivre le rang des messages du réflecteur

        // Boucle de jeu
        while (!sac.estVide()) {
            Joueur joueurCourant = listeJoueurs.get((tour - 1) % listeJoueurs.size());
            String nomJoueurCourant = joueurCourant.getNom();
            TuileV2 tuilePiochee = sac.piocher();
            
            // Envoi du plateau complet (sous forme brut) aux clients
            envoyerReseau("BOARD_START");
            for (java.util.Map.Entry<Position, TuileV2> entry : plateau.getCases().entrySet()) {
                Position position = entry.getKey();
                TuileV2 tuile = entry.getValue();
                envoyerReseau("TUILE " + position.getX() + ":" + position.getY() + " " + tuile.getRepresentationReseau());
            }
            envoyerReseau("BOARD_END"); 

            // on va calculer toutes les positions disponibles, dans les 4 sens
            for (int i = 0; i < 4; i++) {
                List<Position> dispo = getPositionsDisponiblesV2(plateau, tuilePiochee);

                if (!dispo.isEmpty()) {
                    // envoie des moves ok
                    StringBuilder sbMoves = new StringBuilder("VALID_MOVES " + tuilePiochee.getRepresentationReseau());

                    //parcours des positions
                    for (Position p : dispo) {
                        sbMoves.append(" ").append(p.getX()).append(":").append(p.getY());
                    }
                    envoyerReseau(sbMoves.toString());

                    // envoie des meeples ok
                    StringBuilder sbMeeples = new StringBuilder("VALID_MEEPLES " + tuilePiochee.getRepresentationReseau());
                    RegleMeeple regleMeeple = new RegleMeeple();

                    for (Position p : dispo) {
                        // On pose temporairement la tuile
                        plateau.Poser(tuilePiochee, p);

                        sbMeeples.append(" ").append(p.getX()).append(":").append(p.getY());

                        //deduire les meeples valides
                        List<String> validSegments = new ArrayList<>();
                        if (joueurCourant.getMeeplesRestants() > 0) {
                            for (Segment s : tuilePiochee.getSegments()) {
                                if (regleMeeple.validate(plateau, p, joueurCourant, s)) {
                                    validSegments.add(s.getRepresentation());
                                }
                            }
                        }

                        // On retire la tuile temporaire 
                        plateau.getCases().remove(p);

                        //si pas de meeeples
                        if (validSegments.isEmpty()) {
                            sbMeeples.append(" none");
                        } 
                        
                        //si meeples
                        else {
                            for (String s : validSegments) {
                                sbMeeples.append(" ").append(s);
                            }
                        }
                    }
                    envoyerReseau(sbMeeples.toString());
                }
                tuilePiochee.orienterDroite();
            }

            // L'Arbitre propose la tuile encodé selon le reaseau
            String representationReseauTuile = tuilePiochee.getRepresentationReseau();
            envoyerReseau("OFFERS " + representationReseauTuile + " " + nomJoueurCourant);
            
            boolean tourFini = false;

            // Boucle d'attente d'une action valide du joueur
            while (!tourFini) {
                
                // reponse du client
                if (scanner.hasNextLine()) {
                    String ligneRecue = scanner.nextLine().trim();
                    
                    if (ligneRecue.isEmpty() || ligneRecue.startsWith("#")) continue;

                    // On a reçu un message valide du réflecteur, on incrémente le rang !
                    rangGlobal++;

                    String[] mots = ligneRecue.split("\\s+");

                    //attente des arguments 
                    if (mots.length < 2) continue;

                    String source = mots[0];
                    String keyword = mots[1];

                    // Si le joueur courant tente de poser une tuile
                    if (source.equals(nomJoueurCourant) && keyword.equals("PLACES")) {
                        
                        // Mise en force du message : Joueur PLACES tuile x:y meeple
                        if (mots.length >= 4) {
                            //construction coordonénes
                            String tuileProposee = mots[2];
                            String coordonnees = mots[3];
                            String[] coords = coordonnees.split(":");
                            
                            if (coords.length == 2) {
                                try {
                                    int x = Integer.parseInt(coords[0]);
                                    int y = Integer.parseInt(coords[1]);
                                    Position posChoisie = new Position(x, y);

                                    //logique de rotation de la tuile
                                    boolean rotationValide = false;
                                    // essayer les 4 rotations pour voir si la tuile est ok
                                    for (int i = 0; i < 4; i++) {
                                        //rotation ok
                                        if (tuilePiochee.getRepresentationReseau().equals(tuileProposee)) {
                                            rotationValide = true;
                                            break; 
                                        }
                                        //sinon tourner
                                        tuilePiochee.orienterDroite();
                                    }

                                    // rotation pas ok
                                    if (!rotationValide) {
                                        System.out.println("Arbitre: La tuile proposée n'est pas valide (" + tuileProposee + ").");
                                        envoyerReseau("BLAMES " + rangGlobal + " illegal-tile");
                                        continue; 
                                    }

                                    // Vérification des règles de placement
                                    ReglePlacement reglePlacement = new ReglePlacement();
                                    if (reglePlacement.validate(plateau, tuilePiochee, posChoisie)) {

                                        plateau.Poser(tuilePiochee, posChoisie);

                                        Segment segmentCible = null;
                                        boolean erreurMeeple = false;
                                        
                                        // On vérifie si le joueur a demandé à poser un meeple
                                        String meepleSaisie = (mots.length >= 5) ? mots[4] : "none";
                                        
                                        //si on place un meeple
                                        if (!meepleSaisie.equals("none")) {
                                            // on cherche si ce segment existe sur la tuile
                                            for (Segment s : tuilePiochee.getSegments()) {
                                                if (s.getRepresentation().equals(meepleSaisie)) {
                                                    segmentCible = s;
                                                    break;
                                                }
                                            }
                                            
                                            // verif des regles de pose du meeple
                                            RegleMeeple regleMeeple = new RegleMeeple();
                                            // Si segment introuvable ou interdit par les règles
                                            if (segmentCible == null || !regleMeeple.validate(plateau, posChoisie, joueurCourant, segmentCible)) {
                                                erreurMeeple = true;
                                            }
                                        }

                                        //plus de meeple ou mauvais segment
                                        if (erreurMeeple) {
                                            System.out.println("Meeple invalide ou zone occupée");

                                            //on retire la tuile courante du plateau
                                            plateau.getCases().remove(posChoisie);

                                            envoyerReseau("BLAMES " + rangGlobal + " illegal-meeple");
                                            continue;
                                        }

                                        // dès que meeple ok, on place la tuile + meeple
                                        if (segmentCible != null) {
                                            boolean poseOk = plateau.poserMeeple(posChoisie, joueurCourant, segmentCible);

                                            if (poseOk) {
                                                System.out.println("Meeple posé sur " + meepleSaisie + " par le joueur " + nomJoueurCourant);
                                            }
                                        }
                                        
                                        //##################################### 
                                        // Partie scoring
                                        //##################################### 
                                        int pointsGagnes = 0;
                                        
                                        pointsGagnes += gererScoreRoute(scoreRouteV2, plateau, tuilePiochee, posChoisie, nomJoueurCourant);
                                        pointsGagnes += gererScoreAbbaye(scoreAbbayeV2, plateau, tuilePiochee, posChoisie, nomJoueurCourant);
                                        pointsGagnes += gererScoreVille(scoreVilleV2, plateau, tuilePiochee, posChoisie, nomJoueurCourant);
                                        pointsGagnes += gererScoreChamp(scoreChampV2, plateau, tuilePiochee, posChoisie, nomJoueurCourant);

                                        System.out.println("Arbitre: Coup valide en " + coordonnees + " pour le joueur " + source);
                                        envoyerReseau("SCORES " + rangGlobal + " " + pointsGagnes);
                                        tourFini = true;
                                    } 
                                    
                                    //coup invalide
                                    else {
                                        System.out.println("Arbitre: Coup invalide en " + coordonnees);
                                        envoyerReseau("BLAMES " + rangGlobal + " illegal-position");
                                    }
                                } 
                                //coordonnées pas bonne
                                catch (NumberFormatException e) {
                                    envoyerReseau("BLAMES " + rangGlobal + " invalid-coordinates");
                                }
                            } 
                            
                            //mauvaise syntaxe message
                            else {
                                envoyerReseau("BLAMES " + rangGlobal + " syntax-error");
                            }
                        } else {
                            envoyerReseau("BLAMES " + rangGlobal + " missing-arguments");
                        }
                    }
                }
            }

            tour++;
        }

        //fin de partie
        afficherGagnant(listeJoueurs);
        envoyerReseau("CLOSES");
        scanner.close();
    }

    // ====================
    // METHODES DE SCORING
    // ====================

    /**
     * Va calculer les points attribués par une route
     * @param scoreRoute le score de la route
     * @param plateau le plateau 
     * @param tuile la tuile actuelle
     * @param pos la position courante
     * @param nomJoueurCourant le nom du joueur courant 
     * @return le nombre de points obtenu pour une route
     */
    private static int gererScoreRoute(ScoreRouteV2 scoreRoute, PlateauV2 plateau, TuileV2 tuile, Position pos, String nomJoueurCourant) {
        int pointsCourant = 0;
        ScoreRouteV2.RouteScoreResult result = scoreRoute.attribuerScorePlacement(plateau, tuile, pos);
        
        if (result.getPointsRoute() > 0 && !result.getJoueursMajoritaires().isEmpty()) {
            for (Joueur j : result.getJoueursMajoritaires()) {
                System.out.println("Arbitre: Route complétée ! Le joueur " + j.getNom() + " a obtenu " + result.getPointsRoute() + " points.");
                if (j.getNom().equals(nomJoueurCourant)) {
                    pointsCourant += result.getPointsRoute();
                }
            }
        }
        return pointsCourant;
    }

    /**
     * Va calculer les points attribués par une abbaye
     * @param scoreAbbaye le score
     * @param plateau le plateau 
     * @param tuile la tuile actuelle
     * @param pos la position courante
     * @param nomJoueurCourant le nom du joueur courant 
     * @return le nombre de points obtenu pour une abbaye
     */
    private static int gererScoreAbbaye(ScoreAbbayeV2 scoreAbbaye, PlateauV2 plateau, TuileV2 tuile, Position pos, String nomJoueurCourant) {
        int pointsCourant = 0;
        ScoreAbbayeV2.AbbayeScoreResult result = scoreAbbaye.attribuerScorePlacement(plateau, tuile, pos);
        
        if (result.getPointsAbbaye() > 0 && result.getProprietaire() != null) {
            System.out.println("Arbitre: Abbaye complétée ! Le joueur " + result.getProprietaire().getNom() + " a obtenu " + result.getPointsAbbaye() + " points.");
            if (result.getProprietaire().getNom().equals(nomJoueurCourant)) {
                pointsCourant += result.getPointsAbbaye();
            }
        }
        return pointsCourant;
    }

    /**
     * Va calculer les points attribués par une ville
     * @param scoreAbbaye le score
     * @param plateau le plateau 
     * @param tuile la tuile actuelle
     * @param pos la position courante
     * @param nomJoueurCourant le nom du joueur courant 
     * @return le nombre de points obtenu pour une ville
     */
    private static int gererScoreVille(ScoreVilleV2 scoreVille, PlateauV2 plateau, TuileV2 tuile, Position pos, String nomJoueurCourant) {
        int pointsCourant = 0;
        ScoreVilleV2.VilleScoreResult result = scoreVille.attribuerScorePlacement(plateau, tuile, pos);
        
        if (result.getPointsVille() > 0 && !result.getJoueursMajoritaires().isEmpty()) {
            for (Joueur j : result.getJoueursMajoritaires()) {
                System.out.println("Arbitre: Ville complétée ! Le joueur " + j.getNom() + " a obtenu " + result.getPointsVille() + " points.");
                if (j.getNom().equals(nomJoueurCourant)) {
                    pointsCourant += result.getPointsVille();
                }
            }
        }
        return pointsCourant;
    }

    /**
     * Va calculer les points attribués par un champ
     * @param scoreAbbaye le score
     * @param plateau le plateau 
     * @param tuile la tuile actuelle
     * @param pos la position courante
     * @param nomJoueurCourant le nom du joueur courant 
     * @return le nombre de points obtenu pour un champ
     */
    private static int gererScoreChamp(ScoreChampV2 scoreChamp, PlateauV2 plateau, TuileV2 tuile, Position pos, String nomJoueurCourant) {
        int pointsCourant = 0;
        ScoreChampV2.ChampScoreResult result = scoreChamp.attribuerScorePlacement(plateau, tuile, pos);
        
        if (result.getPointsChamp() > 0 && !result.getJoueursMajoritaires().isEmpty()) {
            for (Joueur j : result.getJoueursMajoritaires()) {
                System.out.println("Arbitre: Champ évalué ! Le joueur " + j.getNom() + " a obtenu " + result.getPointsChamp() + " points.");
                if (j.getNom().equals(nomJoueurCourant)) {
                    pointsCourant += result.getPointsChamp();
                }
            }
        }
        return pointsCourant;
    }


    /**
     * Fonction interne pour calculer les positions disponibles pour le joeur
     * @param plateau le plateau du jeu
     * @param tuile la tuile actuelle
     * @return les positions sur lesquelles on peut poser des tuiles
     */
    private static List<Position> getPositionsDisponiblesV2(PlateauV2 plateau, TuileV2 tuile){

        List<Position> dispo = new ArrayList<>();

        //parcours de toute les cases
        for (int x = 0; x < 72; x++) {
            for (int y = 0; y < 72; y++) {
                Position p = new Position(x, y);
                ReglePlacement regle = new ReglePlacement();
                //si ça valide, on ajoute a la liste
                if(regle.validate(plateau, tuile, p)){
                    dispo.add(p);
                }
            }
        }
        return dispo;
    }

    /**
     * Calcule et affiche le gagnant à la fin de la partie.
     * @param listeJoueurs La liste des joueurs de la partie
     */
    private static void afficherGagnant(List<Joueur> listeJoueurs) {
        System.out.println("\n----------------------------------------");
        System.out.println("           FIN DE LA PARTIE !            ");
        System.out.println("----------------------------------------\n");

        Joueur gagnant = null;
        int maxScore = -1;
        boolean egalite = false;

        //affichage de tous les scores 
        for (Joueur j : listeJoueurs) {
            System.out.println("Score final de " + j.getNom() + " : " + j.getScore() + " points.");
            
            //recherche du plus haut score
            if (j.getScore() > maxScore) {
                maxScore = j.getScore();
                gagnant = j;
                egalite = false;
            } 
            else if (j.getScore() == maxScore) {
                egalite = true; 
            }
        }
        System.out.println("\n-----------------------------------------");
        if (egalite) {
            System.out.println("La partie est finie. C'est une egalité !");
        } 
        else if (gagnant != null) {
            System.out.println("La partie est finie. Le gagnant est : " + gagnant.getNom().toUpperCase() + " avec " + maxScore + " points !");
        }
        System.out.println("-----------------------------------------\n");
    }
}