/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.protobuf;

import jdplus.x13.base.api.regarima.RegArimaSpec;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author PALATEJ
 */
public class RegArimaProtoTest {

    public RegArimaProtoTest() {
    }

    @Test
    public void testDefault() {
        RegArimaSpec s = RegArimaSpec.RG0;
        RegArimaSpec ns = RegArimaProto.convert(RegArimaProto.convert(s));
        assertTrue(s.equals(ns));
        s = RegArimaSpec.RG1;
        ns = RegArimaProto.convert(RegArimaProto.convert(s));
        assertTrue(s.equals(ns));
        s = RegArimaSpec.RG2;
        ns = RegArimaProto.convert(RegArimaProto.convert(s));
        assertTrue(s.equals(ns));
        s = RegArimaSpec.RG3;
        ns = RegArimaProto.convert(RegArimaProto.convert(s));
        assertTrue(s.equals(ns));
        s = RegArimaSpec.RG4;
        ns = RegArimaProto.convert(RegArimaProto.convert(s));
        assertTrue(s.equals(ns));
        s = RegArimaSpec.RG5;
        ns = RegArimaProto.convert(RegArimaProto.convert(s));
        assertTrue(s.equals(ns));

    }

}
