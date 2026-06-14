# Documentation Des Classes Et Modules

Ce document présente les classes et modules principaux du dépôt, avec une explication simple de leur rôle, de leurs entrées et de leurs sorties. 

## 1. Package `game`

### `Main`
- Rôle : classe principale du jeu en mode local. Elle gère la création de la partie, l’alternance des joueurs, la pioche, la pose des tuiles, la pose éventuelle d’un meeple, l’affichage du plateau et le calcul des scores.
- Entrées : les actions tapées par les joueurs dans la console, le contenu du sac de tuiles, les positions disponibles et les segments choisis.
- Sorties : affichages console du plateau, des tours, des scores immédiats, puis des scores finaux.

### `MainReseau`
- Rôle : version réseau du jeu. Cette classe joue le rôle d’arbitre côté Java et dialogue avec le réflecteur grâce à des messages.
- Entrées : les messages reçus depuis le réseau, les placements proposés par les joueurs, les règles de pose et de score.
- Sorties : des messages envoyés sur le réseau, des validations de coups, des blâmes en cas d’erreur et des scores transmis au protocole.

### `RepresentationManager`
- Rôle : stocke les représentations textuelles des tuiles du jeu et leur quantité. Il sert de base pour construire le sac de tuiles.
- Entrées : aucune entrée utilisateur directe. La méthode `initTuilesRepresentation()` remplit la table avec les représentations codées dans la classe.
- Sorties : une `HashMap<String, Integer>` qui associe une représentation de tuile à son nombre d’occurrences.

## 2. Package `game.joueur`

### `Joueur`
- Rôle : représente un joueur, avec son nom, son score, son nombre de meeples restants et la liste des segments qu’il possède.
- Entrées : le nom du joueur au moment de la création, puis les demandes de pose et de récupération de meeples.
- Sorties : le score du joueur, son nombre de meeples restants et l’état de ses segments occupés.

### `Meeple`
- Rôle : représente un meeple posé sur le plateau. Il relie un joueur à une zone précise du jeu.
- Entrées : un propriétaire (`Joueur`) et un segment (`Segment`) au moment de la création.
- Sorties : le joueur propriétaire et la zone occupée.

## 3. Package `game.listchooser`

### `ListChooser<T>`
- Rôle : interface qui définit une méthode générique de choix d’un élément dans une liste.
- Entrées : un message à afficher et une liste d’éléments.
- Sorties : l’élément choisi.

### `InteractiveListChooser<T>`
- Rôle : implémentation concrète de `ListChooser`. Elle permet à l’utilisateur de choisir un élément dans une liste via la console.
- Entrées : une question et une liste d’objets à afficher.
- Sorties : l’objet sélectionné, ou `null` si l’utilisateur annule.

### `Input`
- Rôle : classe utilitaire pour lire une chaîne ou un entier depuis l’entrée standard.
- Entrées : la saisie clavier de l’utilisateur.
- Sorties : une `String` ou un `int`, ou bien une exception en cas d’entrée invalide.

## 4. Package `game.rules`

### `RegleGenerale`
- Rôle : classe abstraite de base pour les règles du jeu. Elle impose une méthode de validation commune.
- Entrées : un plateau, une tuile et une position.
- Sorties : un booléen indiquant si la règle est validée.

### `RegleScore`
- Rôle : classe abstraite spécialisée pour les règles de score. Elle distingue le score pendant la partie du score de fin de partie.
- Entrées : un plateau, une tuile et une position.
- Sorties : un score immédiat ou un score de fin de partie.

### `ReglePlacement`
- Rôle : vérifie si une tuile peut être posée à une position donnée. Elle contrôle que la case est libre, qu’il existe au moins un voisin et que les bords sont compatibles.
- Entrées : le plateau, la tuile à poser et la position visée.
- Sorties : `true` si la pose est autorisée, `false` sinon.

### `RegleMeeple`
- Rôle : vérifie si un meeple peut être posé sur un segment donné. Elle empêche la pose sur une zone déjà occupée par un autre meeple connecté.
- Entrées : le plateau, la position, le joueur et le segment ciblé.
- Sorties : `true` si la pose du meeple est autorisée, `false` sinon.

### `RuleEngine`
- Rôle : moteur de règles de score. Il regroupe plusieurs règles de scoring et calcule le total des points.
- Entrées : un plateau, une tuile et une position.
- Sorties : un entier représentant le score total au moment du placement ou en fin de partie.

### `ScoreAbbayeV2`
- Rôle : calcule et attribue les points des abbayes dans le modèle `V2`.
- Entrées : le plateau, la tuile contenant l’abbaye et sa position.
- Sorties : un objet `AbbayeScoreResult` contenant le nombre de voisins, le nombre de points, l’état de complétion et le propriétaire du meeple.

### `ScoreRouteV2`
- Rôle : calcule et attribue les points des routes dans le modèle `V2`. La classe parcourt les composantes de route et gère la majorité des meeples.
- Entrées : le plateau, une tuile de la route et sa position.
- Sorties : un objet `RouteScoreResult` contenant l’état de la route, le nombre de tuiles, les points, les joueurs majoritaires et les segments concernés.

### `ScoreVilleV2`
- Rôle : calcule et attribue les points des villes dans le modèle `V2`. Elle prend en compte les tuiles, les blasons, la complétion de la ville et la majorité des meeples.
- Entrées : le plateau, une tuile de la ville et sa position.
- Sorties : un objet `VilleScoreResult` contenant l’état de la ville, le nombre de tuiles, le nombre de blasons, les points et les joueurs concernés.

### `ScoreChampV2`
- Rôle : calcule et attribue les points des champs à la fin de la partie. Elle repère les composantes de champ et les villes complètes adjacentes.
- Entrées : le plateau, une tuile appartenant au champ et sa position.
- Sorties : un objet `ChampScoreResult` contenant le nombre de villes complètes adjacentes, les points et les joueurs majoritaires.

## 5. Package `game.plateau`

### `Plateau`
- Rôle : ancienne version du plateau. Elle stocke des objets `Tuile` dans une `HashMap` et gère une logique plus ancienne du projet.
- Entrées : des tuiles, des positions, des joueurs et des meeples.
- Sorties : l’état du plateau, l’affichage console et les opérations de pose ou de récupération.

### `PlateauV2`
- Rôle : version actuelle du plateau. Elle stocke les `TuileV2`, initialise la tuile de départ et gère l’affichage complet du plateau.
- Entrées : les tuiles à poser, les positions choisies et les meeples des joueurs.
- Sorties : l’état mis à jour du plateau, les possibilités de pose et l’affichage console.

## 6. Package `game.plateau.tuiles`

### `SacTuiles`
- Rôle : représente le sac de tuiles du jeu. Il permet d’ajouter, de mélanger et de piocher des tuiles.
- Entrées : des objets `TuileV2`.
- Sorties : une tuile piochée, le nombre de tuiles restantes ou l’état vide/non vide du sac.

### `Tuile`
- Rôle : ancienne version d’une tuile. Elle s’appuie sur la classe `Orientation` et sur les points cardinaux.
- Entrées : une orientation, un booléen abbaye et un booléen blason.
- Sorties : les bords de la tuile, son état, sa position et son affichage textuel.

### `TuileV2`
- Rôle : version actuelle d’une tuile. Elle stocke ses segments par direction, ses segments globaux, son éventuelle abbaye, son éventuel blason et les meeples placés.
- Entrées : des segments ajoutés selon une direction, un segment d’abbaye, un booléen blason et éventuellement des meeples.
- Sorties : les bords, la représentation texte, la représentation réseau et l’état complet de la tuile.

### `TuileV3`
- Rôle : version intermédiaire ou expérimentale d’une tuile. Elle repose encore sur `Orientation`, mais stocke aussi les segments séparément.
- Entrées : une orientation et un booléen blason.
- Sorties : les segments de la tuile, sa représentation et l’état de son abbaye ou de son meeple.

### `TuileFactory`
- Rôle : ancienne fabrique de tuiles. Cette classe n’est plus au centre du projet et reste incomplète.
- Entrées : une représentation textuelle d’une tuile.
- Sorties : en théorie une `Tuile`, mais l’implémentation actuelle est encore partielle.

### `TuileFactoryV2`
- Rôle : fabrique principale des `TuileV2`. Elle lit une représentation textuelle et construit les segments correspondants.
- Entrées : une chaîne décrivant la tuile.
- Sorties : une `TuileV2` prête à être utilisée dans le jeu.

### `TuileManager`
- Rôle : construit le sac de tuiles à partir des données du `RepresentationManager`.
- Entrées : un `RepresentationManager`.
- Sorties : un `SacTuiles` mélangé contenant les tuiles du jeu.

## 7. Package `game.plateau.tuiles.direction`

### `Direction`
- Rôle : énumération des quatre directions principales du plateau.
- Entrées : aucune.
- Sorties : les valeurs `NORD`, `SUD`, `EST`, `OUEST`.

### `Position`
- Rôle : représente la position d’une tuile sur le plateau avec des coordonnées `x` et `y`.
- Entrées : deux entiers.
- Sorties : une position comparable, affichable et utilisable comme clé dans une `HashMap`.

### `Orientation`
- Rôle : ancienne structure de rotation des tuiles. Elle relie quatre points cardinaux et permet de faire tourner leur contenu.
- Entrées : un point cardinal nord, est, sud et ouest.
- Sorties : une orientation modifiée après rotation, ou l’accès aux quatre points cardinaux.

## 8. Package `game.plateau.tuiles.direction.pointsCardinaux`

### `pointsCardinaux`
- Rôle : classe de base pour représenter un côté de tuile découpé en trois segments.
- Entrées : une liste de segments.
- Sorties : les segments du côté considéré.

### `pointCardinalNord`
- Rôle : spécialisation du point cardinal nord.
- Entrées : les segments affectés à ce côté.
- Sorties : une représentation textuelle du côté nord.

### `pointCardinalEst`
- Rôle : spécialisation du point cardinal est.
- Entrées : les segments affectés à ce côté.
- Sorties : une représentation textuelle du côté est.

### `pointCardinalSud`
- Rôle : spécialisation du point cardinal sud.
- Entrées : les segments affectés à ce côté.
- Sorties : une représentation textuelle du côté sud.

### `pointCardinalOuest`
- Rôle : spécialisation du point cardinal ouest.
- Entrées : les segments affectés à ce côté.
- Sorties : une représentation textuelle du côté ouest.

## 9. Package `game.plateau.tuiles.segment`

### `Segment`
- Rôle : classe de base des segments. Elle représente une zone élémentaire de tuile et peut contenir un meeple.
- Entrées : une représentation textuelle du segment et éventuellement un meeple.
- Sorties : la représentation, le meeple associé et une version texte du segment.

## 10. Package `game.plateau.tuiles.segment.SegmentType`

### `SegmentAbbaye`
- Rôle : segment spécialisé pour une abbaye.
- Entrées : une représentation de type abbaye.
- Sorties : un segment d’abbaye utilisable dans les règles de pose et de score.

### `SegmentChamp`
- Rôle : segment spécialisé pour un champ.
- Entrées : une représentation de type champ.
- Sorties : un segment de champ.

### `SegmentRoute`
- Rôle : segment spécialisé pour une route.
- Entrées : une représentation de type route.
- Sorties : un segment de route.

### `SegmentVille`
- Rôle : segment spécialisé pour une ville.
- Entrées : une représentation de type ville.
- Sorties : un segment de ville.

## 11. Package `core`

### `Message`
- Rôle : représente un message reçu ou envoyé dans le protocole du réflecteur.
- Entrées : un rang, une source, un mot-clé et une liste de paramètres.
- Sorties : un objet structuré contenant les informations du message, ainsi qu’une méthode d’affichage.

### `parser.py`
- Rôle : module de parsing des lignes brutes du protocole. Il ignore les lignes vides ou commentées et transforme une ligne valide en objet `Message`.
- Entrées : une ligne brute, un rang et un booléen indiquant si les commentaires sont autorisés.
- Sorties : un objet `Message` ou `None`.

### `WebSocketClient`
- Rôle : client réseau principal du projet. Il se connecte au réflecteur, reçoit les messages, maintient un plateau local, affiche les coups possibles et guide le joueur via un menu interactif.
- Entrées : l’URL du réflecteur, l’identifiant du client, les messages reçus et les actions tapées au clavier.
- Sorties : des messages envoyés sur la WebSocket, un affichage du plateau et des choix de jeu.

### `ArbitreProxy`
- Rôle : pont entre le réflecteur WebSocket et le moteur Java `MainReseau`. Il relaie les messages entre le réseau et le programme Java.
- Entrées : l’URL du réflecteur et les messages reçus depuis le réseau.
- Sorties : les messages transmis au programme Java ou renvoyés au réflecteur.

## 12. Package `util.rediffuseur`

### `Rediffuseur`
- Rôle : lit un fichier contenant un historique de partie et rediffuse les messages au réflecteur.
- Entrées : l’URL du réflecteur et le chemin du fichier à rejouer.
- Sorties : les messages relancés sur le réseau et des affichages de suivi.

## 13. Package `util.enregistreur`

### `Enregistreur.py`
- Rôle : script qui se connecte au réflecteur et enregistre tous les messages reçus dans un fichier.
- Entrées : l’URL du réflecteur et le nom du fichier de sortie.
- Sorties : un fichier texte contenant l’historique de la partie.

### `Persistance`
- Rôle : gère l’écriture dans le fichier pour l’enregistreur.
- Entrées : le chemin du fichier, le mode d’ouverture et les lignes à écrire.
- Sorties : un fichier correctement rempli et fermé proprement.

### `deux_clients.py`
- Rôle : script de test qui simule deux clients en parallèle pour vérifier l’enregistrement ou le comportement du réseau.
- Entrées : les messages codés dans le script.
- Sorties : des messages envoyés au réflecteur.

## 14. Package `util.websocket`

### `ClientActif`
- Rôle : ancien client actif WebSocket. Il permettait de se connecter au serveur, d’envoyer des messages et de lire les réponses.
- Entrées : une URL et un identifiant.
- Sorties : des messages envoyés au serveur et des affichages console.

### `client_passif.py`
- Rôle : ancien client passif WebSocket. Il écoute simplement les messages sans intervenir.
- Entrées : l’URL du serveur.
- Sorties : les messages affichés dans la console.

### `serveur_test.py`
- Rôle : ancien serveur WebSocket de test qui sert de passerelle entre plusieurs clients et un réflecteur.
- Entrées : le port d’écoute et les messages échangés par les clients.
- Sorties : la diffusion de messages aux clients connectés.

## 15. Classes De Test

### `JoueurTest`
- Rôle : vérifie le comportement de la classe `Joueur`.
- Entrées : des objets `Joueur` de test.
- Sorties : des assertions sur le score et les meeples.

### `MeepleTest`
- Rôle : vérifie la classe `Meeple`.
- Entrées : un joueur et un segment de test.
- Sorties : des assertions sur le propriétaire et la zone.

### `DirectionTest`
- Rôle : vérifie les valeurs de l’énumération `Direction`.
- Entrées : les constantes de l’énumération.
- Sorties : des assertions sur leur présence ou leur ordre.

### `PositionTest`
- Rôle : vérifie l’égalité, le hash et l’affichage des positions.
- Entrées : des objets `Position`.
- Sorties : des assertions de cohérence.

### `OrientationTest`
- Rôle : vérifie le fonctionnement des rotations d’orientation.
- Entrées : des orientations de test.
- Sorties : des assertions sur le contenu des côtés après rotation.

### `PointsCardinauxTest`
- Rôle : vérifie les classes de points cardinaux.
- Entrées : des segments ou des points cardinaux de test.
- Sorties : des assertions sur le contenu ou l’affichage.

### `SegmentTest`
- Rôle : vérifie le comportement de la classe `Segment` et des types de segments.
- Entrées : des segments de test.
- Sorties : des assertions sur la représentation ou la gestion des meeples.

### `SacTuilesTest`
- Rôle : vérifie le fonctionnement du sac de tuiles.
- Entrées : un sac de tuiles de test.
- Sorties : des assertions sur le nombre de tuiles, la pioche et l’état vide.

### `TuileFactoryTest`
- Rôle : vérifie la création des tuiles par la fabrique.
- Entrées : des représentations textuelles de tuiles.
- Sorties : des assertions sur les tuiles créées.

### `TuileManagerTest`
- Rôle : vérifie la création du sac à partir du gestionnaire de tuiles.
- Entrées : un `RepresentationManager`.
- Sorties : des assertions sur la taille et le contenu du sac.

### `TuileV3Test`
- Rôle : vérifie certains comportements de `TuileV3`.
- Entrées : des tuiles construites pour les tests.
- Sorties : des assertions sur les segments, les rotations ou l’abbaye.

### `ReglePlacementTest`
- Rôle : teste la validité des placements de tuiles.
- Entrées : un plateau, des tuiles et des positions de test.
- Sorties : des assertions `true` ou `false` selon les cas.

### `RegleMeepleTest`
- Rôle : teste la pose des meeples selon les règles.
- Entrées : un plateau, un joueur, une position et un segment.
- Sorties : des assertions sur l’autorisation ou l’interdiction de pose.

### `RuleEngineTest`
- Rôle : vérifie que le moteur de règles additionne correctement les différents scores.
- Entrées : des configurations simples de plateau.
- Sorties : des assertions sur les scores calculés.

### `ScoreAbbayeV2Test`
- Rôle : vérifie le calcul des points des abbayes.
- Entrées : des plateaux contenant des abbayes complètes ou incomplètes.
- Sorties : des assertions sur les points attribués.

### `ScoreRouteV2Test`
- Rôle : vérifie le calcul des routes, la majorité, l’égalité et les cas de route incomplète.
- Entrées : des plateaux de test avec routes et meeples.
- Sorties : des assertions sur le nombre de points et l’état des meeples.

### `ScoreVilleV2Test`
- Rôle : vérifie le calcul des villes, avec ou sans blason, ainsi que les cas d’égalité et de fin de partie.
- Entrées : des plateaux de test avec villes et meeples.
- Sorties : des assertions sur les points, les blasons et les majorités.

### `ScoreChampV2Test`
- Rôle : vérifie le calcul des champs en fin de partie.
- Entrées : des plateaux de test contenant des champs et des villes complètes ou incomplètes.
- Sorties : des assertions sur le nombre de villes adjacentes et les points obtenus.

## 16. Remarque 
Le dépôt contient plusieurs générations de classes, en particulier autour des tuiles et du plateau. Les classes les plus importantes pour l’état actuel du projet sont surtout :
- `PlateauV2`
- `TuileV2`
- `TuileFactoryV2`
- `ReglePlacement`
- `RegleMeeple`
- `ScoreAbbayeV2`
- `ScoreRouteV2`
- `ScoreVilleV2`
- `ScoreChampV2`
- `Main`

=>Les anciennes classes comme `Plateau`, `Tuile`, `TuileFactory`, `Orientation` ou `TuileV3` restent utiles pour comprendre l’évolution du projet, mais elles ne correspondent pas à la version finale utilisée dans le jeu courant
