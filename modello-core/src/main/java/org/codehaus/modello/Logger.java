package org.codehaus.modello;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface Logger
{
    void debug( String message );

    void debug( String message, Throwable throwable );

    void info( String message );

    void info( String message, Throwable throwable );

    void warn( String message );

    void warn( String message, Throwable throwable );

    void fatal( String message );

    void fatal( String message, Throwable throwable );
}
