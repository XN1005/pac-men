package pacmen;

import javafx.application.Application;
import javafx.stage.Stage;
import pacmen.util.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.init(stage);
        SceneManager.goTo(SceneManager.MENU);
    }

    public static void main(String[] args) {
        launch(args);
    }
}