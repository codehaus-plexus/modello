package org.codehaus.modello;

/*
 * LICENSE
 */

import com.thoughtworks.xstream.XStream;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface ModelloPlugin
{
    void setLogger( Logger logger );

    String getId();

    /**
     * Returns the top level class for the metadata.
     * 
     * @param xstream
     * @return Returns the top level class for the metadata.
     * @throws ModelloRuntimeException
     */
    Class initializeXStream( XStream xstream )
        throws ModelloRuntimeException;

    void generate( Model model )
        throws ModelloRuntimeException;
}
