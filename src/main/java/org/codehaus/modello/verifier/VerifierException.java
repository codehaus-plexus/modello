package org.codehaus.modello.verifier;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class VerifierException
    extends RuntimeException
{
    public VerifierException( String msg )
    {
        super( msg );
    }

    public VerifierException( String msg, Exception ex )
    {
        super( msg, ex );
    }
}
