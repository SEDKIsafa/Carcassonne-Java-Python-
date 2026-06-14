Lancement principal :

1. Compiler le projet :
   make

2. Lancer l'interface :
   make run-gui

Lanceur graphique :
- un ecran d'accueil permet maintenant de choisir :
  local partage
  reseau
- en mode reseau, on choisit aussi le joueur et l'URL du reflecteur

Mode actuel :
- deux fenetres s'ouvrent, une pour Alice et une pour Bob
- les deux fenetres partagent exactement la meme partie locale
- seul le joueur dont c'est le tour peut agir

Commandes dans chaque fenetre :
- tourner la tuile a gauche ou a droite
- cliquer sur une case verte pour selectionner une position valide
- poser la tuile, avec ou sans meeple

Remarque :
- cette interface pilote la logique locale Java existante
- elle ne remplace pas la partie reseau, l'enregistreur ou le rediffuseur

Mode reseau :
- une seconde interface graphique existe pour se connecter au reflecteur
- elle suit le protocole deja utilise par le client Python
- lancement direct possible aussi :
  make run-gui-network-alice
  make run-gui-network-bob

Pre-requis mode reseau :
- lancer le reflecteur
- lancer l'arbitre reseau
- puis ouvrir les fenetres Alice et Bob
