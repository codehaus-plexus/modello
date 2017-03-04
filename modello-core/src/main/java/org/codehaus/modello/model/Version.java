package org.codehaus.modello.model;

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

import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.plexus.util.StringUtils;

/**
 * A version string is on the form &lt;major&gt;.&lt;minor&gt;.&lt;micro&gt;.
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class Version
    implements Comparable<Version>
{
    public static final Version INFINITE = new Version( "32767.32767.32767" );

    private short major;

    private short minor;

    private short micro;

    public Version( String version )
    {
        if ( version == null )
        {
            throw new ModelloRuntimeException( "Syntax error in the version field: Missing. " );
        }

        String[] splittedVersion = StringUtils.split( version.trim(), "." );

        if ( splittedVersion.length > 3 )
        {
            throw new ModelloRuntimeException(
                "Syntax error in the <version> field: The field must be at more 3 parts long (major, minor and micro). Was: '"
                + version + "'." );
        }

        String majorString = splittedVersion[0];

        String minorString = "0";

        String microString = "0";

        if ( splittedVersion.length > 1 )
        {
            minorString = splittedVersion[1];

            if ( splittedVersion.length > 2 )
            {
                microString = splittedVersion[2];

            }
        }

        try
        {
            major = Short.parseShort( majorString );

            minor = Short.parseShort( minorString );

            micro = Short.parseShort( microString );
        }
        catch ( NumberFormatException e )
        {
            throw new ModelloRuntimeException( "Invalid version string: '" + version + "'." );
        }
    }

    public int getMajor()
    {
        return major;
    }

    public int getMinor()
    {
        return minor;
    }

    public int getMicro()
    {
        return micro;
    }

    /**
     * Returns true if <code>this</code> is greater that <code>other</code>.
     *
     * @param other the other {@link Version}
     * @return {@code true} if this instance is greater than other instance, otherwise {@code false}
     */
    public boolean greaterThan( Version other )
    {
        if ( this.major != other.major )
        {
            return major > other.major;
        }

        if ( this.minor != other.minor )
        {
            return this.minor > other.minor;
        }

        if ( this.micro != other.micro )
        {
            return this.micro > other.micro;
        }

        return false;
    }

    /**
     * Returns true if <code>this</code> is greater or equals than <code>other</code>.
     *
     * @param other the other {@link Version}
     * @return {@code true} if this instance is greater or equals than other instance, otherwise {@code false}
     */
    public boolean greaterOrEqualsThan( Version other )
    {
        if ( this.major != other.major )
        {
            return major >= other.major;
        }

        if ( this.minor != other.minor )
        {
            return this.minor >= other.minor;
        }

        if ( this.micro != other.micro )
        {
            return this.micro >= other.micro;
        }

        return false;
    }

    /**
     * Returns true if <code>this</code> is lesser than <code>other</code>.
     *
     * @param other the other {@link Version}
     * @return {@code true} if this instance is lesser than other instance, otherwise {@code false}
     */
    public boolean lesserThan( Version other )
    {
        if ( this.major != other.major )
        {
            return major < other.major;
        }

        if ( this.minor != other.minor )
        {
            return this.minor < other.minor;
        }

        if ( this.micro != other.micro )
        {
            return this.micro < other.micro;
        }

        return false;
    }

    /**
     * Returns true if <code>this</code> is lesser or equals that <code>other</code>.
     *
     * @param other the other {@link Version}
     * @return {@code true} if this instance is lesser or equals than other instance, otherwise {@code false}
     */
    public boolean lesserOrEqualsThan( Version other )
    {
        if ( this.major != other.major )
        {
            return major <= other.major;
        }

        if ( this.minor != other.minor )
        {
            return this.minor <= other.minor;
        }

        if ( this.micro != other.micro )
        {
            return this.micro <= other.micro;
        }

        return false;
    }

    public boolean inside( VersionRange range )
    {
        if ( range.getFromVersion().equals( this ) )
        {
            return true;
        }
        else if ( ( this.greaterThan( range.getFromVersion() ) ) && ( this.lesserThan( range.getToVersion() ) ) )
        {
            return true;
        }
        else if ( this.equals( range.getFromVersion() ) || this.equals( range.getToVersion() ) )
        {
            return true;
        }

        return false;
    }

    // ----------------------------------------------------------------------
    // Object overrides
    // ----------------------------------------------------------------------

    public boolean equals( Object object )
    {
        if ( !( object instanceof Version ) )
        {
            return false;
        }

        Version other = (Version) object;

        return this.major == other.major && this.minor == other.minor && this.micro == other.micro;
    }

    public int hashCode()
    {
        return toString( "", null ).hashCode();
    }

    public String toString()
    {
        return toString( "", "." );
    }

    public String toString( String prefix, String separator )
    {
        return prefix + major + separator + minor + separator + micro;
    }

    public int compareTo( Version otherVersion )
    {
        if ( greaterThan( otherVersion ) )
        {
            return +1;
        }
        else if ( equals( otherVersion ) )
        {
            return 0;
        }
        else
        {
            return -1;
        }
    }
}
