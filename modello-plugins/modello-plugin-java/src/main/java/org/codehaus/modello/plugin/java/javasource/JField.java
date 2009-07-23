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
 * Copyright 1999-2000 (C) Intalio, Inc. All Rights Reserved.
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
 * A class which holds information about a field.
 * Modelled closely after the Java Reflection API.
 * This class is part of package which is used to
 * create source code in memory.
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision$ $Date$
 **/
public class JField implements JMember
{

    /**
     * The set of modifiers for this JField
     **/
    private JModifiers modifiers = null;

    private JType type = null;

    private String name = null;

    private JDocComment comment = null;

    private String initString = null;

    private JAnnotations annotations = null;

    /**
     * The Class in this JField has been declared
     **/
    private JClass declaringClass = null;

    public JField( JType type, String name )
    {

        setName( name );
        this.type = type;
        this.modifiers = new JModifiers();
        this.modifiers.makePrivate();
        comment = new JDocComment();
        comment.appendComment( "Field " + name + "." );
        annotations = new JAnnotations();
    } //-- JField


    /**
     * Returns the comment describing this member.
     * @return the comment describing this member, or
     * null if no comment has been set.
     **/
    public JDocComment getComment()
    {
        return this.comment;
    } //-- getComment


    /**
     * Returns the class in which this JField has been declared
     * @return the class in which this JField has been declared
     **/
    public JClass getDeclaringClass()
    {
        return this.declaringClass;
    } //-- getDeclaringClass

    /**
     * Returns the initialization String for this JField
     * @return the initialization String for this JField,
     * or null if no initialization String was specified.
     **/
    public String getInitString()
    {
        return initString;
    } //-- getInitString


    /**
     * Returns the modifiers for this JField
     * @return the modifiers for this JField
     **/
    public JModifiers getModifiers()
    {
        return this.modifiers;
    } //-- getModifiers

    /**
     * Returns the name of this JField
     * @return the name of this JField
     **/
    public String getName()
    {
        return this.name;
    } //-- getName

    /**
     * Returns the JType represting the type of this JField
     * @return the JClass represting the type of this JField
     **/
    public JType getType()
    {
        return this.type;
    } //-- getType

    /**
     * Sets the comment describing this member.
     * @param comment the JDocComment for this member
     **/
    public void setComment( JDocComment comment )
    {
        this.comment = comment;
    } //-- setComment

    /**
     * Sets the comment describing this member.
     * @param comment the JDocComment for this member
     **/
    public void setComment( String comment )
    {
        if ( this.comment == null )
        {
            this.comment = new JDocComment();
        }
        this.comment.setComment( comment );
    } //-- setComment

    /**
     * Sets the initialization string for this JField;
     * Allows some flexibility in declaring default values.
     * @param init the initialization string for this member.
     **/
    public void setInitString( String init )
    {
        this.initString = init;
    } //-- setInitString

    /**
     * Sets the name of this JField
     * @param name the name of this JField
     * @exception java.lang.IllegalArgumentException when the
     * name is not a valid Java member name, or if a member
     * with the given name already exists in the declaring class
     **/
    public void setName( String name ) throws
        IllegalArgumentException
    {
        if ( !JNaming.isValidJavaIdentifier( name ) )
        {
            String err = "'" + name + "' is ";
            if ( JNaming.isKeyword( name ) )
                err += "a reserved word and may not be used as "
                    + " a field name.";
            else
                err += "not a valid Java identifier.";
            throw new IllegalArgumentException( err );
        }
        this.name = name;
    } //-- setName

    public void setModifiers( JModifiers modifiers )
    {
        this.modifiers = modifiers;
    } //-- setModifiers

    protected void setDeclaringClass( JClass declaringClass )
    {
        this.declaringClass = declaringClass;
    } //-- setDeclaringClass

    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        sb.append( modifiers.toString() );
        sb.append( ' ' );
        sb.append( type );
        sb.append( ' ' );
        sb.append( name );
        return sb.toString();
    } //-- toString

    /**
     * @return the annotations
     */
    public JAnnotations getAnnotations()
    {
        return annotations;
    }

    /**
     * @param annotation the annotation to append
     */
    public void appendAnnotation( String annotation )
    {
        if ( annotations == null )
        {
            annotations = new JAnnotations();
        }
        annotations.appendAnnotation( annotation );
    }

    /**
     * @param annotations the annotations to set
     */
    public void setAnnotations( JAnnotations annotations )
    {
        this.annotations = annotations;
    }

} //-- JField

