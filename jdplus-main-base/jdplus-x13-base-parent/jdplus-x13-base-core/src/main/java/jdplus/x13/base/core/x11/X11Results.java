/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.core.x11;

import jdplus.toolkit.base.api.information.GenericExplorable;
import jdplus.sa.base.api.DecompositionMode;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.x13.base.api.x11.MsrTable;
import jdplus.x13.base.api.x11.SeasonalFilterOption;

/**
 *
 * @author Thomas Witthohn
 */
@lombok.Value
@lombok.Builder
public class X11Results implements GenericExplorable {

    int nbackcasts, nforecasts;

    TsData b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b13, b17, b20;
    TsData c1, c2, c4, c5, c6, c7, c9, c10, c11, c13, c17, c20;
    TsData d1, d2, d4, d5, d6, d7, d8, d9, d10, d11, d12, d13;

    SeasonalFilterOption[] finalSeasonalFilter;
    int finalHendersonFilterLength;
    DecompositionMode mode;

    /**
     * I/C-Ratio on D1 !o D10 (D11bis)
     */
    double iCRatio;

    /**
     * MSR (moving seasonality ratio) table on D9 and related
     */
    MsrTable d9Msr;
    SeasonalFilterOption d9filter;
    boolean d9default;

    public TsDomain getFullDomain() {
        return b1.getDomain();
    }

    public TsDomain getBackcastDomain() {
        return b1.getDomain().range(0, nbackcasts);
    }

    public TsDomain getForecastDomain() {
        TsDomain domain = b1.getDomain();
        int n = domain.getLength();
        return domain.range(n - nforecasts, n);
    }

    public TsDomain getActualDomain() {
        TsDomain domain = b1.getDomain();
        return domain.range(nbackcasts, domain.getLength() - nforecasts);
    }
}
