package de.devoxx4kids.dronecontroller.jfx.control;

import java.io.Closeable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Date: 13.04.2016
 * Time: 09:08
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
@RunWith (MockitoJUnitRunner.class)
public class PilotingTest implements PilotingTrait {

    @Mock
    private ControllerEnvironment environment;

    @Mock
    private Controller controller;

    private final List<Closeable> closeables = new LinkedList<> ();

    @Test
    public void schedule () throws Exception {
                             subject ().awaitTermination (50, TimeUnit.MILLISECONDS);
        assertEquals (false, subject ().isShutdown ());
    }

    @Test (timeout = 1000)
    public void schedulePeriod () throws Exception {
        CountDownLatch latch = new CountDownLatch (2);

        when (controller.poll ()).thenReturn (true);
        when (controller.getEventQueue ()).thenAnswer (invocation -> {
            latch.countDown ();
            return queue (event (component (Button._0), 1.f, 0));
        });

        when (environment.getControllers ()).thenReturn (new Controller[] { controller });

        subject (maybe -> maybe == controller);

        latch.await ();

        verify (controller, atLeast (2)).getEventQueue ();
        verify (controller, atLeast (2)).poll ();
    }

    @Test
    public void task () throws Exception {
        EventQueue queue = queue (event (component (Button._0), 1.f, 0));

        when (controller.poll ()).thenReturn (true);
        when (controller.getEventQueue ()).thenReturn (queue);

        when (environment.getControllers ()).thenReturn (new Controller[] { controller });

        @SuppressWarnings ("unchecked") Consumer<Event> control = spy (Consumer.class);

        Map<Component.Identifier, Consumer<Event>> controls = new HashMap<> ();
                                                   controls.put (Button._0, control);

        subject (maybe -> maybe == controller, controls).awaitTermination (50, TimeUnit.MILLISECONDS);

        once (control);
    }

    @After
    public void teardown () {
        closeables.forEach (c -> { try { c.close (); } catch (Exception e) {} });
    }

    private ScheduledExecutorService subject () {
        return                       subject (controller -> false);
    }

    private ScheduledExecutorService subject (Predicate<Controller> predicate) {
        return                       subject (                      predicate, new HashMap<> ());
    }

    private ScheduledExecutorService subject (Predicate<Controller> predicate, Map<Component.Identifier, Consumer<Event>> controls) {
        Piloting piloting = new Piloting (environment);

        ScheduledExecutorService service;

                        service = piloting.create (predicate, controls);
        closeables.add (service::shutdown);

        return service;
    }

}
