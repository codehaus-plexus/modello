package org.codehaus.modello;

import junit.framework.TestCase;
import org.codehaus.modello.generator.java.JavaGenerator;
import org.codehaus.modello.generator.xml.schema.XmlSchemaGenerator;
import org.codehaus.modello.generator.xml.xdoc.XdocGenerator;

import java.io.File;

/**
 *
 *
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class GeneratorTest
    extends TestCase
{
    String outputDirectory = "target/output";

    String model = "maven.mdo";

    public void testJavaGenerator()
        throws Exception
    {
        JavaGenerator generator = new JavaGenerator( model, new File( outputDirectory, "java" ).getPath() );

        generator.generate();
    }

    public void testXmlSchemaGenerator()
        throws Exception
    {
        XmlSchemaGenerator generator = new XmlSchemaGenerator( model, new File( outputDirectory, "xsd" ).getPath() );

        generator.generate();
    }

    public void testXdocGenerator()
        throws Exception
    {
        XdocGenerator generator = new XdocGenerator( model, new File( outputDirectory, "xdoc" ).getPath() );

        generator.generate();
    }

}
