package org.codehaus.modello.plugin.jpox;

/*
 * Copyright (c) 2006, Codehaus.org
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

import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.compiler.CompilerException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import junit.framework.AssertionFailedError;

public abstract class AbstractJpoxGeneratorTestCase
    extends AbstractModelloGeneratorTest
{
    protected ModelloCore modello;

    protected AbstractJpoxGeneratorTestCase( String name )
    {
        super( name );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        modello = (ModelloCore) container.lookup( ModelloCore.ROLE );
    }

    protected void verifyModel( Model model, String className )
        throws IOException, ModelloException, CompilerException, CommandLineException
    {
        verifyModel( model, className, null );
    }

    protected void verifyModel( Model model, String className, String[] versions )
        throws IOException, ModelloException, CompilerException, CommandLineException
    {
        File generatedSources = new File( getTestPath( "target/" + getName() + "/sources" ) );

        File classes = new File( getTestPath( "target/" + getName() + "/classes" ) );

        FileUtils.deleteDirectory( generatedSources );

        FileUtils.deleteDirectory( classes );

        generatedSources.mkdirs();

        classes.mkdirs();

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, generatedSources.getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "1.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );

        modello.generate( model, "java", parameters );

        modello.generate( model, "jpox-store", parameters );

        modello.generate( model, "jpox-metadata-class", parameters );

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, classes.getAbsolutePath() );
        modello.generate( model, "jpox-jdo-mapping", parameters );

        if ( versions != null && versions.length > 0 )
        {
            parameters.setProperty( ModelloParameterConstants.ALL_VERSIONS, StringUtils.join( versions, "," ) );

            for ( int i = 0; i < versions.length; i++ )
            {
                parameters.setProperty( ModelloParameterConstants.VERSION, versions[i] );

                parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY,
                                        generatedSources.getAbsolutePath() );

                parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( true ) );

                modello.generate( model, "java", parameters );

                modello.generate( model, "jpox-store", parameters );

                parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, classes.getAbsolutePath() );
                modello.generate( model, "jpox-jdo-mapping", parameters );
            }
        }

        addDependency( "org.codehaus.modello", "modello-core", getModelloVersion() );

        addDependency( "jpox", "jpox", "1.1.1" );
        addDependency( "javax.jdo", "jdo2-api", "2.0" );
        addDependency( "org.apache.derby", "derby", "10.1.3.1" );
        addDependency( "log4j", "log4j", "1.2.8" );

        compile( generatedSources, classes );

        enhance( classes );
        
        verify( className, getName() );
    }

    private void enhance( File classes )
        throws CommandLineException, ModelloException, IOException
    {
        Properties loggingProperties = new Properties();
        loggingProperties.setProperty( "log4j.appender.root", "org.apache.log4j.ConsoleAppender" );
        loggingProperties.setProperty( "log4j.appender.root.layout", "org.apache.log4j.PatternLayout" );
        loggingProperties.setProperty( "log4j.appender.root.layout.ConversionPattern", "%-5p [%c] - %m%n" );
        loggingProperties.setProperty( "log4j.category.JPOX", "INFO, root" );
        File logFile = new File( classes, "log4j.properties" );
        loggingProperties.store( new FileOutputStream( logFile ), "logging" );

        Commandline cl = new Commandline();

        cl.setExecutable( "java" );

        StringBuffer cpBuffer = new StringBuffer();

        cpBuffer.append( classes.getAbsolutePath() );

        for ( Iterator it = getClassPathElements().iterator(); it.hasNext(); )
        {
            cpBuffer.append( File.pathSeparator );

            cpBuffer.append( it.next() );
        }

        File enhancerJar = getDepedencyFile( "jpox", "jpox-enhancer", "1.1.1" );
        cpBuffer.append( File.pathSeparator + enhancerJar.getAbsolutePath() );
        File bcelJar = getDepedencyFile( "org.apache.bcel", "bcel", "5.2" );
        cpBuffer.append( File.pathSeparator + bcelJar.getAbsolutePath() );

        cl.createArgument().setValue( "-cp" );

        cl.createArgument().setValue( cpBuffer.toString() );

        cl.createArgument().setValue( "-Dlog4j.configuration=" + logFile.toURL() );

        cl.createArgument().setValue( "org.jpox.enhancer.JPOXEnhancer" );

        cl.createArgument().setValue( "-v" );

        for ( Iterator i = FileUtils.getFiles( classes, "**/*.jdo", null ).iterator(); i.hasNext(); )
        {
            cl.createArgument().setFile( (File) i.next() );
        }

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        System.out.println( cl );
        int exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );

        String stream = stderr.getOutput();

        if ( stream.trim().length() > 0 )
        {
            System.err.println( stderr.getOutput() );
        }

        stream = stdout.getOutput();

        if ( stream.trim().length() > 0 )
        {
            System.out.println( stdout.getOutput() );
        }

        if ( exitCode != 0 )
        {
            throw new ModelloException( "The JPox enhancer tool exited with a non-null exit code." );
        }
    }
    
    protected void assertAttributeEquals( Document doc, String xpathToNode, String attributeKey, String expectedValue )
    {
        if ( expectedValue == null )
        {
            throw new AssertionFailedError( "Unable to assert an attribute using a null expected value." );
        }
    
        Attribute attribute = findAttribute( doc, xpathToNode, attributeKey );
    
        if ( attribute == null )
        {
            throw new AssertionFailedError( "Element at '" + xpathToNode + "' is missing the '" + attributeKey
                            + "' attribute." );
        }
    
        assertEquals( "Attribute value for '" + xpathToNode + "'", expectedValue, attribute.getValue() );
    }

    protected void assertElementExists( Document doc, String xpathToNode )
    {
        findElement( doc, xpathToNode );
    }

    protected void assertElementNotExists( Document doc, String xpathToNode )
    {
        if ( StringUtils.isEmpty( xpathToNode ) )
        {
            throw new AssertionFailedError( "Unable to assert an attribute using an empty xpath." );
        }
    
        if ( doc == null )
        {
            throw new AssertionFailedError( "Unable to assert an attribute using a null document." );
        }
    
        XPath xpath = doc.createXPath( xpathToNode );
    
        Node node = xpath.selectSingleNode( doc );
    
        if ( node != null )
        {
            throw new AssertionFailedError( "Element at '" + xpathToNode + "' should not exist." );
        }
    
        // In case node returns something other than an element.
    }

    private Element findElement( Document doc, String xpathToNode )
    {
        if ( StringUtils.isEmpty( xpathToNode ) )
        {
            throw new AssertionFailedError( "Unable to assert an attribute using an empty xpath." );
        }
    
        if ( doc == null )
        {
            throw new AssertionFailedError( "Unable to assert an attribute using a null document." );
        }
    
        XPath xpath = doc.createXPath( xpathToNode );
    
        Node node = xpath.selectSingleNode( doc );
    
        if ( node == null )
        {
            throw new AssertionFailedError( "Expected Node at '" + xpathToNode + "', but was not found." );
        }
    
        if ( node.getNodeType() != Node.ELEMENT_NODE )
        {
            throw new AssertionFailedError( "Node at '" + xpathToNode + "' is not an xml element." );
        }
    
        return (Element) node;
    }

    private Attribute findAttribute( Document doc, String xpathToNode, String attributeKey ) throws AssertionFailedError
    {
        if ( StringUtils.isEmpty( attributeKey ) )
        {
            throw new AssertionFailedError( "Unable to assert an attribute using an empty attribute key." );
        }
    
        Element elem = findElement( doc, xpathToNode );
    
        Attribute attribute = elem.attribute( attributeKey );
        return attribute;
    }

    protected void assertAttributeMissing( Document doc, String xpathToNode, String attributeKey )
    {
        Attribute attribute = findAttribute( doc, xpathToNode, attributeKey );
    
        if ( attribute != null )
        {
            throw new AssertionFailedError( "Node at '" + xpathToNode + "' should not have the attribute named '"
                            + attributeKey + "'." );
        }
    }

    protected void assertNoTextNodes( Document doc, String xpathToParentNode, boolean recursive )
    {
        if ( StringUtils.isEmpty( xpathToParentNode ) )
        {
            throw new AssertionFailedError( "Unable to assert an attribute using an empty xpath." );
        }
    
        if ( doc == null )
        {
            throw new AssertionFailedError( "Unable to assert an attribute using a null document." );
        }
    
        XPath xpath = doc.createXPath( xpathToParentNode );
    
        List nodes = xpath.selectNodes( doc );
    
        if ( ( nodes == null ) || nodes.isEmpty() )
        {
            throw new AssertionFailedError( "Expected Node(s) at '" + xpathToParentNode + "', but was not found." );
        }
    
        Iterator it = nodes.iterator();
        while ( it.hasNext() )
        {
            Node node = (Node) it.next();
    
            assertNoTextNode( "No Text should exist in '" + xpathToParentNode + "'", node, recursive );
        }
    }

    private boolean assertNoTextNode( String message, Node node, boolean recursive )
    {
        if ( node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.CDATA_SECTION_NODE )
        {
            // Double check that it isn't just whitespace.
            String text = StringUtils.trim( node.getText() );
    
            if ( StringUtils.isNotEmpty( text ) )
            {
                throw new AssertionFailedError( message + " found <" + text + ">" );
            }
        }
    
        if ( recursive )
        {
            if ( node instanceof Branch )
            {
                Iterator it = ( (Branch) node ).nodeIterator();
                while ( it.hasNext() )
                {
                    Node child = (Node) it.next();
                    assertNoTextNode( message, child, recursive );
                }
            }
        }
    
        return false;
    }
}
