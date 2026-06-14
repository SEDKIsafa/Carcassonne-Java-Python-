# persistance.py
# Rôle : gérer l'accès au fichier (écrire une ligne, fermer proprement, etc.)

class Persistance:
    def __init__(self, path: str, mode: str = "w"):
        """
        path : chemin du fichier
        mode :
          - "w"  -> écrase le fichier au début (recommandé pour un nouvel enregistrement)
          - "a"  -> ajoute à la fin (append)
        """
        self.path = path
        self.file = open(path, mode, encoding="utf-8", newline="\n")

    def ecrire_ligne(self, ligne: str) -> None:
        """Écrit une ligne dans le fichier (sans ajouter de double retour)."""
        self.file.write(ligne.rstrip("\r\n") + "\n")
        self.file.flush()

    def fermer(self) -> None:
        """Ferme le fichier."""
        if self.file and not self.file.closed:
            self.file.close()

    # Bonus : permet d'utiliser "with Persistance(...) as p:"
    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, tb):
        self.fermer()
        # False => si une exception arrive, elle n'est pas "avalée"
        return False
