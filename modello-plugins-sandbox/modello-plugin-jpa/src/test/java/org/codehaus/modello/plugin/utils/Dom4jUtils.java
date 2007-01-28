/**
 * 
 */
package org.codehaus.modello.plugin.utils;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.codehaus.plexus.util.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;

/**
 * Collection of helper methods to locate structural elements and verify DOM.
 * 
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id$
 * @since 1.0.0
 */
public final class Dom4jUtils
{

    /**
     * Verifies if a specified {@link Attribute} exists in the specified
     * {@link Document} and its actual value matches the passed in expected
     * value.
     * 
     * @param doc target {@link Document} where the attribute is expected to
     *            occur.
     * @param xpathToNode XPATH expression to locate the attribute in the target
     *            {@link Document}.
     * @param attributeKey identifier/name of the attribute.
     * @param expectedValue expected value to match the attribute's actual
     *            value.
     */
    public static void assertAttributeEquals( Document doc, String xpathToNode, String attributeKey,
                                              String expectedValue )
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

        Assert.assertEquals( "Attribute value for '" + xpathToNode + "'", expectedValue, attribute.getValue() );
    }

    /**
     * Asserts <b>existence</b> of an element specified by an XPATH expression
     * in the target {@link Document}.
     * 
     * @param doc target {@link Document} instance where the element is to be
     *            verified.
     * @param xpathToNode XPATH expression to locate the element.
     */
    public static void assertElementExists( Document doc, String xpathToNode )
    {
        findElement( doc, xpathToNode );
    }

    /**
     * Asserts <b>non-existence</b> of an element specified by an XPATH
     * expression in the target {@link Document}.
     * 
     * @param doc target {@link Document} instance where the element is to be
     *            verified.
     * @param xpathToNode XPATH expression to locate the element.
     */
    public static void assertElementNotExists( Document doc, String xpathToNode )
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

    /**
     * Locates and returns ther {@link Element} specified by a passed in XPATH
     * expression in a target {@link Document}.
     * 
     * @param doc target {@link Document} instance where the element is to be
     *            located.
     * @param xpathToNode XPATH expression to locate the element.
     * @return {@link Element} instance that matches the XPATH expression. An
     *         {@link AssertionFailedError} is thrown if no matching Element was
     *         found.
     */
    public static Element findElement( Document doc, String xpathToNode )
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

    /**
     * Locates and returns ther {@link Attribute} specified by a passed in XPATH
     * expression in a target {@link Document}.
     * 
     * @param doc target {@link Document} instance where the attribute is to be
     *            located.
     * @param xpathToNode XPATH expression to locate the attribute.
     * @param attributeKey identifier/name of the attribute.
     * @return {@link Element} instance that matches the XPATH expression. A
     *         <code>null</code> is returned if no matching attribute could be
     *         found.
     * @throws AssertionFailedError if an invalid (empty) attribute key was
     *             specified.
     */
    public static Attribute findAttribute( Document doc, String xpathToNode, String attributeKey )
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

    /**
     * Asserts that the specified Attribute does not exists in the document.
     * 
     * @param doc target {@link Document} instance where the attribute is to be
     *            located.
     * @param xpathToNode XPATH expression to locate the attribute.
     * @param attributeKey identifier/name of the attribute.
     */
    public static void assertAttributeMissing( Document doc, String xpathToNode, String attributeKey )
    {
        Attribute attribute = findAttribute( doc, xpathToNode, attributeKey );

        if ( attribute != null )
        {
            throw new AssertionFailedError( "Node at '" + xpathToNode + "' should not have the attribute named '"
                + attributeKey + "'." );
        }
    }

    /**
     * Asserts that the specified {@link Document} does not contains any <b>text</b>
     * nodes at the location specified by an XPATH expression.
     * 
     * @param doc target {@link Document} instance where the text nodes are to
     *            be located.
     * @param xpathToParentNode XPATH expression to locate the parent node where
     *            the search for text nodes is to be started.
     * @param recursive true if the specified parent node is to be searched
     *            recursively.
     */
    public static void assertNoTextNodes( Document doc, String xpathToParentNode, boolean recursive )
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

    /**
     * Asserts that the specified {@link Node} is not a {@link Node#TEXT_NODE}
     * or {@link Node#CDATA_SECTION_NODE}.
     * 
     * @param message Assertion message to print.
     * @param node Node to interrogate for {@link Node#TEXT_NODE} or
     *            {@link Node#CDATA_SECTION_NODE} property.
     * @param recursive <code>true</code> if the node is to be recursively
     *            searched, else <code>false</code>.
     * @return <code>true</code> if the specified {@link Node} is not of type
     *         {@link Node#TEXT_NODE} or {@link Node#CDATA_SECTION_NODE}
     */
    public static boolean assertNoTextNode( String message, Node node, boolean recursive )
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
