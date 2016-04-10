package de.devoxx4kids.dronecontroller.jfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Date: 10.04.2016
 * Time: 12:15
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
public final class Demonstration extends Application {

    private final static int width  = 640;
    private final static int height = 480;

    @Override
    public final void start (Stage primary) throws Exception {
        primary.setTitle ("Jumping Sumo Fx");

        StackPane root = new StackPane ();

        primary.setScene (new Scene (root, width, height));
        primary.show ();

        primary.setOnCloseRequest ((event) -> {
            Platform.exit ();
            System.exit   (0);
        });
    }

    public static void main (String[] args) {
        launch (args);
    }

}
