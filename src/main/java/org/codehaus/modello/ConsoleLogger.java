package org.codehaus.modello;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ConsoleLogger
    implements Logger
{
    public void debug( String message )
    {
        output( "DEBUG", message, null );
    }

    public void debug( String message, Throwable throwable )
    {
        output( "DEBUG", message, throwable );
    }

    public void info( String message )
    {
        output( "INFO", message, null );
    }

    public void info( String message, Throwable throwable )
    {
        output( "INFO", message, throwable );
    }

    public void warn( String message )
    {
        output( "WARN", message, null );
    }

    public void warn( String message, Throwable throwable )
    {
        output( "WARN", message, throwable );
    }

    public void fatal( String message )
    {
        output( "FATAL", message, null );
    }

    public void fatal( String message, Throwable throwable )
    {
        output( "FATAL", message, throwable );
    }

    private void output( String level, String msg, Throwable throwable )
    {
        System.out.println( "[" + level + "] " + msg );

        if ( throwable != null )
        {
            throwable.printStackTrace( System.out );
        }

        System.out.flush();
    }
}
