/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package jdplus.x13.base.api.x13;

import jdplus.x13.base.api.x13.X13Dictionaries;

/**
 *
 * @author PALATEJ
 */
public class X13DictionariesTest {
    
    public X13DictionariesTest() {
    }

    public static void regsarima() {
       X13Dictionaries.X13DICTIONARY.entries().forEach(entry
                -> System.out.println(entry.display()));
    }
    
    public static void main(String[] arg){
        regsarima();
    }
}
