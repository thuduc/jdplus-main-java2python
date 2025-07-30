/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package jdplus.toolkit.base.api.util;

import java.util.Collections;
import java.util.function.Function;
import lombok.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class MultiLineNameUtil {

    private static final char RIGHT_POINTING_TRIANGLE = '\u25b6';
    private static final char HORIZONTAL_ELLIPSIS = '\u2026';

    private static final String JOIN_SEP = " " + RIGHT_POINTING_TRIANGLE + " ";
    private static final String LAST_PREFIX = HORIZONTAL_ELLIPSIS + " ";

    public static final String SEPARATOR = "\n";

    @NonNull
    public static String join(@NonNull String input) {
        return join(input, JOIN_SEP);
    }

    @NonNull
    public static String join(@NonNull String input, @NonNull String separator) {
        return input.replace(SEPARATOR, separator);
    }

    @NonNull
    public static String toHtml(@NonNull String input) {
//        return "<html>" + input.replace(SEPARATOR, "<br>");
        String[] items = input.split(SEPARATOR, -1);
        Function<Integer, Iterable<Integer>> children = o -> o < items.length - 1 ? Collections.singletonList(o + 1) : Collections.emptyList();
        Function<Integer, String> toString = o -> (o == 0 ? "" : " ") + items[o] + "<br>";
        String result = TreeTraverser.of(0, children).prettyPrintToString(Integer.MAX_VALUE, toString);
        return "<html>" + result.replace(" ", "&nbsp;");
    }

    @NonNull
    public static String last(@NonNull String input) {
        int index = input.lastIndexOf(SEPARATOR);
        return index == -1 ? input : input.substring(index + 1);
    }

    @NonNull
    public static String lastWithMax(@NonNull String input, int maxLength) {
        String last = last(input);
        return last.length() < maxLength ? last : (LAST_PREFIX + last.substring(last.length() - maxLength));
    }
}
