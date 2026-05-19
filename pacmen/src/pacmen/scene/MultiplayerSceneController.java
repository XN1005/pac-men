package pacmen.scene;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import pacmen.entities.Ghost;
import pacmen.entities.Player;
import pacmen.map.*;
import pacmen.util.SceneManager;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controls the Multiplayer lobby screen.
 *
 * Host tab  – shows your local IP and a live lobby list.
 *             START GAME enables once ≥ 2 players are present.
 * Join tab  – enter host IP, click CONNECT, then READY when lobby shows.
 *
 * Wire up to your MultiplayerManager:
 *   MultiplayerManager.getInstance().setLobbyListener(this::onLobbyUpdate);
 */
public class MultiplayerSceneController implements Initializable {

    // ── FXML – tabs ───────────────────────────────────────────────
    @FXML private Button tabHost;
    @FXML private Button tabJoin;

    // ── FXML – host panel ─────────────────────────────────────────
    @FXML private VBox   hostPanel;
    @FXML private Label  ipLabel;
    @FXML private Label  portLabel;
    @FXML private VBox   lobbyList;
    @FXML private Label  playerCountLabel;
    @FXML private Button btnStartGame;

    // ── FXML – join panel ─────────────────────────────────────────
    @FXML private VBox      joinPanel;
    @FXML private TextField ipField;
    @FXML private TextField portField;
    @FXML private Label     connectionStatus;
    @FXML private VBox      joinLobbyList;
    @FXML private Button    btnReady;

    private final Set<KeyCode> keysPressed = new HashSet<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        detectLocalIp();
        showTab(true); // default to Host tab
    }

    // ── Tab switching ─────────────────────────────────────────────
    @FXML private void onTabHost() { showTab(true); }
    @FXML private void onTabJoin() { showTab(false); }

    private void showTab(boolean host) {
        hostPanel.setVisible(host);  hostPanel.setManaged(host);
        joinPanel.setVisible(!host); joinPanel.setManaged(!host);

        String active   = "-fx-font-family:'Press Start 2P',monospace;-fx-font-size:10px;" +
                          "-fx-background-color:#FFD700;-fx-text-fill:#050810;" +
                          "-fx-background-radius:0;-fx-border-radius:0;-fx-cursor:hand;";
        String inactive = "-fx-font-family:'Press Start 2P',monospace;-fx-font-size:10px;" +
                          "-fx-background-color:transparent;-fx-text-fill:rgba(255,215,0,0.55);" +
                          "-fx-background-radius:0;-fx-border-radius:0;-fx-cursor:hand;" +
                          "-fx-border-color:rgba(255,215,0,0.25);-fx-border-width:0 0 2 0;";

        tabHost.setStyle(host  ? active : inactive);
        tabJoin.setStyle(!host ? active : inactive);
    }

    // ── Host: detect local IP ─────────────────────────────────────
    private void detectLocalIp() {
        new Thread(() -> {
            try {
                String ip = InetAddress.getLocalHost().getHostAddress();
                Platform.runLater(() -> ipLabel.setText(ip));
            } catch (Exception e) {
                Platform.runLater(() -> ipLabel.setText("127.0.0.1"));
            }
        }).start();
    }

    // ── Host: copy IP to clipboard ────────────────────────────────
    @FXML private void onCopyIp() {
        String ip = ipLabel.getText();
        javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(ip);
        cb.setContent(content);
        // Brief visual feedback
        ipLabel.setStyle("-fx-text-fill:#00FF88;-fx-font-family:'Press Start 2P',monospace;-fx-font-size:14px;");
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
        pause.setOnFinished(e -> ipLabel.setStyle(
            "-fx-font-family:'Press Start 2P',monospace;-fx-font-size:14px;-fx-text-fill:#FFD700;"));
        pause.play();
    }

    // ── Host: start game (when enough players ready) ──────────────
    @FXML private void onStartGame() {
        // TODO: signal MultiplayerManager to begin game
        launchDraftMultiplayerGame();
    }

    private void launchDraftMultiplayerGame() {
        GameMap gameMap = new GameMap();
        MapLoader.loadMap(gameMap, "resources\\maps\\level1.txt");

        Player p1 = new Player(gameMap, 1.5, 1);
        Player p2 = new Player(gameMap, 1.5, 2);

        Ghost g1 = new Ghost(gameMap, 250, 280, 1.5, Color.AQUA);
        Ghost g2 = new Ghost(gameMap, 270, 280, 1.5, Color.ORANGE);
        Ghost g3 = new Ghost(gameMap, 270, 280, 1.5, Color.PINK);

        p1.setInput(keysPressed);
        p2.setInput(keysPressed);

        Pane root = new Pane();
        root.setStyle("-fx-background-color: black;");

        Label scoreLabelP1 = new Label("Score: " + p1.score);
        Label scoreLabelP2 = new Label("Score: " + p2.score);
        scoreLabelP1.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        scoreLabelP2.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        scoreLabelP1.setTextFill(Color.WHITE);
        scoreLabelP2.setTextFill(Color.WHITE);
        scoreLabelP1.setLayoutX(50.0);
        scoreLabelP1.setLayoutY(630.0);
        scoreLabelP2.setLayoutX(410.0);
        scoreLabelP2.setLayoutY(630.0);

        for (int x = 0; x < 28; x++) {
            for (int y = 0; y < 36; y++) {
                Cell cell = gameMap.getCell(x, y);
                if (cell instanceof PelletCell) {
                    root.getChildren().add(((PelletCell) cell).getPellet().sprite);
                }
                if (cell instanceof CherryCell) {
                    root.getChildren().add(((CherryCell) cell).getCherry().sprite);
                }
                if (cell instanceof WallCell) {
                    root.getChildren().add(((WallCell) cell).getSprite());
                }
            }
        }

        File imageFile = new File("resources/assets/MAP_Level1.png");
        Image image = new Image(imageFile.toURI().toString());
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(560);
        imageView.setPreserveRatio(true);

        root.getChildren().addAll(p1.sprite, p2.sprite);
        root.getChildren().addAll(scoreLabelP1, scoreLabelP2);
        root.getChildren().add(imageView);
        root.getChildren().addAll(g1.sprite, g2.sprite, g3.sprite);

        Scene scene = new Scene(root, 28 * 20, 36 * 20);
        scene.setOnKeyPressed(e -> keysPressed.add(e.getCode()));
        scene.setOnKeyReleased(e -> keysPressed.remove(e.getCode()));

        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                p1.update();
                p2.update();
                g1.update();
                g2.update();
                g3.update();
                scoreLabelP1.setText("Score: " + p1.score);
                scoreLabelP2.setText("Score: " + p2.score);

                if (p1.state.equals("DEAD") || p2.state.equals("DEAD")) {
                    // TODO: call score manager for actions
                }
            }
        };
        gameLoop.start();

        if (SceneManager.getStage() != null) {
            SceneManager.getStage().setTitle("Pac-Men Multiplayer");
            SceneManager.getStage().setScene(scene);
            SceneManager.getStage().show();
        }
    }

    // ── Join: connect to host ─────────────────────────────────────
    @FXML private void onConnect() {
        String ip   = ipField.getText().trim();
        String port = portField.getText().trim();

        if (ip.isEmpty()) {
            connectionStatus.setText("⚠ ENTER A HOST IP");
            connectionStatus.setStyle("-fx-text-fill:#FF3B3B;-fx-font-family:'Press Start 2P',monospace;-fx-font-size:8px;");
            return;
        }

        connectionStatus.setText("CONNECTING...");
        connectionStatus.setStyle("-fx-text-fill:#FFD700;-fx-font-family:'Press Start 2P',monospace;-fx-font-size:8px;");

        // TODO: Replace this stub with MultiplayerManager.connect(ip, port, callback)
        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
        pause.setOnFinished(e -> {
            connectionStatus.setText("✓ CONNECTED TO " + ip + ":" + port);
            connectionStatus.setStyle("-fx-text-fill:#00FF88;-fx-font-family:'Press Start 2P',monospace;-fx-font-size:8px;");
            btnReady.setDisable(false);
            addPlayerRow(joinLobbyList, "YOU", true);
            addPlayerRow(joinLobbyList, ip.substring(ip.lastIndexOf('.') + 1).equals("1") ? "HOST" : "HOST", false);
        });
        pause.play();
    }

    // ── Join: signal ready ────────────────────────────────────────
    @FXML private void onReady() {
        btnReady.setDisable(true);
        btnReady.setText("✓  WAITING...");
        // TODO: MultiplayerManager.sendReady();
    }

    // ── Lobby helpers ─────────────────────────────────────────────

    /**
     * Add a player row to a lobby VBox.
     * Call this from a MultiplayerManager lobby-update listener.
     *
     * @param list   the lobbyList or joinLobbyList VBox
     * @param name   player username
     * @param ready  whether this player is ready
     */
    public void addPlayerRow(VBox list, String name, boolean ready) {
        HBox row = new HBox();
        row.getStyleClass().add("player-row");
        row.setSpacing(0);
        row.setPrefHeight(44);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("player-row-name");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);

        Label statusDot = new Label(ready ? "●" : "○");
        statusDot.getStyleClass().add(ready ? "status-dot-ready" : "status-dot-waiting");

        Label statusText = new Label(ready ? " READY" : " WAITING");
        statusText.getStyleClass().add("player-row-status");

        row.getChildren().addAll(nameLabel, statusDot, statusText);
        list.getChildren().add(row);

        // Update player count
        int count = list.getChildren().size();
        playerCountLabel.setText(count + " / 4 PLAYERS");
        btnStartGame.setDisable(count < 2);
    }

    /** Clear and repopulate the host lobby. */
    public void refreshLobby(java.util.List<pacmen.entities.Player> players) {
        lobbyList.getChildren().clear();
        for (pacmen.entities.Player p : players) {
            addPlayerRow(lobbyList, p.toString(), false);
        }
    }

    // ── Navigation ────────────────────────────────────────────────
    @FXML private void onBack() {
        // TODO: MultiplayerManager.disconnect();
        SceneManager.goTo(SceneManager.MENU);
    }
}