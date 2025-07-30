/*
 * Copyright 2023 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.toolkit.base.api.math.linearfilters;

/**
 *
 * @author palatej
 */
@lombok.Value
@lombok.AllArgsConstructor
public class HendersonSpec implements FilterSpec {

    int filterHorizon;
    double leftIcRatio, rightIcRatio;
    
    public HendersonSpec(int horizon, double icRatio){
        this.filterHorizon=horizon;
        this.leftIcRatio=icRatio;
        this.rightIcRatio=icRatio;
    }

    public boolean isSymmetric() {
        return leftIcRatio == rightIcRatio;
    }

}
