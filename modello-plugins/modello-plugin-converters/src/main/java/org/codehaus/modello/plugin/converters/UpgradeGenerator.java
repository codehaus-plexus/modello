package org.codehaus.modello.plugin.converters;

import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

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

public class UpgradeGenerator
    extends AbstractConversionGenerator
{
    
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        String[] versions = parameters.getProperty( ModelloParameterConstants.ALL_VERSIONS ).split( "," );

        Version nextVersion = null, prevVersion = null;
        for ( int i = 0; i < versions.length; i++ )
        {
            Version v = new Version( versions[i] );

            if ( v.equals( getGeneratedVersion() ) && ! isPackageWithVersion() )
            {
                // do not generate upgrade code for packaged latest version
                continue;
            }

            if ( v.greaterThan( getGeneratedVersion() ) && ( nextVersion == null || nextVersion.greaterThan( v ) ) )
            {
                nextVersion = v;
            }

            if ( v.lesserThan( getGeneratedVersion() ) && ( prevVersion == null || prevVersion.lesserThan( v ) ) )
            {
                prevVersion = v;
            }
        }

        if ( prevVersion == null && nextVersion == null )
        {
            getLogger().warn( "Need to have at least two versions to generate a converter" );
            
            return;
        }

        try
        {
            if ( nextVersion != null )
            {
                generateConverters( getGeneratedVersion(), nextVersion, true );
            }
            else if ( ! isPackageWithVersion() ) // prevVersion != null
            {
                // if nextVersion remains null, there is none greater so we are converting back to the unpackaged version
                generateConverters( prevVersion, getGeneratedVersion(), false );
            }
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating model upgrader.", ex );
        }
    }

    private void generateConverters( Version fromVersion, Version toVersion, boolean toPackagedVersion )
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName = objectModel.getDefaultPackageName( true, toVersion ) + ".upgrade";

        String jDoc = "Converts between version " + fromVersion + " and version " + toVersion + " of the model.";

        JInterface conversionInterface = new JInterface( "VersionUpgrade" );
        conversionInterface.getJDocComment().setComment( jDoc );
        conversionInterface.setPackageName( packageName );

        JClass basicConverterClass = new JClass( "BasicVersionUpgrade" );
        basicConverterClass.getJDocComment().setComment( jDoc );
        basicConverterClass.setPackageName( packageName );
        basicConverterClass.addInterface( conversionInterface );

        VersionDefinition versionDefinition = objectModel.getVersionDefinition();

        for ( Iterator i = objectModel.getClasses( fromVersion ).iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            JavaClassMetadata javaClassMetadata = (JavaClassMetadata) modelClass.getMetadata( JavaClassMetadata.ID );

            if ( !javaClassMetadata.isEnabled() )
            {
                // Skip generation of those classes that are not enabled for the java plugin.
                continue;
            }

            // check if it's present in the next version
            if ( !toVersion.inside( modelClass.getVersionRange() ) )
            {
                // Don't convert - it's not there in the next one
                continue;
            }

            String methodName = "upgrade" + modelClass.getName();
            String parameterName = uncapitalise( modelClass.getName() );
            String sourceClass = getSourceClassName( modelClass, fromVersion );
            String targetClass =
                modelClass.getPackageName( toPackagedVersion, toVersion ) + "." + modelClass.getName();

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

                sc.add( "value = (" + targetClass + ") upgrade" + modelClass.getSuperClass() + "( " + parameterName +
                    ", value );" );

                sc.add( "" );
            }

            for ( Iterator j = modelClass.getFields( fromVersion ).iterator(); j.hasNext(); )
            {
                ModelField modelField = (ModelField) j.next();

                String name = capitalise( modelField.getName() );

                if ( versionDefinition != null && "field".equals( versionDefinition.getType() ) )
                {
                    if ( versionDefinition.getValue().equals( modelField.getName() ) ||
                        versionDefinition.getValue().equals( modelField.getAlias() ) )
                    {
                        sc.add( "value.set" + name + "( \"" + toVersion + "\" );" );
                        continue;
                    }
                }

                // check if it's present in the next version
                if ( !toVersion.inside( modelField.getVersionRange() ) )
                {
                    continue;
                }

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
                                String className = getSourceClassName( assoc.getToClass(), fromVersion );
                                sc.add( className + " v = (" + className + ") i.next();" );
                            }
                            else
                            {
                                sc.add( assoc.getTo() + " v = (" + assoc.getTo() + ") i.next();" );
                            }

                            if ( isClassInModel( assoc.getTo(), objectModel ) )
                            {
                                sc.add( "list.add( upgrade" + assoc.getTo() + "( v ) );" );
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
                                String className = getSourceClassName( assoc.getToClass(), fromVersion );
                                sc.add( className + " v = (" + className + ") entry.getValue();" );
                            }
                            else
                            {
                                sc.add( assoc.getTo() + " v = (" + assoc.getTo() + ") entry.getValue();" );
                            }

                            if ( isClassInModel( assoc.getTo(), objectModel ) )
                            {
                                sc.add( "map.put( entry.getKey(), upgrade" + assoc.getTo() + "( v ) );" );
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
                        sc.add( "value.set" + name + "( upgrade" + assoc.getTo() + "( " + parameterName + ".get" +
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

        JSourceWriter interfaceWriter = null;
        JSourceWriter classWriter = null;

        try
        {
            classWriter = newJSourceWriter( packageName, basicConverterClass.getName( true ) );
            interfaceWriter = newJSourceWriter( packageName, conversionInterface.getName( true ) );

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

}
