package de.devoxx4kids.dronecontroller.jfx.utility;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 10.04.2016
 * Time: 12:48
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
public final class Lambda {

    public final static <T> Consumer<T> trylog (Consumer<T> consumer) {
        return (item) -> {
            try {
                consumer.accept (item);
            } catch (RuntimeException e) {
                Logger logger = LoggerFactory.getLogger (MethodHandles.lookup ().lookupClass ());
                       logger.error (e.getMessage (), e);
            }
        };
    }

}
