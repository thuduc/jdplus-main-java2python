/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.base.api.x11;

import nbbrd.design.Development;

/**
 *
 * @author palatej
 */
@Development(status = Development.Status.Beta)
public enum BiasCorrection {
    None,
    Legacy,
    Smooth,
    Ratio
}
