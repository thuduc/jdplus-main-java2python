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

package jdplus.toolkit.desktop.plugin.html.modelling;

import jdplus.toolkit.desktop.plugin.html.Bootstrap4;
import jdplus.toolkit.desktop.plugin.html.HtmlElement;
import jdplus.toolkit.desktop.plugin.html.HtmlStream;
import jdplus.toolkit.desktop.plugin.html.HtmlTag;
import java.io.IOException;
import jdplus.toolkit.base.core.arima.IArimaModel;

/**
 * 
 * @author Jean Palate & BAYENSK
 */
public class HtmlUcarima implements HtmlElement {

    private IArimaModel model_;
    private String[] names_;
    private IArimaModel[] cmps_;

    /**
     * 
     * @param model
     * @param cmps
     * @param names
     */
    public HtmlUcarima(IArimaModel model, IArimaModel[] cmps, String[] names)
    {
	model_ = model;
	cmps_ = cmps;
	names_ = names;
    }

    /**
     * 
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException
    {
	writeDecomposition(stream);
    }
    
    public void writeDecomposition(HtmlStream stream) throws IOException
    {
	// HTMLFont mfont=new HTMLFont("arial", "blue", null, 3, false);
	// HTMLFont font=new HTMLFont("arial", null, null, 3, false);
	// stream.open(font);
	if (model_ != null)
	    stream.write(HtmlTag.IMPORTANT_TEXT, "Model", Bootstrap4.TEXT_INFO).newLine()
		    .write(new HtmlArima(model_)).newLine();
	for (int i = 0; i < cmps_.length; ++i) {
	    if (cmps_[i] == null || cmps_[i].isNull())
		continue;
	    String name = names_ != null ? names_[i] : "Cmp_"
		    + Integer.toString(i + 1);
	    stream.write(HtmlTag.IMPORTANT_TEXT, name, Bootstrap4.TEXT_INFO).newLine().write(new HtmlArima(cmps_[i]));
	}
    }

    public void writeSummary(HtmlStream stream) throws IOException {
        if (names_ != null) {
            for (int i = 0; i<names_.length; ++i) {
                HtmlArima arima = new HtmlArima(cmps_[i]);
                stream.write(names_[i]);
                arima.writeShortModel(stream);
                stream.newLine();
            }
        }
    }
}
