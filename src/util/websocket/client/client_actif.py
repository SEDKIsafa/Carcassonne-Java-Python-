import websocket
import sys
import threading
import time

class ClientActif:
    def __init__(self,url,identifiant):
        self.url = url
        self.identifiant = identifiant
        self.ws = None                  #websocket du client
        self.connected = False      
        self.reception_thread = None    #thread pour la reception des messages   

    def on_message(self,wsapp,message):
        """
        Called when we receive a message 
        :param ws: web socket app
        :param message: the sended message
        """
        print(f"{message}")

    def on_error(self,wsapp,err):
        """
        Called when we receive an error
        
        :param ws: web socket app
        :param err: the error
        """
        print(f"{err}")

    def on_close(self,wsapp,close_status_code, close_msg):
        """
        Called when we close a connexion
        
        :param wsapp: web socket app
        :param close_status_code: the close status code
        :param close_msg: the close message
        """
        #deconnexion
        self.connected = False
        print("DISCONECTED")
        print(f"Close status code: {close_status_code}")
        print(f"Close Message: {close_msg}")

    def on_open(self,wsapp):
        """
        Called when we open a connexion in active mod
        
        :param wsapp: web socket app
        """
        #connexion
        self.connected = True 
        print(f"You are now connected to the server with the id: {self.identifiant}")
        print("-------------------------------------------------")

        #on doit envoyer directement le message ENTERS en tant que client actif
        self.send_message("ENTERS")
        self.display_quickstart()

    def display_quickstart():
        """
        Will display the quickstart informations for the user
        """
        print("\nNow that you are connected to the server you can:")
        print("Write a message to send it")
        print("Here is the format -> KEYWORD PARAM1 PARAM2 ...")
        print("Use \'quit\' to leave the chat")
        print("Use \'help\' to get some help on the commands")
        print("-------------------------------------------------")

    def send_message(self,content):
        """
        Will define the send loop
        """
        if not self.connected:
            print("Error not connected")
            return False
        
        message = f"{self.identifiant} {content}"
        try:
            self.ws.send(message)
            print(f"Message envoyé:  {message}")
            return True
        
        except Exception as e:
            print(f"Error: {e}")
            return False

    def connect(self):
        """
        Will start the connexion
        """
        self.ws = websocket.WebSocketApp(self.url,
                                        on_message= self.on_message,
                                        on_error= self.on_error,
                                        on_close= self.on_close,
                                        on_open= self.on_open)
        self.reception_thread = threading.Thread(target=self.ws.run_forever,daemon = True)
        self.reception_thread.start()

        # 5 secondes pour essayer de se connecter
        timeout = 5
        try_number = 0
        while not self.connected and try_number < timeout:
            time.sleep(1)
            try_number += 1
        
        if not self.connected:
            print("Connexion error")
            return False
        self.send_loop()

        return True
    
    def send_loop(self):
        while self.connected:
            try:
                user_input = input()

                #quit pour leave
                if user_input.lower() == "quit":
                    print("Deconnexion")
                    self.send_message("LEAVES")
                    self.ws.close()
                    break
                
                #envoie simple
                else:
                    self.send_message(user_input)
                    
                print(f"{self.identifiant}-> ", end="", flush=True)

            except KeyboardInterrupt:
                print("Interuption")
                break
        
#main
if  __name__ == "__main__":
    if len(sys.argv) !=3:
        print("Nombre d'arguments incorrect")
        print("Usage: python client_actif.py <url> <identifiant>")
        sys.exit(1)

    #recup arguments    
    url = sys.argv[1]
    identifiant = sys.argv[2]

    client = ClientActif(url,identifiant)
    try:
        client.connect()
    except KeyboardInterrupt:
        print("Stop connexion...")