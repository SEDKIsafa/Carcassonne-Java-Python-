import websocket
import sys
import threading
import time
import os
import subprocess

class ArbitreProxy:
    def __init__(self, url):
        self.url = url
        self.id = "Arbitre"
        self.ws = None
        self.connected = False
        
        # On lance le programme Java en arrière-plan et on connecte ses entrées/sorties
        print("[ARBITRE] Démarrage du moteur Java...")
        self.java_process = subprocess.Popen(
            ["java", "-classpath", "../classes", "game.MainReseau"],
            # entrees sortie  
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            #erreurs 
            stderr=sys.stderr,
            text=True,
            bufsize=1
        )
 
    def on_message(self, ws, message):
        # Quand le réflecteur envoie un message, on le transfere en java
        #si le process n'est pas fini 
        if self.java_process and self.java_process.poll() is None:
            self.java_process.stdin.write(message + "\n")
            self.java_process.stdin.flush()

    def on_error(self, ws, err):
        print(f"[ARBITRE] Erreur WebSocket: {err}")

    def on_close(self, ws, close_status_code, close_msg):
        self.connected = False
        print("[ARBITRE] Déconnecté du réflecteur.")
        #arrete le process si on se deconnecte 
        if self.java_process:
            self.java_process.terminate()
        os._exit(0)

    def on_open(self, ws):
        self.connected = True
        print(f"[ARBITRE] Connecté au Réflecteur en tant que {self.id}")
        # On envoie le message d'entrée obligatoire
        self.ws.send("Arbitre ENTERS")

    # Thread qui écoute Java et transfert au réflecteur 
    def lire_sortie_java(self):
        while self.connected and self.java_process.poll() is None:
            ligne = self.java_process.stdout.readline()

            #aucun message 
            if not ligne:
                break
            
            ligne = ligne.strip()
            
            # Si la ligne commence par MSG, c'est un ordre à envoyer sur le réseau
            if ligne.startswith("MSG:"):
                # On enlève "MSG: "
                msg_a_envoyer = ligne[4:].strip() 
                self.ws.send(msg_a_envoyer)
            # sinon message java classique 
            else:
                print(f"[MESSAGE JAVA] {ligne}")

    def run(self):
        # Connexion WebSocket
        self.ws = websocket.WebSocketApp(self.url,
                                        on_message=self.on_message,
                                        on_error=self.on_error,
                                        on_close=self.on_close,
                                        on_open=self.on_open)

        # Lancement du WebSocket dans un thread
        wst = threading.Thread(target=self.ws.run_forever)
        wst.daemon = True
        wst.start()

        # Attendre que la connexion soit établie
        while not self.connected:
            time.sleep(0.1)

        # Lancer le thread qui écoute le programme Java
        java_thread = threading.Thread(target=self.lire_sortie_java)
        java_thread.daemon = True
        java_thread.start()

        # Boucle principale arbirtre
        try:
            while self.connected and self.java_process.poll() is None:
                time.sleep(1)
        except KeyboardInterrupt:
            print("\n[ARBITRE] Interruption clavier, fermeture...")
         
        # arret du programme 
        finally:
            if self.ws:
                self.ws.close()
            if self.java_process:
                self.java_process.terminate()

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python3 -m core.arbitre_reseau ws://localhost:3000")
        sys.exit(1)
    
    proxy = ArbitreProxy(sys.argv[1])
    proxy.run()