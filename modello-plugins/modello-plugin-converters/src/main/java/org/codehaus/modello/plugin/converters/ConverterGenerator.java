package org.codehaus.modello.plugin.converters;

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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.model.VersionDefinition;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.java.JavaClassMetadata;
import org.codehaus.modello.plugin.java.JavaFieldMetadata;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JInterface;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JMethodSignature;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JType;
import org.codehaus.plexus.util.IOUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Generate a basic conversion class between two versions of a model.
 */
public class ConverterGenerator
    extends AbstractModelloGenerator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        String[] versions = parameters.getProperty( ModelloParameterConstants.ALL_VERSIONS ).split( "," );

        List allVersions = new ArrayList( versions.length );
        for ( int i = 0; i < versions.length; i++ )
        {
            allVersions.add( new Version( versions[i] ) );
        }
        Collections.sort( allVersions );

        Version nextVersion = null;
        for ( Iterator i = allVersions.iterator(); i.hasNext() && nextVersion == null; )
        {
            Version v = (Version) i.next();

            if ( v.greaterThan( getGeneratedVersion() ) )
            {
                nextVersion = v;
            }
        }
        // if nextVersion remains null, there is none greater so we are converting back to the unpackaged version

        generateConverters( nextVersion );
    }

    private void generateConverters( Version nextVersion )
        throws ModelloException
    {
        Model objectModel = getModel();

        Version generatedVersion = getGeneratedVersion();
        String packageName = objectModel.getDefaultPackageName( true, generatedVersion ) + ".convert";

        String jDoc = "Converts between version " + generatedVersion + " and version " + nextVersion + " of the model.";

        JInterface conversionInterface = new JInterface( "VersionConverter" );
        conversionInterface.getJDocComment().setComment( jDoc );
        conversionInterface.setPackageName( packageName );

        JClass basicConverterClass = new JClass( "BasicVersionConverter" );
        basicConverterClass.getJDocComment().setComment( jDoc );
        basicConverterClass.setPackageName( packageName );
        basicConverterClass.addInterface( conversionInterface );

        VersionDefinition versionDefinition = objectModel.getVersionDefinition();

        for ( Iterator i = objectModel.getClasses( generatedVersion ).iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            JavaClassMetadata javaClassMetadata = (JavaClassMetadata) modelClass.getMetadata( JavaClassMetadata.ID );

            if ( !javaClassMetadata.isEnabled() )
            {
                // Skip generation of those classes that are not enabled for the java plugin.
                continue;
            }

            if ( nextVersion != null && !nextVersion.inside( modelClass.getVersionRange() ) )
            {
                // Don't convert - it's not there in the next one
                continue;
            }

            String methodName = "convert" + modelClass.getName();
            String parameterName = uncapitalise( modelClass.getName() );
            String sourceClass = getSourceClassName( modelClass, generatedVersion );
            String targetClass =
                modelClass.getPackageName( nextVersion != null, nextVersion ) + "." + modelClass.getName();

            if ( !javaClassMetadata.isAbstract() )
            {
                // Don't generate converter for abstract classes.

                JMethodSignature methodSig = new JMethodSignature( methodName, new JType( targetClass ) );
                methodSig.addParameter( new JParameter( new JType( sourceClass ), parameterName ) );
                conversionInterface.addMethod( methodSig );

                // Method from interface, delegates to converter with the given implementation of the target class
                JMethod jMethod = new JMethod( new JType( targetClass ), methodName );
                jMethod.addParameter( new JParameter( new JType( sourceClass ), parameterName ) );
                basicConverterClass.addMethod( jMethod );

                JSourceCode sc = jMethod.getSourceCode();

                sc.add( "return " + methodName + "( " + parameterName + ", new " + targetClass + "() );" );
            }

            // Actual conversion method, takes implementation as a parameter to facilitate being called as a superclass
            JMethod jMethod = new JMethod( new JType( targetClass ), methodName );
            jMethod.addParameter( new JParameter( new JType( sourceClass ), parameterName ) );
            jMethod.addParameter( new JParameter( new JType( targetClass ), "value" ) );
            basicConverterClass.addMethod( jMethod );

            JSourceCode sc = jMethod.getSourceCode();

            sc.add( "if ( " + parameterName + " == null )" );

            sc.add( "{" );
            sc.indent();

            sc.add( "return null;" );

            sc.unindent();
            sc.add( "}" );

            if ( modelClass.getSuperClass() != null )
            {
                sc.add( "// Convert super class" );

                sc.add( "value = (" + targetClass + ") convert" + modelClass.getSuperClass() + "( " + parameterName +
                    ", value );" );

                sc.add( "" );
            }

            for ( Iterator j = modelClass.getFields( generatedVersion ).iterator(); j.hasNext(); )
            {
                ModelField modelField = (ModelField) j.next();

                String name = capitalise( modelField.getName() );

                if ( nextVersion != null )
                {
                    if ( versionDefinition != null && "field".equals( versionDefinition.getType() ) )
                    {
                        if ( versionDefinition.getValue().equals( modelField.getName() ) ||
                            versionDefinition.getValue().equals( modelField.getAlias() ) )
                        {
                            sc.add( "value.set" + name + "( \"" + nextVersion + "\" );" );
                            continue;
                        }
                    }
                }

                if ( nextVersion != null && !nextVersion.inside( modelField.getVersionRange() ) )
                {
                    // Don't convert - it's not there in the next one
                    continue;
                }

                if ( modelField instanceof ModelAssociation )
                {
                    ModelAssociation assoc = (ModelAssociation) modelField;

                    if ( ModelAssociation.MANY_MULTIPLICITY.equals( assoc.getMultiplicity() ) )
                    {
                        String type = assoc.getType();
                        if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                        {
                            sc.add( "{" );

                            sc.indent();

                            sc.add( assoc.getType() + " list = " + assoc.getDefaultValue() + ";" );

                            sc.add( "for ( java.util.Iterator i = " + parameterName + ".get" + name +
                                "().iterator(); i.hasNext(); )" );

                            sc.add( "{" );

                            sc.indent();

                            if ( isClassInModel( assoc.getTo(), modelClass.getModel() ) )
                            {
                                String className = getSourceClassName( assoc.getToClass(), generatedVersion );
                                sc.add( className + " v = (" + className + ") i.next();" );
                            }
                            else
                            {
                                sc.add( assoc.getTo() + " v = (" + assoc.getTo() + ") i.next();" );
                            }

                            if ( isClassInModel( assoc.getTo(), objectModel ) )
                            {
                                sc.add( "list.add( convert" + assoc.getTo() + "( v ) );" );
                            }
                            else
                            {
                                sc.add( "list.add( v );" );
                            }

                            sc.unindent();

                            sc.add( "}" );

                            sc.add( "value.set" + name + "( list );" );

                            sc.unindent();

                            sc.add( "}" );
                        }
                        else
                        {
                            sc.add( "{" );

                            sc.indent();

                            // Map or Properties
                            sc.add( assoc.getType() + " map = " + assoc.getDefaultValue() + ";" );

                            sc.add( "for ( java.util.Iterator i = " + parameterName + ".get" + name +
                                "().entrySet().iterator(); i.hasNext(); )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( "java.util.Map.Entry entry = (java.util.Map.Entry) i.next();" );

                            if ( isClassInModel( assoc.getTo(), modelClass.getModel() ) )
                            {
                                String className = getSourceClassName( assoc.getToClass(), generatedVersion );
                                sc.add( className + " v = (" + className + ") entry.getValue();" );
                            }
                            else
                            {
                                sc.add( assoc.getTo() + " v = (" + assoc.getTo() + ") entry.getValue();" );
                            }

                            if ( isClassInModel( assoc.getTo(), objectModel ) )
                            {
                                sc.add( "map.put( entry.getKey(), convert" + assoc.getTo() + "( v ) );" );
                            }
                            else
                            {
                                sc.add( "map.put( entry.getKey(), v );" );
                            }

                            sc.unindent();

                            sc.add( "}" );

                            sc.add( "value.set" + name + "( map );" );

                            sc.unindent();

                            sc.add( "}" );
                        }
                    }
                    else
                    {
                        sc.add( "value.set" + name + "( convert" + assoc.getTo() + "( " + parameterName + ".get" +
                            name + "() ) );" );
                    }
                }
                else
                {
                    sc.add( "// Convert field " + modelField.getName() );

                    JavaFieldMetadata javaFieldMetadata =
                        (JavaFieldMetadata) modelField.getMetadata( JavaFieldMetadata.ID );
                    String value = parameterName + "." + getPrefix( javaFieldMetadata ) + name + "()";
                    sc.add( "value.set" + name + "( " + value + " );" );
                }
            }

            sc.add( "" );

            sc.add( "return value;" );
        }

        String directory = packageName.replace( '.', '/' );

        File dir = new File( getOutputDirectory(), directory );
        if ( !dir.exists() )
        {
            dir.mkdirs();
        }

        Writer interfaceWriter = getFileWriter( dir, "VersionConverter.java" );
        Writer classWriter = getFileWriter( dir, "BasicVersionConverter.java" );

        try
        {
            conversionInterface.print( new JSourceWriter( interfaceWriter ) );
            basicConverterClass.print( new JSourceWriter( classWriter ) );

            // this one already flushes/closes the interfaceWriter
        }
        finally
        {
            IOUtil.close( classWriter );
            IOUtil.close( interfaceWriter );
        }
    }

    private static String getSourceClassName( ModelClass modelClass, Version generatedVersion )
    {
        return modelClass.getPackageName( true, generatedVersion ) + "." + modelClass.getName();
    }

    private static FileWriter getFileWriter( File dir, String name )
        throws ModelloException
    {
        File f = new File( dir, name );

        FileWriter writer;
        try
        {
            writer = new FileWriter( f );
        }
        catch ( IOException e )
        {
            throw new ModelloException( "Unable to generate: " + f + "; reason: " + e.getMessage(), e );
        }
        return writer;
    }
}
