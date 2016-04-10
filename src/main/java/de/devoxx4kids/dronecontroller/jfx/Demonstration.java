package de.devoxx4kids.dronecontroller.jfx;

import java.util.function.Consumer;
import java.util.function.Supplier;

import de.devoxx4kids.dronecontroller.command.multimedia.VideoStreaming;
import de.devoxx4kids.dronecontroller.jfx.io.ImageConverter;
import de.devoxx4kids.dronecontroller.listener.multimedia.VideoListener;
import de.devoxx4kids.dronecontroller.network.DroneConnection;
import de.devoxx4kids.dronecontroller.network.WirelessLanDroneConnection;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static javafx.application.Platform.runLater;

/**
 * Date: 10.04.2016
 * Time: 12:15
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
public final class Demonstration extends Application {

    private final String wlan = "JumpingSumo-schwarz2";
    private final String ip   = "192.168.2.1";

    private final Integer port = 44444;

    private final static int width  = 640;
    private final static int height = 480;

    @Override
    public final void start (Stage primary) throws Exception {
        primary.setTitle ("Jumping Sumo Fx");

        WritableImage destination = new WritableImage (width, height);

        ImageView view = new ImageView (destination);
                  view.setScaleY (-1);

        StackPane root = new StackPane ();
                  root.getChildren ().add (view);

        primary.setScene (new Scene (root, width, height));
        primary.show ();

        primary.setOnCloseRequest ((event) -> {
            Platform.exit ();
            System.exit   (0);
        });

        drone (this::wireless, renderer (destination)).sendCommand (VideoStreaming.enableVideoStreaming ());
    }

    private DroneConnection drone (Supplier<DroneConnection> connection, Consumer<byte[]> video) {
        DroneConnection drone = connection.get ();
                        drone.addEventListener (VideoListener.videoListener (video));
                        drone.connect ();

        return drone;
    }

    private Consumer<byte[]> renderer (WritableImage destination) {
        int offset   = 54 /* bmp header size */;
        int capacity = width * height * 3 /* rgb */ + offset;

        return new ImageConverter (bitmap -> runLater (

                () -> destination.getPixelWriter ().setPixels (0, 0, width, height, PixelFormat.getByteRgbInstance (), bitmap, offset, width * 3)

        ), "bmp", capacity);
    }

    private DroneConnection wireless () {
        return new WirelessLanDroneConnection (ip, port, wlan);
    }

    public static void main (String[] args) {
        launch (args);
    }

}
