/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.tramoseats.desktop.plugin.tramoseats.ui;

import jdplus.toolkit.desktop.plugin.TsDynamicProvider;
import jdplus.toolkit.desktop.plugin.modelling.ForecastsFactory;
import jdplus.toolkit.desktop.plugin.modelling.InputFactory;
import jdplus.toolkit.desktop.plugin.modelling.LikelihoodFactory;
import jdplus.toolkit.desktop.plugin.modelling.ModelArimaFactory;
import jdplus.toolkit.desktop.plugin.modelling.ModelRegressorsFactory;
import jdplus.toolkit.desktop.plugin.modelling.NiidTestsFactory;
import jdplus.toolkit.desktop.plugin.modelling.OutOfSampleTestFactory;
import jdplus.sa.desktop.plugin.processing.BenchmarkingUI;
import jdplus.sa.desktop.plugin.processing.SIFactory;
import jdplus.sa.desktop.plugin.ui.DemetraSaUI;
import jdplus.sa.desktop.plugin.ui.SaViews;
import jdplus.sa.desktop.plugin.ui.WkComponentsUI;
import jdplus.sa.desktop.plugin.ui.WkInformation;
import jdplus.sa.desktop.plugin.ui.WkFinalEstimatorsUI;
import jdplus.toolkit.desktop.plugin.ui.processing.GenericChartUI;
import jdplus.toolkit.desktop.plugin.ui.processing.GenericTableUI;
import jdplus.toolkit.desktop.plugin.ui.processing.HtmlItemUI;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.IProcDocumentViewFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentItemFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.ProcDocumentViewFactory;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.EstimationUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.ResidualsDistUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.ResidualsUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.RevisionHistoryUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.SlidingSpansUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.SpectrumUI;
import jdplus.toolkit.desktop.plugin.ui.processing.stats.StabilityUI;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlElements;
import jdplus.toolkit.desktop.plugin.html.HtmlFragment;
import jdplus.toolkit.desktop.plugin.html.HtmlHeader;
import jdplus.toolkit.desktop.plugin.html.core.HtmlDiagnosticsSummary;
import jdplus.toolkit.base.api.information.Explorable;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.modelling.ComponentInformation;
import jdplus.toolkit.base.api.modelling.SeriesInfo;
import jdplus.toolkit.base.api.processing.ProcDiagnostic;
import jdplus.sa.base.api.ComponentDescriptor;
import jdplus.sa.base.api.ComponentType;
import jdplus.sa.base.api.EstimationPolicyType;
import jdplus.sa.base.api.SaDictionaries;
import jdplus.sa.base.api.SaManager;
import jdplus.sa.base.api.SaProcessingFactory;
import jdplus.sa.base.api.SeriesDecomposition;
import jdplus.sa.base.api.StationaryVarianceDecomposition;
import jdplus.sa.desktop.plugin.html.HtmlSaSlidingSpanSummary;
import jdplus.sa.desktop.plugin.html.HtmlSeasonalityDiagnostics;
import jdplus.sa.desktop.plugin.html.HtmlSignificantSeasons;
import jdplus.sa.desktop.plugin.html.HtmlStationaryVarianceDecomposition;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDocument;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.dictionaries.Dictionary;
import jdplus.toolkit.base.api.dictionaries.RegressionDictionaries;
import jdplus.toolkit.desktop.plugin.html.core.HtmlInformationSet;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlRegSarima;
import jdplus.toolkit.desktop.plugin.html.modelling.HtmlUcarima;
import jdplus.tramoseats.base.api.tramo.TramoSpec;
import jdplus.tramoseats.base.api.tramoseats.TramoSeatsSpec;
import jdplus.tramoseats.desktop.plugin.html.HtmlModelBasedRevisionsAnalysis;
import jdplus.tramoseats.desktop.plugin.html.HtmlSeatsGrowthRates;
import jdplus.tramoseats.desktop.plugin.html.HtmlWienerKolmogorovDiagnostics;
import jdplus.tramoseats.base.information.TramoSeatsSpecMapping;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import jdplus.toolkit.base.core.regsarima.regular.RegSarimaModel;
import jdplus.sa.base.core.SaBenchmarkingResults;
import jdplus.sa.base.core.diagnostics.SignificantSeasonalityTest;
import jdplus.sa.base.core.tests.SeasonalityTests;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.tramoseats.base.core.seats.SeatsResults;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.DiagnosticInfo;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.MovingProcessing;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.RevisionHistory;
import jdplus.toolkit.base.core.timeseries.simplets.analysis.SlidingSpans;
import jdplus.tramoseats.base.core.tramo.TramoFactory;
import jdplus.tramoseats.base.core.tramo.TramoKernel;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsDiagnostics;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsResults;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsDocument;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsFactory;
import jdplus.tramoseats.base.core.tramoseats.TramoSeatsKernel;
import jdplus.toolkit.base.core.ucarima.UcarimaModel;
import jdplus.toolkit.base.core.ucarima.WienerKolmogorovDiagnostics;
import jdplus.toolkit.base.core.ucarima.WienerKolmogorovEstimators;
import jdplus.toolkit.desktop.plugin.html.core.HtmlProcessingLog;
import jdplus.toolkit.desktop.plugin.ui.processing.ContextualChartUI;
import jdplus.toolkit.desktop.plugin.ui.processing.ContextualIds;
import jdplus.toolkit.desktop.plugin.ui.processing.ContextualTableUI;
import jdplus.tramoseats.base.api.tramo.TransformSpec;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
public class TramoSeatsViewFactory extends ProcDocumentViewFactory<TramoSeatsDocument> {

    //Seats nodes
    public static final String STOCHASTIC = "Stochastic series", COMPONENTS = "Components",
            STOCHASTIC_TREND = "Trend",
            STOCHASTIC_SA = "SA Series",
            STOCHASTIC_SEAS = "Seasonal",
            STOCHASTIC_IRR = "Irregular",
            MODELBASED = "Model-based tests",
            WKANALYSIS = "WK analysis",
            WK_COMPONENTS = "Components",
            WK_FINALS = "Final estimators",
            WK_PRELIMINARY = "Preliminary estimators",
            WK_ERRORS = "Errors analysis",
            WK_RATES = "Growth rates",
            SIGSEAS = "Significant seasonality",
            STVAR = "Stationary variance decomposition";

    public static final Id DECOMPOSITION_SUMMARY = new LinearId(SaViews.DECOMPOSITION);
    public static final Id DECOMPOSITION_STOCH_TREND = new LinearId(SaViews.DECOMPOSITION, STOCHASTIC, STOCHASTIC_TREND);
    public static final Id DECOMPOSITION_STOCH_SEAS = new LinearId(SaViews.DECOMPOSITION, STOCHASTIC, STOCHASTIC_SEAS);
    public static final Id DECOMPOSITION_STOCH_SA = new LinearId(SaViews.DECOMPOSITION, STOCHASTIC, STOCHASTIC_SA);
    public static final Id DECOMPOSITION_STOCH_IRR = new LinearId(SaViews.DECOMPOSITION, STOCHASTIC, STOCHASTIC_IRR);
    public static final Id DECOMPOSITION_SERIES = new LinearId(SaViews.DECOMPOSITION, STOCHASTIC);
    public static final Id DECOMPOSITION_CMPSERIES = new LinearId(SaViews.DECOMPOSITION, COMPONENTS);
    public static final Id DECOMPOSITION_WK_COMPONENTS = new LinearId(SaViews.DECOMPOSITION, WKANALYSIS, WK_COMPONENTS);
    public static final Id DECOMPOSITION_WK_FINALS = new LinearId(SaViews.DECOMPOSITION, WKANALYSIS, WK_FINALS);
    public static final Id DECOMPOSITION_ERRORS = new LinearId(SaViews.DECOMPOSITION, WK_ERRORS);
    public static final Id DECOMPOSITION_RATES = new LinearId(SaViews.DECOMPOSITION, WK_RATES);
    public static final Id DECOMPOSITION_TESTS = new LinearId(SaViews.DECOMPOSITION, MODELBASED);
    public static final Id DECOMPOSITION_VAR = new LinearId(SaViews.DECOMPOSITION, STVAR);
    public static final Id DECOMPOSITION_SIGSEAS = new LinearId(SaViews.DECOMPOSITION, SIGSEAS);

    private static final AtomicReference<IProcDocumentViewFactory<TramoSeatsDocument>> INSTANCE = new AtomicReference();

    private final static Function<TramoSeatsDocument, RegSarimaModel> MODELEXTRACTOR = source -> {
        TramoSeatsResults tr = source.getResult();
        return tr == null ? null : tr.getPreprocessing();
    };

    private final static Function<TramoSeatsDocument, TramoSeatsDocument> VALIDEXTRACTOR = source -> {
        TramoSeatsResults tr = source.getResult();
        if (tr == null) {
            return null;
        }
        return tr.isValid() ? source : null;
    };

    private final static Function<TramoSeatsDocument, SeatsResults> DECOMPOSITIONEXTRACTOR = source -> {
        TramoSeatsResults tr = source.getResult();
        return tr == null ? null : tr.getDecomposition();
    };

    private final static Function<TramoSeatsDocument, TramoSeatsDiagnostics> DIAGSEXTRACTOR = source -> {
        TramoSeatsResults tr = source.getResult();
        return tr == null ? null : tr.getDiagnostics();
    };

    private final static Function<TramoSeatsDocument, TsData> RESEXTRACTOR = MODELEXTRACTOR
            .andThen(regarima -> regarima == null ? null : regarima.fullResiduals());

    private static Function<SeatsResults, EstimationUI.Information> cmpExtractor(ComponentType type) {

        return (SeatsResults source) -> {
            if (source == null) {
                return null;
            }
            TsData s = source.getInitialComponents().getSeries(type, ComponentInformation.Value);
            if (s == null) {
                return new EstimationUI.Information(null, null, null, null);
            }
            TsData es = source.getInitialComponents().getSeries(type, ComponentInformation.Stdev);
//            TsPeriodSelector sel = new TsPeriodSelector();
//            sel.last(2 * s.getFrequency().intValue());
            TsData fs = source.getInitialComponents().getSeries(type, ComponentInformation.Forecast);
            TsData efs = source.getInitialComponents().getSeries(type, ComponentInformation.StdevForecast);
            LocalDateTime x = s.getDomain().getEndPeriod().start();
            s = TsData.concatenate(s, fs);
            es = TsData.concatenate(es, efs);
//           s = s.select(sel).update(fs);
//            es = es.select(sel).update(efs);
            EstimationUI.Information rslt
                    = new EstimationUI.Information(s, null, s.fastFn(es, (a, b) -> a + b * 1.96), s.fastFn(es, (a, b) -> a - b * 1.96));
            rslt.markers = new LocalDateTime[]{x};
            return rslt;
        };
    }

    public static IProcDocumentViewFactory<TramoSeatsDocument> getDefault() {
        IProcDocumentViewFactory<TramoSeatsDocument> fac = INSTANCE.get();
        if (fac == null) {
            fac = new TramoSeatsViewFactory();
            INSTANCE.lazySet(fac);
        }
        return fac;
    }

    public static void setDefault(IProcDocumentViewFactory<TramoSeatsDocument> factory) {
        INSTANCE.set(factory);
    }

    public TramoSeatsViewFactory() {
        registerFromLookup(TramoSeatsDocument.class
        );
    }

    @Override
    public Id getPreferredView() {
        return SaViews.MAIN_SUMMARY;

    }

//<editor-fold defaultstate="collapsed" desc="INPUT">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1010)
    public static class SpecFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public SpecFactory() {
            super(TramoSeatsDocument.class, SaViews.INPUT_SPEC,
                    (TramoSeatsDocument doc) -> {
                        InformationSet info = TramoSeatsSpecMapping.write(doc.getSpecification(), doc.getInput().getData().getDomain(), true);
                        return new HtmlInformationSet(info);
                    },
                    new HtmlItemUI()
            );
        }

        @Override
        public int getPosition() {
            return 1010;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1020)
    public static class LogFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public LogFactory() {
            super(TramoSeatsDocument.class, SaViews.MAIN_LOG,
                    (TramoSeatsDocument doc) -> {
                        TramoSeatsResults result = doc.getResult();
                        if (result == null) {
                            return null;
                        } else {
                            HtmlProcessingLog html = new HtmlProcessingLog(result.getLog());
                            html.displayInfos(true);
                            return html;
                        }
                    },
                    new HtmlItemUI()
            );
        }

        @Override
        public int getPosition() {
            return 1020;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 1000)
    public static class Input extends InputFactory<TramoSeatsDocument> {

        public Input() {
            super(TramoSeatsDocument.class, SaViews.INPUT_SERIES);
        }

        @Override
        public int getPosition() {
            return 1000;
        }
    }
//</editor-fold>

    private static String generateId(String id) {
        return generateId(id, id);
    }

    private static String generateId(String name, String id) {
        return TsDynamicProvider.CompositeTs.builder()
                .name(name)
                .back(id + SeriesInfo.B_SUFFIX)
                .now(id)
                .fore(id + SeriesInfo.F_SUFFIX)
                .build().toString();
    }

    private static String nsuffix(String suffix, int n) {
        StringBuilder builder = new StringBuilder();
        return builder.append(suffix).append('(').append(n).append(')').toString();
    }

    private static String generateId(String name, String id, int nb, int nf) {
        TsDynamicProvider.CompositeTs.Builder builder = TsDynamicProvider.CompositeTs.builder()
                .name(name);
        if (nb != 0) {
            builder.back(id + nsuffix(SeriesInfo.B_SUFFIX, nb));
        }
        builder.now(id);
        if (nf != 0) {
            builder.fore(id + nsuffix(SeriesInfo.F_SUFFIX, nf));
        }
        return builder.build().toString();
    }

    private static String generateStdErrorId(String name, String id) {
        return TsDynamicProvider.CompositeTs.builder()
                .name(name)
                .back(id + SeriesInfo.EB_SUFFIX)
                .now(id + SeriesInfo.E_SUFFIX)
                .fore(id + SeriesInfo.EF_SUFFIX)
                .build().toString();
    }

    public static String[] lowSeries() {
        return new String[]{
            generateId("Series", SaDictionaries.Y),
            generateId("Seasonally adjusted", SaDictionaries.SA),
            generateId("Trend", SaDictionaries.T)
        };
    }

    public static String[] highSeries(int nb, int nf) {
        return new String[]{
            generateId("Seasonal (component)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.S_CMP)),
            generateId("Calendar effects", RegressionDictionaries.CAL, nb, nf),
            generateId("Irregular", SaDictionaries.I)
        };
    }

    public static String[] finalSeries() {
        return new String[]{
            generateId("Series", SaDictionaries.Y),
            generateId("Seasonally adjusted", SaDictionaries.SA),
            generateId("Trend", SaDictionaries.T),
            generateId("Seasonal", SaDictionaries.S),
            generateId("Irregular", SaDictionaries.I)
        };

    }

//<editor-fold defaultstate="collapsed" desc="MAIN">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2000)
    public static class MainSummaryFactory extends ProcDocumentItemFactory<TramoSeatsDocument, TramoSeatsDocument> {

        public MainSummaryFactory() {
            super(TramoSeatsDocument.class, SaViews.MAIN_SUMMARY, VALIDEXTRACTOR, new TramoSeatsSummary());
        }

        @Override
        public int getPosition() {
            return 2000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2100)
    public static class MainLowChart extends ProcDocumentItemFactory<TramoSeatsDocument, TramoSeatsDocument> {

        public MainLowChart() {
            super(TramoSeatsDocument.class, SaViews.MAIN_CHARTS_LOW, VALIDEXTRACTOR, new GenericChartUI(false, lowSeries()));
        }

        @Override
        public int getPosition() {
            return 2100;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2200)
    public static class MainHighChart extends ProcDocumentItemFactory<TramoSeatsDocument, ContextualIds<TramoSeatsDocument>>  {

        public MainHighChart() {
            
            super(TramoSeatsDocument.class, SaViews.MAIN_CHARTS_HIGH, s -> {
                if (s.getResult() == null) {
                    return null;
                }
                     int p = s.getInput().getData().getAnnualFrequency();
              int nf = s.getSpecification().getSeats().getForecastCount();
                if (nf < 0) {
                     nf = -nf * p;
                }
               int nb = s.getSpecification().getSeats().getBackcastCount();
                if (nb < 0) {
                    nb = -nb * p;
                }
                return new ContextualIds<>(highSeries(nb, nf), s);
            }, new ContextualChartUI(true));
        }

        @Override
        public int getPosition() {
            return 2200;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2300)
    public static class MainTable extends ProcDocumentItemFactory<TramoSeatsDocument, TramoSeatsDocument> {

        public MainTable() {
            super(TramoSeatsDocument.class, SaViews.MAIN_TABLE, VALIDEXTRACTOR, new GenericTableUI(false, finalSeries()));
        }

        @Override
        public int getPosition() {
            return 2300;
        }

    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 2400)
    public static class MainSiFactory extends SIFactory<TramoSeatsDocument> {

        public MainSiFactory() {
            super(TramoSeatsDocument.class, SaViews.MAIN_SI, (TramoSeatsDocument source) -> {
                TramoSeatsResults result = source.getResult();
                if (result == null || !result.isValid()) {
                    return null;
                }
                return result.getDecomposition().getFinalComponents();
            });
        }

        @Override
        public int getPosition() {
            return 2400;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3000)
    public static class SummaryFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public SummaryFactory() {

            super(TramoSeatsDocument.class, SaViews.PREPROCESSING_SUMMARY, MODELEXTRACTOR
                    .andThen(regarima -> regarima == null ? null
                    : new HtmlRegSarima(regarima, false)),
                    new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 3000;
        }
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-FORECASTS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3110)
    public static class ForecastsTable extends ProcDocumentItemFactory<TramoSeatsDocument, ContextualIds<TsDocument>> {

        public ForecastsTable() {
            super(TramoSeatsDocument.class, SaViews.PREPROCESSING_FCASTS_TABLE, s -> {
                if (s.getResult() == null) {
                    return null;
                }
                int nf = s.getSpecification().getSeats().getForecastCount();
                if (nf < 0) {
                    int p = s.getInput().getData().getAnnualFrequency();
                    nf = -nf * p;
                }
                return new ContextualIds<>(generateItems(nf), s);
            }, new ContextualTableUI(true));
        }

        @Override
        public int getPosition() {
            return 3110;
        }

        private static String[] generateItems(int nf) {
            StringBuilder builder = new StringBuilder();
            builder.append(RegressionDictionaries.Y_F).append('(').append(nf).append(')');
            StringBuilder ebuilder = new StringBuilder();
            ebuilder.append(RegressionDictionaries.Y_EF).append('(').append(nf).append(')');
            return new String[]{builder.toString(), ebuilder.toString()};
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3100)
    public static class FCastsFactory extends ForecastsFactory<TramoSeatsDocument> {

        public FCastsFactory() {
            super(TramoSeatsDocument.class, SaViews.PREPROCESSING_FCASTS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3100;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3120)
    public static class FCastsOutFactory extends OutOfSampleTestFactory<TramoSeatsDocument> {

        public FCastsOutFactory() {
            super(TramoSeatsDocument.class, SaViews.PREPROCESSING_FCASTS_OUTOFSAMPLE, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3120;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-DETAILS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3200)
    public static class ModelRegsFactory extends ModelRegressorsFactory<TramoSeatsDocument> {

        public ModelRegsFactory() {
            super(TramoSeatsDocument.class, SaViews.PREPROCESSING_REGS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3200;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3300)
    public static class ArimaFactory extends ModelArimaFactory {

        public ArimaFactory() {
            super(TramoSeatsDocument.class, SaViews.PREPROCESSING_ARIMA, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3300;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3400)
    public static class PreprocessingDetFactory extends ProcDocumentItemFactory<TramoSeatsDocument, TsDocument> {

        public PreprocessingDetFactory() {
            super(TramoSeatsDocument.class, SaViews.PREPROCESSING_DET, source -> source.getResult().isValid() ? source : null, new GenericTableUI(false,
                    generateId(RegressionDictionaries.YC),
                    generateId(RegressionDictionaries.YLIN),
                    generateId(RegressionDictionaries.YCAL),
                    generateId(RegressionDictionaries.DET),
                    generateId(RegressionDictionaries.CAL),
                    generateId(RegressionDictionaries.TDE),
                    generateId(RegressionDictionaries.EE),
                    generateId(SaDictionaries.OUT_T),
                    generateId(SaDictionaries.OUT_S),
                    generateId(SaDictionaries.OUT_I),
                    generateId(RegressionDictionaries.OUT),
                    generateId(SaDictionaries.REG_Y),
                    generateId(SaDictionaries.REG_SA),
                    generateId(SaDictionaries.REG_T),
                    generateId(SaDictionaries.REG_S),
                    generateId(SaDictionaries.REG_I),
                    generateId(RegressionDictionaries.REG)));
        }

        @Override
        public int getPosition() {
            return 3400;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-RESIDUALS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3500)
    public static class ModelResFactory extends ProcDocumentItemFactory<TramoSeatsDocument, TsData> {

        public ModelResFactory() {
            super(TramoSeatsDocument.class, SaViews.PREPROCESSING_RES, RESEXTRACTOR,
                    new ResidualsUI()
            );
        }

        @Override
        public int getPosition() {
            return 3500;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3510)
    public static class ModelResStatsFactory extends NiidTestsFactory<TramoSeatsDocument> {

        public ModelResStatsFactory() {
            super(TramoSeatsDocument.class, SaViews.PREPROCESSING_RES_STATS, MODELEXTRACTOR);
        }

        @Override
        public int getPosition() {
            return 3510;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3520)
    public static class ModelResDist extends ProcDocumentItemFactory<TramoSeatsDocument, TsData> {

        public ModelResDist() {
            super(TramoSeatsDocument.class, SaViews.PREPROCESSING_RES_DIST,
                    RESEXTRACTOR,
                    new ResidualsDistUI());

        }

        @Override
        public int getPosition() {
            return 3520;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="PREPROCESSING-OTHERS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 3600)
    public static class LFactory extends LikelihoodFactory<TramoSeatsDocument> {

        public LFactory() {
            super(TramoSeatsDocument.class, SaViews.PREPROCESSING_LIKELIHOOD, MODELEXTRACTOR);
            setAsync(true);
        }

        @Override
        public int getPosition() {
            return 3600;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="SEATS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4000)
    public static class DecompositionSummaryFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public DecompositionSummaryFactory() {
            super(TramoSeatsDocument.class, DECOMPOSITION_SUMMARY, DECOMPOSITIONEXTRACTOR
                    .andThen((SeatsResults seats) -> {
                        if (seats == null) {
                            return null;
                        }
                        UcarimaModel ucm = seats.getUcarimaModel();
                        return new HtmlUcarima(ucm.getModel(),
                                SeatsResults.getComponents(ucm),
                                SeatsResults.getComponentsName(ucm));
                    }),
                    new HtmlItemUI());

        }

        @Override
        public int getPosition() {
            return 4000;
        }
    }

    private static String[] linSeries() {
        return new String[]{
            generateId("Series (lin)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.Y_LIN)),
            generateId("Seasonally adjusted (lin)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.SA_LIN)),
            generateId("Trend (lin)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.T_LIN)),
            generateId("Seasonal (lin)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.S_LIN)),
            generateId("Irregular (lin)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.I_LIN)),
            generateStdErrorId("Seasonally adjusted (stde lin)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.SA_LIN)),
            generateStdErrorId("Trend (stde lin)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.T_LIN)),
            generateStdErrorId("Seasonal (stde lin)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.S_LIN)),
            generateStdErrorId("Irregular (stde lin)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.I_LIN))
        };

    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4100)
    public static class LinearizedSeriesFactory extends ProcDocumentItemFactory<TramoSeatsDocument, TsDocument> {

        public LinearizedSeriesFactory() {
            super(TramoSeatsDocument.class, DECOMPOSITION_SERIES, s -> s.getResult().isValid() ? s : null, new GenericTableUI(true, linSeries()));
        }

        @Override
        public int getPosition() {
            return 4100;
        }
    }

    private static String[] cmpSeries() {
        return new String[]{
            generateId("Series (cmp)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.Y_CMP)),
            generateId("Seasonally adjusted (cmp)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.SA_CMP)),
            generateId("Trend (cmp)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.T_CMP)),
            generateId("Seasonal (cmp)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.S_CMP)),
            generateId("Irregular (cmp)", Dictionary.concatenate(SaDictionaries.DECOMPOSITION, SaDictionaries.I_CMP))
        };

    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4110)
    public static class DecompositionStochTrendFactory extends ProcDocumentItemFactory<TramoSeatsDocument, EstimationUI.Information> {

        public DecompositionStochTrendFactory() {
            super(TramoSeatsDocument.class, DECOMPOSITION_STOCH_TREND, DECOMPOSITIONEXTRACTOR.andThen(cmpExtractor(ComponentType.Trend)), new EstimationUI());
        }

        @Override
        public int getPosition() {
            return 4110;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4120)
    public static class DecompositionStochSeasFactory extends ProcDocumentItemFactory<TramoSeatsDocument, EstimationUI.Information> {

        public DecompositionStochSeasFactory() {
            super(TramoSeatsDocument.class, DECOMPOSITION_STOCH_SEAS, DECOMPOSITIONEXTRACTOR.andThen(cmpExtractor(ComponentType.Seasonal)), new EstimationUI());
        }

        @Override
        public int getPosition() {
            return 4120;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4200)
    public static class ComponentsSeriesFactory extends ProcDocumentItemFactory<TramoSeatsDocument, TsDocument> {

        public ComponentsSeriesFactory() {
            super(TramoSeatsDocument.class, DECOMPOSITION_CMPSERIES, s -> s, new GenericTableUI(true, cmpSeries()));
        }

        @Override
        public int getPosition() {
            return 4200;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4210)
    public static class DecompositionWkComponentsFactory extends ProcDocumentItemFactory<TramoSeatsDocument, WkInformation> {

        public DecompositionWkComponentsFactory() {
            super(TramoSeatsDocument.class, DECOMPOSITION_WK_COMPONENTS, DECOMPOSITIONEXTRACTOR.andThen(
                    (SeatsResults seats) -> {
                        if (seats == null) {
                            return null;
                        }
                        ComponentDescriptor[] descriptors = SeatsResults.descriptors.toArray(ComponentDescriptor[]::new);
                        WienerKolmogorovEstimators estimators = new WienerKolmogorovEstimators(seats.getUcarimaModel());
                        int period = seats.getOriginalModel().getPeriod();
                        return new WkInformation(estimators, descriptors, period);
                    }),
                    new WkComponentsUI());
        }

        @Override
        public int getPosition() {
            return 4210;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4220)
    public static class DecompositionWkFinalFactory extends ProcDocumentItemFactory<TramoSeatsDocument, WkInformation> {

        public DecompositionWkFinalFactory() {
            super(TramoSeatsDocument.class, DECOMPOSITION_WK_FINALS, DECOMPOSITIONEXTRACTOR.andThen(
                    (SeatsResults seats) -> {
                        if (seats == null) {
                            return null;
                        }
                        ComponentDescriptor[] descriptors = SeatsResults.descriptors.toArray(ComponentDescriptor[]::new);
                        WienerKolmogorovEstimators estimators = new WienerKolmogorovEstimators(seats.getUcarimaModel());
                        int period = seats.getOriginalModel().getPeriod();
                        return new WkInformation(estimators, descriptors, period);
                    }),
                    new WkFinalEstimatorsUI());
        }

        @Override
        public int getPosition() {
            return 4220;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4300)
    public static class DecompositionWkErrorsFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public DecompositionWkErrorsFactory() {
            super(TramoSeatsDocument.class, DECOMPOSITION_ERRORS, DECOMPOSITIONEXTRACTOR.andThen(
                    (SeatsResults seats) -> {
                        if (seats == null) {
                            return null;
                        }
                        try {
                            ComponentDescriptor[] descriptors = SeatsResults.airlineDescriptors.toArray(ComponentDescriptor[]::new);
                            WienerKolmogorovEstimators estimators = new WienerKolmogorovEstimators(seats.getCompactUcarimaModel());
                            int period = seats.getOriginalModel().getPeriod();
                            return new HtmlModelBasedRevisionsAnalysis(period, estimators, descriptors);
                        } catch (Exception err) {
                        }
                        return new HtmlFragment("Unable to compute model-based diagnostics");
                    }
            ), new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 4300;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4310)
    public static class DecompositionGrowthFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public DecompositionGrowthFactory() {
            super(TramoSeatsDocument.class, DECOMPOSITION_RATES, DECOMPOSITIONEXTRACTOR.andThen(
                    (SeatsResults seats) -> {
                        if (seats == null) {
                            return null;
                        }
                        return new HtmlSeatsGrowthRates(seats);
                    }
            ), new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 4310;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4320)
    public static class DecompositionTestsFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public DecompositionTestsFactory() {
            super(TramoSeatsDocument.class, DECOMPOSITION_TESTS, DECOMPOSITIONEXTRACTOR.andThen(
                    (SeatsResults seats) -> {
                        if (seats == null) {
                            return null;
                        }
                        try {
                            SeriesDecomposition decomposition = seats.getInitialComponents();
                            UcarimaModel ucm = seats.getCompactUcarimaModel();
                            String[] desc = new String[]{"Trend", "Seasonally adjusted", "Seasonal", "Irregular"};
                            int[] cmps = new int[]{1, -2, 2, 3};
                            boolean[] signals = new boolean[]{true, false, true, true};
                            double err = Math.sqrt(seats.getInnovationVariance());
                            TsData t = decomposition.getSeries(ComponentType.Trend, ComponentInformation.Value);
                            TsData s = decomposition.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
                            TsData i = decomposition.getSeries(ComponentType.Irregular, ComponentInformation.Value);
                            TsData sa = decomposition.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);

                            double[][] data = new double[][]{
                                t.getValues().toArray(),
                                sa == null ? null : sa.getValues().toArray(),
                                s == null ? null : s.getValues().toArray(),
                                i == null ? null : i.getValues().toArray()
                            };
                            WienerKolmogorovDiagnostics diags = WienerKolmogorovDiagnostics.make(ucm, err, data, cmps);
                            if (diags != null) {
                                return new HtmlWienerKolmogorovDiagnostics(diags, desc, signals, t.getAnnualFrequency());
                            }
                        } catch (Exception err) {
                        }
                        return new HtmlFragment("Unable to compute model-based diagnostics");
                    }), new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 4320;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4900)
    public static class SignificantSeasonalityFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public SignificantSeasonalityFactory() {
            super(TramoSeatsDocument.class, DECOMPOSITION_SIGSEAS, DECOMPOSITIONEXTRACTOR.andThen(
                    (SeatsResults seats) -> {
                        if (seats == null) {
                            return null;
                        }
                        TsData s = seats.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value);
                        TsData es = seats.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Stdev);
                        TsData fs = seats.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
                        TsData fes = seats.getInitialComponents().getSeries(ComponentType.Seasonal, ComponentInformation.StdevForecast);
                        int[] test99 = SignificantSeasonalityTest.test(s, es, fs, fes, 0.01);
                        int[] test95 = SignificantSeasonalityTest.test(s, es, fs, fes, 0.05);
                        return new HtmlSignificantSeasons(test99, test95);
                    }
            ), new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 4900;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4910)
    public static class StationaryVarianceDecompositionFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public StationaryVarianceDecompositionFactory() {
            super(TramoSeatsDocument.class, DECOMPOSITION_VAR, DIAGSEXTRACTOR.andThen(
                    (TramoSeatsDiagnostics diags) -> {
                        if (diags == null) {
                            return null;
                        }
                        StationaryVarianceDecomposition decomp = diags.getVarianceDecomposition();
                        if (decomp == null) {
                            return null;
                        }
                        return new HtmlStationaryVarianceDecomposition(decomp);
                    }),
                    new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 4910;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="BENCHMARKING">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 4900)
    public static class BenchmarkingFactory extends ProcDocumentItemFactory<TramoSeatsDocument, BenchmarkingUI.Input> {

        public BenchmarkingFactory() {
            super(TramoSeatsDocument.class, SaViews.BENCHMARKING_SUMMARY, (TramoSeatsDocument doc) -> {
                TramoSeatsResults rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                SaBenchmarkingResults benchmarking = rslt.getBenchmarking();
                if (benchmarking == null) {
                    return null;
                }
                boolean mul = rslt.getFinals().getMode().isMultiplicative();
                return new BenchmarkingUI.Input(mul, benchmarking);
            }, new BenchmarkingUI());
        }

        @Override
        public int getPosition() {
            return 4900;
        }

    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="DIAGNOSTICS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5000)
    public static class DiagnosticsSummaryFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public DiagnosticsSummaryFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_SUMMARY, (TramoSeatsDocument doc) -> {
                TramoSeatsResults rslt = doc.getResult();
                if (rslt == null) {
                    return null;
                }
                SaProcessingFactory factory = SaManager.factoryFor(doc.getSpecification());
                List<ProcDiagnostic> diags = new ArrayList<>();
                factory.fillDiagnostics(diags, null, rslt);
                return new HtmlDiagnosticsSummary(diags);
            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5000;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5010)
    public static class OriginalSeasonalityFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public OriginalSeasonalityFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_OSEASONALITY, (TramoSeatsDocument doc) -> {
                TramoSeatsResults rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                TsData s = rslt.getPreprocessing().transformedSeries();
                if (s == null) {
                    return null;
                }
                return new HtmlElements(new HtmlHeader(1, "Original [transformed] series", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), false));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5010;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5020)
    public static class LinSeasonalityFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public LinSeasonalityFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_LSEASONALITY, (TramoSeatsDocument doc) -> {
                TramoSeatsResults rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                TsData s = rslt.getPreprocessing().linearizedSeries();
                if (s == null) {
                    return null;
                }
                return new HtmlElements(new HtmlHeader(1, "Linearized series", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), false));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5020;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5030)
    public static class ResSeasonalityFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public ResSeasonalityFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_RSEASONALITY, (TramoSeatsDocument doc) -> {
                TramoSeatsResults rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                TsData s = rslt.getPreprocessing().fullResiduals();
                if (s == null) {
                    return null;
                }
                return new HtmlElements(new HtmlHeader(1, "Full residuals", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5030;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5040)
    public static class SaSeasonalityFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public SaSeasonalityFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_SASEASONALITY, (TramoSeatsDocument doc) -> {
                TramoSeatsResults rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                TsData s = rslt.getDecomposition().getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
                if (s == null) {
                    return null;
                }
                return new HtmlElements(new HtmlHeader(1, "[Linearized] seasonally adjusted series", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5030;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5050)
    public static class IrrSeasonalityFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public IrrSeasonalityFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_ISEASONALITY, (TramoSeatsDocument doc) -> {
                TramoSeatsResults rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                TsData s = rslt.getDecomposition().getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value);
                if (s == null) {
                    return null;
                }
                return new HtmlElements(new HtmlHeader(1, "[Linearized] irregular component", true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5050;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5060)
    public static class LastResSeasonalityFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public LastResSeasonalityFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_LASTRSEASONALITY, (TramoSeatsDocument doc) -> {
                TramoSeatsResults rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                TsData s = rslt.getPreprocessing().fullResiduals();
                if (s == null) {
                    return null;
                }
                StringBuilder header = new StringBuilder().append("Full residuals");
                int ny = DemetraSaUI.get().getSeasonalityLength();
                if (ny > 0) {
                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny), 0);
                    header.append(" (last ").append(ny).append(" years)");
                }
                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5060;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5070)
    public static class LastSaSeasonalityFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public LastSaSeasonalityFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_LASTSASEASONALITY, (TramoSeatsDocument doc) -> {
                TramoSeatsResults rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                TsData s = rslt.getDecomposition().getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
                if (s == null) {
                    return null;
                }
                StringBuilder header = new StringBuilder().append("[Linearized] seasonally adjusted series");
                int ny = DemetraSaUI.get().getSeasonalityLength();
                if (ny > 0) {
                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny - 1), 0);
                    header.append(" (last ").append(ny).append(" years)");
                }
                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 1, true, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5070;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5080)
    public static class LastIrrSeasonalityFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public LastIrrSeasonalityFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_LASTISEASONALITY, (TramoSeatsDocument doc) -> {
                TramoSeatsResults rslt = doc.getResult();
                if (rslt == null || !rslt.isValid()) {
                    return null;
                }
                TsData s = rslt.getDecomposition().getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value);
                if (s == null) {
                    return null;
                }
                StringBuilder header = new StringBuilder().append("[Linearized] irregular component");
                int ny = DemetraSaUI.get().getSeasonalityLength();
                if (ny > 0) {
                    s = s.drop(Math.max(0, s.length() - s.getAnnualFrequency() * ny), 0);
                    header.append(" (last ").append(ny).append(" years)");
                }
                return new HtmlElements(new HtmlHeader(1, header.toString(), true),
                        new HtmlSeasonalityDiagnostics(SeasonalityTests.seasonalityTest(s.getValues(), s.getAnnualFrequency(), 0, false, true), true));

            }, new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 5080;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5310)
    public static class ModelResSpectrum extends ProcDocumentItemFactory<TramoSeatsDocument, SpectrumUI.Information> {

        public ModelResSpectrum() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_SPECTRUM_RES,
                    RESEXTRACTOR.andThen(
                            res
                            -> res == null ? null
                                    : SpectrumUI.Information.builder()
                                            .series(res)
                                            .differencingOrder(0)
                                            .log(false)
                                            .mean(true)
                                            .whiteNoise(true)
                                            .build()),
                    new SpectrumUI());
        }

        @Override
        public int getPosition() {
            return 5310;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5320)
    public static class DiagnosticsSpectrumIFactory extends ProcDocumentItemFactory<TramoSeatsDocument, SpectrumUI.Information> {

        public DiagnosticsSpectrumIFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_SPECTRUM_I,
                    DECOMPOSITIONEXTRACTOR.andThen(
                            (SeatsResults seats) -> {
                                if (seats == null) {
                                    return null;
                                }
                                TsData s = seats.getInitialComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value);
                                return s == null ? null
                                        : SpectrumUI.Information.builder()
                                                .series(s)
                                                .differencingOrder(0)
                                                .log(false)
                                                .mean(true)
                                                .whiteNoise(false)
                                                .build();
                            }),
                    new SpectrumUI());
        }

        @Override
        public int getPosition() {
            return 5320;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 5330)
    public static class DiagnosticsSpectrumSaFactory extends ProcDocumentItemFactory<TramoSeatsDocument, SpectrumUI.Information> {

        public DiagnosticsSpectrumSaFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_SPECTRUM_SA,
                    DECOMPOSITIONEXTRACTOR.andThen(
                            (SeatsResults seats) -> {
                                if (seats == null) {
                                    return null;
                                }
                                TsData s = seats.getInitialComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
                                return s == null ? null
                                        : SpectrumUI.Information.builder()
                                                .series(s)
                                                .differencingOrder(1)
                                                .differencingLag(1)
                                                .log(false)
                                                .mean(true)
                                                .whiteNoise(false)
                                                .build();
                            }),
                    new SpectrumUI());
        }

        @Override
        public int getPosition() {
            return 5330;
        }
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="REGISTER SLIDING SPANS">
    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6300)
    public static class DiagnosticsSlidingSummaryFactory extends ProcDocumentItemFactory<TramoSeatsDocument, HtmlElement> {

        public DiagnosticsSlidingSummaryFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_SLIDING_SUMMARY, (TramoSeatsDocument source) -> {
                TramoSeatsResults result = source.getResult();
                if (result == null || !result.isValid()) {
                    return null;
                }
                TsData input = source.getInput().getData();
                TsDomain domain = input.getDomain();
                TramoSeatsSpec pspec = TramoSeatsFactory.getInstance().generateSpec(source.getSpecification(), result);
                TramoSeatsSpec nspec = TramoSeatsFactory.getInstance().refreshSpec(pspec, source.getSpecification(), EstimationPolicyType.FreeParameters, domain);
                TramoSeatsKernel kernel = TramoSeatsKernel.of(nspec, source.getContext());
                SlidingSpans<TramoSeatsResults> ss = new SlidingSpans<>(domain, d -> kernel.process(TsData.fitToDomain(input, d), null));
                boolean mul = result.getFinals().getMode().isMultiplicative();
                return new HtmlSaSlidingSpanSummary<>(ss, mul, (TramoSeatsResults cur) -> {
                    if (cur == null) {
                        return null;
                    }
                    return cur.getDecomposition().getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value);
                }, (var cur) -> {
                    if (cur == null) {
                        return null;
                    }
                    TsData seas = cur.getDecomposition().getFinalComponents().getSeries(ComponentType.Seasonal, ComponentInformation.Value);
                    TsData irr = cur.getDecomposition().getFinalComponents().getSeries(ComponentType.Irregular, ComponentInformation.Value);
                    return (mul ? TsData.multiply(seas, irr) : TsData.add(seas, irr)).commit();
                });
            },
                    new HtmlItemUI());
        }

        @Override
        public int getPosition() {
            return 6300;
        }
    }

    private static Function<TramoSeatsDocument, SlidingSpansUI.Information<TramoSeatsResults>> ssExtractor(String name, boolean changes, Function<TramoSeatsResults, TsData> fn) {
        return (TramoSeatsDocument source) -> {
            TramoSeatsResults result = source.getResult();
            if (result == null || !result.isValid()) {
                return null;
            }
            TsData input = source.getInput().getData();
            TsDomain domain = input.getDomain();
            TramoSeatsSpec pspec = TramoSeatsFactory.getInstance().generateSpec(source.getSpecification(), result);
            TramoSeatsSpec nspec = TramoSeatsFactory.getInstance().refreshSpec(pspec, source.getSpecification(), EstimationPolicyType.FreeParameters, domain);
            TramoSeatsKernel kernel = TramoSeatsKernel.of(nspec, source.getContext());
            SlidingSpans<TramoSeatsResults> ss = new SlidingSpans<>(domain, d -> kernel.process(TsData.fitToDomain(input, d), null));
            boolean mul = result.getFinals().getMode().isMultiplicative();
            Function<TramoSeatsResults, TsData> extractor = tsrslt -> {
                if (tsrslt == null) {
                    return null;
                }
                return fn.apply(tsrslt);
            };
            DiagnosticInfo info;
            if (changes) {
                info = mul ? DiagnosticInfo.PeriodToPeriodGrowthDifference : DiagnosticInfo.PeriodToPeriodDifference;
            } else {
                info = mul ? DiagnosticInfo.RelativeDifference : DiagnosticInfo.AbsoluteDifference;
            }
            return new SlidingSpansUI.Information<>(mul, ss, info, name, extractor);
        };
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6310)
    public static class DiagnosticsSlidingSeasFactory extends ProcDocumentItemFactory<TramoSeatsDocument, SlidingSpansUI.Information<TramoSeatsResults>> {

        public DiagnosticsSlidingSeasFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_SLIDING_SEAS,
                    ssExtractor("Seasonal", false,
                            rslt -> rslt.getFinals() == null ? null : rslt.getFinals().getSeries(ComponentType.Seasonal, ComponentInformation.Value)),
                    new SlidingSpansUI<>());
        }

        @Override
        public int getPosition() {
            return 6310;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6320)
    public static class DiagnosticsSlidingTdFactory extends ProcDocumentItemFactory<TramoSeatsDocument, SlidingSpansUI.Information<TramoSeatsResults>> {

        public DiagnosticsSlidingTdFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_SLIDING_TD,
                    ssExtractor("Trading days", false,
                            rslt -> rslt.getPreprocessing().getTradingDaysEffect(null)),
                    new SlidingSpansUI<>());
        }

        @Override
        public int getPosition() {
            return 6320;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6330)
    public static class DiagnosticsSlidingSaFactory extends ProcDocumentItemFactory<TramoSeatsDocument, SlidingSpansUI.Information<TramoSeatsResults>> {

        public DiagnosticsSlidingSaFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_SLIDING_SA,
                    ssExtractor("Seasonally adjusted", true,
                            rslt -> rslt.getDecomposition().getFinalComponents().getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value)),
                    new SlidingSpansUI<>());
        }

        @Override
        public int getPosition() {
            return 6330;
        }
    }

//</editor-fold>
//<editor-fold defaultstate="collapsed" desc="REGISTER REVISION HISTORY VIEW">
    private static Function<TramoSeatsDocument, RevisionHistoryUI.Information> revisionExtractor(String info, DiagnosticInfo diag) {
        return (TramoSeatsDocument source) -> {
            TramoSeatsResults result = source.getResult();
            if (result == null) {
                return null;
            }
            if (result.getPreprocessing() == null) {
                return null;
            }
            TsData input = source.getInput().getData();
            TimeSelector span = source.getSpecification().getTramo().getTransform().getSpan();
            TsDomain domain = input.getDomain().select(span);
            TramoSeatsSpec pspec = TramoSeatsFactory.getInstance().generateSpec(source.getSpecification(), result);
            TramoSeatsSpec nspec = TramoSeatsFactory.getInstance().refreshSpec(pspec, source.getSpecification(), DemetraSaUI.get().getEstimationPolicyType(), domain);
            if (!span.isAll()) {
                TransformSpec ntr = nspec.getTramo().getTransform().toBuilder()
                        .span(TimeSelector.all())
                        .build();
                TramoSpec reg = nspec.getTramo().toBuilder()
                        .transform(ntr)
                        .build();
                nspec = nspec.toBuilder().tramo(reg).build();
            }
            TramoSeatsKernel kernel = TramoSeatsKernel.of(nspec, source.getContext());
            RevisionHistory<Explorable> rh = new RevisionHistory<>(domain, d -> kernel.process(TsData.fitToDomain(input, d), null));
            return new RevisionHistoryUI.Information(info, diag, rh);
        };

    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6410)
    public static class RevisionHistorySaFactory extends ProcDocumentItemFactory<TramoSeatsDocument, RevisionHistoryUI.Information> {

        public RevisionHistorySaFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_REVISION_SA, revisionExtractor("sa", DiagnosticInfo.RelativeDifference), new RevisionHistoryUI());
        }

        @Override
        public int getPosition() {
            return 6410;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6420)
    public static class RevisionHistoryTrendFactory extends ProcDocumentItemFactory<TramoSeatsDocument, RevisionHistoryUI.Information> {

        public RevisionHistoryTrendFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_REVISION_TREND, revisionExtractor("t", DiagnosticInfo.RelativeDifference), new RevisionHistoryUI());
        }

        @Override
        public int getPosition() {
            return 6420;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6430)
    public static class RevisionHistorySaChangesFactory extends ProcDocumentItemFactory<TramoSeatsDocument, RevisionHistoryUI.Information> {

        public RevisionHistorySaChangesFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_REVISION_SA_CHANGES, revisionExtractor("sa", DiagnosticInfo.PeriodToPeriodGrowthDifference), new RevisionHistoryUI());
        }

        @Override
        public int getPosition() {
            return 6430;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6440)
    public static class RevisionHistoryTrendChangesFactory extends ProcDocumentItemFactory<TramoSeatsDocument, RevisionHistoryUI.Information> {

        public RevisionHistoryTrendChangesFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_REVISION_TREND_CHANGES, revisionExtractor("t", DiagnosticInfo.PeriodToPeriodGrowthDifference), new RevisionHistoryUI());
        }

        @Override
        public int getPosition() {
            return 6440;
        }
    }
//</editor-fold>

//<editor-fold defaultstate="collapsed" desc="REGISTER STABILITY VIEWS">
    private static Function<TramoSeatsDocument, StabilityUI.Information> stabilityExtractor(EstimationPolicyType policy, String[] items, String msg) {
        return (TramoSeatsDocument source) -> {
            TramoSeatsResults result = source.getResult();
            if (result == null) {
                return null;
            }
            if (result.getPreprocessing() == null) {
                return null;
            }
            TsData input = source.getInput().getData();
            TsDomain domain = input.getDomain();
            TramoSpec pspec = TramoFactory.getInstance().generateSpec(source.getSpecification().getTramo(), result.getPreprocessing().getDescription());
            TramoSpec nspec = TramoFactory.getInstance().refreshSpec(pspec, source.getSpecification().getTramo(), policy, domain);
            TramoKernel kernel = TramoKernel.of(nspec, source.getContext());
            MovingProcessing<Explorable> mp = new MovingProcessing<>(domain, (TsDomain d) -> kernel.process(TsData.fitToDomain(input, d), null));
            mp.setWindowLength(DemetraSaUI.get().getStabilityLength() * input.getAnnualFrequency());
            return new StabilityUI.Information(mp, items, msg);
        };

    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6510)
    public static class StabilityTDFactory extends ProcDocumentItemFactory<TramoSeatsDocument, StabilityUI.Information> {

        public StabilityTDFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_STABILITY_TD,
                    stabilityExtractor(DemetraSaUI.get().getEstimationPolicyType(), ITEMS, EXCEPTION), new StabilityUI());
        }

        @Override
        public int getPosition() {
            return 6510;
        }

        private static final String EXCEPTION = "No information available on trading days !";
        private static final String[] ITEMS = new String[]{
            "regression.td(1)",
            "regression.td(2)",
            "regression.td(3)",
            "regression.td(4)",
            "regression.td(5)",
            "regression.td(6)",
            "regression.td(7)"
        };

    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6520)
    public static class StabilityEasterFactory extends ProcDocumentItemFactory<TramoSeatsDocument, StabilityUI.Information> {

        public StabilityEasterFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_STABILITY_EASTER,
                    stabilityExtractor(DemetraSaUI.get().getEstimationPolicyType(), ITEMS, EXCEPTION), new StabilityUI());
        }

        private static final String EXCEPTION = "No information available on Easter effects !";
        private static final String[] ITEMS = new String[]{
            "regression.easter"
        };

        @Override
        public int getPosition() {
            return 6520;
        }
    }

    @ServiceProvider(service = IProcDocumentItemFactory.class, position = 6530)
    public static class StabilityArimaFactory extends ProcDocumentItemFactory<TramoSeatsDocument, StabilityUI.Information> {

        public StabilityArimaFactory() {
            super(TramoSeatsDocument.class, SaViews.DIAGNOSTICS_STABILITY_ARIMA,
                    stabilityExtractor(EstimationPolicyType.FreeParameters, ITEMS, EXCEPTION), new StabilityUI());
        }

        @Override
        public int getPosition() {
            return 6530;
        }

        private static final String EXCEPTION = "No information available on the ARIMA model !";
        private static final String[] ITEMS = new String[]{
            "arima.phi(1)", "arima.phi(2)", "arima.phi(3)", "arima.theta(1)", "arima.theta(2)", "arima.theta(3)",
            "arima.bphi(1)", "arima.btheta(1)"
        };

    }
//</editor-fold>
}
