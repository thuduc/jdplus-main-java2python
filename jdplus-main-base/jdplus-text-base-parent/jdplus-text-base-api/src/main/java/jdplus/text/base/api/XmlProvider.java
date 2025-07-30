package jdplus.text.base.api;

import jdplus.toolkit.base.api.timeseries.TsCollection;
import jdplus.toolkit.base.api.timeseries.TsProvider;
import jdplus.toolkit.base.tsp.*;
import jdplus.toolkit.base.tsp.stream.HasTsStream;
import jdplus.toolkit.base.tsp.stream.TsStreamAsProvider;
import jdplus.toolkit.base.tsp.util.FallbackDataMoniker;
import jdplus.toolkit.base.tsp.util.ImmutableValuePool;
import internal.text.base.api.*;
import lombok.NonNull;
import nbbrd.design.DirectImpl;
import nbbrd.service.ServiceProvider;

import java.io.IOException;
import java.util.List;

@DirectImpl
@ServiceProvider(TsProvider.class)
public final class XmlProvider implements FileLoader<XmlBean> {

    public static final String NAME = "Xml";

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList mutableListSupport;

    @lombok.experimental.Delegate
    private final HasDataMoniker monikerSupport;

    @lombok.experimental.Delegate
    private final HasDataSourceBean<XmlBean> beanSupport;

    @lombok.experimental.Delegate
    private final HasFilePaths filePathSupport;

    @lombok.experimental.Delegate
    private final HasDataDisplayName displayNameSupport;

    @lombok.experimental.Delegate(excludes = HasTsStream.class, types = HasDataHierarchy.class)
    private final XmlSupport xmlSupport;

    @lombok.experimental.Delegate
    private final TsProvider tsSupport;

    @lombok.experimental.Delegate
    private final XmlFileFilter fileFilter;

    public XmlProvider() {
        XmlParam param = new XmlParam.V1();

        ImmutableValuePool<List<TsCollection>> pool = ImmutableValuePool.of();

        this.mutableListSupport = HasDataSourceMutableList.of(NAME, pool::remove);
        this.monikerSupport = FallbackDataMoniker.of(HasDataMoniker.usingUri(NAME), XmlLegacyMoniker.of(NAME, param));
        this.beanSupport = HasDataSourceBean.of(NAME, param, param.getVersion());
        this.filePathSupport = HasFilePaths.of(pool::clear);
        this.displayNameSupport = XmlDataDisplayName.of(NAME, param, pool::peek);
        this.xmlSupport = XmlSupport.of(NAME, pool.asFactory(dataSource -> load(dataSource, filePathSupport, param)), ignore -> param.getCollectionParam(), ignore -> param.getSeriesParam());
        this.tsSupport = TsStreamAsProvider.of(NAME, xmlSupport, monikerSupport, pool::clear);
        this.fileFilter = new XmlFileFilter();
    }

    @Override
    public @NonNull String getDisplayName() {
        return "Xml files";
    }

    private static List<TsCollection> load(DataSource dataSource, HasFilePaths paths, XmlParam param) throws IOException {
        return new XmlLoader(paths).load(param.get(dataSource));
    }
}
