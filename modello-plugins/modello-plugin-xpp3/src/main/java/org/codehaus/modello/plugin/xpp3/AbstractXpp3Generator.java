package org.codehaus.modello.plugin.xpp3;

/*
 * Copyright (c) 2004, Jason van Zyl
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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.plugins.xml.AbstractXmlJavaGenerator;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public abstract class AbstractXpp3Generator
    extends AbstractXmlJavaGenerator
{
    protected boolean requiresDomSupport;

    protected ModelClass locationTracker;

    protected ModelClass sourceTracker;

    protected boolean isLocationTracking()
    {
        return false;
    }

    @Override
    protected void initialize( Model model, Properties parameters )
        throws ModelloException
    {
        super.initialize( model, parameters );

        requiresDomSupport = false;
        locationTracker = sourceTracker = null;

        if ( isLocationTracking() )
        {
            locationTracker = model.getLocationTracker( getGeneratedVersion() );
            if ( locationTracker == null )
            {
                throw new ModelloException( "No model class has been marked as location tracker"
                    + " via the attribute locationTracker=\"locations\", cannot generate extended reader." );
            }

            sourceTracker = model.getSourceTracker( getGeneratedVersion() );
        }
    }
}
