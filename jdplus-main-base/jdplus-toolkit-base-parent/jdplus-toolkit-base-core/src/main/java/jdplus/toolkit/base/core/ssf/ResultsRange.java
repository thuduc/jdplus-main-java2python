/*
 * Copyright 2016 National Bank of Belgium
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
package jdplus.toolkit.base.core.ssf;

/**
 *
 * @author Jean Palate
 */
public final class ResultsRange implements Cloneable{
    
    private int start, end;
    
    public ResultsRange(){}
    
    public ResultsRange(int start, int end){
        this.start=start;
        this.end=end;
    }
    
    @Override
    public ResultsRange clone(){
        try {
            return (ResultsRange) super.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    public boolean isEmpty(){
        return start>=end;
    }
    
    public int getStart(){
        return start;
    }
    
    public int getEnd(){
        return end;
    }
    
    public void add(int pos){
        if (pos<start)
            start=pos;
        else if (pos>=end)
            end=pos+1;
    }

    public void setRange(int start, int end){
        this.start=start;
        this.end=end;
    }
    
    public void clear(){
        start=end=0;
    }
}
