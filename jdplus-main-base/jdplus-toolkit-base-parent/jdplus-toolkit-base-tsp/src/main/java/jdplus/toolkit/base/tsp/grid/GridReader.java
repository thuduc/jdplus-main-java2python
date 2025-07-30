/*
 * Copyright 2017 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.tsp.grid;

import internal.toolkit.base.tsp.grid.InternalValueReader;
import internal.toolkit.base.tsp.grid.MarkableStream;
import internal.toolkit.base.tsp.grid.TsDataBuilders;
import jdplus.toolkit.base.api.timeseries.Ts;
import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsInformationType;
import jdplus.toolkit.base.api.timeseries.util.ObsGathering;
import jdplus.toolkit.base.api.timeseries.util.TsDataBuilder;
import jdplus.toolkit.base.api.util.MultiLineNameUtil;
import jdplus.toolkit.base.tsp.fixme.Substitutor;
import jdplus.toolkit.base.tsp.util.ObsFormat;
import lombok.AccessLevel;
import lombok.NonNull;
import nbbrd.design.LombokWorkaround;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static jdplus.toolkit.base.tsp.grid.GridLayout.HORIZONTAL;
import static jdplus.toolkit.base.tsp.grid.GridLayout.VERTICAL;

/**
 * @author Philippe Charles
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public final class GridReader {

    public static final GridReader DEFAULT = builder().build();

    @lombok.NonNull
    private ObsFormat format;

    @lombok.NonNull
    private ObsGathering gathering;

    @lombok.NonNull
    private GridLayout layout;

    @lombok.NonNull
    private String namePattern;

    @lombok.NonNull
    private String nameSeparator;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .format(ObsFormat.DEFAULT)
                .gathering(ObsGathering.DEFAULT)
                .layout(GridLayout.UNDEFINED)
                .namePattern("S${index}")
                .nameSeparator(MultiLineNameUtil.SEPARATOR);
    }

    @NonNull
    public TsCollection read(@NonNull GridInput input) throws IOException {
        TsCollection.Builder output = TsCollection.builder()
                .type(TsInformationType.Data)
                .name(input.getName());

        try (TypedInputStream stream = TypedInputStream.of(input.getDataTypes(), format, input.open())) {
            if (isSeriesByRow(stream)) {
                readSeriesByRow(stream, output);
            } else {
                readPeriodByRow(stream, output);
            }
        }

        return output.build();
    }

    private boolean isSeriesByRow(TypedInputStream stream) throws IOException {
        return layout.equals(HORIZONTAL) || peekSeriesByRow(stream);
    }

    private void readSeriesByRow(TypedInputStream input, TsCollection.Builder output) throws IOException {
        output.meta(GridLayout.PROPERTY, HORIZONTAL.name());

        SeriesByRowHead head = SeriesByRowHead.peek(input);

        TypedInputStreamFunc<String> names = head.getNameFunc(namePattern, nameSeparator);

        TsDataBuilder<LocalDateTime> data = TsDataBuilder.byDateTime(gathering);

        head.skip(input);
        while (input.readRow()) {
            String nullableName = names.apply(input);
            for (int col = 0; col < head.columns && input.readCell(); col++) {
                data.add(head.getPeriod(col), input.getNumber());
            }
            TsData result = data.build();
            if (isNotVoid(nullableName, result)) {
                output.item(Ts.builder()
                        .type(TsInformationType.Data)
                        .name(fixNullName(nullableName))
                        .data(result)
                        .build());
            }
            data.clear();
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class SeriesByRowHead {

        private final int firstObsIndex;
        private final int columns;
        private final List<LocalDateTime> dates;

        static SeriesByRowHead peek(TypedInputStream stream) throws IOException {
            stream.mark();
            try {
                List<LocalDateTime> dates = stream.readRow()
                        ? readCells(stream, TypedInputStream::getDateTime)
                        : Collections.emptyList();
                int firstObsIndex = getFirstObsIndex(dates);
                dates = dates.subList(firstObsIndex, getLastObsIndex(dates) + 1);
                return new SeriesByRowHead(firstObsIndex, dates.size(), dates);
            } finally {
                stream.reset();
            }
        }

        private static int getFirstObsIndex(List<LocalDateTime> dates) {
            return IntStream.range(0, dates.size())
                    .filter(i -> dates.get(i) != null)
                    .findFirst()
                    .orElse(0);
        }

        private static int getLastObsIndex(List<LocalDateTime> dates) {
            return GridWriter.reverseRange(0, dates.size())
                    .filter(i -> dates.get(i) != null)
                    .findFirst()
                    .orElse(0);
        }

        void skip(TypedInputStream stream) throws IOException {
            stream.readRow();
        }

        LocalDateTime getPeriod(int column) {
            return dates.get(column);
        }

        TypedInputStreamFunc<String> getNameFunc(String namePattern, String nameSeparator) {
            switch (firstObsIndex) {
                case 0:
                    Supplier<String> nameGenerator = getNameGenerator(namePattern);
                    return (stream) -> nameGenerator.get();
                case 1:
                    return (stream) -> stream.readCell() ? stream.getString() : NULL_NAME;
                default:
                    Collector<CharSequence, ?, String> nameJoiner = Collectors.joining(nameSeparator);
                    String[] path = new String[firstObsIndex];
                    return (stream) -> {
                        boolean hasHeader = false;
                        for (int index = 0; index < path.length && stream.readCell(); index++) {
                            String name = stream.getString();
                            if (name != null) {
                                hasHeader = true;
                                path[index] = name;
                            } else if (hasHeader) {
                                path[index] = null;
                            }
                        }
                        return !hasHeader ? NULL_NAME : joinSkippingNulls(path, nameJoiner);
                    };
            }
        }
    }

    private void readPeriodByRow(TypedInputStream input, TsCollection.Builder output) throws IOException {
        output.meta(GridLayout.PROPERTY, VERTICAL.name());

        PeriodByRowHead head = PeriodByRowHead.peek(input);

        TsDataBuilders<LocalDateTime> data = TsDataBuilders.byDateTime(head.columns, gathering);

        head.skip(input);
        while (input.readRow()) {
            if (input.readCell()) {
                LocalDateTime period = input.getDateTime();
                if (period != null) {
                    for (int col = 0; col < head.columns && input.readCell(); col++) {
                        data.add(col, period, input.getNumber());
                    }
                }
            }
        }

        List<String> names = head.getNames(namePattern, nameSeparator);
        for (int column = 0; column < head.columns; column++) {
            String nullableName = names.get(column);
            TsData result = data.build(column);
            if (isNotVoid(nullableName, result)) {
                output.item(Ts.builder()
                        .type(TsInformationType.Data)
                        .name(fixNullName(nullableName))
                        .data(result)
                        .build());
            }
        }
    }

    @lombok.RequiredArgsConstructor
    private static final class PeriodByRowHead {

        private final int rows;
        private final int columns;
        private final String[][] values;

        static PeriodByRowHead peek(TypedInputStream stream) throws IOException {
            stream.mark();
            try {
                List<String> line;

                // empty
                if (!stream.readRow()) {
                    return new PeriodByRowHead(0, 0, new String[0][]);
                }

                // level 0
                if ((line = readNameLine(stream)) == null) {
                    int columns = countConsecutiveNonNullCellNumber(stream);
                    return new PeriodByRowHead(0, columns, new String[0][]);
                }

                // level 1
                List<String> firstLine = line;
                if (!stream.readRow() || (line = readNameLine(stream)) == null) {
                    return new PeriodByRowHead(1, firstLine.size(), new String[][]{firstLine.toArray(new String[firstLine.size()])});
                }

                // level n
                List<String[]> lines = new ArrayList<>();
                lines.add(firstLine.toArray(new String[firstLine.size()]));
                do {
                    lines.add(line.toArray(new String[firstLine.size()]));
                } while (stream.readRow() && (line = readNameLine(stream)) != null);
                return new PeriodByRowHead(lines.size(), firstLine.size(), lines.toArray(new String[lines.size()][]));
            } finally {
                stream.reset();
            }
        }

        private static int countConsecutiveNonNullCellNumber(TypedInputStream stream) throws IOException {
            int result = 0;
            while (stream.readCell() && stream.getNumber() != null) {
                result++;
            }
            return result;
        }

        private static List<String> readNameLine(TypedInputStream stream) throws IOException {
            return stream.readCell() && stream.getDateTime() == null
                    ? readCells(stream, TypedInputStream::getString)
                    : null;
        }

        @SuppressWarnings("StatementWithEmptyBody")
        void skip(TypedInputStream stream) throws IOException {
            for (int row = 0; row < rows && stream.readRow(); row++) {
            }
        }

        List<String> getNames(String namePattern, String nameSeparator) {
            switch (rows) {
                case 0: {
                    return Stream
                            .generate(getNameGenerator(namePattern))
                            .limit(columns)
                            .collect(Collectors.toList());
                }
                case 1: {
                    return Stream.of(values[0])
                            .limit(columns)
                            .collect(Collectors.toList());
                }
                default: {
                    Collector<CharSequence, ?, String> nameJoiner = Collectors.joining(nameSeparator);
                    String[] path = new String[rows];
                    List<String> result = new ArrayList<>();
                    for (int column = 0; column < columns; column++) {
                        boolean hasHeader = false;
                        for (int row = 0; row < rows - 1; row++) {
                            String name = values[row][column];
                            if (name != null) {
                                hasHeader = true;
                                path[row] = name;
                            }
                        }
                        String lastCell = values[rows - 1][column];
                        if (lastCell != null || hasHeader) {
                            path[rows - 1] = lastCell;
                            result.add(joinSkippingNulls(path, nameJoiner));
                        } else {
                            result.add(null);
                        }
                    }
                    return result;
                }
            }
        }
    }

    private static String joinSkippingNulls(String[] items, Collector<CharSequence, ?, String> nameJoiner) {
        return Stream.of(items).filter(Objects::nonNull).collect(nameJoiner);
    }

    @lombok.RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TypedInputStream implements Closeable {

        static TypedInputStream of(Set<GridDataType> dataTypes, ObsFormat format, GridInput.Stream stream) {

            InternalValueReader<String> string;
            InternalValueReader<LocalDateTime> dateTimeFallback;
            InternalValueReader<Number> numberFallback;

            if (dataTypes.contains(GridDataType.STRING)) {
                string = InternalValueReader.onString();
                dateTimeFallback = InternalValueReader.onStringParser(format.dateTimeParser()::parse);
                numberFallback = InternalValueReader.onStringParser(format.numberParser()::parse);
            } else {
                string = InternalValueReader.onNull();
                dateTimeFallback = InternalValueReader.onNull();
                numberFallback = InternalValueReader.onNull();
            }

            InternalValueReader<LocalDateTime> dateTime
                    = dataTypes.contains(GridDataType.LOCAL_DATE_TIME)
                    ? InternalValueReader.onDateTime().or(dateTimeFallback)
                    : dateTimeFallback;

            InternalValueReader<Number> number
                    = dataTypes.contains(GridDataType.DOUBLE)
                    ? InternalValueReader.onNumber().or(numberFallback)
                    : numberFallback;

            return new TypedInputStream(string, dateTime, number, new MarkableStream(stream));
        }

        @lombok.NonNull
        private final InternalValueReader<String> string;

        @lombok.NonNull
        private final InternalValueReader<LocalDateTime> dateTime;

        @lombok.NonNull
        private final InternalValueReader<Number> number;

        @lombok.NonNull
        @lombok.experimental.Delegate
        private final MarkableStream delegate;

        @Nullable
        public Number getNumber() throws IOException {
            return number.read(delegate.getCell());
        }

        @Nullable
        public LocalDateTime getDateTime() throws IOException {
            return dateTime.read(delegate.getCell());
        }

        @Nullable
        public String getString() throws IOException {
            return string.read(delegate.getCell());
        }
    }

    private interface TypedInputStreamFunc<T> {

        T apply(TypedInputStream stream) throws IOException;
    }

    private static boolean peekSeriesByRow(TypedInputStream typedStream) throws IOException {
        typedStream.mark();
        try {
            if (typedStream.readRow()) {
                // skip first cell
                if (typedStream.readCell()) {
                    // search for datetime in subsequent cells
                    while (typedStream.readCell()) {
                        if (typedStream.getDateTime() != null) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } finally {
            typedStream.reset();
        }
    }

    private static <T> List<T> readCells(TypedInputStream stream, TypedInputStreamFunc<T> func) throws IOException {
        List<T> result = new ArrayList<>();
        while (stream.readCell()) {
            result.add(func.apply(stream));
        }
        return result;
    }

    private static Supplier<String> getNameGenerator(String namePattern) {
        AtomicInteger index = new AtomicInteger(-1);
        Substitutor substitutor = getIndexSubstitutor(index);
        return () -> {
            index.incrementAndGet();
            return substitutor.replace(namePattern);
        };
    }

    private static Substitutor getIndexSubstitutor(AtomicInteger counter) {
        return Substitutor.of(key -> {
            switch (key) {
                case "index":
                    return counter.get();
                case "number":
                    return counter.get() + 1;
                default:
                    return null;
            }
        });
    }

    private static final String NULL_NAME = null;

    private static boolean isNotVoid(String nullableName, TsData result) {
        return nullableName != null || !result.isEmpty();
    }

    private static String fixNullName(String name) {
        return name != null ? name : "null";
    }
}
