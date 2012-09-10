package org.codehaus.modello.plugin.model;

/*
 * Copyright (c) 2004, Codehaus.org
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

import org.codehaus.modello.metadata.ClassMetadata;

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class ModelClassMetadata
    implements ClassMetadata
{
    public static final String ID = ModelClassMetadata.class.getName();

    private boolean rootElement = false;

    private String locationTracker;

    private String sourceTracker;

    public boolean isRootElement()
    {
        return rootElement;
    }

    public void setRootElement( boolean rootElement )
    {
        this.rootElement = rootElement;
    }

    public String getLocationTracker()
    {
        return locationTracker;
    }

    public void setLocationTracker( String locationTracker )
    {
        this.locationTracker = locationTracker;
    }

    public String getSourceTracker()
    {
        return sourceTracker;
    }

    public void setSourceTracker( String sourceTracker )
    {
        this.sourceTracker = sourceTracker;
    }

}
