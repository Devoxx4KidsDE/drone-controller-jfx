package de.devoxx4kids.dronecontroller.jfx.utility;

import java.util.function.Consumer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Date: 12.04.2016
 * Time: 16:42
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
public class LambdaTest {

    @Test
    public void trylog () {
        Consumer<Integer> spy;

                       spy = spy (Consumer.class);
        Lambda.trylog (spy).accept (42);

        verify (spy, times (1)).accept (42);
    }

    @Test
    public void trylogEx () {
        Consumer<Integer> spy;

                                               spy = mock (Consumer.class);
        doThrow (RuntimeException.class).when (spy).accept (42);

        Lambda.trylog (spy).accept (42);

        verify (spy, times (1)).accept (42);
    }

    @Test
    public void supply () throws Exception {
        Integer[] values = {0, 1, 2, 3, 4};

        assertEquals (2, (int) Lambda.supply (() -> values, t -> t == 2).get ());
    }

    @Test
    public void supplyDefault () throws Exception {
        Integer[] values = {0, 1};

        assertEquals ((int) Lambda.supply (() -> values, t -> t == 2, () -> 2).get (), 2);
    }

    @Test
    public void supplyUnknown () throws Exception {
        Integer[] values = {0, 1};

        assertEquals (null, Lambda.supply (() -> values, t -> t == 2).get ());
    }

    @Test
    public void supplyNull () throws Exception {
        Integer[] values = null;

        assertEquals (null, Lambda.supply (() -> values, t -> t == 2).get ());
    }

    @Test
    public void supplyEmpty () throws Exception {
        Integer[] values = {};

        assertEquals (null, Lambda.supply (() -> values, t -> t == 2).get ());
    }

}
