package org.codehaus.modello.maven;

/*
 * Copyright (c) 2013, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.util.Properties;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.modello.ModelloParameterConstants;

/**
 * Creates a JSON Schema from the model.
 *
 * @author <a href="mailto:simonetripodi@apache.org">Simone Tripodi</a>
 * @since 1.8
 */
@Mojo( name = "jsonschema", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public final class ModelloJsonSchemaGeneratorMojo
    extends AbstractModelloGeneratorMojo
{

    /**
     * The output directory of the generated JSON Schema. Hint: if you want to publish the schema automatically with
     * the site, configure this parameter to <code>${basedir}/target/generated-site/resources/jsonschema</code>.
     */
    @Parameter( defaultValue = "${project.build.directory}/generated-site/jsonschema", required = true )
    private File outputDirectory;

    @Parameter
    private String jsonSchemaFileName;

    protected void customizeParameters( Properties parameters )
    {
        super.customizeParameters( parameters );

        if ( jsonSchemaFileName != null )
        {
            parameters.put( ModelloParameterConstants.OUTPUT_JSONSCHEMA_FILE_NAME, jsonSchemaFileName );
        }
    }

    @Override
    protected String getGeneratorType()
    {
        return "jsonschema";
    }

    @Override
    public File getOutputDirectory()
    {
        return outputDirectory;
    }

}
