package de.devoxx4kids.dronecontroller.jfx.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

/**
 * Date: 10.04.2016
 * Time: 13:17
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
public final class ImageConverter implements Consumer<byte[]> {

    private final Consumer<byte[]> delegate;
    private final String           type;

    private final ByteArrayOutputStream outstream;

    public ImageConverter (Consumer<byte[]> delegate, String type) {
        this (delegate, type, 1024 * 1024);
    }

    public ImageConverter (Consumer<byte[]> delegate, String type, int capacity) {
        this.delegate  = delegate;
        this.type      = type;
        this.outstream = new ByteArrayOutputStream (capacity);
    }

    @Override
    public final void accept (byte[] bytes) {
        try {
            outstream.reset ();

            ImageIO.write (ImageIO.read (new ByteArrayInputStream (bytes)), type, outstream);

            delegate.accept (outstream.toByteArray ());
        } catch (IOException e) {
            throw new RuntimeException (e);
        }
    }

}
