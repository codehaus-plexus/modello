/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio, Inc.  For written permission,
 *    please contact info@codehaus.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio, Inc. Exolab is a registered
 *    trademark of Intalio, Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.codehaus.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO, INC. AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id$
 */


package org.codehaus.modello.plugin.java.javasource;

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

/**
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision$ $Date$
 **/
public class JType
{

    public static final JType BOOLEAN = new JType( "boolean" );
    public static final JType BYTE = new JType( "byte" );
    public static final JType CHAR = new JType( "char" );
    public static final JType DOUBLE = new JType( "double" );
    public static final JType FLOAT = new JType( "float" );
    public static final JType INT = new JType( "int" );
    public static final JType LONG = new JType( "long" );
    public static final JType SHORT = new JType( "short" );

    private String name = null;

    private boolean _isArray = false;

    /**
     * used for array types
     **/
    private JType _componentType = null;

    /**
     * Creates a new JType with the given name
     * @param name the name of the type
     **/
    public JType( String name )
    {
        super();
        this.name = name;
    } //-- JType

    /**
     * Creates a JType Object representing an array of the current
     * JType.
     * @return the new JType which is represents an array.
     * @deprecated removed in javasource 1.3rc1, replaced by JArrayType
     **/
    public final JType createArray()
    {
        JType jType = new JType( getName() );
        jType._isArray = true;
        jType._componentType = this;
        return jType;
    } //-- createArray

    /**
     * If this JType is an array this method will returns the component type
     * of the array, otherwise null will be returned.
     * @return the component JType if this JType is an array, otherwise null.
     **/
    public JType getComponentType()
    {
        return _componentType;
    } //-- getComponentType

    public String getLocalName()
    {

        //-- use getName method in case it's been overloaded
        String name = getName();

        if ( name == null ) return null;
        int idx = name.lastIndexOf( '.' );
        if ( idx >= 0 )
        {
            name = name.substring( idx + 1 );
        }
        return name;
    } //-- getLocalName

    public String getName()
    {
        return this.name;
    } //-- getName

    /**
     * Checks to see if this JType represents an array.
     * @return true if this JType represents an array, otherwise false
     **/
    public final boolean isArray()
    {
        return _isArray;
    }

    /**
     * Checks to see if this JType represents a primitive
     * @return true if this JType represents a primitive, otherwise false
     **/
    public boolean isPrimitive()
    {
        return ( ( this == BOOLEAN ) ||
            ( this == BYTE ) ||
            ( this == CHAR ) ||
            ( this == DOUBLE ) ||
            ( this == FLOAT ) ||
            ( this == INT ) ||
            ( this == LONG ) ||
            ( this == SHORT ) );
    } //-- isPrimitive

    /**
     * Returns the String representation of this JType, which is
     * simply the name of this type.
     * @return the String representation of this JType
     **/
    public String toString()
    {

        if ( _isArray )
            return _componentType.toString() + "[]";
        else
            return this.name;

    } //-- toString

    //---------------------/
    //- Protected methods -/
    //---------------------/

    /**
     * Allows subtypes, such as JClass to alter the package to which
     * this JType belongs
     * @param newPackage the new package to which this JType belongs
     * <BR>
     * <B>Note:</B> The package name cannot be changed on a primitive type.
     **/
    protected void changePackage( String newPackage )
    {

        if ( this.name == null ) return;
        if ( this.isPrimitive() ) return;

        String localName = null;
        int idx = name.lastIndexOf( '.' );
        if ( idx >= 0 )
            localName = this.name.substring( idx + 1 );
        else
            localName = this.name;

        if ( ( newPackage == null ) || ( newPackage.length() == 0 ) )
            this.name = localName;
        else
            this.name = newPackage + "." + localName;

    } //-- changePackage

} //-- JType
