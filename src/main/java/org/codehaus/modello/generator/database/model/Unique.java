package org.codehaus.modello.generator.database.model;

/*
 * Copyright (c) 2004, Jason van Zyl
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

import java.util.List;

/**
 * Provides compatibility with Torque-style xml with separate &lt;index&gt; and
 * &lt;unique&gt; tags, but adds no functionality.  All indexes are treated the
 * same by the Table.
 * 
 * @author <a href="mailto:jmarshall@connectria.com">John Marshall</a>
 * @version $Revision$
 */
public class Unique extends Index
{
    public Unique()
    {
        setUnique( true );
    }

    public void setUnique( boolean unique )
    {
        if ( unique == false )
        {
            throw new IllegalArgumentException( "Unique index cannot be made non-unique" );
        }
        super.setUnique( unique );
    }

    public boolean isUnique()
    {
        return true;
    }

    public void addUniqueColumn( UniqueColumn indexColumn )
    {
        super.addIndexColumn( indexColumn );
    }

    public List getUniqueColumns()
    {
        return super.getIndexColumns();
    }

}
