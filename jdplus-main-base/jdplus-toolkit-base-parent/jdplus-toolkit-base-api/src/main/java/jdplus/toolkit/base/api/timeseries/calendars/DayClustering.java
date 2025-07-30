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
package jdplus.toolkit.base.api.timeseries.calendars;

import nbbrd.design.Development;
import java.time.DayOfWeek;
import java.util.Arrays;

/**
 * Repartition of the days of the week in different groups
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
public class DayClustering {

    private final int[] groups;

    /**
     * Partition the days of the week in different groups.
     *
     * @param groups Indicates the group corresponding to each day. The length
     * of this array must be 7, corresponding to the different days of the week.
     * The first entry corresponds to Mondays and the last one to Sundays. The
     * different groups are identified by numbers ranging from 0 (corresponding
     * by default to non-working days) to ngroups-1 (6 at most). For instance,
     * the usual splitting in working days is identified by the array
     * [1,1,1,1,1,0,0] and the tradng days correspond to [1,2,3,4,5,6,0]
     * @return The requested clustering if the splitting is valid, null
     * otherwise.
     */
    public static DayClustering of(int[] groups) {
        if (groups.length != 7) {
            return null;
        }
        if (!checkGroups(groups)) {
            return null;
        }
        return new DayClustering(groups);
    }

    public static DayClustering of(TradingDaysType type) {
        switch (type) {
            case TD7:
                return TD7;
            case TD2:
                return TD2;
            case TD2c:
                return TD2c;
            case TD3:
                return TD3;
            case TD3c:
                return TD3c;
            case TD4:
                return TD4;
            default:
                return null;
        }
    }

    public TradingDaysType getType() {
        if (Arrays.equals(groups, TD7_IDX)) {
            return TradingDaysType.TD7;
        }
        if (Arrays.equals(groups, TD2_IDX)) {
            return TradingDaysType.TD2;
        }
        if (Arrays.equals(groups, TD3_IDX)) {
            return TradingDaysType.TD3;
        }
        if (Arrays.equals(groups, TD3C_IDX)) {
            return TradingDaysType.TD3c;
        }
        if (Arrays.equals(groups, TD2C_IDX)) {
            return TradingDaysType.TD2c;
        }
        if (Arrays.equals(groups, TD4_IDX)) {
            return TradingDaysType.TD4;
        }
        return TradingDaysType.TDuser;

    }

    private static boolean checkGroups(int[] groups) {
        int n = 0;
        int gr = 0;
        while (n < 7) {
            int ncur = 0;
            for (int i = 0; i < groups.length; ++i) {
                if (groups[i] == gr) {
                    ++ncur;
                }
            }
            if (ncur == 0) {
                return false;
            }
            n += ncur;
            gr++;
        }
        return gr > 1;
    }

    private DayClustering(final int[] groups) {
        this.groups = groups;
    }

    /**
     * The group of the given day of the week
     *
     * @param dw
     * @return
     */
    public int getGroup(DayOfWeek dw) {
        return groups[dw.getValue() - 1];
    }

    /**
     * Group of the 0-based day. Raw equivalent of getGroup
     *
     * @param day 0 for Mondays...
     * @return
     */
    public int groupOf(int day) {
        return groups[day];
    }

    /**
     * The number of groups
     *
     * @return
     */
    public int getGroupsCount() {
        int n = 0;
        for (int i = 0; i < groups.length; ++i) {
            if (groups[i] > n) {
                n = groups[i];
            }
        }
        return n + 1;
    }

    /**
     * The number of days in the given group
     *
     * @param idx
     * @return
     */
    public int getGroupCount(int idx) {
        int n = 0;
        for (int i = 0; i < groups.length; ++i) {
            if (groups[i] == idx) {
                ++n;
            }
        }
        return n;
    }

    /**
     * The days of the week belonging to the given group
     *
     * @param idx
     * @return
     */
    public DayOfWeek[] group(int idx) {
        int n = getGroupCount(idx);
        DayOfWeek[] dw = new DayOfWeek[n];
        for (int i = 0, j = 0; j < n; ++i) {
            if (groups[i] == idx) {
                dw[j++] = DayOfWeek.of(i + 1);
            }
        }
        return dw;
    }

    public int[] positions(int idx) {
        int n = 0;
        for (int i = 0; i < groups.length; ++i) {
            if (groups[i] == idx) {
                ++n;
            }
        }
        int[] dw = new int[n];
        for (int i = 0, j = 0; j < n; ++i) {
            if (groups[i] == idx) {
                dw[j++] = i;
            }
        }
        return dw;
    }

    /**
     *
     * @return element i of the result contains the days of the week
     * corresponding to the group i
     */
    public int[][] allPositions() {
        int n = getGroupsCount();
        int[][] all = new int[n][];
        for (int i = 0; i < n; ++i) {
            all[i] = positions(i);
        }
        return all;
    }

    /**
     *
     * @param dc
     * @return true if each group of the given clustering is included in one
     * group of this clustering
     */
    public boolean isInside(DayClustering dc) {
        int n = dc.getGroupsCount();
        for (int i = 0; i < n; ++i) {
            int cur = -1;
            for (int j = 0; j < 7; ++j) {
                if (dc.groups[j] == i) {
                    int g = groups[j];
                    if (cur == -1) {
                        cur = g;
                    } else if (g != cur) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        TradingDaysType type = getType();
        if (type != TradingDaysType.TDuser) {
            return type.name();
        }
        StringBuilder builder = new StringBuilder();
        int ng = getGroupsCount();
        for (int i = 0; i < ng; ++i) {
            builder.append('{').append(toString(i)).append('}');
        }
        return builder.toString();
    }

    public String toString(int i) {
        StringBuilder builder = new StringBuilder();
        DayOfWeek[] gr = group(i);
        builder.append(SHORTNAMES[gr[0].getValue()]);
        for (int j = 1; j < gr.length; ++j) {
            builder.append(',').append(SHORTNAMES[gr[j].getValue()]);
        }
        return builder.toString();
    }

    private static final int[] TD7_IDX = new int[]{1, 2, 3, 4, 5, 6, 0},
            TD2_IDX = new int[]{1, 1, 1, 1, 1, 0, 0},
            TD2C_IDX = new int[]{1, 1, 1, 1, 1, 1, 0},
            TD3_IDX = new int[]{1, 1, 1, 1, 1, 2, 0},
            TD3C_IDX = new int[]{1, 1, 1, 1, 2, 2, 0},
            TD4_IDX = new int[]{1, 1, 1, 1, 2, 3, 0};

    private static final String[] SHORTNAMES = new String[]{"mon", "tue", "wed", "thu", "fri", "sat", "sun"};

    public static final DayClustering TD2 = new DayClustering(TD2_IDX),
            TD2c = new DayClustering(TD2C_IDX),
            TD3 = new DayClustering(TD3_IDX),
            TD3c = new DayClustering(TD3C_IDX),
            TD4 = new DayClustering(TD4_IDX),
            TD7 = new DayClustering(TD7_IDX);

    /**
     * @return the groups
     */
    public int[] getGroupsDefinition() {
        return groups.clone();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Arrays.hashCode(this.groups);
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof DayClustering) {
            DayClustering x = (DayClustering) other;
            return Arrays.equals(groups, x.groups);
        } else {
            return false;
        }
    }
}
