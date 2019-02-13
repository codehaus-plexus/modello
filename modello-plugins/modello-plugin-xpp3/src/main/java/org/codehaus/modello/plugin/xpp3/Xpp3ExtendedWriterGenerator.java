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

import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.plugin.java.javasource.JClass;
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
    ModelClass locationTrackerModelClass;

    ModelClass sourceTrackerModelClass;

    @Override
    protected boolean isLocationTracking()
    {
        return true;
    }

    @Override
    protected void prepareLocationTracking( JClass jClass )
    {
        locationTrackerModelClass = getModel().getLocationTracker( getGeneratedVersion() );
        sourceTrackerModelClass = getModel().getSourceTracker( getGeneratedVersion() );

        String packageName = locationTrackerModelClass.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

        jClass.addImport( packageName + '.' + locationTrackerModelClass.getName() + "Tracker" );
        addModelImport( jClass, locationTrackerModelClass, null );

        createLocationTrackingMethod( jClass );
    }

    private void createLocationTrackingMethod( JClass jClass )
    {
        JMethod method = new JMethod( "writeLocationTracking" );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JType( locationTrackerModelClass.getName() + "Tracker" ), "locationTracker" ) );
        method.addParameter( new JParameter( new JClass( "Object" ), "key" ) );
        method.addParameter( new JParameter( new JClass( "XmlSerializer" ), "serializer" ) );

        method.addException( new JClass( "java.io.IOException" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( locationTrackerModelClass.getName() + " location = locationTracker.getLocation( key );" );
        sc.add( "if ( location != null )" );
        sc.add( "{" );
        sc.addIndented( "serializer.comment( toString( location ) );" );
        sc.add( "}" );

        jClass.addMethod( method );

        method = new JMethod( "toString", new JType( "String" ), null );
        method.getModifiers().makeProtected();

        method.addParameter( new JParameter( new JType( locationTrackerModelClass.getName() ), "location" ) );

        sc = method.getSourceCode();
        sc.add( "return ' ' + location.getSource().toString() + ':' + location.getLineNumber() + ' ';" );

        jClass.addMethod( method );
    }
}
