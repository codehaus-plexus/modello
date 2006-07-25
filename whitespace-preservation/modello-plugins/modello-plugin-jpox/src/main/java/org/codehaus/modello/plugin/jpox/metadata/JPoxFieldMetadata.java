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

import org.codehaus.modello.metadata.FieldMetadata;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class JPoxFieldMetadata
    implements FieldMetadata
{
    public static final String ID = JPoxFieldMetadata.class.getName();

    private List fetchGroupNames;

    private String mappedBy;

    private String nullValue;

    public List getFetchGroupNames()
    {
        return fetchGroupNames;
    }

    public void setFetchGroupNames( List fetchGroupNames )
    {
        this.fetchGroupNames = fetchGroupNames;
    }

    public String getMappedBy()
    {
        return mappedBy;
    }

    public void setMappedBy( String mappedBy )
    {
        this.mappedBy = mappedBy;
    }

    public String getNullValue()
    {
        return nullValue;
    }

    public void setNullValue( String nullValue )
    {
        this.nullValue = nullValue;
    }

}
