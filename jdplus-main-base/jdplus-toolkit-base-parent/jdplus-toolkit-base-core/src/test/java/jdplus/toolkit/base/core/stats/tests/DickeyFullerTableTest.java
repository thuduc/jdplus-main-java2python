/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.toolkit.base.core.stats.tests;

import jdplus.toolkit.base.core.stats.tests.DickeyFuller;
import jdplus.toolkit.base.core.stats.tests.DickeyFullerTable;

/**
 *
 * @author PALATEJ
 */
public class DickeyFullerTableTest {

    public DickeyFullerTableTest() {
    }

    public static void test1() {
        for (int i = 10; i < 500; ++i) {
            System.out.print(DickeyFullerTable.probability(i, 1.6, DickeyFuller.DickeyFullerType.NC, false));
            System.out.print('\t');
            System.out.print(DickeyFullerTable.probability2(i, 1.6, DickeyFuller.DickeyFullerType.NC));
            System.out.print('\t');
            System.out.println(DickeyFullerTable.probability3(i, 1.6, DickeyFuller.DickeyFullerType.NC));
        }
    }

    public static void test2() {
        for (int i = -250; i < 250; ++i) {
            System.out.print(DickeyFullerTable.probability(50, .1*i, DickeyFuller.DickeyFullerType.NC, false));
            System.out.print('\t');
            System.out.print(DickeyFullerTable.probability2(50, .1*i, DickeyFuller.DickeyFullerType.NC));
            System.out.print('\t');
            System.out.println(DickeyFullerTable.probability3(50, .1*i, DickeyFuller.DickeyFullerType.NC));
        }
    }
    
    public static void main(String[] args){
        test1();
        System.out.println("");
        test2();
    }
}
