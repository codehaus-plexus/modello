package org.codehaus.modello.plugin.xpp3;

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

import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JField;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JType;

/**
 * The generator for XPP3-based writers that support input location tracking.
 * 
 * @author Hervé Boutemy
 * @since 1.10
 */
public class Xpp3ExtendedWriterGenerator
    extends Xpp3WriterGenerator
{
    @Override
    protected boolean isLocationTracking()
    {
        return true;
    }

    @Override
    protected void prepareLocationTracking( JClass jClass )
    {
        String packageName = locationTracker.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

        jClass.addImport( packageName + '.' + locationTracker.getName() + "Tracker" );
        addModelImport( jClass, locationTracker, null );

        createLocationTrackingMethod( jClass );

        if ( requiresDomSupport && domAsXpp3 )
        {
            createXpp3DomToSerializerMethod( jClass );
        }
    }

    private void createLocationTrackingMethod( JClass jClass )
    {
        JMethod method = new JMethod( "writeLocationTracking" );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JType( locationTracker.getName() + "Tracker" ), "locationTracker" ) );
        method.addParameter( new JParameter( new JClass( "Object" ), "key" ) );
        method.addParameter( new JParameter( new JClass( "XmlSerializer" ), "serializer" ) );

        method.addException( new JClass( "java.io.IOException" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( locationTracker.getName() + " location = ( locationTracker == null ) ? null : locationTracker.getLocation( key );" );
        sc.add( "if ( location != null )" );
        sc.add( "{" );
        sc.addIndented( "serializer.comment( toString( location ) );" );
        sc.add( "}" );

        jClass.addMethod( method );

        JField field = new JField( new JType( locationTracker.getName() + ".StringFormatter" ), "stringFormatter" );
        field.getModifiers().makeProtected();
        jClass.addField( field );

        method = new JMethod( "setStringFormatter", null, null );
        method.addParameter( new JParameter( new JType( locationTracker.getName() + ".StringFormatter" ), "stringFormatter" ) );
        sc = method.getSourceCode();
        sc.add( "this.stringFormatter = stringFormatter;" );
        jClass.addMethod( method );

        method = new JMethod( "toString", new JType( "String" ), null );
        method.getModifiers().makeProtected();

        method.addParameter( new JParameter( new JType( locationTracker.getName() ), "location" ) );

        sc = method.getSourceCode();
        sc.add( "if ( stringFormatter != null )" );
        sc.add( "{" );
        sc.addIndented( "return stringFormatter.toString( location );" );
        sc.add( "}" );
        sc.add( "return ' ' + " + ( ( sourceTracker == null ) ? "" : "location.getSource().toString() + ':' + " )
            + "location.getLineNumber() + ' ';" );

        jClass.addMethod( method );
    }

    private void createXpp3DomToSerializerMethod( JClass jClass )
    {
        JMethod method = new JMethod( "writeXpp3DomToSerializer" );
        method.getModifiers().makeProtected();

        method.addParameter( new JParameter( new JClass( "Xpp3Dom" ), "dom" ) );
        method.addParameter( new JParameter( new JClass( "XmlSerializer" ), "serializer" ) );

        method.addException( new JClass( "java.io.IOException" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "serializer.startTag( NAMESPACE, dom.getName() );" );
        sc.add( "" );
        sc.add( "String[] attributeNames = dom.getAttributeNames();" );
        sc.add( "for ( String attributeName : attributeNames )" );
        sc.add( "{" );
        sc.addIndented( "serializer.attribute( NAMESPACE, attributeName, dom.getAttribute( attributeName ) );" );
        sc.add( "}" );
        sc.add( "for ( Xpp3Dom aChild : dom.getChildren() )" );
        sc.add( "{" );
        sc.addIndented( "writeXpp3DomToSerializer( aChild, serializer );" );
        sc.add( "}" );
        sc.add( "" );
        sc.add( "String value = dom.getValue();" );
        sc.add( "if ( value != null )" );
        sc.add( "{" );
        sc.addIndented( "serializer.text( value );" );
        sc.add( "}" );
        sc.add( "" );
        sc.add( "serializer.endTag( NAMESPACE, dom.getName() );" );
        sc.add( "" );
        sc.add( "if ( dom.getInputLocation() != null && dom.getChildCount() == 0 )" );
        sc.add( "{" );
        sc.addIndented( "serializer.comment( toString( (InputLocation) dom.getInputLocation() ) );" );
        sc.add( "}" );

        jClass.addMethod( method );
    }
}
