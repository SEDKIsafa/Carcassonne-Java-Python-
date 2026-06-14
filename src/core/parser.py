from __future__ import annotations
from typing import Optional

from core.message import Message

def parse_line(raw: str, rang: int = 0, allow_comments: bool = True) -> Optional[Message]:
    """
    Parse OFFICIEL du projet.

    - raw : la ligne brute reçue (depuis WebSocket ou depuis un fichier)
    - rang : numéro du message 
    - allow_comments : si True, ignore les lignes vides et celles qui commencent par '#'

    Retour:
    - un Message si la ligne contient au moins "source keyword ..."
    - None si la ligne est vide / commentaire / invalide
    """
    if raw is None:
        return None

    line = raw.strip() #on enleve les espaces avant et apres 

    # format fichier : lignes vides ou commentaires 
    if allow_comments and (line == "" or line.startswith("#")):
        return None #on ignore la ligne si elle est vide / si c'est un commentaire

    parts = line.split()#on decoupe les lignes en mots
    if len(parts) < 2:
        return None

    source = parts[0]
    keyword = parts[1]
    params = parts[2:]

    return Message(rang, source, keyword, params)
