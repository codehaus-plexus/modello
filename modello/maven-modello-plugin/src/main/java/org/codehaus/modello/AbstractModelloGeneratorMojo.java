package org.codehaus.mojo.modello;

/*
 * LICENSE
 */

import java.io.FileReader;
import java.util.Properties;

import org.apache.maven.plugin.PluginExecutionRequest;
import org.apache.maven.plugin.PluginExecutionResponse;

import org.codehaus.modello.Modello;
import org.codehaus.modello.ModelloParameterConstants;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractModelloGeneratorMojo
    extends AbstractModelloMojo
{
    protected abstract String getGeneratorType();

    public void execute( PluginExecutionRequest request, PluginExecutionResponse response )
        throws Exception
    {
        Modello modello = new Modello();

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getOutputDirectory( request ) );

        parameters.setProperty( ModelloParameterConstants.VERSION, getModelVersion( request ) );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, getPackageWithVersion( request ) );

        modello.generate( new FileReader( getModelFile( request) ), 
                          getGeneratorType(), 
                          parameters );
    }
}
