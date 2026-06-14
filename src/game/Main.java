package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import game.joueur.Joueur;
import game.listchooser.InteractiveListChooser;
import game.plateau.PlateauV2;
import game.plateau.tuiles.SacTuiles;
import game.plateau.tuiles.TuileManager;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.Segment;
import game.plateau.tuiles.segment.SegmentType.SegmentAbbaye;
import game.plateau.tuiles.segment.SegmentType.SegmentChamp;
import game.plateau.tuiles.segment.SegmentType.SegmentRoute;
import game.plateau.tuiles.segment.SegmentType.SegmentVille;
import game.rules.ReglePlacement;
import game.rules.ScoreAbbayeV2;
import game.rules.ScoreChampV2;
import game.rules.ScoreRouteV2;
import game.rules.ScoreVilleV2;


/**main class for the game */
public class Main {
    public static void main(String[] args) {
        //pour les inputs
        Scanner scanner = new Scanner(System.in);

        // ListChoosers pour faciliter les choix de placement
        InteractiveListChooser<Position> positionChooser = new InteractiveListChooser<>();
        InteractiveListChooser<Segment> segmentChooser = new InteractiveListChooser<>();

        System.out.println("\nJEU CARCASSONNE");
        System.out.println("Mise en place du plateau...");

        PlateauV2 plateau = new PlateauV2();
        ScoreRouteV2 scoreRouteV2 = new ScoreRouteV2();
        ScoreAbbayeV2 scoreAbbayeV2 = new ScoreAbbayeV2();
        ScoreVilleV2 scoreVilleV2 = new ScoreVilleV2();
        ScoreChampV2 scoreChampV2 = new ScoreChampV2();

        //creation des joueurs
        System.out.println("Connexion des joueurs...");
        List<Joueur> joueurs = new ArrayList<>();
        joueurs.add(new Joueur("Alice"));
        joueurs.add(new Joueur("Bob"));
        //TODO gerer les connexions des joueurs avec websocket 

        //creer le sac de tuile
        System.out.println("Création du sac de tuiles...");
        RepresentationManager representationManager = new RepresentationManager();
        TuileManager tuileManager = new TuileManager();
        SacTuiles sac = tuileManager.creerSac(representationManager);
        
        System.out.println("Votre sac contient: " + sac.getNombreTuilesRestante() + " tuiles restantes");
        System.out.println("La tuile de départ a été posée au centre du plateau en (36, 36)");

        int tour = 0;
        //boucle de jeu
        while (!sac.estVide()){
            //joueur courant
            Joueur joueurCourant = joueurs.get(tour % joueurs.size()); 
            System.out.println("\n---------------------------------------");
            System.out.println("------------TOUR SUIVANT---------------");
            System.out.println("---------------------------------------");
            System.out.println("Votre sac contient: " + sac.getNombreTuilesRestante() + " tuiles restantes");
            System.out.println("C'est au tour de: " + joueurCourant.getNom()+ "\nScore : " + joueurCourant.getScore() + "\nNombre de meeples: "+ joueurCourant.getMeeplesRestants());

            System.out.println("\n---------------------------------------");
            System.out.println("----------------PLATEAU----------------");
            plateau.displayPlateau();
            System.out.println("---------------------------------------");
            
            //tirer la Tuile 
            TuileV2 tuilePiochee = sac.piocher();
            System.out.println("\nLa tuile qui a été piochée est: " + tuilePiochee.toString());

            boolean tuilePlacee = false;

            //boucler ici sur les gauche, droite et poser
            while(!tuilePlacee) {
                //choix de l'action du joueur
                System.out.println("\nMerci de choisir l'action a faire: ");
                System.out.println("gauche -> tourner à gauche");
                System.out.println("droite -> tourner à droite");
                System.out.println("poser -> poser la tuile");
                System.out.println("Votre choix? : ");
                
                //recup de l'action avec trim pour formatter la message
                String action = scanner.nextLine().trim().toLowerCase();

                //orienter à gauche
                if(action.equals("gauche")){
                    tuilePiochee.orienterGauche();
                    System.out.println("Votre tuile a été orientée vers la gauche: " + tuilePiochee.toString());
                }
                //orienter à droite
                else if(action.equals("droite")){
                    tuilePiochee.orienterDroite();
                    System.out.println("Votre tuile a été orientée vers la droite: " + tuilePiochee.toString());
                }
                //placer tuile
                else if(action.equals("poser")){
                    
                    // On récupère toutes les positions adjacentes aux tuiles déjà posées
                    List<Position> posDisponibles = getPositionsDisponiblesV2(plateau,tuilePiochee);

                    if (posDisponibles.isEmpty()) {
                        System.out.println("Aucune position disponible trouvée !");
                        continue;
                    }

                    //proposer au joueur la liste des tuiles dispo
                    Position choixPos = positionChooser.choose("Sur quelle position voulez-vous poser la tuile ?", posDisponibles);

                    //cas ou on est pas dans 0
                    if(choixPos != null){
                        plateau.Poser(tuilePiochee, choixPos);
                        System.out.println("La tuile a été posée en : " + choixPos.toString());

                        //on a placé une tuile donc sortie de la selection
                        tuilePlacee = true;

                        //pose des Meeples 
                        if(joueurCourant.getMeeplesRestants() > 0) {
                            System.out.println("Voulez-vous poser un meeple ? (O/N)");
                            String reponseMeeple = scanner.nextLine().trim().toLowerCase();
                            
                            //oui pour poser un meeple
                            if(reponseMeeple.equals("o") || reponseMeeple.equals("oui")) {
                                // Listchooser avec liste de segments de la tuile
                                Segment choixSegment = segmentChooser.choose("Sur quel segment poser le meeple ?", tuilePiochee.getSegments());
                                
                                //si pose de meeple
                                if (choixSegment != null) {
                                    //TODO check regle si on peut poser selon le type de segment & pose sur le segment
                                    boolean pose = plateau.poserMeeple(choixPos, joueurCourant, choixSegment);
                                    if(pose) {
                                        System.out.println("Meeple posé !");
                                    } 
                                    else {
                                        System.out.println("Impossible de poser le meeple ici.");
                                    }
                                }  
                                //si abandon pose de meeple
                                else {
                                    System.out.println("Pose de meeple annulée.");
                                }
                            }
                            //non pour poser un meeple
                            else{
                                System.out.println("Vous avez choisi de ne pas poser de meeple.");
                            }
                        } 
                        //plus de meeple
                        else {
                            System.out.println("Vous n'avez plus de meeples !");
                        }

                        ScoreRouteV2.RouteScoreResult scoreRouteResult =
                            scoreRouteV2.attribuerScorePlacement(plateau, tuilePiochee, choixPos);
                        afficherScoreRoutePlacement(scoreRouteResult);

                        ScoreVilleV2.VilleScoreResult scoreVilleResult =
                            scoreVilleV2.attribuerScorePlacement(plateau, tuilePiochee, choixPos);
                        afficherScoreVillePlacement(scoreVilleResult);

                        ScoreAbbayeV2.AbbayeScoreResult scoreAbbayeResult =
                            scoreAbbayeV2.attribuerScorePlacement(plateau, tuilePiochee, choixPos);
                        afficherScoreAbbayePlacement(scoreAbbayeResult);
                    }
                    //le joeur de choisi pas de poser
                    else {
                        System.out.println("Pose annulée. Vous retournez au choix d'action.");
                    }
                }
                else {
                    System.out.println("Commande non reconnue.");
                }
            }
            tour++;
        }

        System.out.println("\nCalcul des scores de fin de partie...");
        calculerScoresFinPartie(plateau, scoreRouteV2, scoreVilleV2, scoreChampV2, scoreAbbayeV2);

        System.out.println("-----------------------------------------");
        System.out.println("           FIN DE LA PARTIE              ");
        plateau.displayPlateau();
        afficherClassementFinal(joueurs);

        scanner.close();
    }

    private static void afficherScoreRoutePlacement(ScoreRouteV2.RouteScoreResult scoreRouteResult) {
        if (scoreRouteResult.getPointsRoute() > 0 && !scoreRouteResult.getJoueursMajoritaires().isEmpty()) {
            for (Joueur joueurScore : scoreRouteResult.getJoueursMajoritaires()) {
                System.out.println(
                    "Route completee: +" + scoreRouteResult.getPointsRoute() +
                    " point(s) pour " + joueurScore.getNom()
                );
            }
        } else if (scoreRouteResult.isRouteComplete() && scoreRouteResult.getPointsRoute() > 0) {
            System.out.println("Route completee mais aucun meeple dessus: aucun point attribue.");
        }
    }

    private static void afficherScoreVillePlacement(ScoreVilleV2.VilleScoreResult scoreVilleResult) {
        if (scoreVilleResult.getPointsVille() > 0 && !scoreVilleResult.getJoueursMajoritaires().isEmpty()) {
            for (Joueur joueurScore : scoreVilleResult.getJoueursMajoritaires()) {
                System.out.println(
                    "Ville completee: +" + scoreVilleResult.getPointsVille() +
                    " point(s) pour " + joueurScore.getNom()
                );
            }
        } else if (scoreVilleResult.isVilleComplete() && scoreVilleResult.getPointsVille() > 0) {
            System.out.println("Ville completee mais aucun meeple dessus: aucun point attribue.");
        }
    }

    private static void afficherScoreAbbayePlacement(ScoreAbbayeV2.AbbayeScoreResult scoreAbbayeResult) {
        if (scoreAbbayeResult.getPointsAbbaye() > 0 && scoreAbbayeResult.getProprietaire() != null) {
            System.out.println(
                "Abbaye: +" + scoreAbbayeResult.getPointsAbbaye() +
                " point(s) pour " + scoreAbbayeResult.getProprietaire().getNom()
            );
        } else if (scoreAbbayeResult.isAbbayeComplete() && scoreAbbayeResult.getPointsAbbaye() > 0) {
            System.out.println("Abbaye completee mais aucun meeple dessus: aucun point attribue.");
        }
    }

    private static void calculerScoresFinPartie(
        PlateauV2 plateau,
        ScoreRouteV2 scoreRouteV2,
        ScoreVilleV2 scoreVilleV2,
        ScoreChampV2 scoreChampV2,
        ScoreAbbayeV2 scoreAbbayeV2
    ) {
        Set<Segment> segmentsDejaScores = Collections.newSetFromMap(new IdentityHashMap<>());

        for (Map.Entry<Position, TuileV2> entry : plateau.getCases().entrySet()) {
            Position position = entry.getKey();
            TuileV2 tuile = entry.getValue();

            if (tuile.hasAbbaye()) {
                Segment abbaye = tuile.getAbbaye();
                if (abbaye != null && abbaye.getMeeple() != null && segmentsDejaScores.add(abbaye)) {
                    ScoreAbbayeV2.AbbayeScoreResult resultatAbbaye =
                        scoreAbbayeV2.attribuerScoreFinPartie(plateau, tuile, position);
                    if (resultatAbbaye.getPointsAbbaye() > 0 && resultatAbbaye.getProprietaire() != null) {
                        System.out.println(
                            "Fin de partie - abbaye: +" + resultatAbbaye.getPointsAbbaye() +
                            " point(s) pour " + resultatAbbaye.getProprietaire().getNom()
                        );
                    }
                }
            }

            for (Segment segment : tuile.getSegments()) {
                if (segment == null || segment.getMeeple() == null || segmentsDejaScores.contains(segment)) {
                    continue;
                }

                if (segment instanceof SegmentRoute) {
                    ScoreRouteV2.RouteScoreResult resultatRoute =
                        scoreRouteV2.attribuerScoreFinPartie(plateau, tuile, position);
                    segmentsDejaScores.addAll(resultatRoute.getSegmentsRouteAvecMeeple());
                    if (resultatRoute.getPointsRoute() > 0) {
                        for (Joueur joueurScore : resultatRoute.getJoueursMajoritaires()) {
                            System.out.println(
                                "Fin de partie - route: +" + resultatRoute.getPointsRoute() +
                                " point(s) pour " + joueurScore.getNom()
                            );
                        }
                    }
                } else if (segment instanceof SegmentVille) {
                    ScoreVilleV2.VilleScoreResult resultatVille =
                        scoreVilleV2.attribuerScoreFinPartie(plateau, tuile, position);
                    segmentsDejaScores.addAll(resultatVille.getSegmentsVilleAvecMeeple());
                    if (resultatVille.getPointsVille() > 0) {
                        for (Joueur joueurScore : resultatVille.getJoueursMajoritaires()) {
                            System.out.println(
                                "Fin de partie - ville: +" + resultatVille.getPointsVille() +
                                " point(s) pour " + joueurScore.getNom()
                            );
                        }
                    }
                } else if (segment instanceof SegmentChamp) {
                    ScoreChampV2.ChampScoreResult resultatChamp =
                        scoreChampV2.attribuerScoreFinPartie(plateau, tuile, position);
                    segmentsDejaScores.addAll(resultatChamp.getSegmentsChampAvecMeeple());
                    if (resultatChamp.getPointsChamp() > 0) {
                        for (Joueur joueurScore : resultatChamp.getJoueursMajoritaires()) {
                            System.out.println(
                                "Fin de partie - champ: +" + resultatChamp.getPointsChamp() +
                                " point(s) pour " + joueurScore.getNom()
                            );
                        }
                    }
                } else if (segment instanceof SegmentAbbaye) {
                    segmentsDejaScores.add(segment);
                }
            }
        }
    }

    private static void afficherClassementFinal(List<Joueur> joueurs) {
        System.out.println("\nScores finaux:");
        for (Joueur joueur : joueurs) {
            System.out.println(joueur.getNom() + " : " + joueur.getScore() + " points");
        }
    }

    /**
     * Calcule et retourne la liste des positions jouables sur le plateau.
     * Une position est jouable si elle est vide et adjacente à au moins une tuile existante.
     * @param plateau Le plateau de jeu actuel
     * @return Une liste de Position valides
     */
    private static List<Position> getPositionsDisponibles(PlateauV2 plateau) {
        List<Position> dispo = new ArrayList<>();
        
        // Le plateau fait virtuellement 72x72, on parcourt une zone sûre
        // (En vrai, on pourrait optimiser en ne cherchant qu'entre xMin-1 et xMax+1)
        for (int x = 0; x < 72; x++) {
            for (int y = 0; y < 72; y++) {
                Position p = new Position(x, y);
                
                // Si la case est vide
                if (plateau.peutPoser(p)) {
                    // On vérifie s'il y a une tuile sur l'une des 4 cases adjacentes (Haut, Bas, Gauche, Droite)
                    if (plateau.getTuile(new Position(x + 1, y)) != null || plateau.getTuile(new Position(x - 1, y)) != null || plateau.getTuile(new Position(x, y + 1)) != null || plateau.getTuile(new Position(x, y - 1)) != null) {
                        dispo.add(p);
                    }
                }
            }
        }
        return dispo;
    }


    private static List<Position> getPositionsDisponiblesV2(PlateauV2 plateau, TuileV2 tuile){
        List<Position> dispo = new ArrayList<>();
        
        // Le plateau fait virtuellement 72x72, on parcourt une zone sûre
        // (En vrai, on pourrait optimiser en ne cherchant qu'entre xMin-1 et xMax+1)
        for (int x = 0; x < 72; x++) {
            for (int y = 0; y < 72; y++) {
                Position p = new Position(x, y);

                ReglePlacement regle = new ReglePlacement();
                if(regle.validate(plateau,tuile,p)) dispo.add(p);
            }
        }
        return dispo;
    }
}
