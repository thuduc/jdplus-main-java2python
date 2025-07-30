/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.tramoseats.base.api.seats;

import jdplus.sa.base.api.SaDictionaries;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.dictionaries.AtomicDictionary;
import jdplus.toolkit.base.api.dictionaries.AtomicDictionary.Item;
import jdplus.toolkit.base.api.dictionaries.Dictionary;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class SeatsDictionaries {

    public final Dictionary LINDECOMPOSITION = AtomicDictionary.builder()
            .name("stochastic components")
            .item(Item.builder().name(SaDictionaries.Y_LIN).description("linearized series").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.SA_LIN).description("seasonal adjusted linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.T_LIN).description("trend linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.S_LIN).description("seasonal linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.I_LIN).description("irregular linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.SA_LIN_E).description("error on the seasonal adjusted linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.T_LIN_E).description("error on the trend linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.S_LIN_E).description("error on the seasonal linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.I_LIN_E).description("error on the irregular linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.Y_LIN_F).description("forecasts of the linearized series").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.SA_LIN_F).description("forecasts of the seasonal adjusted linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.T_LIN_F).description("forecasts of the trend linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.S_LIN_F).description("forecasts of the seasonal linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.I_LIN_F).description("forecasts of the irregular linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.Y_LIN_EF).description("forecast errors of the linearized series").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.SA_LIN_EF).description("forecast errors of the seasonal adjusted linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.T_LIN_EF).description("forecast errors of the trend linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.S_LIN_EF).description("forecast errors of the seasonal linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.I_LIN_EF).description("forecast errors of the irregular linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.Y_LIN_B).description("backcast of the linearized series").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.SA_LIN_B).description("backcast of the seasonal adjusted linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.T_LIN_B).description("backcast of the trend linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.S_LIN_B).description("backcast of the seasonal linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.I_LIN_B).description("backcast of the irregular linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.Y_LIN_EB).description("backcast errors of the linearized series").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.SA_LIN_EB).description("backcast errors of the seasonal adjusted linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.T_LIN_EB).description("backcast errors of the trend linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.S_LIN_EB).description("backcast errors of the seasonal linearized component").outputClass(TsData.class).build())
            .item(Item.builder().name(SaDictionaries.I_LIN_EB).description("backcast errors of the irregular linearized component").outputClass(TsData.class).build())
            .build();
}
