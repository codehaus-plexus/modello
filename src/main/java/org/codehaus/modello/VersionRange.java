package org.codehaus.modello;

/*
 * LICENSE
 */

public class VersionRange
{
    private Version fromVersion;

    private char modifier;

    public VersionRange( String fromVersion )
    {
        this.fromVersion = new Version( fromVersion );

        if ( fromVersion.length() >= 6 )
        {
            modifier = fromVersion.charAt( 5 );

            if ( modifier != '+' )
            {
                throw new ModelloRuntimeException( "Invalid modifier. Must be '+', was '" + modifier + "'." );
            }
        }
    }

    public Version getFromVersion()
    {
        return fromVersion;
    }

    public boolean isToInfinite()
    {
        return modifier == '+';
    }
}
