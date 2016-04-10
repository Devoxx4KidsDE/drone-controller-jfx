package de.devoxx4kids.dronecontroller.jfx.control;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

/**
 * Date: 10.04.2016
 * Time: 12:21
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
public final class Piloting {

    public final static ScheduledExecutorService create (Predicate<Controller> locator, Map<Component.Identifier, Consumer<Event>> controls) {
        ScheduledExecutorService single;

        single = Executors.newSingleThreadScheduledExecutor ();
        single.scheduleAtFixedRate (new Runnable () {

            @Override
            public void run () {
                first ().ifPresent (this::notify);
            }

            private void notify (Controller controller) {
                if (!                       controller.poll ()) return;

                EventQueue events = controller.getEventQueue ();
                Event event;

                while (events.getNextEvent (event = new Event ())) {
                    Component component;

                                           component = event.getComponent ();
                    controls.getOrDefault (component.getIdentifier (), ev -> {}).accept (event);
                }
            }

            private Optional<Controller> first () {
                return Stream.of (ControllerEnvironment.getDefaultEnvironment ().getControllers ()).filter (locator).findFirst ();
            }

        }, 0, 50, TimeUnit.MILLISECONDS);

        return single;
    }

}
