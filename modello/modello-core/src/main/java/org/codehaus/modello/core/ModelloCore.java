package org.codehaus.modello.core;

/*
 * LICENSE
 */

import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelValidationException;
import org.codehaus.modello.ModelloException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public interface ModelloCore
{
    String ROLE = ModelloCore.class.getName();

    Model loadModel( Reader reader )
        throws ModelloException, ModelValidationException;

    void saveModel( Model model, Writer writer )
        throws ModelloException;

    Model translate( Reader reader, String inputType, Properties parameters )
        throws ModelloException, ModelValidationException;

    void generate( Model model, String outputType, Properties parameters )
        throws ModelloException;
}
