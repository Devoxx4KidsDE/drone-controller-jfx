package de.devoxx4kids.dronecontroller.jfx.utility;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

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

    public final static <T> Supplier<T> supply (Supplier<T[]> controllers, Predicate<? super T> locator) {
        return                          supply (              controllers,                      locator, () -> null);
    }

    public final static <T> Supplier<T> supply (Supplier<T[]> controllers, Predicate<? super T> locator, Supplier<T> defaultvalue) {
        return () -> Stream.generate (controllers).limit (1).filter (Objects::nonNull)
                           .flatMap  (Arrays::stream)
                               .filter (locator)
                                   .findFirst ().orElseGet (defaultvalue);
    }

}
