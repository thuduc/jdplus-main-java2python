/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.x13.desktop.plugin.html;

import jdplus.toolkit.desktop.plugin.html.AbstractHtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTable;
import jdplus.toolkit.desktop.plugin.html.HtmlTableCell;
import jdplus.toolkit.desktop.plugin.html.HtmlTableHeader;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import jdplus.x13.base.api.x11.MsrTable;
import java.io.IOException;

/**
 *
 * @author Thomas Witthohn
 */
public class HtmlMovingSeasonalityRatios extends AbstractHtmlElement {

    private final MsrTable msrTable;
    private final String TITLE = "Moving Seasonality Ratios (MSR)";
    private final String[] HEADERS = new String[]{"Period", "I", "S", "MSR"};

    public HtmlMovingSeasonalityRatios(MsrTable msrTable) {
        this.msrTable = msrTable;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, TITLE);
        double[][] data = new double[3][];
        data[0] = msrTable.getMeanIrregularEvolutions();
        data[1] = msrTable.getMeanSeasonalEvolutions();

        int len = data[0].length;

        data[2] = new double[len];
        for (int i = 0; i < len; ++i) {
            data[2][i] = msrTable.getMsr(i);
        }

        stream.open(new HtmlTable().withWidth(50 * HEADERS.length));
        stream.open(HtmlTag.TABLEROW);
        for (int j = 0; j < HEADERS.length; ++j) {
            stream.write(new HtmlTableHeader(HEADERS[j]).withWidth(50));
        }
        stream.close(HtmlTag.TABLEROW);
        for (int i = 1; i <= len; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(Integer.toString(i)).withWidth(50));
            for (int j = 0; j < data.length; ++j) {
                if (data[j] != null) {
                    stream.write(new HtmlTableCell(df4.format(data[j][i - 1])).withWidth(50));
                } else {
                    stream.write(new HtmlTableCell(".").withWidth(50));
                }
            }
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);
    }

}
