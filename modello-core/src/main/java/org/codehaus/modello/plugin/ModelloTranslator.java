package org.codehaus.modello.plugin;

/*
 * LICENSE
 */

import java.io.Reader;
import java.util.Properties;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelloException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface ModelloTranslator
{
    Model translate( Reader reader, Properties parameters )
        throws ModelloException;
}
