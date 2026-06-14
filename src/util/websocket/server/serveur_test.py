import asyncio
import websockets
import sys

#ensemble des clients 
clients = set()
reflecteur_ws = None

async def client_handler(ws):
    """
    Handler for the connected clients
    
    :param ws: the websocket
    """
    clients.add(ws)
    id_client = id(ws)
    print(f"Le client {id_client} vient de se connecter")
    print(f"Total: {len(clients)} clients")

    try:
        #message bienvenue
        await ws.send(f"Bienvenue Client({id_client})")

        # boucle reception
        async for message in ws:
            print(f"Client({id_client}): {message}")
            if reflecteur_ws :
                await reflecteur_ws.send(message)
            else :
                print("reflecteur non connecté")
            # un espace entre les mots + envoie a tous
            # formated_msg = " ".join(message.split())
            # websockets.broadcast(clients,formated_msg)
            # print(f"-> {formated_msg}")

    #la connexion du client à été fermé 
    except websockets.exceptions.ConnectionClosed:
        print(f"Client: {id_client} disconnected")

    #fermeture de connexion
    finally:
        clients.discard(ws)
        print(f"Deconnexion du client {id_client}")
        print(f"Total: {len(clients)} clients")
        
        
async def bridge_to_reflecteur():
    
    global reflecteur_ws
    
    while True : 
        try : 
            async with websockets.connect("ws://localhost:3000") as ws:
                reflecteur_ws = ws
                
                async for message in ws:
                    if clients:
                        websockets.broadcast(clients,message)
                        
        except Exception as e:
            print(f"Reflecteur injoignable : {e}")
        finally : 
            reflecteur_ws = None
            await asyncio.sleep(3)

async def main(port):
    """
    Will start the server
    
    :param port: The port to start the server
    """
    print("STARTING SERVER")
    print(f"PORT: {port}")  
    print(f"URL: ws://localhost:{port}")
    
    asyncio.create_task(bridge_to_reflecteur())

    #demarer un serveur avec un host et un port
    #https://websockets.readthedocs.io/en/stable/
    async with websockets.serve(client_handler,"0.0.0.0",port) as server:
        
        print("The server is waiting for connections!")
        #attendre indefiniment
        await server.serve_forever()

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Nombre d'arguments incorrect")
        print("Usage: python3 serveur_test.py <port>")
        sys.exit(1)
    
    port = int(sys.argv[1])

    #lancer serv
    try:
        asyncio.run(main(port))
    #ctrl + c
    except KeyboardInterrupt:
        print("SERVER SHUTDOWN")

#python3 websocket/server/serveur_test.py 3000