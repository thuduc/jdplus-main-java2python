package jdplus.toolkit.base.core.regsarima.regular;

import jdplus.toolkit.base.api.processing.ProcessingLog;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */

@lombok.Value
@lombok.Builder
public class RegSarimaResults {
    private ModelEstimation regarima;

    @lombok.Singular
    private List<ProcessingLog.Information> logs;
    
    @lombok.Singular
    private Map<String, Object> details;
}
