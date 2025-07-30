/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.data.transformation;

import nbbrd.design.Development;
import java.util.Arrays;

/**
 * Contains the log of the Jacobian of a transformation of a time series.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class LogJacobian {

    /**
     * The value of the Jacobian
     */
    public double value;
    /**
     * The starting position (included) of the transformation
     */
    /**
     * The ending position (excluded) of the transformation
     */
    public final int start, end;

    /**
     * Index corresponding to missing values
     * The Jacobian should not be modified for such items
     */
    public final int[] missing;

    /**
     * Creates a log Jacobian with the limits of the transformation
     *
     * @param start Starting position (included)
     * @param end Ending position (excluded)
     * @param missing Positions of missing values. Should be ordered
     */
    public LogJacobian(int start, int end, int[] missing) {
        this.start = start;
        this.end = end;
        this.missing = missing;
    }

}
