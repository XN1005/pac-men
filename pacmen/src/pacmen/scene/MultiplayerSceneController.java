package pacmen.scene;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pacmen.util.SceneManager;

import java.net.InetAddress;
import java.net.URL;
import java.util.ResourceBundle;

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

    private boolean isHostMode = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        detectLocalIp();
        showTab(true); // default to Host tab
    }

    // ── Tab switching ─────────────────────────────────────────────
    @FXML private void onTabHost() { showTab(true); }
    @FXML private void onTabJoin() { showTab(false); }

    private void showTab(boolean host) {
        isHostMode = host;

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
        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1)) {{
            setOnFinished(e -> ipLabel.setStyle(
                "-fx-font-family:'Press Start 2P',monospace;-fx-font-size:14px;-fx-text-fill:#FFD700;"));
        }}.play();
    }

    // ── Host: start game (when enough players ready) ──────────────
    @FXML private void onStartGame() {
        // TODO: signal MultiplayerManager to begin game
        SceneManager.goTo(SceneManager.GAME);
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
        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1)) {{
            setOnFinished(e -> {
                connectionStatus.setText("✓ CONNECTED TO " + ip + ":" + port);
                connectionStatus.setStyle("-fx-text-fill:#00FF88;-fx-font-family:'Press Start 2P',monospace;-fx-font-size:8px;");
                btnReady.setDisable(false);
                addPlayerRow(joinLobbyList, "YOU", true);
                addPlayerRow(joinLobbyList, ip.substring(ip.lastIndexOf('.') + 1).equals("1") ? "HOST" : "HOST", false);
            });
        }}.play();
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
            addPlayerRow(lobbyList, p.getUsername(), false);
        }
    }

    // ── Navigation ────────────────────────────────────────────────
    @FXML private void onBack() {
        // TODO: MultiplayerManager.disconnect();
        SceneManager.goTo(SceneManager.MENU);
    }
}