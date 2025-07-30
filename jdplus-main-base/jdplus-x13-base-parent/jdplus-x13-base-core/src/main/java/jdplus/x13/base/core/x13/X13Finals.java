/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.core.x13;

import jdplus.toolkit.base.api.timeseries.TsData;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.Builder
public class X13Finals {
    
    TsData d11final, d12final, d13final, d16, d18;
    TsData d11a, d12a, d16a, d18a, d11b, d12b, d16b, d18b;
    TsData e1, e2, e3, e11;
}
