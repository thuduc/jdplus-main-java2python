/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.toolkit.base.api.dictionaries;

import jdplus.toolkit.base.api.dictionaries.ArimaDictionaries;

/**
 *
 * @author PALATEJ
 */
public class ArimaDictionariesTest {

    public ArimaDictionariesTest() {
    }

    public static void ucarima() {
       ArimaDictionaries.UCARIMA.entries().forEach(entry-> System.out.println(entry.display()));
     }
    
    public static void main(String[] arg){
        ucarima();
    }
}
