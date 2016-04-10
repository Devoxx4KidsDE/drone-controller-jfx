package de.devoxx4kids.dronecontroller.jfx.drone;

import java.util.function.Consumer;

/**
 * Date: 10.04.2016
 * Time: 17:56
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
public interface Drone {

    public void start ();

    public void shutdown ();

    public void stream (Consumer<byte[]> video);

}
