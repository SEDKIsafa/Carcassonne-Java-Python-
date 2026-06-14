package game;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import game.joueur.Joueur;
import game.plateau.PlateauV2;
import game.plateau.tuiles.SacTuiles;
import game.plateau.tuiles.TuileManager;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.Segment;
import game.plateau.tuiles.segment.SegmentType.SegmentAbbaye;
import game.plateau.tuiles.segment.SegmentType.SegmentChamp;
import game.plateau.tuiles.segment.SegmentType.SegmentRoute;
import game.plateau.tuiles.segment.SegmentType.SegmentVille;
import game.rules.RegleMeeple;
import game.rules.ReglePlacement;
import game.rules.ScoreAbbayeV2;
import game.rules.ScoreChampV2;
import game.rules.ScoreRouteV2;
import game.rules.ScoreVilleV2;

public class CarcassonneGUI {
    private static final int BOARD_MARGIN = 1;
    private static final Color COLOR_EMPTY = new Color(248, 244, 232);
    private static final Color COLOR_VALID = new Color(181, 230, 179);
    private static final Color COLOR_SELECTED = new Color(255, 225, 130);
    private static final Color COLOR_FIELD = new Color(126, 176, 56);
    private static final Color COLOR_CITY = new Color(226, 188, 142);
    private static final Color COLOR_ROAD = new Color(247, 246, 238);
    private static final Color COLOR_ABBEY = new Color(219, 205, 175);
    private static final Color COLOR_TILE_STROKE = new Color(84, 65, 48);
    private static final Color COLOR_PREVIEW_BG = new Color(239, 231, 209);
    private static final Color COLOR_UI_BG = new Color(244, 238, 223);
    private static final Color COLOR_PANEL_BG = new Color(250, 247, 239);
    private static final Color COLOR_ACCENT = new Color(118, 78, 54);
    private static final Color COLOR_PLAYER_ONE = new Color(43, 97, 180);
    private static final Color COLOR_PLAYER_TWO = new Color(185, 76, 76);
    private static final Color COLOR_FIELD_LIGHT = new Color(162, 204, 86);
    private static final Color COLOR_CITY_LIGHT = new Color(241, 214, 182);
    private static final Color COLOR_CITY_STONE = new Color(247, 238, 222);
    private static final Color COLOR_CITY_ROOF = new Color(205, 103, 54);
    private static final Color COLOR_CITY_ROOF_ALT = new Color(69, 122, 206);
    private static final Color COLOR_ROAD_EDGE = new Color(180, 180, 170);
    private static final Color COLOR_BOARD_GRID = new Color(222, 214, 192);
    private static final Color COLOR_ROAD_SHADE = new Color(206, 205, 197);
    private static final int CELL_SIZE = 110;

    public static void main(String[] args) {
        launch();
    }

    public static void launch() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            SharedGameState gameState = new SharedGameState();
            PlayerWindow aliceWindow = new PlayerWindow(gameState, 0);
            PlayerWindow bobWindow = new PlayerWindow(gameState, 1);
            gameState.attachWindow(aliceWindow);
            gameState.attachWindow(bobWindow);
            gameState.startAnimationLoop();
            aliceWindow.setLocation(80, 40);
            bobWindow.setLocation(1420, 40);
            aliceWindow.setVisible(true);
            bobWindow.setVisible(true);
            gameState.refreshAll();
        });
    }

    private static final class SharedGameState {
        private final PlateauV2 plateau;
        private final SacTuiles sac;
        private final List<Joueur> joueurs;
        private final ScoreRouteV2 scoreRouteV2;
        private final ScoreAbbayeV2 scoreAbbayeV2;
        private final ScoreVilleV2 scoreVilleV2;
        private final ScoreChampV2 scoreChampV2;
        private final ReglePlacement reglePlacement;
        private final RegleMeeple regleMeeple;
        private final List<PlayerWindow> windows;
        private final StringBuilder journal;

        private int tour;
        private TuileV2 tuileCourante;
        private Position positionSelectionnee;
        private Position lastPlacedPosition;
        private long lastPlacementTimestamp;
        private boolean partieTerminee;

        private SharedGameState() {
            this.plateau = new PlateauV2();
            this.scoreRouteV2 = new ScoreRouteV2();
            this.scoreAbbayeV2 = new ScoreAbbayeV2();
            this.scoreVilleV2 = new ScoreVilleV2();
            this.scoreChampV2 = new ScoreChampV2();
            this.reglePlacement = new ReglePlacement();
            this.regleMeeple = new RegleMeeple();
            this.windows = new ArrayList<>();
            this.journal = new StringBuilder();
            this.tour = 0;
            this.positionSelectionnee = null;
            this.lastPlacedPosition = null;
            this.lastPlacementTimestamp = 0L;
            this.partieTerminee = false;

            this.joueurs = new ArrayList<>();
            this.joueurs.add(new Joueur("Alice"));
            this.joueurs.add(new Joueur("Bob"));

            RepresentationManager representationManager = new RepresentationManager();
            TuileManager tuileManager = new TuileManager();
            this.sac = tuileManager.creerSac(representationManager);

            log("Partie initialisee. La tuile de depart est deja posee au centre.");
            preparerProchainTour();
        }

        private void attachWindow(PlayerWindow window) {
            windows.add(window);
        }

        private Joueur getJoueurCourant() {
            return joueurs.get(tour % joueurs.size());
        }

        private boolean isCurrentPlayer(int playerIndex) {
            return !partieTerminee && playerIndex == (tour % joueurs.size());
        }

        private void rotateCurrentTile(int playerIndex, boolean droite) {
            if (!isCurrentPlayer(playerIndex) || tuileCourante == null) {
                return;
            }

            if (droite) {
                tuileCourante.orienterDroite();
                log(getJoueurCourant().getNom() + " tourne la tuile vers la droite.");
            } else {
                tuileCourante.orienterGauche();
                log(getJoueurCourant().getNom() + " tourne la tuile vers la gauche.");
            }

            positionSelectionnee = null;
            refreshAll();
        }

        private void selectPosition(int playerIndex, Position position) {
            if (!isCurrentPlayer(playerIndex) || tuileCourante == null) {
                return;
            }
            positionSelectionnee = position;
            refreshAll();
        }

        private void placeCurrentTile(PlayerWindow window, boolean withMeeple) {
            if (!isCurrentPlayer(window.getPlayerIndex()) || tuileCourante == null) {
                return;
            }

            if (positionSelectionnee == null) {
                JOptionPane.showMessageDialog(window, "Selectionnez d'abord une case verte sur le plateau.");
                return;
            }

            if (!reglePlacement.validate(plateau, tuileCourante, positionSelectionnee)) {
                JOptionPane.showMessageDialog(window, "Cette position n'est plus valide pour la tuile courante.");
                positionSelectionnee = null;
                refreshAll();
                return;
            }

            Joueur joueurCourant = getJoueurCourant();
            plateau.Poser(tuileCourante, positionSelectionnee);
            lastPlacedPosition = positionSelectionnee;
            lastPlacementTimestamp = System.currentTimeMillis();
            log("Tuile posee en " + positionSelectionnee + " par " + joueurCourant.getNom() + ".");

            if (withMeeple) {
                if (joueurCourant.getMeeplesRestants() <= 0) {
                    log("Aucun meeple disponible pour " + joueurCourant.getNom() + ".");
                } else {
                    Map<String, Segment> choix = segmentsMeepleValides(positionSelectionnee, tuileCourante, joueurCourant);
                    if (choix.isEmpty()) {
                        log("Aucun segment valide pour poser un meeple sur cette tuile.");
                    } else {
                        JComboBox<String> combo = new JComboBox<>(choix.keySet().toArray(new String[0]));
                        int result = JOptionPane.showConfirmDialog(
                            window,
                            combo,
                            "Choisissez un segment pour le meeple",
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE
                        );

                        if (result == JOptionPane.OK_OPTION) {
                            Segment segment = choix.get(combo.getSelectedItem());
                            boolean poseOk = plateau.poserMeeple(positionSelectionnee, joueurCourant, segment);
                            if (poseOk) {
                                log("Meeple pose par " + joueurCourant.getNom() + " sur " + combo.getSelectedItem() + ".");
                            } else {
                                log("La pose du meeple a echoue.");
                            }
                        } else {
                            log("Pose du meeple annulee.");
                        }
                    }
                }
            } else {
                log(joueurCourant.getNom() + " choisit de ne pas poser de meeple.");
            }

            appliquerScoresPlacement();
            tour++;
            preparerProchainTour();
            refreshAll();
        }

        private void appliquerScoresPlacement() {
            if (tuileCourante == null || positionSelectionnee == null) {
                return;
            }

            ScoreRouteV2.RouteScoreResult route = scoreRouteV2.attribuerScorePlacement(plateau, tuileCourante, positionSelectionnee);
            if (route.getPointsRoute() > 0 && !route.getJoueursMajoritaires().isEmpty()) {
                for (Joueur joueur : route.getJoueursMajoritaires()) {
                    log("Route completee : +" + route.getPointsRoute() + " pour " + joueur.getNom() + ".");
                }
            }

            ScoreVilleV2.VilleScoreResult ville = scoreVilleV2.attribuerScorePlacement(plateau, tuileCourante, positionSelectionnee);
            if (ville.getPointsVille() > 0 && !ville.getJoueursMajoritaires().isEmpty()) {
                for (Joueur joueur : ville.getJoueursMajoritaires()) {
                    log("Ville completee : +" + ville.getPointsVille() + " pour " + joueur.getNom() + ".");
                }
            }

            ScoreAbbayeV2.AbbayeScoreResult abbaye = scoreAbbayeV2.attribuerScorePlacement(plateau, tuileCourante, positionSelectionnee);
            if (abbaye.getPointsAbbaye() > 0 && abbaye.getProprietaire() != null) {
                log("Abbaye completee : +" + abbaye.getPointsAbbaye() + " pour " + abbaye.getProprietaire().getNom() + ".");
            }
        }

        private void preparerProchainTour() {
            positionSelectionnee = null;

            if (sac.estVide()) {
                terminerPartie();
                return;
            }

            tuileCourante = sac.piocher();
            log("Nouvelle tuile piochee pour " + getJoueurCourant().getNom() + " : " + tuileCourante);
        }

        private void terminerPartie() {
            if (partieTerminee) {
                return;
            }

            partieTerminee = true;
            tuileCourante = null;
            calculerScoresFinPartie();

            Joueur gagnant = joueurs.get(0);
            for (Joueur joueur : joueurs) {
                if (joueur.getScore() > gagnant.getScore()) {
                    gagnant = joueur;
                }
            }

            log("Fin de partie. Gagnant : " + gagnant.getNom() + " avec " + gagnant.getScore() + " points.");
            for (PlayerWindow window : windows) {
                JOptionPane.showMessageDialog(
                    window,
                    "Fin de partie.\nGagnant : " + gagnant.getNom() + " avec " + gagnant.getScore() + " points.",
                    "Partie terminee",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }

        private void calculerScoresFinPartie() {
            Set<Segment> segmentsDejaScores = Collections.newSetFromMap(new IdentityHashMap<>());

            for (Map.Entry<Position, TuileV2> entry : plateau.getCases().entrySet()) {
                Position position = entry.getKey();
                TuileV2 tuile = entry.getValue();

                if (tuile.hasAbbaye()) {
                    Segment abbaye = tuile.getAbbaye();
                    if (abbaye != null && abbaye.getMeeple() != null && segmentsDejaScores.add(abbaye)) {
                        ScoreAbbayeV2.AbbayeScoreResult resultatAbbaye =
                            scoreAbbayeV2.attribuerScoreFinPartie(plateau, tuile, position);
                        if (resultatAbbaye.getPointsAbbaye() > 0 && resultatAbbaye.getProprietaire() != null) {
                            log("Fin de partie - abbaye : +" + resultatAbbaye.getPointsAbbaye()
                                + " pour " + resultatAbbaye.getProprietaire().getNom() + ".");
                        }
                    }
                }

                for (Segment segment : tuile.getSegments()) {
                    if (segment == null || segment.getMeeple() == null || segmentsDejaScores.contains(segment)) {
                        continue;
                    }

                    if (segment instanceof SegmentRoute) {
                        ScoreRouteV2.RouteScoreResult resultatRoute =
                            scoreRouteV2.attribuerScoreFinPartie(plateau, tuile, position);
                        segmentsDejaScores.addAll(resultatRoute.getSegmentsRouteAvecMeeple());
                        if (resultatRoute.getPointsRoute() > 0) {
                            for (Joueur joueur : resultatRoute.getJoueursMajoritaires()) {
                                log("Fin de partie - route : +" + resultatRoute.getPointsRoute()
                                    + " pour " + joueur.getNom() + ".");
                            }
                        }
                    } else if (segment instanceof SegmentVille) {
                        ScoreVilleV2.VilleScoreResult resultatVille =
                            scoreVilleV2.attribuerScoreFinPartie(plateau, tuile, position);
                        segmentsDejaScores.addAll(resultatVille.getSegmentsVilleAvecMeeple());
                        if (resultatVille.getPointsVille() > 0) {
                            for (Joueur joueur : resultatVille.getJoueursMajoritaires()) {
                                log("Fin de partie - ville : +" + resultatVille.getPointsVille()
                                    + " pour " + joueur.getNom() + ".");
                            }
                        }
                    } else if (segment instanceof SegmentChamp) {
                        ScoreChampV2.ChampScoreResult resultatChamp =
                            scoreChampV2.attribuerScoreFinPartie(plateau, tuile, position);
                        segmentsDejaScores.addAll(resultatChamp.getSegmentsChampAvecMeeple());
                        if (resultatChamp.getPointsChamp() > 0) {
                            for (Joueur joueur : resultatChamp.getJoueursMajoritaires()) {
                                log("Fin de partie - champ : +" + resultatChamp.getPointsChamp()
                                    + " pour " + joueur.getNom() + ".");
                            }
                        }
                    } else if (segment instanceof SegmentAbbaye) {
                        segmentsDejaScores.add(segment);
                    }
                }
            }
        }

        private Map<String, Segment> segmentsMeepleValides(Position position, TuileV2 tuile, Joueur joueur) {
            Map<String, Segment> choix = new LinkedHashMap<>();
            int index = 1;
            for (Segment segment : tuile.getSegments()) {
                if (regleMeeple.validate(plateau, position, joueur, segment)) {
                    choix.put("Segment " + index + " : " + segment, segment);
                }
                index++;
            }
            return choix;
        }

        private List<Position> getPositionsDisponibles() {
            List<Position> dispo = new ArrayList<>();
            if (tuileCourante == null) {
                return dispo;
            }

            for (int x = 0; x < 72; x++) {
                for (int y = 0; y < 72; y++) {
                    Position position = new Position(x, y);
                    if (reglePlacement.validate(plateau, tuileCourante, position)) {
                        dispo.add(position);
                    }
                }
            }
            return dispo;
        }

        private String getJournal() {
            return journal.toString();
        }

        private void log(String message) {
            journal.append(message).append('\n');
        }

        private void refreshAll() {
            for (PlayerWindow window : windows) {
                window.refresh();
            }
        }

        private void startAnimationLoop() {
            Timer timer = new Timer(45, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (lastPlacedPosition != null && System.currentTimeMillis() - lastPlacementTimestamp < 700) {
                        refreshAll();
                    }
                }
            });
            timer.start();
        }
    }

    private static final class PlayerWindow extends JFrame {
        private final SharedGameState gameState;
        private final int playerIndex;
        private final JPanel boardPanel;
        private final JLabel titreLabel;
        private final JLabel tourLabel;
        private final JLabel scoreLabel;
        private final JLabel meeplesLabel;
        private final JLabel sacLabel;
        private final JLabel[] playerMeepleLabels;
        private final JProgressBar[] scoreBars;
        private final TilePreviewPanel tuilePreviewPanel;
        private final JTextArea journalArea;
        private final JButton rotateLeftButton;
        private final JButton rotateRightButton;
        private final JButton poserButton;
        private final JButton poserSansMeepleButton;

        private PlayerWindow(SharedGameState gameState, int playerIndex) {
            super("Carcassonne - " + gameState.joueurs.get(playerIndex).getNom());
            this.gameState = gameState;
            this.playerIndex = playerIndex;

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(new BorderLayout(12, 12));
            setSize(1280, 800);
            getContentPane().setBackground(COLOR_UI_BG);

            this.boardPanel = new JPanel();
            this.boardPanel.setBackground(COLOR_UI_BG);
            this.boardPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            JScrollPane boardScroll = new JScrollPane(boardPanel);
            boardScroll.getVerticalScrollBar().setUnitIncrement(16);
            boardScroll.getHorizontalScrollBar().setUnitIncrement(16);
            boardScroll.setBorder(BorderFactory.createEmptyBorder());
            add(boardScroll, BorderLayout.CENTER);

            JPanel sidePanel = new JPanel();
            sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
            sidePanel.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 12));
            sidePanel.setPreferredSize(new Dimension(340, 700));
            sidePanel.setBackground(COLOR_UI_BG);

            titreLabel = new JLabel("Vue joueur : " + gameState.joueurs.get(playerIndex).getNom());
            titreLabel.setFont(titreLabel.getFont().deriveFont(Font.BOLD, 18f));
            titreLabel.setForeground(COLOR_ACCENT);
            tourLabel = new JLabel();
            scoreLabel = new JLabel();
            meeplesLabel = new JLabel();
            sacLabel = new JLabel();
            playerMeepleLabels = new JLabel[gameState.joueurs.size()];
            scoreBars = new JProgressBar[gameState.joueurs.size()];

            JPanel headerPanel = new JPanel();
            headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
            headerPanel.setBackground(COLOR_PANEL_BG);
            headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(224, 216, 195)),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
            ));
            headerPanel.add(titreLabel);
            headerPanel.add(tourLabel);
            headerPanel.add(scoreLabel);
            headerPanel.add(meeplesLabel);
            headerPanel.add(sacLabel);

            JPanel scorePanel = new JPanel(new GridLayout(0, 1, 6, 6));
            scorePanel.setBackground(COLOR_PANEL_BG);
            scorePanel.setBorder(BorderFactory.createTitledBorder("Barres de score"));
            for (int i = 0; i < gameState.joueurs.size(); i++) {
                JProgressBar bar = new JProgressBar(0, 200);
                bar.setStringPainted(true);
                bar.setForeground(i == 0 ? COLOR_PLAYER_ONE : COLOR_PLAYER_TWO);
                bar.setBackground(new Color(233, 228, 214));
                scoreBars[i] = bar;
                scorePanel.add(bar);
                JLabel meepleInfo = new JLabel();
                playerMeepleLabels[i] = meepleInfo;
                scorePanel.add(meepleInfo);
            }

            tuilePreviewPanel = new TilePreviewPanel();
            tuilePreviewPanel.setBorder(BorderFactory.createTitledBorder("Tuile du tour"));

            rotateLeftButton = new JButton("Tourner a gauche");
            rotateRightButton = new JButton("Tourner a droite");
            poserButton = new JButton("Poser avec meeple");
            poserSansMeepleButton = new JButton("Poser sans meeple");

            rotateLeftButton.addActionListener(e -> gameState.rotateCurrentTile(playerIndex, false));
            rotateRightButton.addActionListener(e -> gameState.rotateCurrentTile(playerIndex, true));
            poserButton.addActionListener(e -> gameState.placeCurrentTile(this, true));
            poserSansMeepleButton.addActionListener(e -> gameState.placeCurrentTile(this, false));

            JPanel controlsPanel = new JPanel(new GridLayout(0, 1, 8, 8));
            controlsPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
            controlsPanel.setBackground(COLOR_PANEL_BG);
            controlsPanel.add(rotateLeftButton);
            controlsPanel.add(rotateRightButton);
            controlsPanel.add(poserButton);
            controlsPanel.add(poserSansMeepleButton);

            journalArea = new JTextArea(16, 24);
            journalArea.setEditable(false);
            journalArea.setLineWrap(true);
            journalArea.setWrapStyleWord(true);
            journalArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            journalArea.setBackground(COLOR_PANEL_BG);
            JScrollPane journalScroll = new JScrollPane(journalArea);
            journalScroll.setBorder(BorderFactory.createTitledBorder("Journal partage"));

            sidePanel.add(headerPanel);
            sidePanel.add(scorePanel);
            sidePanel.add(tuilePreviewPanel);
            sidePanel.add(controlsPanel);
            sidePanel.add(journalScroll);

            add(sidePanel, BorderLayout.EAST);
        }

        private int getPlayerIndex() {
            return playerIndex;
        }

        private void refresh() {
            Joueur joueurLocal = gameState.joueurs.get(playerIndex);
            Joueur joueurCourant = gameState.partieTerminee ? null : gameState.getJoueurCourant();
            boolean monTour = gameState.isCurrentPlayer(playerIndex);

            if (joueurCourant != null) {
                tourLabel.setText(monTour
                    ? "Statut : a vous de jouer"
                    : "Statut : tour de " + joueurCourant.getNom());
            } else {
                tourLabel.setText("Statut : partie terminee");
            }

            StringBuilder scoreBuilder = new StringBuilder("Scores : ");
            for (int i = 0; i < gameState.joueurs.size(); i++) {
                if (i > 0) {
                    scoreBuilder.append(" | ");
                }
                Joueur joueur = gameState.joueurs.get(i);
                scoreBuilder.append(joueur.getNom()).append("=").append(joueur.getScore());
            }
            scoreLabel.setText(scoreBuilder.toString());
            meeplesLabel.setText("Vos meeples : " + joueurLocal.getMeeplesRestants());
            sacLabel.setText("Tuiles restantes : " + gameState.sac.getNombreTuilesRestante());

            for (int i = 0; i < gameState.joueurs.size(); i++) {
                Joueur joueur = gameState.joueurs.get(i);
                scoreBars[i].setValue(Math.min(200, joueur.getScore()));
                scoreBars[i].setString(joueur.getNom() + " : " + joueur.getScore() + " pts");
                playerMeepleLabels[i].setText("Meeples restants " + joueur.getNom() + " : " + joueur.getMeeplesRestants());
            }

            tuilePreviewPanel.setTile(gameState.tuileCourante);
            journalArea.setText(gameState.getJournal());
            journalArea.setCaretPosition(journalArea.getDocument().getLength());

            rotateLeftButton.setEnabled(monTour && gameState.tuileCourante != null);
            rotateRightButton.setEnabled(monTour && gameState.tuileCourante != null);
            poserButton.setEnabled(monTour && gameState.tuileCourante != null);
            poserSansMeepleButton.setEnabled(monTour && gameState.tuileCourante != null);

            reconstruirePlateau(monTour);
        }

        private void reconstruirePlateau(boolean monTour) {
            boardPanel.removeAll();

            List<Position> positionsValides = gameState.getPositionsDisponibles();
            Set<Position> setPositionsValides = Set.copyOf(positionsValides);

            int minX = 36;
            int maxX = 36;
            int minY = 36;
            int maxY = 36;

            for (Position position : gameState.plateau.getCases().keySet()) {
                minX = Math.min(minX, position.getX());
                maxX = Math.max(maxX, position.getX());
                minY = Math.min(minY, position.getY());
                maxY = Math.max(maxY, position.getY());
            }

            for (Position position : positionsValides) {
                minX = Math.min(minX, position.getX());
                maxX = Math.max(maxX, position.getX());
                minY = Math.min(minY, position.getY());
                maxY = Math.max(maxY, position.getY());
            }

            minX -= BOARD_MARGIN;
            maxX += BOARD_MARGIN;
            minY -= BOARD_MARGIN;
            maxY += BOARD_MARGIN;

            int cols = maxX - minX + 1;
            int rows = maxY - minY + 1;
            boardPanel.setLayout(new GridLayout(rows, cols, 4, 4));

            for (int y = maxY; y >= minY; y--) {
                for (int x = minX; x <= maxX; x++) {
                    Position position = new Position(x, y);
                    TuileV2 tuile = gameState.plateau.getTuile(position);
                    TileCellButton cell = new TileCellButton(
                        position,
                        tuile,
                        position.equals(gameState.lastPlacedPosition),
                        gameState.lastPlacementTimestamp
                    );
                    cell.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));

                    if (tuile != null) {
                        cell.setEnabled(false);
                    } else if (setPositionsValides.contains(position) && !gameState.partieTerminee) {
                        cell.setPlacementState(true, position.equals(gameState.positionSelectionnee));
                        if (monTour) {
                            cell.addActionListener(e -> gameState.selectPosition(playerIndex, position));
                        } else {
                            cell.setEnabled(false);
                        }
                    } else {
                        cell.setEnabled(false);
                    }

                    boardPanel.add(cell);
                }
            }

            boardPanel.revalidate();
            boardPanel.repaint();
        }
    }

    private static void drawTile(Graphics2D g2, TuileV2 tuile, int width, int height, boolean preview) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int margin = preview ? 10 : 6;
        int x = margin;
        int y = margin;
        int w = width - 2 * margin;
        int h = height - 2 * margin;

        g2.setColor(new Color(0, 0, 0, 26));
        g2.fillRoundRect(x + 3, y + 4, w, h, 18, 18);
        g2.setPaint(new GradientPaint(x, y, preview ? COLOR_PREVIEW_BG : COLOR_FIELD_LIGHT, x, y + h, COLOR_FIELD));
        g2.fillRoundRect(x, y, w, h, 18, 18);
        g2.setColor(COLOR_TILE_STROKE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(x, y, w, h, 18, 18);
        drawFieldTexture(g2, x, y, w, h);

        drawCityAreas(g2, tuile, x, y, w, h);
        drawRoads(g2, tuile, x, y, w, h);
        drawAbbaye(g2, tuile, x, y, w, h);
        drawBlason(g2, tuile, x, y, w, h);
        drawMeeples(g2, tuile, x, y, w, h);
    }

    private static void drawFieldTexture(Graphics2D g2, int x, int y, int w, int h) {
        g2.setColor(new Color(226, 247, 176, 55));
        for (int i = x + 8; i < x + w; i += 16) {
            g2.drawLine(i, y + 6, i - 10, y + h - 8);
        }
        g2.setColor(new Color(84, 142, 46, 35));
        for (int j = y + 9; j < y + h; j += 14) {
            g2.drawLine(x + 6, j, x + w - 6, j - 5);
        }
        g2.setColor(new Color(84, 126, 38, 80));
        for (int i = x + 10; i < x + w - 8; i += 18) {
            g2.fillOval(i, y + 12 + ((i / 3) % Math.max(1, h - 24)), 3, 3);
        }
        g2.setColor(new Color(73, 110, 33, 60));
        for (int i = x + 12; i < x + w - 10; i += 22) {
            g2.drawArc(i, y + h / 2 + ((i / 2) % Math.max(1, h / 3)), 8, 5, 0, 180);
        }
    }

    private static void drawCityAreas(Graphics2D g2, TuileV2 tuile, int x, int y, int w, int h) {
        Stroke previous = g2.getStroke();
        g2.setStroke(new BasicStroke(1.5f));

        if (hasCity(tuile.getNordSegments())) {
            Polygon p = new Polygon();
            p.addPoint(x, y);
            p.addPoint(x + w, y);
            p.addPoint(x + (int) (w * 0.66), y + h / 3);
            p.addPoint(x + (int) (w * 0.34), y + h / 3);
            drawCityPolygon(g2, p, true, x, y, w, h);
        }

        if (hasCity(tuile.getSudSegments())) {
            Polygon p = new Polygon();
            p.addPoint(x, y + h);
            p.addPoint(x + w, y + h);
            p.addPoint(x + (int) (w * 0.66), y + (int) (h * 0.66));
            p.addPoint(x + (int) (w * 0.34), y + (int) (h * 0.66));
            drawCityPolygon(g2, p, false, x, y, w, h);
        }

        if (hasCity(tuile.getEstSegments())) {
            Polygon p = new Polygon();
            p.addPoint(x + w, y);
            p.addPoint(x + w, y + h);
            p.addPoint(x + (int) (w * 0.66), y + (int) (h * 0.66));
            p.addPoint(x + (int) (w * 0.66), y + (int) (h * 0.34));
            drawCityPolygon(g2, p, null, x, y, w, h);
        }

        if (hasCity(tuile.getOuestSegments())) {
            Polygon p = new Polygon();
            p.addPoint(x, y);
            p.addPoint(x, y + h);
            p.addPoint(x + w / 3, y + (int) (h * 0.66));
            p.addPoint(x + w / 3, y + (int) (h * 0.34));
            drawCityPolygon(g2, p, null, x, y, w, h);
        }

        g2.setStroke(previous);
    }

    private static void drawCityPolygon(Graphics2D g2, Polygon p, Boolean horizontalTopBottom, int x, int y, int w, int h) {
        GradientPaint paint = new GradientPaint(x, y, COLOR_CITY_LIGHT, x + w, y + h, COLOR_CITY);
        g2.setPaint(paint);
        g2.fillPolygon(p);
        g2.setColor(COLOR_TILE_STROKE);
        g2.drawPolygon(p);
        g2.setColor(COLOR_CITY_STONE);
        if (horizontalTopBottom != null) {
            int yBattlement = horizontalTopBottom ? y + 4 : y + h - 8;
            for (int bx = x + 8; bx < x + w - 10; bx += 12) {
                g2.fillRect(bx, yBattlement, 7, 5);
            }
        } else {
            int xBattlementLeft = p.xpoints[0] == x ? x + 4 : x + w - 10;
            for (int by = y + 8; by < y + h - 10; by += 12) {
                g2.fillRect(xBattlementLeft, by, 5, 7);
            }
        }
        drawCityHouses(g2, p.getBounds().x, p.getBounds().y, p.getBounds().width, p.getBounds().height);
    }

    private static void drawCityHouses(Graphics2D g2, int x, int y, int w, int h) {
        int houseCount = Math.max(2, Math.min(6, w / 18));
        for (int i = 0; i < houseCount; i++) {
            int hx = x + 6 + i * Math.max(12, (w - 18) / houseCount);
            int hy = y + 6 + (i % 2) * 6;
            g2.setColor(COLOR_CITY_STONE);
            g2.fill(new RoundRectangle2D.Float(hx, hy + 5, 9, 8, 3, 3));
            int[] roofX = {hx - 1, hx + 4, hx + 10};
            int[] roofY = {hy + 6, hy, hy + 6};
            g2.setColor(i % 2 == 0 ? COLOR_CITY_ROOF : COLOR_CITY_ROOF_ALT);
            g2.fillPolygon(roofX, roofY, 3);
            g2.setColor(new Color(90, 70, 55, 120));
            g2.drawPolygon(roofX, roofY, 3);
        }
    }

    private static void drawRoads(Graphics2D g2, TuileV2 tuile, int x, int y, int w, int h) {
        int centreX = x + w / 2;
        int centreY = y + h / 2;
        Stroke previous = g2.getStroke();
        drawRoadSegment(g2, hasRoute(tuile.getNordSegments()), centreX, centreY, centreX, y + 6, centreX - w / 9, y + h / 4);
        drawRoadSegment(g2, hasRoute(tuile.getSudSegments()), centreX, centreY, centreX, y + h - 6, centreX + w / 9, y + (3 * h) / 4);
        drawRoadSegment(g2, hasRoute(tuile.getEstSegments()), centreX, centreY, x + w - 6, centreY, x + (3 * w) / 4, centreY - h / 10);
        drawRoadSegment(g2, hasRoute(tuile.getOuestSegments()), centreX, centreY, x + 6, centreY, x + w / 4, centreY + h / 10);

        g2.setStroke(previous);
    }

    private static void drawRoadSegment(Graphics2D g2, boolean present, int sx, int sy, int ex, int ey, int cx, int cy) {
        if (!present) {
            return;
        }
        QuadCurve2D curve = new QuadCurve2D.Float(sx, sy, cx, cy, ex, ey);
        g2.setColor(COLOR_ROAD_SHADE);
        g2.setStroke(new BasicStroke(15f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new QuadCurve2D.Float(sx + 1, sy + 2, cx + 1, cy + 2, ex + 1, ey + 2));
        g2.setColor(COLOR_ROAD_EDGE);
        g2.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(curve);
        g2.setColor(COLOR_ROAD);
        g2.setStroke(new BasicStroke(9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(curve);
        g2.setColor(new Color(255, 255, 255, 160));
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(curve);
    }

    private static void drawAbbaye(Graphics2D g2, TuileV2 tuile, int x, int y, int w, int h) {
        if (!tuile.hasAbbaye()) {
            return;
        }

        int bw = w / 3;
        int bh = h / 3;
        int bx = x + (w - bw) / 2;
        int by = y + (h - bh) / 2;

        g2.setColor(new Color(97, 147, 51, 90));
        g2.fill(new Ellipse2D.Float(bx - 8, by + bh - 3, bw + 16, 12));
        g2.setColor(new Color(233, 222, 198));
        g2.fillRoundRect(bx, by + bh / 4, bw, bh - bh / 4, 10, 10);
        int[] roofX = {bx - 4, bx + bw / 2, bx + bw + 4};
        int[] roofY = {by + bh / 4, by - 6, by + bh / 4};
        g2.setColor(new Color(193, 93, 53));
        g2.fillPolygon(roofX, roofY, 3);
        g2.setColor(COLOR_TILE_STROKE);
        g2.drawRoundRect(bx, by + bh / 4, bw, bh - bh / 4, 10, 10);
        g2.drawPolygon(roofX, roofY, 3);
        g2.drawLine(bx + bw / 2, by + bh / 4 + 4, bx + bw / 2, by + bh - 5);
        g2.drawLine(bx + 5, by + (3 * bh) / 5, bx + bw - 5, by + (3 * bh) / 5);
        g2.drawRect(bx + bw / 2 - 4, by + bh / 2 + 2, 8, bh / 3 - 4);
        g2.setColor(new Color(190, 91, 60));
        g2.fillRect(bx + bw - 8, by + 3, 5, 10);
        g2.setColor(COLOR_TILE_STROKE);
        g2.drawRect(bx + bw - 8, by + 3, 5, 10);
        g2.setColor(new Color(255, 255, 255, 170));
        g2.drawLine(x + w / 2 - 4, y + h, x + w / 2 + 2, y + (3 * h) / 4);
    }

    private static void drawBlason(Graphics2D g2, TuileV2 tuile, int x, int y, int w, int h) {
        if (!tuile.hasBlason()) {
            return;
        }

        int size = Math.max(14, w / 7);
        int cx = x + w / 2 - size / 2;
        int cy = y + h / 2 - size / 2;
        g2.setColor(new Color(236, 196, 56));
        g2.fillOval(cx, cy, size, size);
        g2.setColor(COLOR_TILE_STROKE);
        g2.drawOval(cx, cy, size, size);
    }

    private static void drawMeeples(Graphics2D g2, TuileV2 tuile, int x, int y, int w, int h) {
        drawMeepleOnSegment(g2, firstSegmentWithMeeple(tuile.getNordSegments()), x + w / 2, y + h / 5);
        drawMeepleOnSegment(g2, firstSegmentWithMeeple(tuile.getSudSegments()), x + w / 2, y + h - h / 5);
        drawMeepleOnSegment(g2, firstSegmentWithMeeple(tuile.getEstSegments()), x + w - w / 5, y + h / 2);
        drawMeepleOnSegment(g2, firstSegmentWithMeeple(tuile.getOuestSegments()), x + w / 5, y + h / 2);
        if (tuile.hasAbbaye()) {
            drawMeepleOnSegment(g2, tuile.getAbbaye(), x + w / 2, y + h / 2);
        }
    }

    private static void drawMeepleOnSegment(Graphics2D g2, Segment segment, int cx, int cy) {
        if (segment == null || segment.getMeeple() == null || segment.getMeeple().getOwner() == null) {
            return;
        }

        String nom = segment.getMeeple().getOwner().getNom().toUpperCase();
        String initiales = nom.length() >= 2 ? nom.substring(0, 2) : nom;
        Color body = nom.startsWith("A") ? COLOR_PLAYER_ONE : COLOR_PLAYER_TWO;
        drawMeepleShape(g2, body, initiales, cx, cy, 22);
    }

    private static void drawMeepleShape(Graphics2D g2, Color bodyColor, String initials, int cx, int cy, int size) {
        int head = Math.max(6, size / 3);
        int bodyW = Math.max(10, size / 2);
        int bodyH = Math.max(9, size / 2);
        int headX = cx - head / 2;
        int headY = cy - size / 2;
        int bodyX = cx - bodyW / 2;
        int bodyY = headY + head - 1;

        g2.setColor(bodyColor);
        g2.fillOval(headX, headY, head, head);
        g2.fillRoundRect(bodyX, bodyY, bodyW, bodyH, 8, 8);
        g2.fillRect(cx - 2, bodyY + bodyH - 1, 4, size / 3);
        g2.fillRect(bodyX - 3, bodyY + 4, 3, bodyH / 2);
        g2.fillRect(bodyX + bodyW, bodyY + 4, 3, bodyH / 2);
        g2.fillRect(cx - bodyW / 3, bodyY + bodyH + size / 5, 3, size / 4);
        g2.fillRect(cx + bodyW / 3 - 3, bodyY + bodyH + size / 5, 3, size / 4);
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillOval(headX + 1, headY + 1, Math.max(2, head / 3), Math.max(2, head / 3));

        g2.setColor(Color.WHITE);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 10f));
        FontMetrics fm = g2.getFontMetrics();
        int tx = cx - fm.stringWidth(initials) / 2;
        int ty = bodyY + bodyH / 2 + fm.getAscent() / 2 - 2;
        g2.drawString(initials, tx, ty);
    }

    private static boolean hasCity(List<Segment> segments) {
        if (segments == null) {
            return false;
        }
        for (Segment segment : segments) {
            if (segment instanceof SegmentVille) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasRoute(List<Segment> segments) {
        if (segments == null) {
            return false;
        }
        for (Segment segment : segments) {
            if (segment instanceof SegmentRoute) {
                return true;
            }
        }
        return false;
    }

    private static Segment firstSegmentWithMeeple(List<Segment> segments) {
        if (segments == null) {
            return null;
        }
        for (Segment segment : segments) {
            if (segment != null && segment.getMeeple() != null) {
                return segment;
            }
        }
        return null;
    }

    private static final class TileCellButton extends JButton {
        private final Position position;
        private final TuileV2 tuile;
        private final boolean recentlyPlaced;
        private final long placementTimestamp;
        private boolean placementPossible;
        private boolean selected;

        private TileCellButton(Position position, TuileV2 tuile, boolean recentlyPlaced, long placementTimestamp) {
            this.position = position;
            this.tuile = tuile;
            this.recentlyPlaced = recentlyPlaced;
            this.placementTimestamp = placementTimestamp;
            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(true);
            setBorder(BorderFactory.createLineBorder(Color.GRAY));
        }

        private void setPlacementState(boolean placementPossible, boolean selected) {
            this.placementPossible = placementPossible;
            this.selected = selected;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            drawBoardCellBackground(g2, getWidth(), getHeight(), selected ? COLOR_SELECTED : (placementPossible ? COLOR_VALID : COLOR_EMPTY));

            if (tuile != null) {
                drawTile(g2, tuile, getWidth(), getHeight(), false);
                drawPlacementAnimation(g2, recentlyPlaced, placementTimestamp, getWidth(), getHeight());
            } else if (placementPossible) {
                int s = Math.min(getWidth(), getHeight()) / 4;
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.setColor(new Color(82, 124, 73));
                g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(cx - s, cy, cx + s, cy);
                g2.drawLine(cx, cy - s, cx, cy + s);
            }

            g2.setColor(new Color(60, 60, 60));
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 11f));
            g2.drawString(position.getX() + ":" + position.getY(), 8, 16);
            g2.dispose();
        }
    }

    private static void drawBoardCellBackground(Graphics2D g2, int width, int height, Color base) {
        g2.setPaint(new GradientPaint(0, 0, base.brighter(), 0, height, base));
        g2.fillRect(0, 0, width, height);
        g2.setColor(new Color(COLOR_BOARD_GRID.getRed(), COLOR_BOARD_GRID.getGreen(), COLOR_BOARD_GRID.getBlue(), 120));
        for (int x = 0; x < width; x += 12) {
            g2.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += 12) {
            g2.drawLine(0, y, width, y);
        }
        g2.setColor(new Color(255, 255, 255, 30));
        Path2D path = new Path2D.Float();
        path.moveTo(0, height * 0.2);
        path.curveTo(width * 0.2, height * 0.1, width * 0.4, height * 0.3, width * 0.6, height * 0.18);
        path.curveTo(width * 0.8, height * 0.05, width * 0.9, height * 0.22, width, height * 0.14);
        g2.draw(path);
    }

    private static void drawPlacementAnimation(Graphics2D g2, boolean active, long timestamp, int width, int height) {
        if (!active) {
            return;
        }
        long age = System.currentTimeMillis() - timestamp;
        if (age > 700) {
            return;
        }
        float progress = age / 700f;
        int inset = 4 + (int) (progress * 10);
        int alpha = Math.max(0, 180 - (int) (progress * 180));
        g2.setColor(new Color(255, 245, 120, alpha));
        g2.setStroke(new BasicStroke(Math.max(1.5f, 4f - progress * 2.5f)));
        g2.drawRoundRect(inset, inset, width - inset * 2, height - inset * 2, 18, 18);
    }

    private static final class TilePreviewPanel extends JPanel {
        private TuileV2 tile;

        private TilePreviewPanel() {
            setPreferredSize(new Dimension(300, 220));
            setMinimumSize(new Dimension(300, 220));
            setBackground(COLOR_PREVIEW_BG);
        }

        private void setTile(TuileV2 tile) {
            this.tile = tile;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (tile == null) {
                g2.setColor(new Color(120, 104, 84));
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
                g2.drawString("Aucune tuile", 24, getHeight() / 2);
            } else {
                drawTile(g2, tile, getWidth(), getHeight(), true);
            }
            g2.dispose();
        }
    }
}
