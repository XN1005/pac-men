package pacmen.scene;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import pacmen.util.SceneManager;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controls the main menu screen.
 * Navigation: PLAY → GameScene | HOST/JOIN → MultiplayerScene
 */
public class MainMenuController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private StackPane splashOverlay;
    @FXML private Label     titleLabel;
    @FXML private Label     insertCoinLabel;
    @FXML private Label     playerCountLabel;
    @FXML private HBox      dotRowTop;
    @FXML private HBox      dotRowBottom;
    @FXML private VBox      menuButtons;
    @FXML private Label     ghostBlinky, ghostPinky, ghostInky, ghostClyde;

    private static final String[] GHOST_COLORS = {
        "-fx-text-fill:rgba(255,59,59,0.22);",
        "-fx-text-fill:rgba(255,182,255,0.22);",
        "-fx-text-fill:rgba(77,204,255,0.22);",
        "-fx-text-fill:rgba(255,165,0,0.22);"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buildDotRows();
        styleGhosts();
        startGhostFloatAnimations();
        startTitlePulse();
        startInsertCoinBlink();
        splashOverlay.setOnMouseClicked(e -> dismissSplash());
    }

    // ── Dot rows ──────────────────────────────────────────────────
    private void buildDotRows() {
        for (HBox row : List.of(dotRowTop, dotRowBottom)) {
            for (int i = 0; i < 18; i++) {
                Circle dot = new Circle(i % 5 == 0 ? 5 : 3);
                dot.setFill(Color.web("#FFD700", i % 5 == 0 ? 0.75 : 0.35));
                row.getChildren().add(dot);
            }
        }
    }

    private void styleGhosts() {
        List<Label> g = List.of(ghostBlinky, ghostPinky, ghostInky, ghostClyde);
        for (int i = 0; i < g.size(); i++) g.get(i).setStyle(GHOST_COLORS[i]);
    }

    // ── Splash dismiss ────────────────────────────────────────────
    private void dismissSplash() {
        FadeTransition fade = new FadeTransition(Duration.millis(500), splashOverlay);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            splashOverlay.setVisible(false);
            splashOverlay.setManaged(false);
            animateMenuEntrance();
        });
        fade.play();
    }

    private void animateMenuEntrance() {
        menuButtons.setOpacity(0);
        var items = menuButtons.getChildren();
        for (int i = 0; i < items.size(); i++) {
            var btn = items.get(i);
            btn.setOpacity(0);
            btn.setTranslateY(30);
            int delay = 80 * i;
            new Timeline(
                new KeyFrame(Duration.millis(delay)),
                new KeyFrame(Duration.millis(delay + 300),
                    new KeyValue(btn.opacityProperty(), 1.0, Interpolator.EASE_OUT),
                    new KeyValue(btn.translateYProperty(), 0,  Interpolator.EASE_OUT))
            ).play();
        }
        new FadeTransition(Duration.millis(100), menuButtons) {{ setToValue(1); }}.play();
    }

    // ── Animations ────────────────────────────────────────────────
    private void startGhostFloatAnimations() {
        List<Label> ghosts = List.of(ghostBlinky, ghostPinky, ghostInky, ghostClyde);
        double[] amps   = {14, 12, 16, 10};
        double[] delays = { 0, 400, 700, 1100};
        for (int i = 0; i < ghosts.size(); i++) {
            Label g = ghosts.get(i);
            TranslateTransition tt = new TranslateTransition(Duration.millis(2800 + i * 200), g);
            tt.setByY(-amps[i]);
            tt.setAutoReverse(true);
            tt.setCycleCount(Animation.INDEFINITE);
            tt.setInterpolator(Interpolator.EASE_BOTH);
            new PauseTransition(Duration.millis(delays[i])) {{
                setOnFinished(e -> tt.play());
            }}.play();
        }
    }

    private void startTitlePulse() {
        ScaleTransition st = new ScaleTransition(Duration.millis(1600), titleLabel);
        st.setFromX(1.0); st.setFromY(1.0);
        st.setToX(1.03);  st.setToY(1.03);
        st.setAutoReverse(true);
        st.setCycleCount(Animation.INDEFINITE);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.play();
    }

    private void startInsertCoinBlink() {
        new Timeline(
            new KeyFrame(Duration.millis(0),    new KeyValue(insertCoinLabel.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(600),  new KeyValue(insertCoinLabel.opacityProperty(), 0.0)),
            new KeyFrame(Duration.millis(1200), new KeyValue(insertCoinLabel.opacityProperty(), 1.0))
        ) {{ setCycleCount(Animation.INDEFINITE); }}.play();
    }

    // ── Navigation handlers ───────────────────────────────────────
    @FXML private void onPlay()        { SceneManager.goTo(SceneManager.GAME); }
    @FXML private void onHostGame()    { SceneManager.goTo(SceneManager.MULTIPLAYER); }
    @FXML private void onJoinGame()    { SceneManager.goTo(SceneManager.MULTIPLAYER); }
    @FXML private void onSettings()    { System.out.println("[MENU] Settings – TODO"); }
    @FXML private void onLeaderboard() { System.out.println("[MENU] Leaderboard – TODO"); }

    @FXML private void onQuit() {
        FadeTransition fade = new FadeTransition(Duration.millis(400), rootPane);
        fade.setToValue(0);
        fade.setOnFinished(e -> Platform.exit());
        fade.play();
    }
}