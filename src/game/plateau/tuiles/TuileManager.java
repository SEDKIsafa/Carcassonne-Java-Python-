package game.plateau.tuiles;

import java.util.Map;

import game.RepresentationManager;

/**classe pour le manager de tuiles */
public class TuileManager {
    
    /**empty builder for the tuileManager */
    public TuileManager(){}

    /**
     * Va mettre en place le sac de tuiles melangé
     * On va lire les representations du RepresentationManager pour creer les tuiles 
     * @param representationManager les representations des tuiles avec leur nombre.
     * @return Un sac de 72 tuiles melangés
     */
    public SacTuiles creerSac(RepresentationManager representationManager){
        SacTuiles sac = new SacTuiles();
        representationManager.initTuilesRepresentation();
        Map<String,Integer> representations = representationManager.getTuilesRepresentations();

        //parcours des representation avec recup forme + quantité
        for(Map.Entry<String,Integer> entry : representations.entrySet()){
            //format et quantitée
            String format = entry.getKey();
            int nbTuileACreer = entry.getValue();

            //creation des tuiles selon la quantité a créer
            for(int i = 0; i < nbTuileACreer; i++){
                TuileV2 nouvelleTuile = TuileFactoryV2.buildTuileV2(format);
                sac.addTuile(nouvelleTuile);
            }
        }

        //on melange le sac à la création pour ne pas le melanger à chaque fois
        sac.melanger();

        return sac;
    }
}