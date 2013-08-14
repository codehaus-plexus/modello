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
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.model.VersionDefinition;
import org.codehaus.modello.plugin.java.AbstractJavaModelloGenerator;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JInterface;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JMethodSignature;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JType;
import org.codehaus.modello.plugin.java.metadata.JavaClassMetadata;
import org.codehaus.modello.plugin.java.metadata.JavaFieldMetadata;
import org.codehaus.plexus.util.IOUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Generate a basic conversion class between two versions of a model.
 */
public class ConverterGenerator
    extends AbstractJavaModelloGenerator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        String[] versions = parameters.getProperty( ModelloParameterConstants.ALL_VERSIONS ).split( "," );

        List<Version> allVersions = new ArrayList<Version>( versions.length );
        for ( String version : versions )
        {
            allVersions.add( new Version( version ) );
        }
        Collections.sort( allVersions );

        Version nextVersion = null;
        for ( Version v : allVersions )
        {
            if ( v.greaterThan( getGeneratedVersion() ) )
            {
                nextVersion = v;
                break;
            }
        }

        try
        {
            // if nextVersion remains null, there is none greater so we are converting back to the unpackaged version

            generateConverters( nextVersion );

            if ( nextVersion == null )
            {
                generateConverterTool( allVersions );
            }
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating model converters.", ex );
        }
    }

    private void generateConverters( Version toVersion )
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        Version fromVersion = getGeneratedVersion();
        String packageName = objectModel.getDefaultPackageName( true, fromVersion ) + ".convert";

        Version effectiveToVersion = ( toVersion == null ) ? fromVersion : toVersion;
        String jDoc = "Converts from version " + fromVersion + " (with version in package name) to version "
            + effectiveToVersion + " (with" + ( toVersion != null ? "" : "out" )
            + " version in package name) of the model.";

        JInterface conversionInterface = new JInterface( packageName + ".VersionConverter" );
        initHeader( conversionInterface );
        suppressAllWarnings( objectModel, conversionInterface );
        conversionInterface.getJDocComment().setComment( jDoc );

        JClass basicConverterClass = new JClass( packageName + ".BasicVersionConverter" );
        initHeader( basicConverterClass );
        suppressAllWarnings( objectModel, basicConverterClass );
        basicConverterClass.getJDocComment().setComment( jDoc );
        basicConverterClass.addInterface( conversionInterface );

        VersionDefinition versionDefinition = objectModel.getVersionDefinition();

        for ( ModelClass modelClass : objectModel.getClasses( fromVersion ) )
        {
            JavaClassMetadata javaClassMetadata = (JavaClassMetadata) modelClass.getMetadata( JavaClassMetadata.ID );

            if ( !javaClassMetadata.isEnabled() )
            {
                // Skip generation of those classes that are not enabled for the java plugin.
                continue;
            }

            // check if it's present in the next version
            if ( toVersion != null && !toVersion.inside( modelClass.getVersionRange() ) )
            {
                // Don't convert - it's not there in the next one
                continue;
            }

            String methodName = "convert" + modelClass.getName();
            String parameterName = uncapitalise( modelClass.getName() );
            String sourceClass = getSourceClassName( modelClass, fromVersion );
            String targetClass =
                modelClass.getPackageName( toVersion != null, toVersion ) + "." + modelClass.getName();

            if ( !javaClassMetadata.isAbstract() )
            {
                // Don't generate converter for abstract classes.

                JMethodSignature methodSig = new JMethodSignature( methodName, new JType( targetClass ) );
                methodSig.addParameter( new JParameter( new JType( sourceClass ), parameterName ) );
                conversionInterface.addMethod( methodSig );

                // Method from interface, delegates to converter with the given implementation of the target class
                JMethod jMethod = new JMethod( methodName, new JType( targetClass ), null );
                jMethod.addParameter( new JParameter( new JType( sourceClass ), parameterName ) );
                basicConverterClass.addMethod( jMethod );

                JSourceCode sc = jMethod.getSourceCode();

                sc.add( "return " + methodName + "( " + parameterName + ", new " + targetClass + "() );" );
            }

            // Actual conversion method, takes implementation as a parameter to facilitate being called as a superclass
            JMethod jMethod = new JMethod( methodName, new JType( targetClass ), null );
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

                sc.add( "value = (" + targetClass + ") convert" + modelClass.getSuperClass() + "( " + parameterName
                    + ", value );" );

                sc.add( "" );
            }

            for ( ModelField modelField : modelClass.getFields( fromVersion ) )
            {
                String name = capitalise( modelField.getName() );

                if ( toVersion != null )
                {
                    if ( versionDefinition != null && versionDefinition.isFieldType() )
                    {
                        if ( versionDefinition.getValue().equals( modelField.getName() )
                            || versionDefinition.getValue().equals( modelField.getAlias() ) )
                        {
                            sc.add( "value.set" + name + "( \"" + toVersion + "\" );" );
                            continue;
                        }
                    }
                }

                // check if it's present in the next version
                if ( toVersion != null && !toVersion.inside( modelField.getVersionRange() ) )
                {
                    // check if it is present in a new definition instead
                    ModelField newField = null;
                    try
                    {
                        newField = modelClass.getField( modelField.getName(), toVersion );
                    }
                    catch ( ModelloRuntimeException e )
                    {
                        // Don't convert - it's not there in the next one
                        continue;
                    }

                    if ( !newField.getType().equals( modelField.getType() ) )
                    {
                        // Don't convert - it's a different type in the next one
                        continue;
                    }
                }

                if ( modelField instanceof ModelAssociation )
                {
                    ModelAssociation assoc = (ModelAssociation) modelField;

                    if ( assoc.isManyMultiplicity() )
                    {
                        String type = assoc.getType();
                        if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                        {
                            sc.add( "{" );

                            sc.indent();

                            sc.add( assoc.getType() + " list = " + assoc.getDefaultValue() + ";" );

                            sc.add( "for ( java.util.Iterator i = " + parameterName + ".get" + name
                                + "().iterator(); i.hasNext(); )" );

                            sc.add( "{" );

                            sc.indent();

                            if ( isClassInModel( assoc.getTo(), modelClass.getModel() ) )
                            {
                                String className = getSourceClassName( assoc.getToClass(), fromVersion );
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

                            sc.add( "for ( java.util.Iterator i = " + parameterName + ".get" + name
                                + "().entrySet().iterator(); i.hasNext(); )" );

                            sc.add( "{" );

                            sc.indent();

                            sc.add( "java.util.Map.Entry entry = (java.util.Map.Entry) i.next();" );

                            if ( isClassInModel( assoc.getTo(), modelClass.getModel() ) )
                            {
                                String className = getSourceClassName( assoc.getToClass(), fromVersion );
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
                        sc.add( "value.set" + name + "( convert" + assoc.getTo() + "( " + parameterName + ".get" + name
                            + "() ) );" );
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

        JSourceWriter interfaceWriter = null;
        JSourceWriter classWriter = null;

        try
        {
            interfaceWriter = newJSourceWriter( packageName, conversionInterface.getName( true ) );
            classWriter = newJSourceWriter( packageName, basicConverterClass.getName( true ) );

            conversionInterface.print( interfaceWriter );
            basicConverterClass.print( classWriter );
        }
        finally
        {
            IOUtil.close( classWriter );
            IOUtil.close( interfaceWriter );
        }
    }

    private void generateConverterTool( List<Version> allVersions )
        throws ModelloException, IOException
    {
        Model objectModel = getModel();
        String root = objectModel.getRoot( getGeneratedVersion() );

        ModelClass rootClass = objectModel.getClass( root, getGeneratedVersion() );


        String basePackage = objectModel.getDefaultPackageName( false, null );
        String packageName = basePackage + ".convert";

        String jDoc = "Converts between the available versions of the model.";

        JClass converterClass = new JClass( packageName + ".ConverterTool" );
        initHeader( converterClass );
        suppressAllWarnings( objectModel, converterClass );
        converterClass.getJDocComment().setComment( jDoc );

        converterClass.addImport( "java.io.File" );
        converterClass.addImport( "java.io.IOException" );

        converterClass.addImport( "javax.xml.stream.*" );

        for ( Version v : allVersions )
        {
            writeConvertMethod( converterClass, objectModel, basePackage, allVersions, v, rootClass );
        }
        writeConvertMethod( converterClass, objectModel, basePackage, allVersions, null, rootClass );

        JSourceWriter classWriter = null;
        try
        {
            classWriter = newJSourceWriter( packageName, converterClass.getName( true ) );
            converterClass.print( new JSourceWriter( classWriter ) );
        }
        finally
        {
            IOUtil.close( classWriter );
        }
    }

    private static void writeConvertMethod( JClass converterClass, Model objectModel, String basePackage,
                                            List<Version> allVersions, Version v, ModelClass rootClass )
    {
        String modelName = objectModel.getName();
        String rootClassName = rootClass.getName();

        String targetPackage = objectModel.getDefaultPackageName( v != null, v );
        String targetClass = targetPackage + "." + rootClassName;

        String methodName = "convertFromFile";
        if ( v != null )
        {
            methodName += "_" + v.toString( "v", "_" );
        }
        JMethod method = new JMethod( methodName, new JType( targetClass ), null );
        method.addParameter( new JParameter( new JType( "File" ), "f" ) );
        method.addException( new JClass( "IOException" ) );
        method.addException( new JClass( "XMLStreamException" ) );
        converterClass.addMethod( method );

        JSourceCode sc = method.getSourceCode();

        sc.add( basePackage + ".io.stax." + modelName + "StaxReaderDelegate reader = new " + basePackage + ".io.stax."
            + modelName + "StaxReaderDelegate();" );

        sc.add( "Object value = reader.read( f );" );

        String prefix = "";
        for ( Version sourceVersion : allVersions )
        {
            String sourcePackage = objectModel.getDefaultPackageName( true, sourceVersion );
            String sourceClass = sourcePackage + "." + rootClassName;
            sc.add( prefix + "if ( value instanceof " + sourceClass + " )" );
            sc.add( "{" );
            sc.indent();

            boolean foundFirst = false;
            for ( Version targetVersion : allVersions )
            {
                if ( !foundFirst )
                {
                    if ( targetVersion.equals( sourceVersion ) )
                    {
                        foundFirst = true;
                    }
                    else
                    {
                        continue;
                    }
                }

                if ( targetVersion.equals( v ) )
                {
                    break;
                }

                // TODO: need to be able to specify converter class implementation
                String p = objectModel.getDefaultPackageName( true, targetVersion );
                String c = p + "." + rootClassName;
                sc.add( "value = new " + p + ".convert.BasicVersionConverter().convert" + rootClassName
                    + "( (" + c + ") value );" );
            }

            sc.unindent();
            sc.add( "}" );

            prefix = "else ";

            if ( sourceVersion.equals( v ) )
            {
                break;
            }
        }
        sc.add( "else" );
        sc.add( "{" );
        sc.indent();

        sc.add( "throw new IllegalStateException( \"Can't find converter for class '\" + value.getClass() + \"'\" );" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "return (" + targetClass + ") value;" );
    }

    private static String getSourceClassName( ModelClass modelClass, Version generatedVersion )
    {
        return modelClass.getPackageName( true, generatedVersion ) + "." + modelClass.getName();
    }
}
