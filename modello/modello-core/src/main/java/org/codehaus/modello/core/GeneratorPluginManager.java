package org.codehaus.modello.core;

/*
 * LICENSE
 */

import java.util.Iterator;

import org.codehaus.modello.plugin.ModelloGenerator;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface GeneratorPluginManager
{
    String ROLE = GeneratorPluginManager.class.getName();

    Iterator getPlugins();

    ModelloGenerator getGeneratorPlugin( String generatorId );

    boolean hasGeneratorPlugin( String generatorId );
}
