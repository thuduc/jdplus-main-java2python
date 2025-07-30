/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.toolkit.base.api.timeseries;

import jdplus.toolkit.base.api.data.Range;
import nbbrd.design.Development;
import java.time.LocalDateTime;
import lombok.NonNull;
import lombok.AccessLevel;

/**
 * Defines selection in a time domain
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.Builder(builderClassName="Builder", toBuilder=true)
public class TimeSelector {

    private static final TimeSelector ALL = new TimeSelector(SelectionType.All, LocalDateTime.MIN, LocalDateTime.MAX, 0, 0),
            NONE = new TimeSelector(SelectionType.None, LocalDateTime.MIN, LocalDateTime.MAX, 0, 0);

    public static enum SelectionType {
        /**
         *
         */
        None,
        /**
         *
         */
        All,
        /**
         *
         */
        From,
        /**
         *
         */
        To,
        /**
         *
         */
        Between,
        /**
         *
         */
        Last,
        /**
         *
         */
        First,
        /**
         *
         */
        Excluding;
    }
    
    public static Builder builder(){
        return new Builder()
                .type(SelectionType.All)
                .d0(LocalDateTime.MIN)
                .d1(LocalDateTime.MAX);        
    }

    /**
     * The type of the selector
     */
    @lombok.NonNull
    private final SelectionType type;

    /**
     * The starting day of the selector. Its interpretation depends on on the
     * type of the selector
     */
    @lombok.NonNull
    private final LocalDateTime d0;

    /**
     * The ending day of the selector. Its interpretation depends on on the type
     * of the selector. May be unused
     */
    @lombok.NonNull
    private final LocalDateTime d1;

    /**
     * The number of starting periods defined by the selector. Its
     * interpretation depends on on the type of the selector. May be unused
     */
    private final int n0;

    /**
     * The number of ending periods defined by the selector. Its interpretation
     * depends on on the type of the selector. May be unused
     */
    private final int n1;

    /**
     * Select all the periods
     *
     * @return
     */
    public static TimeSelector all() {
        return ALL;
    }

    /**
     * Select all the periods between two days. The way incomplete periods are
     * considered is left to the classes that use the selector.
     *
     * @param start The starting day
     * @param end The ending day
     * @return
     */
    public static TimeSelector between(@NonNull LocalDateTime start, @NonNull LocalDateTime end) {
        return new TimeSelector(SelectionType.Between, start, end, 0, 0);
    }

    public static TimeSelector between(@NonNull Range<LocalDateTime> range) {
        return new TimeSelector(SelectionType.Between, range.start(), range.end(), 0, 0);
    }

    /**
     * Excludes some periods at the beginning and/or at the end of a time domain
     *
     * @param n0 Number of periods excluded at the beginning of the time domain.
     * Greater or equal to 0.
     * @param n1 Number of periods excluded at the end of the time domain.
     * Greater or equal to 0.
     * @return
     */
    public static TimeSelector excluding(final int n0, final int n1) {
        return new TimeSelector(SelectionType.Excluding, LocalDateTime.MIN, LocalDateTime.MAX, n0, n1);
    }

    /**
     * Select a given number of periods at the beginning of a time domain
     *
     * @param n The number of selected periods
     * @return
     */
    public static TimeSelector first(final int n) {
        return new TimeSelector(SelectionType.First, LocalDateTime.MIN, LocalDateTime.MAX, n, 0);
    }

    /**
     * Select the periods after a given date. The way incomplete periods are
     * considered is left to the classes that use the selector.
     *
     * @param d0 The date for the selection
     * @return
     */
    public static TimeSelector from(@NonNull final LocalDateTime d0) {
        return new TimeSelector(SelectionType.From, d0, LocalDateTime.MAX, 0, 0);
    }

    /**
     * Select a given number of periods at the end of a time domain
     *
     * @param n The number of selected periods
     * @return
     */
    public static TimeSelector last(final int n) {
        return new TimeSelector(SelectionType.Last, LocalDateTime.MIN, LocalDateTime.MAX, 0, n);
    }

    /**
     * Select nothing
     *
     * @return
     */
    public static TimeSelector none() {
        return NONE;
    }

    /**
     * Select the periods up to a given date. The way incomplete periods are
     * considered is left to the classes that use the selector.
     *
     * @param d1 The date for the selection
     * @return
     */
    public static TimeSelector to(final LocalDateTime d1) {
        return new TimeSelector(SelectionType.To, LocalDateTime.MIN, d1, 0, 0);
    }

    @Override
    public String toString() {
        switch (type) {
            case Between:
                return d0.toString() + " - " + d1.toString();
            case Excluding: {
                if (n0 == 0 && n1 == 0) {
                    return "";
                }
                StringBuilder builder = new StringBuilder();
                builder.append("All but ");
                if (n0 != 0) {
                    builder.append("first ");
                    if (n0 > 1) {
                        builder.append(n0).append(" periods");
                    } else if (n0 > 0) {
                        builder.append("period");
                    } else if (n0 < -1) {
                        builder.append(-n0).append(" years");
                    } else if (n0 < 0) {
                        builder.append("year");
                    }
                    if (n1 != 0) {
                        builder.append(" and ");
                    }
                }
                if (n1 != 0) {
                    builder.append("last ");
                    if (n1 > 1) {
                        builder.append(n1).append(" periods");
                    } else if (n1 > 0) {
                        builder.append("period");
                    } else if (n1 < -1) {
                        builder.append(-n1).append(" years");
                    } else if (n1 < 0) {
                        builder.append("year");
                    }
                }
                return builder.toString();
            }
            case First: {
                StringBuilder builder = new StringBuilder();
                if (n0 > 0) {
                    builder.append("first ");
                    if (n0 > 1) {
                        builder.append(n0).append(" periods");
                    } else {
                        builder.append("period");
                    }
                    if (n1 > 0) {
                        builder.append(" and ");
                    }
                }
                return builder.toString();
            }
            case Last: {
                StringBuilder builder = new StringBuilder();
                if (n1 > 0) {
                    builder.append("last ");
                    if (n1 > 1) {
                        builder.append(n1).append(" periods");
                    } else {
                        builder.append("period");
                    }
                }
                return builder.toString();
            }
            case From:
                return "From " + d0.toString();
            case To:
                return "Until " + d1.toString();
            case All:
                return "All";
            case None:
                return "None";
            default:
                return "";
        }
    }

   public String toDateString() {
       StringBuilder builder=new StringBuilder();
        switch (type) {
            case Between:
                if (!d0.equals(LocalDateTime.MIN))
                    builder.append(d0.toLocalDate());
                builder.append(" - ");
                if (!d1.equals(LocalDateTime.MAX))
                    builder.append(d1.toLocalDate().minusDays(1));
                return builder.toString();
            case Excluding: {
                if (n0 == 0 && n1 == 0) {
                    return "";
                }
                builder.append("All but ");
                if (n0 != 0) {
                    builder.append("first ");
                    if (n0 > 1) {
                        builder.append(n0).append(" periods");
                    } else if (n0 > 0) {
                        builder.append("period");
                    } else if (n0 < -1) {
                        builder.append(-n0).append(" years");
                    } else if (n0 < 0) {
                        builder.append("year");
                    }
                    if (n1 != 0) {
                        builder.append(" and ");
                    }
                }
                if (n1 != 0) {
                    builder.append("last ");
                    if (n1 > 1) {
                        builder.append(n1).append(" periods");
                    } else if (n1 > 0) {
                        builder.append("period");
                    } else if (n1 < -1) {
                        builder.append(-n1).append(" years");
                    } else if (n1 < 0) {
                        builder.append("year");
                    }
                }
                return builder.toString();
            }
            case First: {
                if (n0 > 0) {
                    builder.append("first ");
                    if (n0 > 1) {
                        builder.append(n0).append(" periods");
                    } else {
                        builder.append("period");
                    }
                    if (n1 > 0) {
                        builder.append(" and ");
                    }
                }
                return builder.toString();
            }
            case Last: {
                 if (n1 > 0) {
                    builder.append("last ");
                    if (n1 > 1) {
                        builder.append(n1).append(" periods");
                    } else {
                        builder.append("period");
                    }
                }
                return builder.toString();
            }
            case From:
                builder.append("From ");
                if (!d0.equals(LocalDateTime.MIN))
                    builder.append(d0.toLocalDate());
                return builder.toString();
            case To:
                builder.append("Until ");
                if (!d1.equals(LocalDateTime.MAX))
                    builder.append(d1.toLocalDate().minusDays(1));
                 return builder.toString();
           case All:
                return "All";
            case None:
                return "None";
            default:
                return "";
        }
    }

   public boolean isAll() {
        return type == SelectionType.All;
    }

    public boolean isNone() {
        return type == SelectionType.None;
    }
}
