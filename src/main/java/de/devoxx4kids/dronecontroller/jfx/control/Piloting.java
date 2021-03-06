package de.devoxx4kids.dronecontroller.jfx.control;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

import static de.devoxx4kids.dronecontroller.jfx.utility.Lambda.supply;
import static de.devoxx4kids.dronecontroller.jfx.utility.Lambda.trylog;

/**
 * Date: 10.04.2016
 * Time: 12:21
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
public final class Piloting {

    private final ControllerEnvironment environment;

    public Piloting (ControllerEnvironment environment) {
        this.environment = environment;
    }

    public final ScheduledExecutorService create (Predicate<Controller> locator, Map<Component.Identifier, Consumer<Event>> controls) {
        ScheduledExecutorService single;

        single = Executors.newSingleThreadScheduledExecutor ();
        single.scheduleAtFixedRate (new Processing (supply (environment::getControllers, locator), controls), 0, 50, TimeUnit.MILLISECONDS);

        return single;
    }

    final static class Processing implements Runnable {

        private final Supplier<Controller>                       controller;
        private final Map<Component.Identifier, Consumer<Event>> controls;

        Processing (Supplier<Controller> controller, Map<Component.Identifier, Consumer<Event>> controls) {
            this.controller = controller;
            this.controls   = controls;
        }

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
                trylog (controls.getOrDefault (component.getIdentifier (), ev -> {})).accept (event);
            }
        }

        private    Optional<Controller> first () {
            return Optional.ofNullable (controller.get ());
        }

    }

}
