package org.codehaus.modello.generator;

/*
 * LICENSE
 */

import java.util.Properties;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelloException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface GeneratorPlugin
{
    void generate( Model model, Properties parameters )
        throws ModelloException;
}
