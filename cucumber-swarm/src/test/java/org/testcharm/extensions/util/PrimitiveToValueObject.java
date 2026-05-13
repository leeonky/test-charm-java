package org.testcharm.extensions.util;

import io.cucumber.plugin.event.Location;
import org.testcharm.util.Converter;
import org.testcharm.util.ConverterExtension;

import java.net.URI;
import java.nio.file.Paths;
import java.time.Duration;

public class PrimitiveToValueObject implements ConverterExtension {
    @Override
    public void extend(Converter converter) {
        converter.addTypeConverter(Integer.class, Location.class, i -> new Location(i, 0));
        converter.addTypeConverter(String.class, URI.class, s -> Paths.get(s).toUri());
        converter.addTypeConverter(Integer.class, Duration.class, i -> Duration.ofMillis(i));
    }
}
