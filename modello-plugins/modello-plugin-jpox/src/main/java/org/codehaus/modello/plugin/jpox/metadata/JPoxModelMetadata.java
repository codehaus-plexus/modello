package org.codehaus.modello.plugin.jpox.metadata;

/*
 * Copyright (c) 2005, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.codehaus.modello.metadata.ModelMetadata;

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class JPoxModelMetadata implements ModelMetadata
{
    public static final String ID = JPoxModelMetadata.class.getName();
    
    public static final String ERROR = "error";
    
    public static final String WARNING = "warning";

    private String columnPrefix;

    private String tablePrefix;
    
    private String reservedWordStrictness;
    
    public String getColumnPrefix()
    {
        return columnPrefix;
    }

    public void setColumnPrefix( String columnPrefix )
    {
        this.columnPrefix = columnPrefix;
    }

    public String getTablePrefix()
    {
        return tablePrefix;
    }

    public void setTablePrefix( String tablePrefix )
    {
        this.tablePrefix = tablePrefix;
    }

    public String getReservedWordStrictness()
    {
        return reservedWordStrictness;
    }

    public void setReservedWordStrictness( String reservedWordStrictness )
    {
        this.reservedWordStrictness = reservedWordStrictness;
    }
}
