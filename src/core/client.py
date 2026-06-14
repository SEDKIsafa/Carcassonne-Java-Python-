import websocket
import sys
import threading
import time
import os
from core.message import Message

class WebSocketClient:
    def __init__(self,url,identifiant):
        self.url = url
        self.id = identifiant
        self.ws = None                  #websocket du client    
        self.reception_thread = None    #thread de reception
        self.connected = False  
        self.compteur_rang = 0          #rang des messages
        self.plateau_local = {}         #dictionnaire pour stocker le plateau
        self.valid_moves = {}           # liste des coups valides par tuile  
        self.valid_meeples ={}         # liste des segments valides pour poser un meeple  
        
        # gestion du menu interactif
        self.is_my_turn = False
        self.current_tile = ""
        self.active_player = ""

    # =========================================================
    # FONCTION DE DESSIN DU PLATEAU
    # =========================================================
    def afficher_plateau(self):
        """
        Affiche le plateau de jeu en console à partir des tuiles reçues.
        Dessine une grille dynamique qui s'adapte à la taille des tuiles posées.
        """

        # si plateau vide on fait rien
        if not self.plateau_local:
            return
            
        print("\n-----------------------------------")
        print("           PLATEAU DE JEU            ")
        print("-----------------------------------")
        
        # Trouver les limites du plateau
        xs = []
        ys = []

        # On parcourt toutes les positions (x, y) dans notre dictionnaire
        for pos in self.plateau_local.keys():
            xs.append(pos[0])
            ys.append(pos[1])
            
        # limites plateau
        min_x, max_x = min(xs), max(xs)
        min_y, max_y = min(ys), max(ys)

        largeur_cellule = 22 
        
        # dessiner l'en-tête des colonnes
        header = "      "
        for x in range(min_x, max_x + 1):
            # centrer le numero de cellule
            header += str(x).center(largeur_cellule)
        print(header)
        
        #separateurs
        sep = "   " + "-" * (largeur_cellule * (max_x - min_x + 1) + 3)
        print(sep)
        
        for y in range(max_y, min_y - 1,-1):
            # chaque num de ligne sur 2 espaces
            ligne = f"{y:2d} |"
            
            # parcours des x
            for x in range(min_x, max_x + 1):
                if (x, y) in self.plateau_local:
                    tuile = self.plateau_local[(x, y)]
                    ligne += tuile.center(largeur_cellule)
                else:
                    # si pas de tuile -> espace 
                    ligne += " ".center(largeur_cellule)
            
            print(ligne)
            print(sep)
            
        print()
    
    # =========================================================
    # Menu interactif
    # =========================================================
    def touner_tuile(self, tuile, direction):
        """ 
        Fait tourner la représentation réseau de la tuile  
        """
        partie = tuile.split(":")
        bords = partie[0].split("-")
        #si on a deux parties, on a une abbaye 
        abbaye = ":" + partie[1] if len(partie) > 1 else ""

        # N,E,S,O a O,N,E,S donc 0,1,2,3 -> 3,0,1,2
        if direction == "droite":
            nouveau_bords = [bords[3], bords[0], bords[1], bords[2]]

        # N,E,S,O a E,S,O,N donc 0,1,2,3 -> 1,2,3,0
        elif direction == "gauche":
            nouveau_bords = [bords[1], bords[2], bords[3], bords[0]]

        # pas changement  
        else:
            nouveau_bords = bords

        return "-".join(nouveau_bords) + abbaye

    def print_action_menu(self):
        """ 
        Affiche le menu interactif pour le joueur 
        """
        print(f"=> C'EST À VOUS DE JOUER !")
        print(f"Tuile en main : {self.current_tile}")
        print("Que voulez-vous faire ?")
        print("  1 - Tourner à gauche")
        print("  2 - Tourner à droite")
        print("  3 - Poser la tuile")
        print("---------------------------------------------------------")
        #re affichage prompt 
        print(f"{self.id}-> ", end="", flush=True)

    def cacher_mots_cles(self, keyword):
        """
        Vérifie si un mot-clé doit être caché de l'écran du joueur.
        """
        mots_cles_caches = ["BOARD_START", "TUILE", "BOARD_END", "DISPLAY", "VALID_MOVES", "VALID_MEEPLES", "OFFERS", "PLACES", "SCORES", "BLAMES"]
        return keyword in mots_cles_caches


    def traiter_valid_moves(self, params):
        """ 
        Décode les mouvements valides 
        """
        representation_tuile = params[0]
        positions = params[1:]
        self.valid_moves[representation_tuile] = positions

    def traiter_valid_meeples(self, params):
        """ 
        Décode les meeples valides envoyés en un seul message.
        Format : representation pos1 liste_meeple1 pos2 liste_meeple2,...
        Ex: ['c-f-f-f', '35:36', 'c', 'f0', '37:36', 'none']
        """
        representation_tuile = params[0]
        data = params[1:]
        
        self.valid_meeples[representation_tuile] = {}
        cordo_actuelle = None
        
        # On decode le champ data
        for mot in data:
            
            # coordonne -> 35:36 
            if ":" in mot:
                cordo_actuelle = mot
                self.valid_meeples[representation_tuile][cordo_actuelle] = []

            # liste meeple -> c f r ... 
            elif cordo_actuelle is not None:
                if mot != "none":
                    self.valid_meeples[representation_tuile][cordo_actuelle].append(mot)
                    
        # suppression doublons
        for coord in self.valid_meeples[representation_tuile]:
            self.valid_meeples[representation_tuile][coord] = list(set(self.valid_meeples[representation_tuile][coord]))
    
    # =========================================================
    # LOGIQUE CLIENT
    # =========================================================
    def on_message(self,ws,message):
        """
        Called when we receive a message from the reflector
        :param ws: web socket app
        :param message: the sended message
        """
        self.compteur_rang += 1
        # on transforme le message en objet message
        formated_msg = Message.create_message(self.compteur_rang,message)

        #si on a un message
        if formated_msg:
            if formated_msg.source == self.id and formated_msg.keyword == "LEAVES":
                self.connected = False
                self.ws.close()
                return
            
            # ignore les messages serveur pour le client
            if not self.cacher_mots_cles(formated_msg.keyword):
                print(f"\rNew Message : {formated_msg}")
            
            ###############################################
            #         LOGIQUE  CARCASSONNE                #
            ###############################################

            # reset plateau
            if formated_msg.keyword == "BOARD_START":
                self.plateau_local.clear()
                self.valid_moves.clear()
                self.valid_meeples.clear()

            # reception d'une tuile
            elif formated_msg.keyword == "TUILE":
                if len(formated_msg.params) >= 2:
                    cord = formated_msg.params[0].split(":")
                    if len(cord) == 2:
                        x, y = int(cord[0]), int(cord[1])
                        self.plateau_local[(x, y)] = formated_msg.params[1]

            # affichage plateau une fois reception complete
            elif formated_msg.keyword == "BOARD_END":
                self.afficher_plateau()
                
            # MAJ des coups valides
            elif formated_msg.keyword == "VALID_MOVES":
                self.traiter_valid_moves(formated_msg.params)
                
            #MAJ des meeples  
            elif formated_msg.keyword == "VALID_MEEPLES":
                self.traiter_valid_meeples(formated_msg.params)

            # tuile proposé par l'arbitre 
            elif formated_msg.keyword == "OFFERS":

                tuile = formated_msg.params[0]
                joueurs = formated_msg.params[1:]

                self.active_player = joueurs[0] 

                print("\n----------------------------------------------------------------------------------------")
                print(f"L'arbitre a pioché la tuile : {tuile}")
                
                #si on est le joueur courant 
                if self.id in joueurs:
                    self.is_my_turn = True
                    self.current_tile = tuile
                    self.print_action_menu()
                else:
                    self.is_my_turn = False
                    print(f"C'est au tour de {', '.join(joueurs)} de jouer...")
                
            #  cas d'un blame
            elif formated_msg.keyword == "BLAMES":
                
                rang_faute = formated_msg.params[0]
                raison = formated_msg.params[1] if len(formated_msg.params) > 1 else "Inconnue"

                print(f"\n [BLAME] L'arbitre a refusé votre message n°{rang_faute} pour la raison : {raison}")
                print("Veuillez proposer un autre placement.\n")

                if self.active_player == self.id:
                    self.is_my_turn = True
                    self.print_action_menu()

            # cas d'un score 
            elif formated_msg.keyword == "SCORES":
                
                rang_score = formated_msg.params[0]
                points = formated_msg.params[1] if len(formated_msg.params) > 1 else "0"

                print(f"\n[SCORE] Votre coup n°{rang_score} est validé ! Points gagnés : {points}\n")

            # cas d'un affichage plateau
            elif formated_msg.keyword == "DISPLAY":
                print(f"\n [PLATEAU] Début d'un nouveau tour.\n")

        else:
            print(f"\rLe message ({message}) n'a pas la bonne forme")

    
    def on_error(self,ws,err):
        """
        Called when we receive an error
        
        :param ws: web socket app
        :param err: the error
        """
        print(f"Error: {err}")

    def on_close(self,ws,close_status_code,close_msg):
        """
        Called when we close a connexion
        
        :param ws: web socket app
        :param close_status_code: the close status code
        :param close_msg: the close message
        """
        self.connected = False
        print("DISCONECTED")
        print(f"Close status code: {close_status_code}")
        print(f"Close Message: {close_msg}")

    def on_open(self,ws):
        """
        Called when we open a connexion in active mod
        
        :param ws: web socket app
        """
        self.connected = True 
        print(f"Connected to {self.url} with the id: {self.id}")
        print("-------------------------------------------------")

        #on doit envoyer directement le message ENTERS en tant que client actif
        self.send_message("ENTERS",[])
    
    def send_message(self,keyword,params):
        """
        Will define the message to send
        :param keyword: the chosen keyword
        :param params: the params of the keyword 
        """
        if not self.connected:
            print("Error not connected")
            return
        
        param_list = " ".join(params)
        message = f"{self.id} {keyword} {param_list}".strip()

        try:
            self.ws.send(message)

        except Exception as e:
            print(f"Error: {e}")

    def connect(self):
        """
        Will start the connexion
        """
        self.ws = websocket.WebSocketApp(self.url,
                                        on_message= self.on_message,
                                        on_error= self.on_error,
                                        on_close= self.on_close,
                                        on_open= self.on_open)
        # Lancer le thread reseau
        self.reception_thread = threading.Thread(target=self.ws.run_forever)
        self.reception_thread.daemon = True # ne pas attendre le thread pour fermer le programme
        self.reception_thread.start()

        # 5 secondes pour essayer de se connecter
        timeout = 5
        start = time.time()
        while not self.connected and time.time() - start < timeout:
            print("Connection...")
            time.sleep(1)
        
        return self.connected

    ''' utile plus tard avec la logique du jeu
    def apply_message(self, message: Message):
        """
        Will apply the message logic depending on the keyword
        
        :param message: The message to apply
        """
        si ENTERS -> 
        si PLACES ->
        etc
    '''
    def input_thread(self):
        """
        Thread used to send the messages
        """
        while self.connected:
            try:
                #saisie utilisateur
                user_input = input()
                
                #si utilisateur ban ou deco on arrete la connexion
                if not self.connected:
                    break
                
                #on reaffiche a l'utilisateur qu'il peut envoyer des messages
                if not user_input.strip():
                    if not self.is_my_turn:
                        print(f"{self.id}-> ", end="", flush=True)
                    continue

                #quit pour leave
                if user_input.lower() == "quit":
                    self.send_message("LEAVES",[])
                    print("Deconnexion")
                    self.connected = False
                    self.ws.close()
                    break
                
                #gestion menu interactif 
                if self.is_my_turn:
                    choix = user_input.strip().lower()
                    
                    # tourner a gauche 
                    if choix == "1":
                        self.current_tile = self.touner_tuile(self.current_tile, "gauche")
                        self.print_action_menu()
                        
                    # tourner a droite 
                    elif choix == "2":
                        self.current_tile = self.touner_tuile(self.current_tile, "droite")
                        self.print_action_menu()
                        
                    # poser 
                    elif choix == "3":
                        # On récupère les positions valides pour l'orientation actuelle
                        moves = self.valid_moves.get(self.current_tile, [])
                        
                        # si pas de move dispo, on est pas dans le bon sens 
                        if not moves:
                            print("\nImpossible de poser la tuile dans ce sens !")
                            print("\n Tournez la tuile !")
                            self.print_action_menu()
                            continue
                        
                        print(f"\nVoici la lsite des positions valides: {', '.join(moves)}")
                        cordo = input("Entrez les coordonnées (x:y) : ").strip()

                        #/////////////////
                        # LOGIQUE MEEPLE
                        #///////////////// 

                        meeples_dispos = []

                        #recuperer les meeples dispo 
                        if self.current_tile in self.valid_meeples and cordo in self.valid_meeples[self.current_tile]:
                            meeples_dispos = self.valid_meeples[self.current_tile][cordo]

                        # on a plus de meeple dispo
                        if not meeples_dispos:
                            print("\n Aucun meeple ne peut être placé sur cette position (Zone occupée ou stock épuisé).")
                            meeple = "none"

                        #afficher tout les segments ou on peut placer des meeples 
                        else:
                            print(f"\nLes segments disponibles sont : {', '.join(meeples_dispos)}")
                            meeple = input("Entrez le segment du meeple (ou tapez 'none') : ").strip()

                        if not meeple or meeple.lower() == "none":
                            meeple = "none"
                        
                        # on place la tuile
                        self.send_message("PLACES", [self.current_tile, cordo, meeple])
                        self.is_my_turn = False
                    else:
                        print("Commande non reconue. Merci de taper 1,2 ou 3")
                        self.print_action_menu()
                    continue 
                # =====================================================
                
                #envoie simple (si on est spectateur ou hors tour)
                input_parts = user_input.split()
                keyword = input_parts[0]
                params = input_parts[1:]
                self.send_message(keyword,params)

            except Exception as e:
                break
            
    def run(self):
        """
        main program
        Will start the inputThread 
        And take survey of the connection of the user
        """
        #keyboard thread
        input_thread = threading.Thread(target=self.input_thread)
        input_thread.daemon = True 
        input_thread.start()
        
        #on check si le client est toujours connecté (toute les 0.5 secondes)
        try:
            while self.connected:
                time.sleep(0.5)
        except KeyboardInterrupt:
            print("\nKeyboard Interuption (CTRL + C)")
            self.ws.close()
        #kill le input bloquant
        finally:
            os._exit(0) 

#main
if  __name__ == "__main__":
    if len(sys.argv) !=3:
        print("Nombre d'arguments incorrect")
        print("Usage: python3 -m core.client <url> <identifiant>")
        print("Example: python3 -m core.client ws://localhost:3000 Alice")
        sys.exit(1)

    #recup arguments
    url = sys.argv[1]
    identifiant = sys.argv[2]

    client = WebSocketClient(url,identifiant)
    # si on peut se connecter, on lance boucle d'envoie
    if client.connect():
        client.run()
    else:
        print("Impossible to connect to the server")