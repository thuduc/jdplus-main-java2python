/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.toolkit.desktop.plugin.ui;

import jdplus.toolkit.base.api.dstats.RandomNumberGenerator;
import jdplus.toolkit.base.api.timeseries.*;
import jdplus.toolkit.base.core.random.XorshiftRNG;
import jdplus.toolkit.base.tsp.TsMeta;
import lombok.NonNull;
import nbbrd.design.BuilderPattern;

import java.time.ZoneOffset;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

/**
 * @author PALATEJ
 */
@BuilderPattern(Ts.class)
public final class DemoTsBuilder {

    @NonNull
    public static TsCollection randomTsCollection(int nSeries) {
        DemoTsBuilder builder = new DemoTsBuilder();
        return IntStream
                .range(0, nSeries)
                .mapToObj(i -> builder.name("S" + i).build())
                .collect(TsCollection.toTsCollection());
    }

    private String name;
    private BiFunction<Integer, RandomNumberGenerator, double[]> generator;
    private int forecastCount;
    private int missingCount;
    private int obsCount;
    private RandomNumberGenerator rng;
    private TsPeriod start;

    public DemoTsBuilder() {
        this.name = "";
        this.forecastCount = 0;
        this.missingCount = 0;
        this.obsCount = 24;
        this.rng = new XorshiftRNG(0);
        this.start = TsPeriod.monthly(2010, 1);
        this.generator = (x, y) -> generateValues(x, y, start.start().toInstant(ZoneOffset.UTC).toEpochMilli());
    }

    @NonNull
    public DemoTsBuilder name(@NonNull String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    @NonNull
    public DemoTsBuilder start(@NonNull TsPeriod start) {
        this.start = Objects.requireNonNull(start);
        return this;
    }

    @NonNull
    public DemoTsBuilder generator(@NonNull BiFunction<Integer, RandomNumberGenerator, double[]> generator) {
        this.generator = Objects.requireNonNull(generator);
        return this;
    }

    @NonNull
    public DemoTsBuilder forecastCount(int forecastCount) {
        this.forecastCount = forecastCount;
        return this;
    }

    @NonNull
    public DemoTsBuilder missingCount(int missingCount) {
        this.missingCount = missingCount;
        return this;
    }

    @NonNull
    public DemoTsBuilder obsCount(int obsCount) {
        this.obsCount = obsCount;
        return this;
    }

    @NonNull
    public DemoTsBuilder rng(@NonNull RandomNumberGenerator rng) {
        this.rng = Objects.requireNonNull(rng);
        return this;
    }

    public Ts build() {
        Ts.Builder result = Ts.builder().name(name).moniker(TsMoniker.of());
        double[] values = generator.apply(obsCount, rng);
        if (missingCount > 0 && values.length > 0) {
            for (int x = 0; x < missingCount; x++) {
                values[rng.nextInt(values.length)] = Double.NaN;
            }
        }
        TsData data = TsData.ofInternal(start, values);
        if (forecastCount > 0) {
            TsMeta.END.store(result::meta, data.getDomain().get(data.length() - forecastCount - 1).end());
        }
        result.data(data);
        return result.build();
    }

    private static double[] generateValues(int obsCount, RandomNumberGenerator rng, long startTimeMillis) {
        int seriesIndex = rng.nextInt();
        double[] result = new double[obsCount];
        for (int j = 0; j < obsCount; j++) {
            result[j] = Math.abs((100 * (Math.cos(startTimeMillis * seriesIndex))) + (100 * (Math.sin(startTimeMillis) - Math.cos(rng.nextDouble()) + Math.tan(rng.nextDouble()))));
        }
        return result;
    }
}
