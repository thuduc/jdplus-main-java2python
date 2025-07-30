/*
 * Copyright 2016 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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
package jdplus.sa.base.xml.benchmarking;

import jdplus.sa.base.api.benchmarking.SaBenchmarkingSpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UnivariateCholetteMethodType", propOrder = {
    "rho",
    "lambda",
    "biasCorrection"
})
public class XmlUnivariateCholetteMethod
    extends XmlBenchmarkingMethod
{

    @XmlElement(name = "Rho")
    protected double rho;
    @XmlElement(name = "Lambda")
    protected double lambda;
    @XmlElement(name = "BiasCorrection")
    protected SaBenchmarkingSpec.BiasCorrection biasCorrection;

    public double getRho() {
        return rho;
    }

    public void setRho(double value) {
        this.rho = value;
    }

    public double getLambda() {
        return lambda;
    }

    public void setLambda(double value) {
        this.lambda = value;
    }

    /**
     * Obtient la valeur de la propriété biasCorrection.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public SaBenchmarkingSpec.BiasCorrection getBiasCorrection() {
        return biasCorrection;
    }

    /**
     * Définit la valeur de la propriété biasCorrection.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBiasCorrection(SaBenchmarkingSpec.BiasCorrection value) {
        this.biasCorrection = value;
    }

}
