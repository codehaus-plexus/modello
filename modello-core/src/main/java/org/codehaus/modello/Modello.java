package org.codehaus.modello;

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

import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelValidationException;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.DefaultPlexusContainer;

/**
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class Modello
{
    private PlexusContainer container;

    private ModelloCore core;

    public Modello()
        throws ModelloException
    {
        try
        {
            container = new DefaultPlexusContainer();

            core = (ModelloCore) container.lookup( ModelloCore.ROLE );
        }
        catch ( Exception ex )
        {
            throw new ModelloException( "Error while starting plexus.", ex );
        }
    }

    public void generate( Reader modelReader, String outputType, Properties parameters )
        throws ModelloException, ModelValidationException
    {
        Model model = core.loadModel( modelReader );

        core.generate( model, outputType, parameters );
    }

    public void translate( Reader reader, Writer writer, String outputType, Properties parameters )
        throws ModelloException, ModelValidationException
    {
        Model model = core.translate( reader, outputType, parameters );

        core.saveModel( model, writer );
    }
}
