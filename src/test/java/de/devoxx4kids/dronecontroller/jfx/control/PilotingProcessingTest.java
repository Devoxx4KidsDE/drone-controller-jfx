package de.devoxx4kids.dronecontroller.jfx.control;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Component.Identifier.Button;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Date: 13.04.2016
 * Time: 09:41
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
@RunWith (MockitoJUnitRunner.class)
public class PilotingProcessingTest implements PilotingTrait {

    @Mock
    private Controller controller;

    private Map<Identifier, Component> components;

    private Map<Identifier, Consumer<Event>> controls;

    {
        controls = new HashMap<> ();
        //noinspection unchecked
        controls.put (Button._0, spy (Consumer.class));
        //noinspection unchecked
        controls.put (Button._1, spy (Consumer.class));
        //noinspection unchecked
        controls.put (Button._2, spy (Consumer.class));

        components = new HashMap<> ();
        components.put (Button._0, component (Button._0));
        components.put (Button._1, component (Button._1));
        components.put (Button._2, component (Button._2));
    }

    @Test
    public void fireNothingNoController () throws Exception {
        subject (() -> null, controls).run ();

        never (control (Button._0));
        never (control (Button._1));
        never (control (Button._2));
    }

    @Test
    public void fireNothingNoPoll () throws Exception {
        when (controller.poll ()).thenReturn (false);

        subject (() -> controller, controls).run ();

        never (control (Button._0));
        never (control (Button._1));
        never (control (Button._2));
    }

    @Test
    public void fireNothingNoEvent () throws Exception {
        EventQueue queue = queue ();

        when (controller.poll ()).thenReturn (true);
        when (controller.getEventQueue ()).thenReturn (queue);

        subject (() -> controller, controls).run ();

        never (control (Button._0));
        never (control (Button._1));
        never (control (Button._2));
    }

    @Test
    public void fireNothingNoConsumer () throws Exception {
        EventQueue queue = queue (event (component (Button._10), 1.f, 0));

        when (controller.poll ()).thenReturn (true);
        when (controller.getEventQueue ()).thenReturn (queue);

        subject (() -> controller, controls).run ();

        never (control (Button._0));
        never (control (Button._1));
        never (control (Button._2));
    }

    @Test
    public void fireEvent () throws Exception {
        EventQueue queue = queue (event (component (Button._0), 1.f, 0));

        when (controller.poll ()).thenReturn (true);
        when (controller.getEventQueue ()).thenReturn (queue);

        subject (() -> controller, controls).run ();

        ArgumentCaptor<Event> argument;

                                    argument = ArgumentCaptor.forClass (Event.class);
        once  (control (Button._0), argument::capture);
        never (control (Button._1));
        never (control (Button._2));

        assertEquals ("value", 1.f, argument.getValue ().getValue (), 0.01);
        assertEquals ("nano",    0, argument.getValue ().getNanos ());
        assertEquals ("component", component (Button._0), argument.getValue ().getComponent ());
    }

    @Test
    public void fireEventEx () throws Exception {
        doThrow (new RuntimeException ("button zero test exception")).when (control (Button._0)).accept (any ());

        EventQueue queue = queue (
            event (component (Button._0), 1.f, 0),
            event (component (Button._1), 1.f, 0),
            event (component (Button._2), 1.f, 0)
        );

        when (controller.poll ()).thenReturn (true);
        when (controller.getEventQueue ()).thenReturn (queue);

        subject (() -> controller, controls).run ();

        once (control (Button._0));
        once (control (Button._1));
        once (control (Button._2));
    }

    @Test
    public void fireEventImmutable () throws Exception {
        EventQueue queue = queue (
            event (component (Button._0), 1.f, 0),
            event (component (Button._0), 1.f, 0)
        );

        when (controller.poll ()).thenReturn (true);
        when (controller.getEventQueue ()).thenReturn (queue);

        subject (() -> controller, controls).run ();

        ArgumentCaptor<Event> argument;

                                                 argument = ArgumentCaptor.forClass (Event.class);
        dynamic (control (Button._0), times (2), argument::capture);
        never   (control (Button._1));
        never   (control (Button._2));

        assertNotEquals (
            argument.getAllValues ().get (0),
            argument.getAllValues ().get (1)
        );
    }

    public Component component (Identifier key) {
        return components.computeIfAbsent (key, PilotingTrait.super::component);
    }

    private Consumer<Event> control (Identifier key) {
        return controls.get (key);
    }

    private Piloting.Processing subject (Supplier<Controller> controller, Map<Identifier, Consumer<Event>> controls) {
        return new Piloting.Processing (controller, controls);
    }

}
