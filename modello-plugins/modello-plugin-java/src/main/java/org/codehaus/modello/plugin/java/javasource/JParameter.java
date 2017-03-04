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
 * Represents a parameter to a JMethod.
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision$ $Date$
 **/
public class JParameter
{

    /**
     * The type associated with this JParameter
     **/
    private JType type = null;

    /**
     * The name of this JParameter
     **/
    private String name = null;

    private JAnnotations annotations = null;

    /**
     * Creates a new JParameter with the given type, and name
     * @param type the type to associate with this JParameter
     * @param name the name of the JParameter
     **/
    public JParameter( JType type, String name )
        throws IllegalArgumentException
    {
        super();
        setType( type );
        setName( name );
    } //-- JParameter

    /**
     * Returns the name of the parameter
     * @return the name of the parameter
     **/
    public String getName()
    {
        return this.name;
    } //-- getName

    /**
     * Returns the parameter type
     * @return the parameter type
     **/
    public JType getType()
    {
        return this.type;
    } //-- getType

    /**
     * Sets the name of this parameter
     * @param name the new name of the parameter
     **/
    public void setName( String name )
    {
        this.name = name;
    } //-- setName

    /**
     * Sets the type of this parameter
     * @param type the new type of this parameter
     **/
    public void setType( JType type )
        throws IllegalArgumentException
    {
        if ( type == null )
        {
            String err = "A Parameter cannot have a null type.";
            throw new IllegalArgumentException( err );
        }
        this.type = type;
    } //-- setType

    /**
     * Returns the String representation of this JParameter. The
     * String returns will consist of the String representation
     * of the parameter type, followed by the name of the parameter
     * @return the String representation of this JParameter
     **/
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        if ( annotations != null )
        {
            sb.append( annotations.toString() );
            sb.append( ' ' );
        }
        sb.append( this.type.toString() );
        sb.append( ' ' );
        sb.append( this.name );
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

} //-- JParamater