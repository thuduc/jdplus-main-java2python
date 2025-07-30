/*
 * Copyright 2021 National Bank of Belgium
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
package jdplus.x13.base.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.protobuf.modelling.ModellingProtosUtility;
import jdplus.x13.base.api.regarima.TradingDaysSpec;
import jdplus.toolkit.base.api.timeseries.calendars.LengthOfPeriodType;
import jdplus.toolkit.base.api.timeseries.calendars.TradingDaysType;
import jdplus.toolkit.base.protobuf.toolkit.ToolkitProtosUtility;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TradingDaysProto {

    public void fill(TradingDaysSpec spec, RegArimaSpec.TradingDaysSpec.Builder builder) {

        String holidays = spec.getHolidays();
        if (holidays != null) {
            builder.setHolidays(holidays)
                    .setLp(ModellingProtosUtility.convert(spec.getLengthOfPeriodType()))
                    .setTd(ModellingProtosUtility.convert(spec.getTradingDaysType()))
                    .setAuto(X13ProtosUtility.convert(spec.getAutomaticMethod()))
                    .setTest(X13ProtosUtility.convert(spec.getRegressionTestType()))
                    .setAutoAdjust(spec.isAutoAdjust())
                    .setPtest1(spec.getAutoPvalue1())
                    .setPtest2(spec.getAutoPvalue2());
            return;
        }

        String[] userVariables = spec.getUserVariables();
        if (userVariables != null && userVariables.length > 0) {
            for (String v : userVariables) {
                builder.addUsers(v);
            }
            builder.setTest(X13ProtosUtility.convert(spec.getRegressionTestType()));
            return;
        }
        int w = spec.getStockTradingDays();
        if (w > 0) {
            builder.setW(w)
                    .setTest(X13ProtosUtility.convert(spec.getRegressionTestType()));
            return;
        }
        builder.setLp(ModellingProtosUtility.convert(spec.getLengthOfPeriodType()))
                .setAutoAdjust(spec.isAutoAdjust());
        if (spec.isAutomatic()) {
            builder.setAuto(X13ProtosUtility.convert(spec.getAutomaticMethod()))
                    .setPtest1(spec.getAutoPvalue1())
                    .setPtest2(spec.getAutoPvalue2());
        } else {
            builder.setTd(ModellingProtosUtility.convert(spec.getTradingDaysType()))
                    .setTest(X13ProtosUtility.convert(spec.getRegressionTestType()));
        }
    }

    public RegArimaSpec.TradingDaysSpec convert(TradingDaysSpec spec) {
        RegArimaSpec.TradingDaysSpec.Builder builder = RegArimaSpec.TradingDaysSpec.newBuilder();
        fill(spec, builder);
        return builder.setLpcoefficient(ToolkitProtosUtility.convert(spec.getLpCoefficient()))
                .addAllTdcoefficients(ToolkitProtosUtility.convert(spec.getTdCoefficients()))
                .build();
    }

    public byte[] toBuffer(TradingDaysSpec spec) {
        return convert(spec).toByteArray();
    }

    private boolean isTest(RegArimaSpec.TradingDaysSpec spec) {
        return spec.getAuto() != AutomaticTradingDays.TD_AUTO_NO
                || spec.getTest() == RegressionTest.TEST_ADD
                || spec.getTest() == RegressionTest.TEST_REMOVE;
    }

    public TradingDaysSpec convert(RegArimaSpec.TradingDaysSpec spec) {
        TradingDaysType td = ModellingProtosUtility.convert(spec.getTd());
        LengthOfPeriodType lp = ModellingProtosUtility.convert(spec.getLp());
        Parameter lpc = ToolkitProtosUtility.convert(spec.getLpcoefficient());
        Parameter[] tdc = ToolkitProtosUtility.convert(spec.getTdcoefficientsList());
        boolean test = isTest(spec);
        String holidays = spec.getHolidays();
        if (holidays != null && holidays.length() > 0) {
            TradingDaysSpec.AutoMethod auto = X13ProtosUtility.convert(spec.getAuto());
            if (auto != TradingDaysSpec.AutoMethod.UNUSED) {
                return TradingDaysSpec.automaticHolidays(holidays, lp, auto, spec.getPtest1(), spec.getPtest2(), spec.getAutoAdjust());
            }
            if (test) {
                return TradingDaysSpec.holidays(holidays, td, lp,
                        X13ProtosUtility.convert(spec.getTest()),
                        spec.getAutoAdjust());
            } else {
                return TradingDaysSpec.holidays(holidays, td, lp, tdc, lpc);
            }

        }
        int nusers = spec.getUsersCount();
        if (nusers > 0) {
            String[] users = new String[nusers];
            for (int i = 0; i < nusers; ++i) {
                users[i] = spec.getUsers(i);
            }
            if (test) {
                return TradingDaysSpec.userDefined(users, X13ProtosUtility.convert(spec.getTest()));
            } else {
                return TradingDaysSpec.userDefined(users, tdc);
            }
        }
        int w = spec.getW();
        if (w > 0) {
            if (test) {
                return TradingDaysSpec.stockTradingDays(w, X13ProtosUtility.convert(spec.getTest()));
            } else {
                return TradingDaysSpec.stockTradingDays(w, tdc);
            }
        }
        TradingDaysSpec.AutoMethod auto = X13ProtosUtility.convert(spec.getAuto());
        if (auto != TradingDaysSpec.AutoMethod.UNUSED) {
            return TradingDaysSpec.automatic(lp, auto, spec.getPtest1(), spec.getPtest2(), spec.getAutoAdjust());
        } else if (td == TradingDaysType.NONE) {
            return TradingDaysSpec.none();
        } else {
            if (test) {
                return TradingDaysSpec.td(td, lp,
                        X13ProtosUtility.convert(spec.getTest()),
                        spec.getAutoAdjust());
            } else {
                return TradingDaysSpec.td(td, lp, tdc, lpc);
            }
        }
    }

    public TradingDaysSpec of(byte[] bytes) throws InvalidProtocolBufferException {
        RegArimaSpec.TradingDaysSpec spec = RegArimaSpec.TradingDaysSpec.parseFrom(bytes);
        return convert(spec);
    }

}
