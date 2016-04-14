package de.devoxx4kids.dronecontroller.jfx.control;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Date: 14.04.2016
 * Time: 12:08
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
public interface PilotingTrait {

    public default Component component (Identifier key) {
        Component spy;
               spy = spy (Component.class);
        when  (spy.getIdentifier ()).thenReturn (key);
        return spy;
    }

    public default Event event (Component component, float value, int nanos) {
        Event event = new Event ();
              event.set (component, value, nanos);

        return event;
    }

    public default EventQueue queue (Event ...    events) throws Exception {
        Method add = EventQueue.class.getDeclaredMethod ("add", Event.class);
               add.setAccessible (true);

        try {
            BiFunction<EventQueue, Event, ?> newevent = (obj, args) -> {
                try {
                    return add.invoke (obj, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException (e);
                }
            };

            EventQueue queue = new EventQueue (events.length);

            Arrays.stream (events).forEach (event -> newevent.apply (queue, event));

            return queue;
        } finally {
            add.setAccessible (false);
        }
    }

    public default <T> void never (Consumer<? super T> consumer) {
                            never (consumer, Matchers::any);
    }

    public default <T> void never   (Consumer<? super T> consumer, Supplier<T> argument) {
                            dynamic (consumer, Mockito.never (), argument);
    }

    public default <T> void once  (Consumer<? super T> consumer) {
                            once  (                    consumer, Matchers::any);
    }

    public default <T> void once    (Consumer<? super T> consumer, Supplier<T>        argument) {
                            dynamic (                    consumer, Mockito.times (1), argument);
    }

    public default <T> void dynamic (Consumer<? super T> consumer, VerificationMode mode) {
                            dynamic (                    consumer,                  mode, Matchers::any);
    }

    public default <T> void dynamic (Consumer<? super T> consumer, VerificationMode mode, Supplier<T> argument) {
        Mockito.verify (consumer, mode).accept (argument.get ());
    }

}
