/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package _demo;

import tck.demetra.data.Data;
import jdplus.toolkit.base.api.timeseries.TsData;

/**
 *
 * @author palatej
 */
public class SurveysDemo {
    public static void main(String[] cmd){
        TsData[] all=Data.surveys();
        System.out.println(all.length);
        System.out.println();
        System.out.println(all[0].length());
        System.out.println();
        System.out.println(all[0]);
    }
}
