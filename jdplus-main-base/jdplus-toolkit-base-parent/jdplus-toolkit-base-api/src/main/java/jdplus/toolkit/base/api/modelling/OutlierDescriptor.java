/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.toolkit.base.api.modelling;

import nbbrd.design.Development;

/**
 *
 * @author palatej
 */
@lombok.Value
@Development(status = Development.Status.Release)
public class OutlierDescriptor {
    private String code;
    private int position;
    
    @Override
    public String toString(){
        return code+"."+(position+1);
    }
}
