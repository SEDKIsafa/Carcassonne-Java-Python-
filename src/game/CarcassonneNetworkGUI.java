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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

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
import javax.swing.border.EmptyBorder;

import game.plateau.tuiles.TuileFactoryV2;
import game.plateau.tuiles.TuileV2;
import game.plateau.tuiles.direction.Position;
import game.plateau.tuiles.segment.Segment;
import game.plateau.tuiles.segment.SegmentType.SegmentRoute;
import game.plateau.tuiles.segment.SegmentType.SegmentVille;

public class CarcassonneNetworkGUI extends JFrame {
    private static final int BOARD_MARGIN = 1;
    private static final int CELL_SIZE = 120;
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
    private static final Color COLOR_CONNECTED = new Color(59, 140, 88);
    private static final Color COLOR_DISCONNECTED = new Color(164, 79, 67);
    private static final Color COLOR_INFO = new Color(99, 90, 78);
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
    private static final Pattern MEEPLE_PATTERN = Pattern.compile("_M_[A-Z0-9]+");

    private final String url;
    private final String playerId;
    private final Map<Position, String> plateauLocal;
    private final Map<String, List<String>> validMoves;
    private final Map<String, Map<String, List<String>>> validMeeples;

    private WebSocket webSocket;
    private boolean connected;
    private boolean isMyTurn;
    private String currentTile;
    private String activePlayer;
    private Position selectedPosition;
    private Position pendingPlacementPosition;
    private String pendingPlacementTile;
    private String pendingMeepleChoice;
    private Position lastPlacedPosition;
    private long lastPlacementTimestamp;
    private int messageRank;
    private int lastPoints;
    private final Map<String, Integer> scoreboard;

    private final JPanel boardPanel;
    private final JLabel playerLabel;
    private final JLabel statusLabel;
    private final JLabel activePlayerLabel;
    private final JLabel scoreLabel;
    private final JLabel selectedLabel;
    private final JLabel[] meepleLabels;
    private final JProgressBar[] scoreBars;
    private final TilePreviewPanel tilePreviewPanel;
    private final JTextArea journalArea;
    private final JButton rotateLeftButton;
    private final JButton rotateRightButton;
    private final JButton poserButton;
    private final JButton poserSansMeepleButton;

    public CarcassonneNetworkGUI(String url, String playerId) {
        super("Carcassonne Reseau - " + playerId);
        this.url = url;
        this.playerId = playerId;
        this.plateauLocal = new HashMap<>();
        this.validMoves = new HashMap<>();
        this.validMeeples = new HashMap<>();
        this.connected = false;
        this.isMyTurn = false;
        this.currentTile = "";
        this.activePlayer = "";
        this.selectedPosition = null;
        this.pendingPlacementPosition = null;
        this.pendingPlacementTile = null;
        this.pendingMeepleChoice = "none";
        this.lastPlacedPosition = null;
        this.lastPlacementTimestamp = 0L;
        this.messageRank = 0;
        this.lastPoints = 0;
        this.scoreboard = new HashMap<>();
        this.scoreboard.put("Alice", 0);
        this.scoreboard.put("Bob", 0);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
        sidePanel.setBorder(new EmptyBorder(12, 0, 12, 12));
        sidePanel.setPreferredSize(new Dimension(360, 700));
        sidePanel.setBackground(COLOR_UI_BG);

        playerLabel = new JLabel("Joueur : " + playerId);
        playerLabel.setFont(playerLabel.getFont().deriveFont(Font.BOLD, 20f));
        playerLabel.setForeground(COLOR_ACCENT);
        statusLabel = new JLabel("Statut : connexion...");
        statusLabel.setForeground(COLOR_DISCONNECTED);
        activePlayerLabel = new JLabel("Joueur actif : -");
        scoreLabel = new JLabel("Derniers points gagnes : 0");
        selectedLabel = new JLabel("Case selectionnee : -");
        meepleLabels = new JLabel[2];
        scoreBars = new JProgressBar[2];

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(COLOR_PANEL_BG);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(224, 216, 195)),
            new EmptyBorder(12, 14, 12, 14)
        ));
        headerPanel.add(playerLabel);
        headerPanel.add(statusLabel);
        headerPanel.add(activePlayerLabel);
        headerPanel.add(scoreLabel);
        headerPanel.add(selectedLabel);

        JPanel scorePanel = new JPanel(new GridLayout(0, 1, 6, 6));
        scorePanel.setBackground(COLOR_PANEL_BG);
        scorePanel.setBorder(BorderFactory.createTitledBorder("Barres de score"));
        String[] names = {"Alice", "Bob"};
        Color[] colors = {COLOR_PLAYER_ONE, COLOR_PLAYER_TWO};
        for (int i = 0; i < names.length; i++) {
            JProgressBar bar = new JProgressBar(0, 200);
            bar.setStringPainted(true);
            bar.setForeground(colors[i]);
            bar.setBackground(new Color(233, 228, 214));
            bar.setString(names[i] + " : 0 pts");
            scoreBars[i] = bar;
            scorePanel.add(bar);
            JLabel meepleLabel = new JLabel(names[i] + " meeples restants : 7");
            meepleLabels[i] = meepleLabel;
            scorePanel.add(meepleLabel);
        }

        tilePreviewPanel = new TilePreviewPanel();
        tilePreviewPanel.setBorder(BorderFactory.createTitledBorder("Tuile du tour"));

        rotateLeftButton = new JButton("Tourner a gauche");
        rotateRightButton = new JButton("Tourner a droite");
        poserButton = new JButton("Poser avec meeple");
        poserSansMeepleButton = new JButton("Poser sans meeple");

        rotateLeftButton.addActionListener(e -> rotateCurrentTile(false));
        rotateRightButton.addActionListener(e -> rotateCurrentTile(true));
        poserButton.addActionListener(e -> submitMove(true));
        poserSansMeepleButton.addActionListener(e -> submitMove(false));

        JPanel controlsPanel = new JPanel(new GridLayout(0, 1, 8, 8));
        controlsPanel.setBorder(BorderFactory.createTitledBorder("Actions"));
        controlsPanel.setBackground(COLOR_PANEL_BG);
        controlsPanel.add(rotateLeftButton);
        controlsPanel.add(rotateRightButton);
        controlsPanel.add(poserButton);
        controlsPanel.add(poserSansMeepleButton);

        journalArea = new JTextArea(18, 24);
        journalArea.setEditable(false);
        journalArea.setLineWrap(true);
        journalArea.setWrapStyleWord(true);
        journalArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        journalArea.setBackground(COLOR_PANEL_BG);
        JScrollPane journalScroll = new JScrollPane(journalArea);
        journalScroll.setBorder(BorderFactory.createTitledBorder("Journal"));

        sidePanel.add(headerPanel);
        sidePanel.add(new JPanel() {{
            setOpaque(false);
            setPreferredSize(new Dimension(1, 10));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        }});
        sidePanel.add(scorePanel);
        sidePanel.add(tilePreviewPanel);
        sidePanel.add(controlsPanel);
        sidePanel.add(journalScroll);

        add(sidePanel, BorderLayout.EAST);

        Timer timer = new Timer(45, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (lastPlacedPosition != null && System.currentTimeMillis() - lastPlacementTimestamp < 700) {
                    rebuildBoard();
                }
            }
        });
        timer.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                sendRawMessage(playerId + " LEAVES");
                if (webSocket != null) {
                    webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
                }
            }
        });

        refreshInterface();
    }

    private void connect() {
        HttpClient.newHttpClient()
            .newWebSocketBuilder()
            .buildAsync(URI.create(url), new NetworkListener())
            .thenAccept(ws -> {
                this.webSocket = ws;
                this.connected = true;
                sendRawMessage(playerId + " ENTERS");
                log("Connecte au reflecteur " + url + " en tant que " + playerId + ".");
                SwingUtilities.invokeLater(this::refreshInterface);
            })
            .exceptionally(error -> {
                SwingUtilities.invokeLater(() -> {
                    log("Erreur de connexion : " + error.getMessage());
                    refreshInterface();
                });
                return null;
            });
    }

    private void rotateCurrentTile(boolean right) {
        if (!isMyTurn || currentTile == null || currentTile.isEmpty()) {
            return;
        }
        currentTile = rotateTileRepresentation(currentTile, right ? "droite" : "gauche");
        selectedPosition = null;
        log("Rotation locale de la tuile " + (right ? "vers la droite." : "vers la gauche."));
        refreshInterface();
    }

    private void submitMove(boolean withMeeple) {
        if (!isMyTurn || currentTile == null || currentTile.isEmpty()) {
            return;
        }
        if (selectedPosition == null) {
            JOptionPane.showMessageDialog(this, "Selectionnez d'abord une case verte.");
            return;
        }

        String positionKey = selectedPosition.getX() + ":" + selectedPosition.getY();
        String meepleChoice = "none";

        if (withMeeple) {
            List<String> segments = validMeeples
                .getOrDefault(currentTile, Map.of())
                .getOrDefault(positionKey, List.of());

            if (!segments.isEmpty()) {
                JComboBox<String> combo = new JComboBox<>(segments.toArray(new String[0]));
                int result = JOptionPane.showConfirmDialog(
                    this,
                    combo,
                    "Choisissez un segment pour le meeple",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
                );
                if (result == JOptionPane.OK_OPTION) {
                    meepleChoice = (String) combo.getSelectedItem();
                } else {
                    log("Pose du meeple annulee.");
                    return;
                }
            } else {
                log("Aucun meeple valide sur cette position, envoi sans meeple.");
            }
        }

        String message = playerId + " PLACES " + currentTile + " " + positionKey + " " + meepleChoice;
        pendingPlacementPosition = selectedPosition;
        pendingPlacementTile = currentTile;
        pendingMeepleChoice = meepleChoice;
        sendRawMessage(message);
        log("Coup envoye : " + message);
        isMyTurn = false;
        refreshInterface();
    }

    private void sendRawMessage(String message) {
        if (webSocket == null) {
            return;
        }
        webSocket.sendText(message, true);
    }

    private void handleMessage(String message) {
        messageRank++;
        String[] parts = message.trim().split("\\s+");
        if (parts.length < 2) {
            return;
        }

        String source = parts[0];
        String keyword = parts[1];
        String[] params = new String[Math.max(0, parts.length - 2)];
        System.arraycopy(parts, 2, params, 0, params.length);

        switch (keyword) {
            case "BOARD_START":
                plateauLocal.clear();
                validMoves.clear();
                validMeeples.clear();
                break;
            case "TUILE":
                if (params.length >= 2) {
                    Position position = parsePosition(params[0]);
                    if (position != null) {
                        plateauLocal.put(position, params[1]);
                    }
                }
                break;
            case "BOARD_END":
                log("Plateau mis a jour.");
                break;
            case "VALID_MOVES":
                if (params.length >= 1) {
                    List<String> positions = new ArrayList<>();
                    for (int i = 1; i < params.length; i++) {
                        positions.add(params[i]);
                    }
                    validMoves.put(params[0], positions);
                }
                break;
            case "VALID_MEEPLES":
                parseValidMeeples(params);
                break;
            case "OFFERS":
                if (params.length >= 2) {
                    currentTile = params[0];
                    activePlayer = params[1];
                    scoreboard.putIfAbsent(activePlayer, scoreboard.getOrDefault(activePlayer, 0));
                    selectedPosition = null;
                    pendingPlacementPosition = null;
                    pendingPlacementTile = null;
                    pendingMeepleChoice = "none";
                    isMyTurn = playerId.equals(activePlayer);
                    log("Nouvelle tuile proposee : " + currentTile + ".");
                }
                break;
            case "SCORES":
                if (params.length >= 2) {
                    try {
                        lastPoints = Integer.parseInt(params[1]);
                    } catch (NumberFormatException ignored) {
                        lastPoints = 0;
                    }
                    if (!activePlayer.isEmpty()) {
                        scoreboard.put(activePlayer, scoreboard.getOrDefault(activePlayer, 0) + lastPoints);
                    }
                    applyPendingPlacement();
                    log("Coup valide. Points gagnes : " + lastPoints + ".");
                }
                break;
            case "BLAMES":
                if (params.length >= 2) {
                    pendingPlacementPosition = null;
                    pendingPlacementTile = null;
                    pendingMeepleChoice = "none";
                    isMyTurn = playerId.equals(activePlayer);
                    log("Coup refuse : " + params[1] + ".");
                }
                break;
            case "CLOSES":
                isMyTurn = false;
                log("La partie reseau est terminee.");
                JOptionPane.showMessageDialog(this, "La partie est terminee.", "Fin de partie", JOptionPane.INFORMATION_MESSAGE);
                break;
            case "LEAVES":
                if (source.equals(playerId)) {
                    connected = false;
                }
                break;
            default:
                if (!keyword.equals("ENTERS")) {
                    log("Message recu : " + message);
                }
                break;
        }

        refreshInterface();
    }

    private void parseValidMeeples(String[] params) {
        if (params.length == 0) {
            return;
        }

        String tileRepresentation = params[0];
        Map<String, List<String>> byPosition = new HashMap<>();
        String currentPosition = null;

        for (int i = 1; i < params.length; i++) {
            String token = params[i];
            if (token.contains(":")) {
                currentPosition = token;
                byPosition.put(currentPosition, new ArrayList<>());
            } else if (currentPosition != null && !"none".equals(token)) {
                byPosition.get(currentPosition).add(token);
            }
        }

        validMeeples.put(tileRepresentation, byPosition);
    }

    private Position parsePosition(String raw) {
        String[] split = raw.split(":");
        if (split.length != 2) {
            return null;
        }
        try {
            return new Position(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void refreshInterface() {
        statusLabel.setText(connected
            ? (isMyTurn ? "Statut : a vous de jouer" : "Statut : tour de " + (activePlayer.isEmpty() ? "?" : activePlayer))
            : "Statut : deconnecte");
        statusLabel.setForeground(connected ? COLOR_CONNECTED : COLOR_DISCONNECTED);
        activePlayerLabel.setText("Joueur actif : " + (activePlayer.isEmpty() ? "-" : activePlayer));
        scoreLabel.setText("Derniers points gagnes : " + lastPoints);
        selectedLabel.setText(selectedPosition == null
            ? "Case selectionnee : -"
            : "Case selectionnee : " + selectedPosition.getX() + ":" + selectedPosition.getY());
        tilePreviewPanel.setTileData(buildTileDataFromNetwork(currentTile));
        scoreBars[0].setValue(Math.min(200, scoreboard.getOrDefault("Alice", 0)));
        scoreBars[0].setString("Alice : " + scoreboard.getOrDefault("Alice", 0) + " pts");
        scoreBars[1].setValue(Math.min(200, scoreboard.getOrDefault("Bob", 0)));
        scoreBars[1].setString("Bob : " + scoreboard.getOrDefault("Bob", 0) + " pts");
        meepleLabels[0].setText("Alice meeples restants : " + estimateMeeplesRemaining("Alice"));
        meepleLabels[1].setText("Bob meeples restants : " + estimateMeeplesRemaining("Bob"));

        rotateLeftButton.setEnabled(isMyTurn && currentTile != null && !currentTile.isEmpty());
        rotateRightButton.setEnabled(isMyTurn && currentTile != null && !currentTile.isEmpty());
        poserButton.setEnabled(isMyTurn && currentTile != null && !currentTile.isEmpty());
        poserSansMeepleButton.setEnabled(isMyTurn && currentTile != null && !currentTile.isEmpty());

        rebuildBoard();
    }

    private void rebuildBoard() {
        boardPanel.removeAll();

        int minX = 36;
        int maxX = 36;
        int minY = 36;
        int maxY = 36;

        for (Position position : plateauLocal.keySet()) {
            minX = Math.min(minX, position.getX());
            maxX = Math.max(maxX, position.getX());
            minY = Math.min(minY, position.getY());
            maxY = Math.max(maxY, position.getY());
        }

        List<String> currentValidMoves = validMoves.getOrDefault(currentTile, List.of());
        for (String raw : currentValidMoves) {
            Position position = parsePosition(raw);
            if (position != null) {
                minX = Math.min(minX, position.getX());
                maxX = Math.max(maxX, position.getX());
                minY = Math.min(minY, position.getY());
                maxY = Math.max(maxY, position.getY());
            }
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
                String networkTile = plateauLocal.get(position);
                TileCellButton cell = new TileCellButton(
                    position,
                    buildTileDataFromNetwork(networkTile),
                    position.equals(lastPlacedPosition),
                    lastPlacementTimestamp
                );
                cell.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));

                if (networkTile != null) {
                    cell.setEnabled(false);
                } else if (isMyTurn && currentValidMoves.contains(x + ":" + y)) {
                    cell.setPlacementState(true, position.equals(selectedPosition));
                    cell.addActionListener(e -> {
                        selectedPosition = position;
                        refreshInterface();
                    });
                } else {
                    cell.setEnabled(false);
                }

                boardPanel.add(cell);
            }
        }

        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private void log(String message) {
        journalArea.append("[" + messageRank + "] " + message + "\n");
        journalArea.setCaretPosition(journalArea.getDocument().getLength());
    }

    private void applyPendingPlacement() {
        if (pendingPlacementPosition == null || pendingPlacementTile == null) {
            return;
        }

        plateauLocal.put(
            pendingPlacementPosition,
            addMeepleToRepresentation(pendingPlacementTile, pendingMeepleChoice, playerId)
        );
        lastPlacedPosition = pendingPlacementPosition;
        lastPlacementTimestamp = System.currentTimeMillis();
        selectedPosition = null;
        pendingPlacementPosition = null;
        pendingPlacementTile = null;
        pendingMeepleChoice = "none";
    }

    private static String addMeepleToRepresentation(String tileRepresentation, String meepleChoice, String playerId) {
        if (tileRepresentation == null || meepleChoice == null || "none".equals(meepleChoice)) {
            return tileRepresentation;
        }

        String initials = playerId.toUpperCase();
        initials = initials.length() >= 2 ? initials.substring(0, 2) : initials;

        String marker = "_M_" + initials;

        if (tileRepresentation.contains(":" + meepleChoice) && !meepleChoice.startsWith("f") && !meepleChoice.startsWith("c")
            && !meepleChoice.startsWith("r") && !meepleChoice.equals("A")) {
            return tileRepresentation;
        }

        if ("A".equals(meepleChoice) && tileRepresentation.contains(":A")) {
            return tileRepresentation.replace(":A", ":A" + marker);
        }

        String[] parts = tileRepresentation.split(":");
        String[] edges = parts[0].split("-");
        for (int i = 0; i < edges.length; i++) {
            if (edges[i].contains(meepleChoice)) {
                edges[i] = edges[i] + marker;
                break;
            }
        }

        String rebuilt = String.join("-", edges);
        if (parts.length > 1) {
            rebuilt += ":" + parts[1];
        }
        return rebuilt;
    }

    private int estimateMeeplesRemaining(String player) {
        int used = 0;
        String initials = player.toUpperCase().startsWith("AL") ? "AL" : "BO";
        for (String tile : plateauLocal.values()) {
            if (tile != null && tile.contains("_M_" + initials)) {
                used++;
            }
        }
        return Math.max(0, 7 - used);
    }

    private static String rotateTileRepresentation(String tile, String direction) {
        String[] parts = tile.split(":");
        String[] edges = parts[0].split("-");
        String abbaye = parts.length > 1 ? ":" + parts[1] : "";

        String[] rotated;
        if ("droite".equals(direction)) {
            rotated = new String[] {edges[3], edges[0], edges[1], edges[2]};
        } else {
            rotated = new String[] {edges[1], edges[2], edges[3], edges[0]};
        }

        return String.join("-", rotated) + abbaye;
    }

    private static NetworkTileData buildTileDataFromNetwork(String networkRepresentation) {
        if (networkRepresentation == null || networkRepresentation.isEmpty()) {
            return null;
        }

        try {
            NetworkTileData data = new NetworkTileData();
            String cleaned = MEEPLE_PATTERN.matcher(networkRepresentation).replaceAll("");
            data.tile = TuileFactoryV2.buildTuileV2(cleaned);

            String[] tileAndAbbaye = networkRepresentation.split(":");
            String[] edges = tileAndAbbaye[0].split("-");
            if (edges.length == 4) {
                data.northMeeple = extractMeepleInitials(edges[0]);
                data.eastMeeple = extractMeepleInitials(edges[1]);
                data.southMeeple = extractMeepleInitials(edges[2]);
                data.westMeeple = extractMeepleInitials(edges[3]);
            }

            if (tileAndAbbaye.length > 1) {
                data.centerMeeple = extractMeepleInitials(tileAndAbbaye[1]);
            }

            return data;
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractMeepleInitials(String token) {
        int idx = token.indexOf("_M_");
        if (idx < 0) {
            return null;
        }
        String initials = token.substring(idx + 3).trim();
        return initials.isEmpty() ? null : initials;
    }

    private static void drawTile(Graphics2D g2, NetworkTileData tileData, int width, int height, boolean preview) {
        if (tileData == null || tileData.tile == null) {
            return;
        }
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

        drawCityAreas(g2, tileData.tile, x, y, w, h);
        drawRoads(g2, tileData.tile, x, y, w, h);
        drawAbbaye(g2, tileData.tile, x, y, w, h);
        drawBlason(g2, tileData.tile, x, y, w, h);
        drawMeeples(g2, tileData, x, y, w, h);
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

    private static void drawMeeples(Graphics2D g2, NetworkTileData tileData, int x, int y, int w, int h) {
        drawMeepleMarker(g2, tileData.northMeeple, x + w / 2, y + h / 5);
        drawMeepleMarker(g2, tileData.southMeeple, x + w / 2, y + h - h / 5);
        drawMeepleMarker(g2, tileData.eastMeeple, x + w - w / 5, y + h / 2);
        drawMeepleMarker(g2, tileData.westMeeple, x + w / 5, y + h / 2);
        drawMeepleMarker(g2, tileData.centerMeeple, x + w / 2, y + h / 2);
    }

    private static void drawMeepleMarker(Graphics2D g2, String initials, int cx, int cy) {
        if (initials == null || initials.isEmpty()) {
            return;
        }

        Color body = initials.startsWith("AL") ? COLOR_PLAYER_ONE : COLOR_PLAYER_TWO;
        drawMeepleShape(g2, body, initials, cx, cy, 22);
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

    private final class NetworkListener implements WebSocket.Listener {
        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            String message = data.toString();
            SwingUtilities.invokeLater(() -> handleMessage(message));
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            SwingUtilities.invokeLater(() -> {
                connected = false;
                log("Erreur reseau : " + error.getMessage());
                refreshInterface();
            });
        }
    }

    private static final class TileCellButton extends JButton {
        private final Position position;
        private final NetworkTileData tileData;
        private final boolean recentlyPlaced;
        private final long placementTimestamp;
        private boolean placementPossible;
        private boolean selected;

        private TileCellButton(Position position, NetworkTileData tileData, boolean recentlyPlaced, long placementTimestamp) {
            this.position = position;
            this.tileData = tileData;
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

            if (tileData != null) {
                drawTile(g2, tileData, getWidth(), getHeight(), false);
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
        private NetworkTileData tileData;

        private TilePreviewPanel() {
            setPreferredSize(new Dimension(300, 220));
            setMinimumSize(new Dimension(300, 220));
            setBackground(COLOR_PREVIEW_BG);
        }

        private void setTileData(NetworkTileData tileData) {
            this.tileData = tileData;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (tileData == null || tileData.tile == null) {
                g2.setColor(new Color(120, 104, 84));
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 16f));
                g2.drawString("Aucune tuile", 24, getHeight() / 2);
            } else {
                drawTile(g2, tileData, getWidth(), getHeight(), true);
            }
            g2.dispose();
        }
    }

    private static final class NetworkTileData {
        private TuileV2 tile;
        private String northMeeple;
        private String eastMeeple;
        private String southMeeple;
        private String westMeeple;
        private String centerMeeple;
    }

    public static void main(String[] args) {
        String url = args.length >= 1 ? args[0] : "ws://localhost:3001";
        String player = args.length >= 2 ? args[1] : "Alice";

        launch(url, player);
    }

    public static void launch(String url, String player) {

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            CarcassonneNetworkGUI gui = new CarcassonneNetworkGUI(url, player);
            gui.setVisible(true);
            gui.connect();
        });
    }
}
