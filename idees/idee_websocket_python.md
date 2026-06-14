# Websocket en python

## Reflecteur
- Doit travailler avec le reflecteur, car il va redistribuer les messages au clients connectés
- Le websocket va permettre d'envoyer et de recevoir des messages, tous en meme temps, selon un ordre de messages bien précis.
- Les messages recpectent un protocole, ils doivent etre bien formés. C'est a dire constitués de lettres minucules,majuscules,chiffres ou signes  _-.:/. Ils peuvent egalement contenir des espaces, \t,\r,\n.
- Les messages bien formés contiennent au moins deux mots. Un identifiant, et un mot clé
- Le reflecteur reconnait des messages de controles qui permettent d'effectuer des actions.
- Tout message est repeté par le reflecteur

## Resumé des fonctionnalités 
Afin de gerer correcteument la logique du websocket, on doit avoir plusieur fonctionnalités.

### Les communications
Dans les communications on va gerer plusieur chose:
- Le client peut se connecter ou se deconnecter
- Il peut envoyer des messages, qui seront analysés et transmis par le reflecteur
- Si le message est OK, alors le reflecteur transmet
- Si le message est pas OK, alors on traite les erreurs

### Le protocole
Le protocole defini la maniere dont les messages vont etre traités.
- Format des messages: Si le format est correct on transmet, sinon on agit.
- Mise en forme des messages -> Chaque message doit etre de la forme : identifiant, mot clé, parametre du mot clé
- Mot clé ENTERS/LEAVE/EXPELS ont un impact sur les identifiants

### Messages de controle
- On doit pouvoir utiliser des messages de controles donnés dans le sujet
- Gestion du rang des messages
- Gestion des identifiants

## Type de client
On a trois type de client pour les websocket

### Client passif - Va observer la partie
- Il peut se connecter au reflecteur
- Il peut observer les messages
- Il ne peut pas participer au conversations

### Client actif
- Il rentre dans la conversation avec ENTERS
- Il peut envoyer des messages avec identifiant atribué
- Si LEAVE -> deconnexion

### Multi client
- Plusieur identifiants sur le meme "compte"
- Rang dans les messages
- Si un client se connecte après les autres, il a quand meme l'historique de la conversation

## Idées diverses pour la mise en oeuvre

### messages
- Pour le format des messages, on peut utiliser des regex. Facile en Haskell, voir le fonctionnement en python
- Avoir minimum identifiant + mot-clé pour chaque message, sinon erreur
- Les composantes des messages sont séparé par un espace grace au reflecteur

### identifiants
- On a une liste d'identifiant grace a ENTERS
- Plusieur ENTERS -> plusieur compte pour le meme client
- Si on utilise un identifiant interdit -> deconnexion

### thread
- Comme vu en reseau, on devra mettre en place des thread pour gerer les clients du websocket

## Architecture des fichiers
- client_websocket.py -> gestion des clients
- protocole.py -> protocle qui va valider les messages
- reflecteur -> fournis
- control_message.py -> definie la liste des messages de controle et leurs fonctionnalités

## Similarités et différences avec chat java en Reaseau
- Il faut coder uniquement le client , car le reflecteur sert de serveur
- Pour le format des messages, on utilise maintenant: id mot_cle param1 param2 ... paramN

# Plan pour realiser websocket
1) faire le client passif, on se connecte au reflecteur, et on receptione messages
2) faire le client actif, idem mais avec envoie de messages. Il faut d'abord envoyer ENTERS + identifiant
3) inclure le respect du protocole de messages. Donc envoie avec verif + si erreur alors deco
4) inclure enregistrement des messages