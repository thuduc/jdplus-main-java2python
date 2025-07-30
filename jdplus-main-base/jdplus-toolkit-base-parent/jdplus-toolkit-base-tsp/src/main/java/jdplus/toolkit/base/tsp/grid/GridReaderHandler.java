package jdplus.toolkit.base.tsp.grid;

import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.tsp.util.ObsFormat;
import jdplus.toolkit.base.tsp.util.PropertyHandler;
import lombok.NonNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

@lombok.Builder(toBuilder = true)
public final class GridReaderHandler implements PropertyHandler<GridReader> {

    @NonNull
    private final PropertyHandler<ObsFormat> format;

    @NonNull
    private final PropertyHandler<ObsGathering> gathering;

    @NonNull
    private final PropertyHandler<GridLayout> layout;

    @NonNull
    private final PropertyHandler<String> namePattern;

    @NonNull
    private final PropertyHandler<String> nameSeparator;

    @Override
    public @NonNull GridReader get(@NonNull Function<? super String, ? extends CharSequence> properties) {
        return GridReader
                .builder()
                .format(format.get(properties))
                .gathering(gathering.get(properties))
                .layout(layout.get(properties))
                .namePattern(namePattern.get(properties))
                .nameSeparator(nameSeparator.get(properties))
                .build();
    }

    @Override
    public void set(@NonNull BiConsumer<? super String, ? super String> properties, GridReader value) {
        if (value != null) {
            format.set(properties, value.getFormat());
            gathering.set(properties, value.getGathering());
            layout.set(properties, value.getLayout());
            namePattern.set(properties, value.getNamePattern());
            nameSeparator.set(properties, value.getNameSeparator());
        }
    }
}
