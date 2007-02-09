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
import org.codehaus.modello.test.model.EmptyReference;
import org.codehaus.modello.test.model.io.registry.ModelRegistryWriter;
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
public class RegistryWriterVerifier
    extends Verifier
{
    private static Reference createReference( String name )
    {
        Reference reference = new Reference();
        reference.setName( name );
        return reference;
    }

    public void verify()
        throws Exception
    {
        Registry registry = new CommonsConfigurationRegistry();
        ( (CommonsConfigurationRegistry) registry ).enableLogging( new ConsoleLogger( Logger.LEVEL_DISABLED, "" ) );
        ( (Initializable) registry ).initialize();

        Model model = new Model();
        model.setName( "name" );
        model.setNumeric( 9 );
        model.setReference( createReference( "ref-name" ) );
        model.setEmptyReference( new EmptyReference() );
        model.setListReferences( Arrays.asList( new Reference[] {
            createReference( "list-name1" ),
            createReference( "list-name2" ),
            createReference( "list-name3" )
        }));
        model.setSetReferences( new HashSet( Arrays.asList( new Reference[] {
            createReference( "set-name1" ),
            createReference( "set-name2" ),
        })));
        model.setStringReferences( Arrays.asList( new String[] { "S1", "S2", "S3", "S4", "S5" } ) );

        Map map = new HashMap();
        map.put( "property", "value1" );
        map.put( "property2", "value2" );
        map.put( "something.else", "value3" );
        model.setMap( map );

        Properties properties = new Properties();
        properties.setProperty( "property", "value1" );
        properties.setProperty( "property2", "value2" );
        properties.setProperty( "something.else", "value3" );
        model.setProperties( properties );

        ModelRegistryWriter modelWriter = new ModelRegistryWriter();

        modelWriter.write( model, registry );

        Assert.assertEquals( "name", registry.getString( "name" ) );
        Assert.assertEquals( 9, registry.getInt( "numeric" ) );
        Assert.assertEquals( "ref-name", registry.getString( "reference.name" ) );
        Assert.assertNull( registry.getString( "missingReference" ) );
        Assert.assertNull( registry.getString( "missingReference.name" ) );
        Assert.assertNull( registry.getString( "emptyReference" ) );
        Assert.assertNull( registry.getString( "emptyReference.name" ) );
        Assert.assertEquals( "list-name1", registry.getString( "listReferences.listReference(0).name" ) );
        Assert.assertEquals( "list-name2", registry.getString( "listReferences.listReference(1).name" ) );
        Assert.assertEquals( "list-name3", registry.getString( "listReferences.listReference(2).name" ) );
        List names = new ArrayList( 2 );
        names.add( registry.getString( "setReferences.setReference(0).name" ) );
        names.add( registry.getString( "setReferences.setReference(1).name" ) );
        Collections.sort( names );
        Assert.assertEquals( Arrays.asList( new String[] { "set-name1", "set-name2" } ), names );
        Assert.assertEquals( Arrays.asList( new String[] { "S1", "S2", "S3", "S4", "S5" } ),
                             registry.getList( "stringReferences.stringReference" ) );

        map = registry.getProperties( "map" );
        Assert.assertEquals( 3, map.size() );
        Assert.assertEquals( "value1", map.get( "property" ) );
        Assert.assertEquals( "value2", map.get( "property2" ) );
        Assert.assertEquals( "value3", map.get( "something.else" ) );

        properties = registry.getProperties( "properties" );
        Assert.assertEquals( 3, properties.size() );
        Assert.assertEquals( "value1", properties.getProperty( "property" ) );
        Assert.assertEquals( "value2", properties.getProperty( "property2" ) );
        Assert.assertEquals( "value3", properties.getProperty( "something.else" ) );

        // test defaults
        Assert.assertNull( registry.getString( "defString" ) );

        try
        {
            registry.getInt( "defNumeric" );
            Assert.fail();
        }
        catch ( NoSuchElementException e )
        {
            // expected
        }

        try
        {
            registry.getBoolean( "defBoolean" );
            Assert.fail();
        }
        catch ( NoSuchElementException e )
        {
            // expected
        }
    }
}
