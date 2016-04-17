package de.devoxx4kids.dronecontroller.jfx.drone;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import de.devoxx4kids.dronecontroller.command.movement.Pcmd;
import de.devoxx4kids.dronecontroller.command.multimedia.VideoStreaming;
import de.devoxx4kids.dronecontroller.jfx.control.Piloting;
import de.devoxx4kids.dronecontroller.listener.multimedia.VideoListener;
import de.devoxx4kids.dronecontroller.network.DroneConnection;
import javafx.geometry.Point2D;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import static java.lang.Math.abs;

/**
 * Date: 10.04.2016
 * Time: 14:47
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
public final class Sumo implements Drone {

    private final Piloting piloting;

    private final          DroneConnection                                    connection;
    private final Function<DroneConnection, Map<Identifier, Consumer<Event>>> userspace;

    private       ScheduledExecutorService executor;

    public Sumo (Piloting piloting, DroneConnection connection, Function<DroneConnection, Map<Identifier, Consumer<Event>>> userspace) {
        this.piloting   = piloting;
        this.connection = connection;
        this.userspace  = userspace;
    }

    @Override
    public final void start () {
        connection.connect ();

        Map<Identifier, Consumer<Event>> controls = movement (100, 90);
                                         controls.putAll (userspace.apply (connection));

        executor = piloting.create (controller -> controller.getType () == Controller.Type.GAMEPAD, controls);
    }

    @Override
    public final void stream (Consumer<byte[]> video) {
        connection.addEventListener (VideoListener.videoListener (video));
        connection.sendCommand (VideoStreaming.enableVideoStreaming ());
    }

    @Override
    public final void shutdown () {
        if (this.executor != null)
            this.executor.shutdown ();
    }

    private Map<Identifier, Consumer<Event>> movement (int maxspeed, int maxturn) {
        int period = 50;

        AtomicReference<Point2D> last = new AtomicReference<> (Point2D.ZERO);

        Subject<Float, Float> speed  = PublishSubject.create ();
        Subject<Float, Float> degree = PublishSubject.create ();

        // combine x, y to a stream but enforce uniqueness and apply a step function prior
        Observable.combineLatest (speed, degree, (Func2<Float, Float, Point2D>) Point2D::new)
                  .map (point -> point.magnitude () < 0.25 ? Point2D.ZERO : point)
                  .map (point -> new Point2D (groups (point.getX (), maxspeed, 4),
                                              groups (point.getY (),  maxturn, 4)))
                      .distinctUntilChanged ()
                          .forEach (last::set);

        // publish the last known command repeatedly if its not halt
        Observable.interval (period, TimeUnit.MILLISECONDS)
                  .map (time  -> last.get ())
                  .map (point -> Pcmd.pcmd ((int) point.getX (),
                                            (int) point.getY (), period))
                         .switchMap  (moving ())
                            .forEach (connection::sendCommand);

        // delegate x, y to the according subjects, but invert speed (personal preference :))
        Map<Identifier, Consumer<Event>> controls = new HashMap<> ();
                                         controls.put (Identifier.Axis.X, ev -> degree.onNext (  ev.getValue ()));
                                         controls.put (Identifier.Axis.Y, ev -> speed.onNext  (- ev.getValue ()));

        return controls;
    }

    private Func1<Pcmd, Observable<Pcmd>> moving () {
        return new Func1<Pcmd, Observable<Pcmd>> () {

            private boolean halt = false;

            @Override
            public Observable<Pcmd> call (Pcmd pcmd) {
                if (halt && pcmd.isHalt ()) return Observable.empty ();
                else
                    halt  = pcmd.isHalt ();

                return Observable.just (pcmd);
            }

        };
    }

    // simple linear step function, a polynomial function (n >= 3) would be much better
    private int groups (double normalized, int max, int groups) {
        double real = abs     (normalized) *   max;

        double unit  = (double) max / (double) groups;
        double pivot = max;

        while (pivot >= 0 && real < pivot) { pivot -= unit; }

        return (int) (Math.signum (normalized) * (pivot));
    }

}
