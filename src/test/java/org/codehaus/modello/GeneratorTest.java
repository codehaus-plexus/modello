package org.codehaus.modello;

import java.io.File;

import junit.framework.TestCase;

import org.codehaus.modello.generator.java.JavaGenerator;
import org.codehaus.modello.generator.xml.schema.XmlSchemaGenerator;
import org.codehaus.modello.generator.xml.xdoc.XdocGenerator;
import org.codehaus.modello.generator.xml.xpp3.Xpp3ReaderGenerator;
import org.codehaus.modello.generator.xml.xpp3.Xpp3WriterGenerator;

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

    String model = "model.xml";

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

    public void testXpp3UnmarshallerGenerator()
        throws Exception
    {
        Xpp3ReaderGenerator generator = new Xpp3ReaderGenerator( model, new File( outputDirectory, "xpp3" ).getPath() );

        generator.generate();
    }

    public void testXpp3MarshallerGenerator()
        throws Exception
    {
        Xpp3WriterGenerator generator = new Xpp3WriterGenerator( model, new File( outputDirectory, "xpp3" ).getPath() );

        generator.generate();
    }
}
