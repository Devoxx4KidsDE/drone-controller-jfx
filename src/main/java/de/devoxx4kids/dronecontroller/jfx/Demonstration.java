package de.devoxx4kids.dronecontroller.jfx;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.devoxx4kids.dronecontroller.command.animation.SpinJump;
import de.devoxx4kids.dronecontroller.command.flip.Balance;
import de.devoxx4kids.dronecontroller.command.flip.DownsideDown;
import de.devoxx4kids.dronecontroller.command.flip.DownsideUp;
import de.devoxx4kids.dronecontroller.command.movement.Jump;
import de.devoxx4kids.dronecontroller.command.multimedia.VideoStreaming;
import de.devoxx4kids.dronecontroller.jfx.drone.Drone;
import de.devoxx4kids.dronecontroller.jfx.drone.Sumo;
import de.devoxx4kids.dronecontroller.jfx.io.ImageConverter;
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
import net.java.games.input.Component.Identifier;
import net.java.games.input.Event;

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

        Drone drone = drone ();
              drone.start ();
              drone.stream (renderer (destination));

        Runtime.getRuntime ().addShutdownHook (new Thread (drone::shutdown));
    }

    private Consumer<byte[]> renderer (WritableImage destination) {
        int offset   = 54 /* bmp header size */;
        int capacity = width * height * 3 /* rgb */ + offset;

        return new ImageConverter (bitmap -> runLater (

                () -> destination.getPixelWriter ().setPixels (0, 0, width, height, PixelFormat.getByteRgbInstance (), bitmap, offset, width * 3)

        ), "bmp", capacity);
    }

    private Map<Identifier, Consumer<Event>> commands (DroneConnection connection) {
            Map<Identifier, Consumer<Event>> controls = new HashMap<> ();
                                                       controls.put (Identifier.Button._0, ev -> connection.sendCommand (SpinJump.spinJump ()));
                                                       controls.put (Identifier.Button._1, ev -> connection.sendCommand (Jump.jump (Jump.Type.Long)));
                                                       controls.put (Identifier.Button._2, ev -> connection.sendCommand (Jump.jump (Jump.Type.High)));
                                                       controls.put (Identifier.Button._3, ev -> connection.sendCommand (Balance.balance ()));
                                                       controls.put (Identifier.Button._4, ev -> connection.sendCommand (DownsideDown.downsideDown ()));
                                                       controls.put (Identifier.Button._5, ev -> connection.sendCommand (DownsideUp.downsideUp ()));
                                                       controls.put (Identifier.Button._6, ev -> connection.sendCommand (VideoStreaming.enableVideoStreaming ()));
                                                       controls.put (Identifier.Button._7, ev -> connection.sendCommand (VideoStreaming.disableVideoStreaming ()));

        return controls;
    }

    private Drone drone () {
        return new Sumo (wireless (), this::commands);
    }

    private DroneConnection wireless () {
        return new WirelessLanDroneConnection (ip, port, wlan);
    }

    public static void main (String[] args) {
        launch (args);
    }

}
