package org.codehaus.modello.maven;

/*
 * Copyright (c) 2004, Codehaus.org
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

import java.util.Properties;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.modello.ModelloParameterConstants;

/**
 * Creates an XPP3 extended writer from the model. An extended writer renders the content with comments about the
 * line/column from which the data was read if the model supports this.
 *
 * @author Hervé Boutemy
 * @since 1.10
 */
@Mojo( name = "xpp3-extended-writer", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true )
public class ModelloXpp3ExtendedWriterMojo
    extends ModelloXpp3WriterMojo
{
    /**
     * The class name suffix for the generated writer.
     */
    @Parameter( defaultValue = "Ex" )
    private String extendedClassnameSuffix;

    @Override
    protected String getGeneratorType()
    {
        return "xpp3-extended-writer";
    }

    protected void customizeParameters( Properties parameters )
    {
        super.customizeParameters( parameters );

        if ( extendedClassnameSuffix != null )
        {
            parameters.put( ModelloParameterConstants.EXTENDED_CLASSNAME_SUFFIX, extendedClassnameSuffix );
        }
    }
}
