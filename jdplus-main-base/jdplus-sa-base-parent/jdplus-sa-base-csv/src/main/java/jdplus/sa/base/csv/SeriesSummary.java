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
package jdplus.sa.base.csv;

import jdplus.toolkit.base.api.information.Explorable;
import jdplus.sa.base.api.SaDocument;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.util.WildCards;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Kristof Bayens
 */
public class SeriesSummary {

    private final String name;
    private final Map<String, TsData> series = new LinkedHashMap<>();

    public SeriesSummary(String[] items, String name, SaDocument document) {
        this.name = name;
        fillDictionary(items, document.getResults());
    }

    public String getName() {
        return name;
    }

    private void fillDictionary(String[] items, Explorable results) {
        for (String item : items) {
            try {
                item = item.toLowerCase(Locale.ROOT);
                if (results != null) {
                    if (WildCards.hasWildCards(item)) {
                        Map<String, TsData> all = results.searchAll(item, TsData.class);
                        all.entrySet().forEach(entry -> series.put(entry.getKey(), entry.getValue()));
                    } else {
                        series.put(item, results.getData(item, TsData.class));
                    }
                } else {
                    series.put(item, null);
                }
            } catch (Exception err) {
                series.put(item, null);
            }

        }
    }

    public TsData getSeries(String name) {
        if (series.containsKey(name)) {
            return series.get(name);
        } else {
            return null;
        }
    }

    void fill(Set<String> set) {
        set.addAll(series.keySet());
    }
}
