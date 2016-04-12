package de.devoxx4kids.dronecontroller.jfx.utility;

import java.util.function.Consumer;

import org.junit.Test;

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

}
