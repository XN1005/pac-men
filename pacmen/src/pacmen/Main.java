package pacmen;

import javafx.application.Application;
import javafx.stage.Stage;
import pacmen.util.SceneManager;
import pacmen.datamanager.ScoreManager;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // initilize score manager
        ScoreManager.getInstance();
        stage.setOnCloseRequest(event -> {
            System.out.println("Application shutting down. Saving game history...");
            ScoreManager.getInstance().saveHistory();
        });

        SceneManager.init(stage);
        SceneManager.goTo(SceneManager.MENU);
    }

    public static void main(String[] args) {
        launch(args);
    }
}