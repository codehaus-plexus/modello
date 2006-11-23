package org.codehaus.modello.plugin.jpox;

/*
 * Copyright (c) 2005, Codehaus.org
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

import junit.framework.AssertionFailedError;
import org.codehaus.modello.AbstractModelloGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.util.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JPoxJdoMappingModelloGeneratorTest
    extends AbstractModelloGeneratorTest
{
    public JPoxJdoMappingModelloGeneratorTest()
    {
        super( "jpox-jdo-mapping" );
    }

    public void testSimpleInvocation()
        throws Exception
    {
        ModelloCore core = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = core.loadModel( new FileReader( getTestPath( "src/test/resources/mergere-tissue.mdo" ) ) );

        // ----------------------------------------------------------------------
        // Generate the code
        // ----------------------------------------------------------------------

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getGeneratedSources().getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "1.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.FALSE.toString() );

        core.generate( model, "jpox-jdo-mapping", parameters );

        // ----------------------------------------------------------------------
        // Assert
        // ----------------------------------------------------------------------

        assertGeneratedFileExists( "package.jdo" );

        SAXReader reader = new SAXReader();
        reader.setEntityResolver( new JdoEntityResolver() );
        Document jdoDocument = reader.read( new File( "target/" + getName() + "/package.jdo" ) );

        assertNotNull( jdoDocument );

        // Tree should consist of only elements with attributes. NO TEXT.
        assertNoTextNodes( jdoDocument, "//jdo", true );

        assertAttributeEquals( jdoDocument, "//class[@name='TissueModelloMetadata']/field[@name='modelVersion']/column",
                               "default-value", "1.0.0" );

        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='summary']", "persistence-modifier",
                               "none" );

        // -----------------------------------------------------------------------
        // Association Tests.

        //   mdo/association/jpox.dependent-element == false (only on association with "*" multiplicity (default type)
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='friends']/collection",
                               "dependent-element", "false" );

        //   mdo/association (default type) with "1" multiplicity.
        assertElementNotExists( jdoDocument, "//class[@name='Issue']/field[@name='assignee']/collection" );

        //   mdo/association (map) with "*" multiplicity.
        assertElementExists( jdoDocument, "//class[@name='Issue']/field[@name='configuration']/map" );

        // -----------------------------------------------------------------------
        // Fetch Group Tests
        assertAttributeMissing( jdoDocument, "//class[@name='Issue']/field[@name='reporter']", "default-fetch-group" );
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='configuration']",
                               "default-fetch-group", "true" );
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='assignee']", "default-fetch-group",
                               "false" );

        // -----------------------------------------------------------------------
        // Value Strategy Tests
        //   defaulted
        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='id']", "value-strategy",
                               "native" );
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='accountId']", "value-strategy",
                               "native" );
        //   intentionally unset
        assertAttributeMissing( jdoDocument, "//class[@name='User']/field[@name='id']", "value-strategy" );

        // -----------------------------------------------------------------------
        // Superclass Tests
        assertAttributeEquals( jdoDocument, "//class[@name='User']", "persistence-capable-superclass",
                               "org.mergere.user.AbstractUser" );

        // -----------------------------------------------------------------------
        // Primary Key Tests
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='accountId']", "primary-key", "true" );
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='summary']", "primary-key", "false" );

        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='id']", "primary-key",
                               "true" );
        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='username']", "primary-key",
                               "false" );
        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='fullName']", "primary-key",
                               "false" );
        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='email']", "primary-key",
                               "false" );
        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='locked']", "primary-key",
                               "false" );
        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='lastLoginDate']",
                               "primary-key", "false" );

        // -----------------------------------------------------------------------
        // Alternate Table and Column Names Tests.
        assertAttributeEquals( jdoDocument, "//class[@name='DifferentTable']", "table", "MyTable" );
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='accountId']", "column", "id" );
    }

    private void assertAttributeEquals( Document doc, String xpathToNode, String attributeKey, String expectedValue )
    {
        if ( expectedValue == null )
        {
            throw new AssertionFailedError( "Unable to assert an attribute using a null expected value." );
        }

        Attribute attribute = findAttribute( doc, xpathToNode, attributeKey );

        if ( attribute == null )
        {
            throw new AssertionFailedError(
                "Element at '" + xpathToNode + "' is missing the '" + attributeKey + "' attribute." );
        }

        assertEquals( "Attribute value for '" + xpathToNode + "'", expectedValue, attribute.getValue() );
    }

    private void assertElementExists( Document doc, String xpathToNode )
    {
        findElement( doc, xpathToNode );
    }

    private void assertElementNotExists( Document doc, String xpathToNode )
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

    private Attribute findAttribute( Document doc, String xpathToNode, String attributeKey )
        throws AssertionFailedError
    {
        if ( StringUtils.isEmpty( attributeKey ) )
        {
            throw new AssertionFailedError( "Unable to assert an attribute using an empty attribute key." );
        }

        Element elem = findElement( doc, xpathToNode );

        Attribute attribute = elem.attribute( attributeKey );
        return attribute;
    }

    private void assertAttributeMissing( Document doc, String xpathToNode, String attributeKey )
    {
        Attribute attribute = findAttribute( doc, xpathToNode, attributeKey );

        if ( attribute != null )
        {
            throw new AssertionFailedError(
                "Node at '" + xpathToNode + "' should not have the attribute named '" + attributeKey + "'." );
        }
    }

    private void assertNoTextNodes( Document doc, String xpathToParentNode, boolean recursive )
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
