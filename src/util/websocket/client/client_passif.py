import websocket
import sys

def on_message(wsapp, message):
    """
    Called when we receive a message 
    :param ws: web socket app
    :param message: the sended message
    """
    print(f"{message}")

def on_error(wsapp,err):
    """
    Called when we receive an error
    
    :param ws: web socket app
    :param err: the error
    """
    print(f"{err}")

def on_close(wsapp,close_status_code, close_msg):
    """
    Called when we close a connexion
    
    :param wsapp: web socket app
    :param close_status_code: the close status code
    :param close_msg: the close message
    """
    print(f"Close status code: {close_status_code}")
    print(f"Close Message: {close_msg}")

def on_open(wsapp):
    """
    Called when we open a connexion
    
    :param wsapp: web socket app
    """
    print("-------------------------------------------------")
    print("You are now connected with the reflector")
    print("You can't talk because you are in passive mod.")
    print("-------------------------------------------------")

def create_passive_client(url):
    """
    Will create a passive client
    
    :param url: the url to create the client
    """
    wsapp = websocket.WebSocketApp(url,
                                   on_message= on_message,
                                   on_error= on_error,
                                   on_close= on_close,
                                   on_open= on_open)
    wsapp.run_forever()
    
# main
if  __name__ == "__main__":
    #on doit avoir nom_fic + url reflecteur
    if len(sys.argv) !=2:
        print("Nombre d'arguments incorrect")
        print("Usage: python3 client_passif.py <url>")
        sys.exit(1)
    
    reflector = sys.argv[1]
    print(f"Connexion to: {reflector}")

    try:
        create_passive_client(reflector)
    except Exception as e:
        print(f"Error: {e}")

#python3 websocket/client/client_passif.py ws://localhost:3000