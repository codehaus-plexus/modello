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
 * Copyright 1999-2002 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id$
 *
 * Contributors:
 * --------------
 * Keith Visco (kvisco@intalio.com) - Original Author
 * Martin Skopp (skopp@riege.de)    - Moved some core code into JStructure
 *                                    and revised to extend JStructure
 *
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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A representation of the Java Source code for a Java Class. This is
 * a useful utility when creating in memory source code.
 * This package was modelled after the Java Reflection API
 * as much as possible to reduce the learning curve.
 *
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @author <a href="mailto:skopp@riege.de">Martin Skopp</a>
 * @version $Revision$ $Date$
 */
public class JClass extends JStructure
{


    /**
     * The list of constructors for this JClass
     */
    private List<JConstructor> _constructors = null;


    /**
     * The list of member variables (fields) of this JClass
     */
    private Map<String, JField> _fields = null;


    private List<JClass> _innerClasses = null;

    /**
     * The list of methods of this JClass
     */
    private List<JMethod> _methods = null;

    /**
     * The superclass for this JClass
     */
    private String _superClass = null;


    /**
     * The source code for static initialization
     **/
    private JSourceCode _staticInitializer = new JSourceCode();

    /**
     * Creates a new JClass with the given name
     * @param name the name of the JClass to create
     * @exception java.lang.IllegalArgumentException when the given name
     * is not a valid Class name
     **/
    public JClass( String name )
        throws IllegalArgumentException
    {
        super( name );
        _constructors = new ArrayList<JConstructor>();
        _fields = new LinkedHashMap<>();
        _methods = new ArrayList<JMethod>();
        _innerClasses = new ArrayList<JClass>();
        //-- initialize default Java doc
        getJDocComment().appendComment( "Class " + getLocalName() + "." );

    } //-- JClass


    /**
     * Adds the given Constructor to this classes list of constructors.
     * The constructor must have been created with this JClass'
     * createConstructor.
     *
     * @param constructor a constructor
     * @throws java.lang.IllegalArgumentException if {@code constructor} is {@code null} or invalid.
     */
    public void addConstructor( JConstructor constructor )
        throws IllegalArgumentException
    {
        if ( constructor == null )
            throw new IllegalArgumentException( "Constructors cannot be null" );

        if ( constructor.getDeclaringClass() == this )
        {

            /** check signatures (add later) **/
            if ( !_constructors.contains( constructor ) )
            {
                _constructors.add( constructor );
            }
        }
        else
        {
            String err = "The given JConstructor was not created ";
            err += "by this JClass";
            throw new IllegalArgumentException( err );
        }
    }

    /**
     * Adds the given JField to this JClass
     *
     * @param jField, the JField to add
     * @exception java.lang.IllegalArgumentException when the given
     * JField has a name of an existing JField
     **/
    public void addField( JField jField )
        throws IllegalArgumentException
    {
        if ( jField == null )
        {
            throw new IllegalArgumentException( "Class members cannot be null" );
        }

        String name = jField.getName();

        if ( _fields.get( name ) != null )
        {
            String err = "duplicate name found: " + name;
            throw new IllegalArgumentException( err );
        }
        _fields.put( name, jField );

    } //-- addField

    /**
     * Adds the given JMember to this JClass
     *
     * @param jMember, the JMember to add
     * @exception java.lang.IllegalArgumentException when the given
     * JMember has the same name of an existing JField
     * or JMethod respectively, or if the JMember is of an
     * unrecognized class.
     **/
    public void addMember( JMember jMember )
        throws IllegalArgumentException
    {
        if ( jMember instanceof JField )
            addField( (JField) jMember );
        else if ( jMember instanceof JMethod )
            addMethod( (JMethod) jMember );
        else
        {
            String error = null;
            if ( jMember == null )
            {
                error = "the argument 'jMember' must not be null.";
            }
            else
            {
                error = "Cannot add JMember '" + jMember.getClass().getName() +
                    "' to JClass, unrecognized type.";
            }
            throw new IllegalArgumentException( error );
        }

    } //-- addMember

    /**
     * Adds the given JMethod to this JClass
     *
     * @param jMethod the JMethod to add
     **/
    public void addMethod( JMethod jMethod )
    {
        addMethod( jMethod, true );
    }

    /**
     * Adds the given JMethod to this JClass
     *
     * @param jMethod, the JMethod to add
     * @param importReturnType true if we add the importReturnType to
     * the class import lists. It could be useful to set it to false when
     * all types are fully qualified.
     * @throws java.lang.IllegalArgumentException when the given
     * JMethod has the same name of an existing JMethod.
     **/
    public void addMethod( JMethod jMethod, boolean importReturnType )
        throws IllegalArgumentException
    {
        if ( jMethod == null )
        {
            throw new IllegalArgumentException( "Class methods cannot be null" );
        }

        //-- check method name and signatures *add later*

        //-- keep method list sorted for esthetics when printing
        //-- START SORT :-)
        boolean added = false;
//        short modifierVal = 0;
        JModifiers modifiers = jMethod.getModifiers();

        if ( modifiers.isAbstract() )
        {
            getModifiers().setAbstract( true );
        }

        for ( int i = 0; i < _methods.size(); i++ )
        {
            JMethod tmp = (JMethod) _methods.get( i );
            //-- first compare modifiers
            if ( tmp.getModifiers().isPrivate() )
            {
                if ( !modifiers.isPrivate() )
                {
                    _methods.add( i, jMethod );
                    added = true;
                    break;
                }
            }
            //-- compare names
            if ( jMethod.getName().compareTo( tmp.getName() ) < 0 )
            {
                _methods.add( i, jMethod );
                added = true;
                break;
            }
        }
        //-- END SORT
        if ( !added ) _methods.add( jMethod );

    } //-- addMethod

    /**
     * Adds the given array of JMethods to this JClass
     *
     * @param jMethods, the JMethod[] to add
     * @exception java.lang.IllegalArgumentException when any of the given
     * JMethods has the same name of an existing JMethod.
     **/
    public void addMethods( JMethod[] jMethods )
        throws IllegalArgumentException
    {
        for ( JMethod jMethod : jMethods )
        {
            addMethod( jMethod );
        }
    } //-- addMethods


    /**
     * Creates a new JConstructor and adds it to this
     * JClass.
     *
     * @return the newly created constructor
     */
    public JConstructor createConstructor()
    {
        return createConstructor( null );
    } //-- createConstructor

    /**
     * Creates a new JConstructor and adds it to this
     * JClass.
     *
     * @param params the parameters
     * @return the newly created constructor
     */
    public JConstructor createConstructor( JParameter[] params )
    {
        JConstructor cons = new JConstructor( this );
        if ( params != null )
        {
            for ( JParameter param : params )
            {
                cons.addParameter( param );
            }
        }
        addConstructor( cons );
        return cons;
    } //-- createConstructor

    /**
     * Creates and returns an inner-class for this JClass
     *
     * @param localname the name of the class (no package name)
     * @return the new JClass
     */
    public JClass createInnerClass( String localname )
    {
        if ( localname == null )
        {
            String err = "argument 'localname' must not be null.";
            throw new IllegalArgumentException( err );
        }
        if ( localname.indexOf( '.' ) >= 0 )
        {
            String err = "The name of an inner-class must not contain a package name.";
            throw new IllegalArgumentException( err );
        }
        String classname = getPackageName();
        if ( classname != null )
        {
            classname = classname + "." + localname;
        }
        else
        {
            classname = localname;
        }

        JClass innerClass = new JInnerClass( classname );
        _innerClasses.add( innerClass );
        return innerClass;

    } //-- createInnerClass

    /**
     * Returns the constructor at the specified index.
     *
     * @param index the index of the constructor to return
     * @return the JConstructor at the specified index.
     */
    public JConstructor getConstructor( int index )
    {
        return (JConstructor) _constructors.get( index );
    } //-- getConstructor

    /**
     * Returns the an array of the JConstructors contained within this JClass
     *
     * @return an array of JConstructor
     */
    public JConstructor[] getConstructors()
    {

        int size = _constructors.size();
        JConstructor[] jcArray = new JConstructor[size];

        for ( int i = 0; i < _constructors.size(); i++ )
        {
            jcArray[i] = _constructors.get( i );
        }
        return jcArray;
    } //-- getConstructors


    /**
     * Returns the member with the given name, or null if no member
     * was found with the given name
     * @param name the name of the member to return
     * @return the member with the given name, or null if no member
     * was found with the given name
     **/
    public JField getField( String name )
    {
        return (JField) _fields.get( name );
    } //-- getField

    /**
     * Returns an array of all the JFields of this JClass
     * @return an array of all the JFields of this JClass
     **/
    public JField[] getFields()
    {
        return  _fields.values().toArray( new JField[0] );
    } //-- getFields

    /**
     * Returns an array of JClass (the inner classes)
     * contained within this JClass.
     *
     * @return an array of JClass contained within this JClass
     */
    public JClass[] getInnerClasses()
    {
        return _innerClasses.toArray( new JClass[0] );
    } //-- getInnerClasses;

    /**
     * Returns an array of all the JMethods of this JClass
     *
     * @return an array of all the JMethods of this JClass
     */
    public JMethod[] getMethods()
    {
        int size = _methods.size();
        JMethod[] marray = new JMethod[size];

        for ( int i = 0; i < _methods.size(); i++ )
        {
            marray[i] = _methods.get( i );
        }
        return marray;
    } //-- getMethods

    /**
     * Returns the first occurrence of the method with the
     * given name, starting from the specified index.
     *
     * @param name the name of the method to look for
     * @param startIndex the starting index to begin the search
     * @return the method if found, otherwise null.
     */
    public JMethod getMethod( String name, int startIndex )
    {
        for ( int i = startIndex; i < _methods.size(); i++ )
        {
            JMethod jMethod = _methods.get( i );
            if ( jMethod.getName().equals( name ) ) return jMethod;
        }
        return null;
    } //-- getMethod

    /**
     * Returns the JMethod located at the specified index
     *
     * @param index the index of the JMethod to return.
     * @return the JMethod
     */
    public JMethod getMethod( int index )
    {
        return _methods.get( index );
    } //-- getMethod


    /**
     * Returns the JSourceCode for the static initializer
     * of this JClass
     *
     * @return the JSourceCode for the static initializer
     * of this JClass
     */
    public JSourceCode getStaticInitializationCode()
    {
        return _staticInitializer;
    } //-- getStaticInitializationCode

    /**
     * Gets the super Class that this class extends
     * @return superClass the super Class that this Class extends
     */
    public String getSuperClass()
    {
        return _superClass;
    } //-- getSuperClass

    /**
     * Prints the source code for this JClass to the given JSourceWriter
     *
     * @param jsw the JSourceWriter to print to. [May not be null]
     */
    public void print( JSourceWriter jsw )
    {
        print( jsw, false );
    } //-- print

    /**
     * Prints the source code for this JClass to the given JSourceWriter
     *
     * @param jsw the JSourceWriter to print to. [May not be null]
     * @param classOnly whether the header, package and imports should be printed too 
     */
    public void print( JSourceWriter jsw, boolean classOnly )
    {

        if ( jsw == null )
        {
            throw new IllegalArgumentException( "argument 'jsw' should not be null." );
        }


        StringBuilder buffer = new StringBuilder();


        if ( !classOnly )
        {
            printHeader( jsw );
            printPackageDeclaration( jsw );

            //-- get imports from inner-classes
            List<String> removeImports = null;
            if ( _innerClasses.size() > 0 )
            {
                removeImports = new ArrayList<String>();
                for ( JClass iClass : _innerClasses )
                {
                    Enumeration<String> e = iClass.getImports();
                    while ( e.hasMoreElements() )
                    {
                        String classname = e.nextElement();
                        if ( !hasImport( classname ) )
                        {
                            addImport( classname );
                            removeImports.add( classname );
                        }
                    }
                }
            }
            printImportDeclarations( jsw );

            //-- remove imports from inner-classes, if necessary
            if ( removeImports != null )
            {
                for ( int i = 0; i < removeImports.size(); i++ )
                {
                    removeImport( removeImports.get( i ) );
                }
            }

        }

        //------------/
        //- Java Doc -/
        //------------/

        getJDocComment().print( jsw );

        JAnnotations annotations = getAnnotations();
        if ( annotations != null ) annotations.print( jsw );

        //-- print class information
        //-- we need to add some JavaDoc API adding comments

        buffer.setLength( 0 );

        JModifiers modifiers = getModifiers();
        if ( modifiers.isPrivate() )
        {
            buffer.append( "private " );
        }
        else if ( modifiers.isPublic() )
        {
            buffer.append( "public " );
        }

        if ( modifiers.isAbstract() )
        {
            buffer.append( "abstract " );
        }

        if ( this instanceof JInnerClass && modifiers.isStatic() )
        {
            buffer.append( "static " );
        }

        if ( modifiers.isFinal() )
        {
            buffer.append( "final " );
        }

        buffer.append( "class " );
        buffer.append( getLocalName() );
        jsw.writeln( buffer.toString() );
        buffer.setLength( 0 );
        jsw.indent();

        if ( _superClass != null )
        {
            buffer.append( "extends " );
            buffer.append( _superClass );
            jsw.writeln( buffer.toString() );
            buffer.setLength( 0 );
        }

        if ( getInterfaceCount() > 0 )
        {
            buffer.append( "implements " );

            Enumeration<String> e = getInterfaces();
            while ( e.hasMoreElements() )
            {
                buffer.append( e.nextElement() );
                if ( e.hasMoreElements() ) buffer.append( ", " );
            }

            jsw.writeln( buffer.toString() );
            buffer.setLength( 0 );
        }

        jsw.unindent();

        jsw.writeln( '{' );

        jsw.indent();

        //-- declare members

        if ( _fields.size() > 0 )
        {
            jsw.writeln();
            jsw.writeln( "  //--------------------------/" );
            jsw.writeln( " //- Class/Member Variables -/" );
            jsw.writeln( "//--------------------------/" );
            jsw.writeln();
        }

        for ( JField jField : _fields.values() )
        {
            //-- print Java comment
            JDocComment comment = jField.getComment();
            if ( comment != null ) comment.print( jsw );

            JAnnotations fieldAnnotations = jField.getAnnotations();
            if ( fieldAnnotations != null ) fieldAnnotations.print( jsw );

            // -- print member
            jsw.write( jField.getModifiers().toString() );
            jsw.write( ' ' );

            JType type = jField.getType();
            String typeName = type.toString();
            //-- for esthetics use short name in some cases
            if ( typeName.equals( toString() ) )
            {
                typeName = type.getLocalName();
            }
            jsw.write( typeName );
            jsw.write( ' ' );
            jsw.write( jField.getName() );

            String init = jField.getInitString();
            if ( init != null )
            {
                jsw.write( " = " );
                jsw.write( init );
            }

            jsw.writeln( ';' );
            jsw.writeln();
        }

        //----------------------/
        //- Static Initializer -/
        //----------------------/

        if ( !_staticInitializer.isEmpty() )
        {
            jsw.writeln();
            jsw.writeln( "static" );
            jsw.writeln( "{" );
            _staticInitializer.print( jsw );
            jsw.writeln( "};" );
            jsw.writeln();
        }

        //-- print constructors
        if ( _constructors.size() > 0 )
        {
            jsw.writeln();
            jsw.writeln( "  //----------------/" );
            jsw.writeln( " //- Constructors -/" );
            jsw.writeln( "//----------------/" );
            jsw.writeln();
        }
        for ( int i = 0; i < _constructors.size(); i++ )
        {
            JConstructor jConstructor = _constructors.get( i );
            jConstructor.print( jsw );
            jsw.writeln();
        }

        //-- print methods
        if ( _methods.size() > 0 )
        {
            jsw.writeln();
            jsw.writeln( "  //-----------/" );
            jsw.writeln( " //- Methods -/" );
            jsw.writeln( "//-----------/" );
            jsw.writeln();
        }

        for ( int i = 0; i < _methods.size(); i++ )
        {
            JMethod jMethod = _methods.get( i );
            jMethod.print( jsw );
            jsw.writeln();
        }

        //-- print inner-classes
        if ( _innerClasses.size() > 0 )
        {
            jsw.writeln();
            jsw.writeln( "  //-----------------/" );
            jsw.writeln( " //- Inner Classes -/" );
            jsw.writeln( "//-----------------/" );
            jsw.writeln();
        }
        for ( int i = 0; i < _innerClasses.size(); i++ )
        {
            JClass jClass = _innerClasses.get( i );
            jClass.print( jsw, true );
            jsw.writeln();
        }

        for ( String sourceCodeEntry : sourceCodeEntries )
        {
            jsw.writeln( sourceCodeEntry );
        }

        jsw.unindent();

        jsw.writeln( '}' );
        jsw.flush();
    } //-- printSource

    private List<String> sourceCodeEntries = new ArrayList<String>();

    public void addSourceCode( String sourceCode )
    {
        sourceCodeEntries.add( sourceCode );
    }

    /**
     * Removes the given constructor from this JClass
     *
     * @param constructor the JConstructor to remove
     * @return true if the constructor was removed, otherwise false.
     */
    public boolean removeConstructor( JConstructor constructor )
    {
        return _constructors.remove( constructor );
    } //-- removeConstructor

    /**
     * Removes the field with the given name from this JClass
     *
     * @param name the name of the field to remove
     * @return the removed field
     **/
    public JField removeField( String name )
    {
        if ( name == null ) return null;

        JField field = (JField) _fields.remove( name );

        //-- clean up imports
        //-- NOT YET IMPLEMENTED
        return field;
    } //-- removeField

    /**
     * Removes the given JField from this JClass
     *
     * @param jField, the JField to remove
     * @return {{@code true} if and only if the field was successfully removed
     **/
    public boolean removeField( JField jField )
    {
        if ( jField == null ) return false;

        Object field = _fields.get( jField.getName() );
        if ( field == jField )
        {
            _fields.remove( jField.getName() );
            return true;
        }
        //-- clean up imports
        //-- NOT YET IMPLEMENTED
        return false;

    } //-- removeField

    /**
     * Removes the given inner-class (JClass) from this JClass.
     *
     * @param jClass the JClass (inner-class) to remove.
     * @return true if the JClass was removed, otherwise false.
     */
    public boolean removeInnerClass( JClass jClass )
    {
        return _innerClasses.remove( jClass );
    } //-- removeInnerClass

    /**
     * Sets the super Class that this class extends
     * @param superClass the super Class that this Class extends
     */
    public void setSuperClass( String superClass )
    {
        _superClass = superClass;
    } //-- setSuperClass

    final class JInnerClass extends JClass
    {

        JInnerClass( String name )
        {
            super( name );
        }

        /**
         * Allows changing the package name of this JStructure
         *
         * @param packageName the package name to use
         */
        public void setPackageName( String packageName )
        {
            throw new IllegalStateException( "Cannot change the package of an inner-class" );
        } //-- setPackageName
    } //-- JInnerClass

} //-- JClass
