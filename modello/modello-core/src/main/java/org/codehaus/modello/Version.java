package org.codehaus.modello;

/*
 * LICENSE
 */

/**
 * A version string is on the form <major>.<minor>.<micro>.
 * 
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public class Version
{
    private short major;

    private short minor;

    private short micro;

    public Version( String version )
    {
        if ( version == null )
        {
            throw new ModelloRuntimeException( "Syntax error in the version field: Missing. " );
        }

        if ( version.length() < 5 )
        {
            throw new ModelloRuntimeException( "Syntax error in the <version> field: The field must be at least 5 characters long. Was: '" + version + "'." );
        }

        version = version.trim();

        String majorString = version.substring( 0, 1 );

        String minorString = version.substring( 2, 3 );

        String microString = version.substring( 4, 5 );

        try
        {
            major = Short.parseShort( majorString );

            minor = Short.parseShort( minorString );

            micro = Short.parseShort( microString );
        }
        catch ( NumberFormatException e )
        {
            throw new ModelloRuntimeException( "Version is invalid: " + version );
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

    // ----------------------------------------------------------------------
    // Comparison methods
    // ----------------------------------------------------------------------

    public boolean equals( Object object )
    {
        if ( !(object instanceof Version ) )
        {
            return false;
        }

        Version other = (Version) object;

        return this.major == other.major &&
               this.minor == other.minor &&
               this.micro == other.micro;
    }

    /**
     * Returns true if <code>this</code> is greater that <code>other</code>.
     * 
     * @param version
     * @return
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

    public boolean inside( VersionRange range )
    {
        if ( range.getFromVersion().equals( this ) )
        {
            return true;
        }
        else if ( ( this.greaterThan( range.getFromVersion() ) ) && range.isToInfinite() )
        {
            return true;
        }

        return false;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public String toString()
    {
        return toString( "v" );
    }

    public String toString( String prefix )
    {
        return prefix + major + minor + micro;
    }
}
