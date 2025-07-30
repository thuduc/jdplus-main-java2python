/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.api.timeseries.regression;

import nbbrd.design.Development;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@Development(status = Development.Status.Exploratory)
public class RegressionItem {
 
    double coefficient;
    double stdError;
    double pvalue;
    String description;
}
