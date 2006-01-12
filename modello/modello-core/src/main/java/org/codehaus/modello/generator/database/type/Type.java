package org.codehaus.modello.generator.database.type;

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

/**
 * Describes an SQL type
 *
 * @author <a href="mailto:tima@intalio.com">Tim Anderson</a>
 * @version 1.1 2003/02/05 08:08:37
 */
public class Type
{

    /**
     * The name of the type.
     */
    private String sqlName;

    /**
     * The maximum precision or value of the type
     */
    private long size;

    /**
     * The minimum scale supported by the type
     */
    private short minScale;

    /**
     * The maximum scale supported by the type
     */
    private short maxScale;


    /**
     * Construct a new <code>Type</code>
     */
    public Type()
    {
    }

    /**
     * Construct a new <code>Type</code>
     *
     * @param sqlName  the SQL name of the type
     * @param size     the maximum size/precision of the type
     * @param minScale the minimum scale supported by the type
     * @param maxScale the maximum scale supported by the type
     */
    public Type( String sqlName, long size, short minScale, short maxScale )
    {
        this.sqlName = sqlName;
        this.size = size;
        this.minScale = minScale;
        this.maxScale = maxScale;
    }

    /**
     * Returns the SQL name of the type
     */
    public String getSQLName()
    {
        return sqlName;
    }

    /**
     * Sets the SQL name of the type
     */
    public void setSQLName( String name )
    {
        this.sqlName = name;
    }

    /**
     * Returns the maximum size (or precision) of the type
     */
    public long getSize()
    {
        return size;
    }

    /**
     * Sets the maximum size (or precision) of the type
     */
    public void setSize( long size )
    {
        this.size = size;
    }

    /**
     * Returns the minimum scale of the type
     */
    public short getMinimumScale()
    {
        return minScale;
    }

    /**
     * Sets the minimum scale of the type
     */
    public void setMinimumScale( short scale )
    {
        minScale = scale;
    }

    /**
     * Returns the maximum scale of the type
     */
    public short getMaximumScale()
    {
        return maxScale;
    }

    /**
     * Sets the maximum scale of the type
     */
    public void setMaximumScale( short scale )
    {
        maxScale = scale;
    }

    /**
     * Helper to return a stringified version of the type, for debug purposes
     */
    public String toString()
    {
        return super.toString() + "[SQLName=" + sqlName + ";size=" + size +
            ";minimumScale=" + minScale + ";maximumScale=" + maxScale + "]";
    }

}
