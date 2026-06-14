import asyncio
import sys
import websockets

from util.enregistreur.persistance import Persistance


async def enregistrer(url: str, fichier: str):
    # On ouvre la persistance (ici mode "w" => écrase au début)
    with Persistance(fichier, mode="w") as p:
        try:
            async with websockets.connect(url) as websocket:
                print("Connecté au réflecteur :", url)

                async for message in websocket:
                    print("Reçu :", message)

                    # Ici, Enregistreur ne s'occupe plus de "comment écrire" -> c'est Persistance
                    p.ecrire_ligne(message)

                    # Arrêt si CLOSES
                    mots = message.split()
                    if len(mots) >= 2 and mots[1] == "CLOSES":
                        print("Fin de la partie détectée.")
                        break

        except Exception as e:
            print("ERREUR :", repr(e))

    print("Enregistrement terminé.")


# ----------- PROGRAMME PRINCIPAL -----------
if len(sys.argv) != 3:
    print("Utilisation : python Enregistreur.py <url> <fichier>")
    sys.exit(1)

url_reflecteur = sys.argv[1]
nom_fichier = sys.argv[2]

asyncio.run(enregistrer(url_reflecteur, nom_fichier))
