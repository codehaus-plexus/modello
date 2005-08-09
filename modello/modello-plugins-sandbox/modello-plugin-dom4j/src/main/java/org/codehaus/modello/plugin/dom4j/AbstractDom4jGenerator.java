package org.codehaus.modello.plugin.dom4j;

/*
 * Copyright (c) 2004, Joakim Erdfelt
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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.generator.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.AbstractModelloGenerator;

/**
 * AbstractDom4jGenerator
 *
 * @since Aug 7, 2005
 * @author Joakim Erdfelt
 * @version $Id$
 */
public abstract class AbstractDom4jGenerator
    extends AbstractModelloGenerator
{
    private static final boolean BREADCRUMB = false;

    protected String getFileName( String suffix )
        throws ModelloException
    {
        String name = getModel().getName();

        return name + suffix;
    }

    /**
     * compile time breadcrumb as a java comment.
     */
    protected void breadcrumb( JSourceCode sc, String msg )
    {
        if ( BREADCRUMB )
        {
            sc.add( "/* " + msg + " */ " );
        }
    }
}
