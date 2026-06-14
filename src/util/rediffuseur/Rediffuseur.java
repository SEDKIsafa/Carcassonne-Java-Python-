package util.rediffuseur;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class Rediffuseur {

    private static BufferedReader bufferedReader;

    public static String readLine(String file) {
        try {
            // on ouvre une seule fois le fichier
            if (bufferedReader == null) {
                bufferedReader = new BufferedReader(new FileReader(file));
            }

            // on lit la prochaine ligne du fichier
            String line = bufferedReader.readLine();

            if (line == null) {
                bufferedReader.close();
                bufferedReader = null;
            }
            return line;

        } catch (Exception e) {
            System.out.println("An error occurred while reading the file.");
            e.printStackTrace();
            return null;
        }
    }
  
    // lecture du fichier + rediffusion
    private static void rediffuserFichier(String fichier, WebSocket ws) {

        String line;

        while ((line = readLine(fichier)) != null) {

            String msg = line.trim(); // enlever les espaces inutiles

            // Ignorer lignes vides et commentaires
            if (msg.isEmpty() || msg.startsWith("#")) {
                continue;
            }

            // Envoi au réflecteur
            ws.sendText(msg, true).join();
            System.out.println("Envoyé : " + msg);
        }
    }

    public static void main(String[] args) {

        // vérification des arguments
        if (args.length != 2) {
            System.out.println("Utilisation : java Rediffuseur <url> <fichier>");
            System.out.println("Exemple    : java Rediffuseur ws://localhost:3000 partie.log");
            System.exit(1);
        }

        // récupération des arguments
        String urlReflecteur = args[0];
        String fichier = args[1];

        // connexion au réflecteur
        HttpClient client = HttpClient.newHttpClient();
        WebSocket ws = client.newWebSocketBuilder()
                .buildAsync(URI.create(urlReflecteur), new WebSocket.Listener() {

                    @Override
                    public void onOpen(WebSocket webSocket) {
                        webSocket.request(1);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        webSocket.request(1);
                        return WebSocket.Listener.super.onText(webSocket, data, last);
                    }
                })
                .join();

        System.out.println("Connecté au réflecteur : " + urlReflecteur);

        // rediffusion
        rediffuserFichier(fichier, ws);

      
        System.out.println("Rediffusion terminée.");
    }
}
