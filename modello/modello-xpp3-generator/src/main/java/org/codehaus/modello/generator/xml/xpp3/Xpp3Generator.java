package org.codehaus.modello.generator.xml.xpp3;

/*
 * LICENSE
 */

import java.util.Properties;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.generator.AbstractGeneratorPlugin;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class Xpp3Generator
    extends AbstractGeneratorPlugin
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
//        initialize( model, parameters );

        Xpp3ReaderGenerator readerGenerator = new Xpp3ReaderGenerator();

        Xpp3WriterGenerator writerGenerator = new Xpp3WriterGenerator();

        readerGenerator.generate( model, parameters );

        writerGenerator.generate( model, parameters );
    }
}
