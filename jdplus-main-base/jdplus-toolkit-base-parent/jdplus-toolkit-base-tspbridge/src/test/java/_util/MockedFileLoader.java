package _util;

import jdplus.toolkit.base.api.timeseries.TsProvider;
import jdplus.toolkit.base.tsp.*;
import jdplus.toolkit.base.tsp.stream.HasTsStream;
import jdplus.toolkit.base.tsp.stream.TsStreamAsProvider;
import nbbrd.io.function.IORunnable;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

public final class MockedFileLoader implements FileLoader<MockedFileBean> {

    private static final String NAME = "mocked";

    @lombok.experimental.Delegate
    private final HasDataSourceMutableList dataSourceMutableList = HasDataSourceMutableList.of(NAME);

    @lombok.experimental.Delegate
    private final HasDataHierarchy dataHierarchy = HasDataHierarchy.noOp(NAME);

    @lombok.experimental.Delegate
    private final HasDataDisplayName dataDisplayName = HasDataDisplayName.usingUri(NAME);

    @lombok.experimental.Delegate
    private final HasDataMoniker dataMoniker = HasDataMoniker.usingUri(NAME);

    @lombok.experimental.Delegate
    private final HasDataSourceBean<MockedFileBean> dataSourceBean = HasDataSourceBean.of(NAME, new Stuff(), "");

    @lombok.experimental.Delegate
    private final HasFilePaths filePaths = HasFilePaths.of();

    @lombok.experimental.Delegate
    private final TsProvider provider = TsStreamAsProvider.of(NAME, HasTsStream.noOp(NAME), dataMoniker, IORunnable.noOp().asUnchecked());

    @Override
    public @NonNull String getFileDescription() {
        return "";
    }

    @Override
    public boolean accept(File pathname) {
        return true;
    }

    private static final class Stuff implements DataSource.Converter<MockedFileBean> {

        @Override
        public @NonNull MockedFileBean getDefaultValue() {
            MockedFileBean result = new MockedFileBean();
            result.setFile(Path.of("abc").toFile());
            return result;
        }

        @Override
        public @NonNull MockedFileBean get(@NonNull DataSource config) {
            MockedFileBean result = new MockedFileBean();
            result.setFile(Path.of("abc").toFile());
            return result;
        }

        @Override
        public void set(DataSource.@NonNull Builder builder, @Nullable MockedFileBean value) {

        }
    }
}
