package org.codehaus.modello.generator.xml.xstream;

import org.codehaus.modello.Model;
import org.codehaus.modello.ModelClass;
import org.codehaus.modello.generator.AbstractGenerator;
import org.codehaus.modello.generator.java.javasource.JClass;
import org.codehaus.modello.generator.java.javasource.JConstructor;
import org.codehaus.modello.generator.java.javasource.JSourceWriter;

import java.io.File;
import java.io.FileWriter;

import java.util.Iterator;

/**
 *
 *
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 *
 * @version $Id$
 */
public class XStreamGenerator
    extends AbstractGenerator
{
    public XStreamGenerator( String model, String outputDirectory )
    {
        super( model, outputDirectory );
    }

    public void generate()
        throws Exception
    {
        Model objectModel = getModel();

        String packageName = objectModel.getPackageName();

        String directory = packageName.replace( '.', '/' );

        String xstreamName = objectModel.getName() + "XStream";

        File f = new File( new File( getOutputDirectory(), directory ), xstreamName + ".java" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter( f );

        JSourceWriter sourceWriter = new JSourceWriter( writer );

        JClass jClass = new JClass( objectModel.getName() + "XStream" );

        jClass.addImport( "com.thoughtworks.xstream.XStream" );

        jClass.setSuperClass( "com.thoughtworks.xstream.XStream" );

        JConstructor jConstructor = new JConstructor( jClass );

        for ( Iterator i = objectModel.getClasses().iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            jClass.addImport( packageName + "." + modelClass.getName() );

            jConstructor.getSourceCode().add( "alias( \"" + uncapitalise( modelClass.getName() + "\", " + modelClass.getName() + ".class );" ) );
        }

        jClass.addConstructor( jConstructor );

        jClass.setPackageName( packageName );

        jClass.print( sourceWriter );

        writer.flush();

        writer.close();
    }
}
