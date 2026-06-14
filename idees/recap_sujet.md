# Résumé du sujet

- Parties informatisés 
- Joueurs doivent dev leur propre interface et programme de jeu
- Tournois réalités(JCJ) ou vituels 

## Tournois réalité 
- JCJ
- partie en direct suivie par public
- score en fin de partie
- protocole de message possible

## Tournois virtuel
- Jeu avec bot
- Grand nombre de partie accélérés
- score -> accumulation de toute les parties

## Animateur 
- ont un logiciel pour suivre progression joueurs 
- planifie rencontre
- logi pour planifier les rencontre(depend du jeu)
- logi qui enregistre deroulement partie
- logi pour arbitrer le jeu

## Arbitre 
- gère le cas où un joueur pose une tuile à un endroit illégal
- gère le cas où un meeple est posé à un endroit illégal

## Interface graphique 
- Proposition des endroits ou l'on peut poser une tuile(légal)
- Pas de proposition de pose de meeple (le joueur peut poser aux 8 points cardinaux)

## Contrainte technique
- les parties doivent se derouler dans un salon de discussion
- indépendant du jeu (design pattern)
- identifie et gere les programmes connectés 

# Evaluation
- Soutenance, montrer en quoi la solution apportée répond au besoins.
- Mettre en place une démo, et montrer comment la demo peut etre appliquée au tournois
- Convaincre le jury de la fiabilité du projet

# Livrables
- Groupe Gitlab intitulé
- Un dépôt intitulé suivi dont le README.md liste les noms des membres de l’équipe.
- Semaine 2:  outils d’enregistrement , rediffusion, indépendant du jeu, au moins deux languages.
- Séquences de messages, en fichiers séparés dans le format de persistance, et utilisables comme cas de tests ou comme scripts de démonstration.
- Librairie des éléments du jeu. Utilisé par mini 2 programes
votre proposition.
- Les programmes constituant suite logicielle, mini 2 languages
- Semaine 10 ->  rapport rétrospectif
- Semaine 12 ->  soutenance

# Journal de bord
Il faut inclure dans le readme les elements indiqués dans la partie journal de bord du sujet

# Carcassone 
Il faut mettre en place les modalités de partie. Comment les joueurs progressent dans le tournoi, comment celui ci evolue, qui gagne, etc.

## Partie realitée
- Fournir une interface de jeu toute faite pour les utilisateurs
- Fournir une interface minimale et des possibilités de la personaliser.
- Représentation graphique en temps réel pour le public + annotations(info sur les villes, chemins, champs, placements) 
- Arbitre humain avec decisions: placer tuile, avoir vote public, coups valides.
- Outil de vote en temps réel

## Partie virtuelles
- arbitrage auto, departage les programmes de jeu selon choix de modélisation
- affichage -> stats
- le gagnant doit respecter des contraintes(temps limité, tuiles, regles)(design pattern possible)
  
## Règles 

# Architecture technique
## Reflecteur
- Il va agir sur le flux de message
- Depend du websocket ou le serveur attend les connections et diffuse messages a tous ceux connectés.
- reflecteur diffuse tout message bien formé.
- traite les messages de controle effectuant des actions spécifiques.

## Messages bien formés
- mots : minuscules ou majuscules, chiffres, et les signes _-.:/ -> sinon incorect
- blancs : espace, tabulation ’\t’, retour chariot ’\r’, ou retour à la ligne ’\n’ -> sinon mal formé.
- Au moins deux mots.
- Reflecteur sépare mot d'un espace
- premier mot -> id client valide, si pas déclaré par client -> faute protocole
- deuxieme mot -> mot clé
- autre mots -> parametres lié au mot clé
- si mot clé inconnu, on skip

## Format de persistance
- Les messages sont enregistrés dans un fichier texte -> 1 message par ligne
- Ligne avec blanc ou # au debut autorisés

## Faute protocole
- Si faute de protocole -> reflecteur deconnecte le client

## Ordre et rang des messages
- Ordre global de reception des messages -> sequencement de l'arrivé des messages.
- Les actions du jeux sont aussi réalisés selon cet ordre, c'est le message de plus petit rang qui a la prio
- Dès qu'un client se connecte, il a l'historique des messages avant sa connexion.
- Si client arrive dans une partie déjà commencé, il est spectateur, pas joueur.


## Messages de controle
Liste des messages traité par le reflecteur
- Uniquement le premier client à accès au appel sensibles (GRANTS,EXPELS,LEAVES), il a la responsabilité d'établir les restrictions.

## Client passif
- le client est anonyme à sa connexion, ou passif, il peut recevoir message mais pas emmettre.
- Il n'est pas visible par les clients non passifs
- il peut superviser mais pas jouer
  
## Liste des messages de controle
### ENTERS
- Attribuer identifiant choisie à un client.
- Si le identifiant déjà utilisé -> faute protocole
- Un client peut avoir plusieur id, chaque id est une identité

### LEAVES
- Client deconnecté si il ferme WebSocket ou si faute de protocole
- Deconnexion d'un client par reflecteur
- Si utilisé par client, alors il deconecte un de ses id 
- l'id est desactivé, pas supprimé, donc on peut pas le voler

### GRANTS
- Donner les droits a certain id d'utiliser des messages choisis.
- On peut stack les droits accordés au même id
- Si on a pas le droit d'utiliser un message -> faute protocole

### EXPELS

- Bannir un id
- Id toujours associé au client mais plus utilisable. Si utilisation -> faute protocole
- On peut leave pour changer d'id

### CLOSE
- ferme le flux de message et deconnecte tout le monde