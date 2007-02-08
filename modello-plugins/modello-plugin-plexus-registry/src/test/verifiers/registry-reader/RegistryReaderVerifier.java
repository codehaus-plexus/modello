package org.codehaus.modello.plugin.registry;

/*
 * Copyright (c) 2007, Codehaus.org
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

import org.codehaus.modello.test.model.Model;
import org.codehaus.modello.test.model.Reference;
import org.codehaus.modello.test.model.io.registry.ModelRegistryReader;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.registry.CommonsConfigurationRegistry;
import org.codehaus.plexus.registry.Registry;

import junit.framework.Assert;

import java.io.File;
import java.util.*;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id: Xpp3Verifier.java 774 2007-01-10 06:29:06Z brett $
 */
public class RegistryReaderVerifier
    extends Verifier
{
    public void verify()
        throws Exception
    {
        Registry registry = new CommonsConfigurationRegistry();
        ( (CommonsConfigurationRegistry) registry ).enableLogging( new ConsoleLogger( Logger.LEVEL_DISABLED, "" ) );
        ( (Initializable) registry ).initialize();
        registry.addConfigurationFromFile( new File( "src/test/verifiers/registry-reader/test.properties" ) );
        registry.addConfigurationFromFile( new File( "src/test/verifiers/registry-reader/test.xml" ) );

        ModelRegistryReader modelReader = new ModelRegistryReader();

        Model model = modelReader.read( registry );

        Assert.assertEquals( "Name", model.getName() );
        Assert.assertEquals( System.getProperty( "user.home" ) + "/.m2/repository", model.getRepository() );
        Assert.assertEquals( 1, model.getNumeric() );
        Assert.assertEquals( "RefName", model.getReference().getName() );
        Assert.assertNull( model.getMissingReference().getName() );
        Assert.assertNotNull( model.getEmptyReference() );
        Assert.assertEquals( "ListName1", ((Reference)model.getListReferences().get( 0 )).getName() );
        Assert.assertEquals( "ListName2", ((Reference)model.getListReferences().get( 1 )).getName() );
        Assert.assertEquals( "ListName3", ((Reference)model.getListReferences().get( 2 )).getName() );
        Set set = model.getSetReferences();
        List names = new ArrayList( set.size() );
        for ( Iterator i = set.iterator(); i.hasNext(); )
        {
            Reference ref = (Reference) i.next();
            names.add( ((Reference)ref).getName() );
        }
        Collections.sort( names );
        Assert.assertEquals( Arrays.asList( new String[] { "SetName1", "SetName2" } ), names );
        Assert.assertEquals( Arrays.asList( new String[] { "S1", "S2", "S3", "S4", "S5" } ), model.getStringReferences() );
    }
}
