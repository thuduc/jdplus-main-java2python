/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.core.ml;

import jdplus.toolkit.base.api.data.DoubleSeq;
import java.util.Random;

import jdplus.toolkit.base.api.math.matrices.Matrix;
import org.junit.jupiter.api.Test;

/**
 *
 * @author PALATEJ
 */
public class DBScanTest {

    public DBScanTest() {
    }

    @Test
    public void testSomeMethod() {
    }

    public static DBScan.Results gaussianTree(double eps, int npts) {

        Random rnd = new Random(0);
        int n = 10000, m = 5;
        double[] data = new double[n * m];
        for (int i = 0; i < data.length; ++i) {
            data[i] = rnd.nextGaussian();
            if (i % 3 == 1) {
                data[i] *= Math.sqrt(.1);
            }
        }
        for (int i = 100*m; i < 100*m+m; ++i) {
            data[i] = 3.3;
        }
        Matrix M = Matrix.of(data, m, n);
        DistanceMeasure<DoubleSeq> d = (l, r) -> l.distance(r);
        DBScan scan = new DBScan(eps, npts, d);
        return scan.cluster(i -> M.column(i), M.getColumnsCount());
    }

    public static void main(String[] args) {
        DBScan.Results r = gaussianTree(2*Math.sqrt(5), 10);
        System.out.println(r.getNoises().size());
        System.out.println(r.getClusters().size());
//        for (double eps = .25; eps < 3; eps+=.25) {
//            for (int i = 5; i < 50; ++i) {
//                DBScan.Results r = gaussianTree(eps, i);
//                System.out.print(r.getClusters().size());
//                System.out.print('\t');
//            }
//            System.out.println();
//        }
    }

}
