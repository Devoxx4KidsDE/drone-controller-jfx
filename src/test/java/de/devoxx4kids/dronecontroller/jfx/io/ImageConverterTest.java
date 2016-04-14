package de.devoxx4kids.dronecontroller.jfx.io;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

/**
 * Date: 12.04.2016
 * Time: 15:19
 *
 * @author Michael Clausen
 * @version $Id: $Id
 */
@RunWith (MockitoJUnitRunner.class)
public class ImageConverterTest {

    private final BiFunction<ImageReader, byte[], Void> isBMP = assertType ("bmp");

    @Test
	public void convertJpg2Bmp () throws IOException, URISyntaxException {
        new ImageConverter (assertImage (isBMP), "bmp"      ).accept (resource ("/image/sample.jpg"));
        new ImageConverter (assertImage (isBMP), "bmp", 1024).accept (resource ("/image/sample.jpg"));
    }

    @Test
    public void convertJpg2BmpMany () throws IOException, URISyntaxException {
        ImageConverter converter = new ImageConverter (assertImage (isBMP, assertSize (400 * 600 * 3 + 54)), "bmp");

        converter.accept (resource ("/image/sample.jpg"));
        converter.accept (resource ("/image/sample.jpg"));
    }

    private BiFunction<ImageReader, byte[], Void> assertSize (int length) {
        return (reader, bytes) -> {
            assertEquals (length, bytes.length);
            return null;
        };
    }

    private BiFunction<ImageReader, byte[], Void> assertType (String type) {
        return (reader, bytes) -> {
            try {
                assertEquals (type, reader.getFormatName ());
            } catch (IOException e) {
                e.printStackTrace ();
            }
            return null;
        };
    }

    @SafeVarargs
    private final Consumer<byte[]> assertImage (BiFunction<ImageReader, byte[], ?> ... assertions) {
        return bytes -> {
            try (ImageInputStream iis = ImageIO.createImageInputStream (new ByteArrayInputStream (bytes))) {
                Iterator<ImageReader> readers;

                                    readers = ImageIO.getImageReaders (iis);
                assertEquals (true, readers.hasNext ());

                Iterable<ImageReader> iterable = () -> readers;

                StreamSupport.stream (iterable.spliterator (), false)
                                 .findFirst ()
                                     .ifPresent (reader -> Arrays.stream (assertions).forEach (fun -> fun.apply (reader, bytes)));
            } catch (IOException            e) {
                throw new RuntimeException (e);
            }
        };
    }

    private byte[] resource (String name) throws URISyntaxException, IOException {
        return Files.readAllBytes (
                    Paths.get (ImageConverterTest.class.getResource (name).toURI ())
               );
    }

}
