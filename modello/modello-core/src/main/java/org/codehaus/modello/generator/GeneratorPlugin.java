package org.codehaus.modello.generator;

/*
 * LICENSE
 */

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelloRuntimeException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface GeneratorPlugin
{
/*
    Class initializeXStream( XStream xstream )
        throws ModelloRuntimeException;
*/

    void generate( Model model )
        throws ModelloRuntimeException;
}
