package org.codehaus.modello.plugin.java;

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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloRuntimeException;
import org.codehaus.modello.model.CodeSegment;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.ModelInterface;
import org.codehaus.modello.plugin.java.javasource.JArrayType;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JCollectionType;
import org.codehaus.modello.plugin.java.javasource.JConstructor;
import org.codehaus.modello.plugin.java.javasource.JDocDescriptor;
import org.codehaus.modello.plugin.java.javasource.JField;
import org.codehaus.modello.plugin.java.javasource.JInterface;
import org.codehaus.modello.plugin.java.javasource.JMapType;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JMethodSignature;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JType;
import org.codehaus.modello.plugin.java.metadata.JavaAssociationMetadata;
import org.codehaus.modello.plugin.java.metadata.JavaClassMetadata;
import org.codehaus.modello.plugin.java.metadata.JavaFieldMetadata;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl</a>
 */
public class JavaModelloGenerator
    extends AbstractJavaModelloGenerator
{

    private Collection<String> immutableTypes = new HashSet<String>( Arrays.asList(
        new String[]{ "boolean", "Boolean", "byte", "Byte", "char", "Character", "short", "Short", "int", "Integer",
            "long", "Long", "float", "Float", "double", "Double", "String" } ) );

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateJava();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating Java.", ex );
        }
    }

    private void generateJava()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        ModelClass locationTrackerClass = objectModel.getLocationTracker( getGeneratedVersion() );
        ModelClass sourceTrackerClass = objectModel.getSourceTracker( getGeneratedVersion() );

        // ----------------------------------------------------------------------
        // Generate the interfaces.
        // ----------------------------------------------------------------------

        for ( ModelInterface modelInterface : objectModel.getInterfaces( getGeneratedVersion() ) )
        {
            generateInterface( modelInterface );
        }

        String locationTrackerInterface = generateLocationTracker( objectModel, locationTrackerClass );

        // ----------------------------------------------------------------------
        // Generate the classes.
        // ----------------------------------------------------------------------

        for ( ModelClass modelClass : objectModel.getClasses( getGeneratedVersion() ) )
        {
            JavaClassMetadata javaClassMetadata = (JavaClassMetadata) modelClass.getMetadata( JavaClassMetadata.ID );

            if ( !javaClassMetadata.isEnabled() )
            {
                // Skip generation of those classes that are not enabled for the java plugin.
                continue;
            }

            String packageName = modelClass.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

            JSourceWriter sourceWriter = newJSourceWriter( packageName, modelClass.getName() );

            JClass jClass = new JClass( packageName + '.' + modelClass.getName() );

            initHeader( jClass );

            suppressAllWarnings( objectModel, jClass );

            if ( StringUtils.isNotEmpty( modelClass.getDescription() ) )
            {
                jClass.getJDocComment().setComment( appendPeriod( modelClass.getDescription() ) );
            }

            addModelImports( jClass, modelClass );

            jClass.getModifiers().setAbstract( javaClassMetadata.isAbstract() );

            boolean superClassInModel = false;
            if ( modelClass.getSuperClass() != null )
            {
                jClass.setSuperClass( modelClass.getSuperClass() );
                superClassInModel = isClassInModel( modelClass.getSuperClass(), objectModel );
            }

            for ( String implementedInterface : modelClass.getInterfaces() )
            {
                jClass.addInterface( implementedInterface );
            }

            jClass.addInterface( Serializable.class.getName() );

            if ( useJava5 && !modelClass.getAnnotations().isEmpty() )
            {
                for ( String annotation : modelClass.getAnnotations() )
                {
                    jClass.appendAnnotation( annotation );
                }
            }

            JSourceCode jConstructorSource = new JSourceCode();

            for ( ModelField modelField : modelClass.getFields( getGeneratedVersion() ) )
            {
                if ( modelField instanceof ModelAssociation )
                {
                    createAssociation( jClass, (ModelAssociation) modelField, jConstructorSource );
                }
                else
                {
                    createField( jClass, modelField );
                }
            }

            // since 1.8
            // needed to understand if the instance can be created with empty ctor or not
            JConstructor jConstructor = null;

            if ( !jConstructorSource.isEmpty() )
            {
                // Ironic that we are doing lazy init huh?
                jConstructor = jClass.createConstructor();
                jConstructor.setSourceCode( jConstructorSource );
                jClass.addConstructor( jConstructor );
            }

            // ----------------------------------------------------------------------
            // equals() / hashCode() / toString()
            // ----------------------------------------------------------------------

            List<ModelField> identifierFields = modelClass.getIdentifierFields( getGeneratedVersion() );

            if ( identifierFields.size() != 0 )
            {
                JMethod equals = generateEquals( modelClass );

                jClass.addMethod( equals );

                JMethod hashCode = generateHashCode( modelClass );

                jClass.addMethod( hashCode );

                // backward compat
                if ( !javaClassMetadata.isGenerateToString() )
                {
                    JMethod toString = generateToString( modelClass, true );

                    jClass.addMethod( toString );
                }

            }

            if ( javaClassMetadata.isGenerateToString() )
            {

                JMethod toString = generateToString( modelClass, false );

                jClass.addMethod( toString );

            }

            // ----------------------------------------------------------------------
            // Model.Builder
            // since 1.8
            // ----------------------------------------------------------------------

            if ( javaClassMetadata.isGenerateBuilder() )
            {
                generateBuilder( modelClass, jClass.createInnerClass( "Builder" ), jConstructor );
            }

            // ----------------------------------------------------------------------
            // Model.newXXXInstance
            // since 1.8
            // ----------------------------------------------------------------------

            if ( javaClassMetadata.isGenerateStaticCreators() )
            {
                generateStaticCreator( modelClass, jClass, jConstructor );
            }

            boolean cloneLocations = !superClassInModel && modelClass != sourceTrackerClass;
            JMethod[] cloneMethods = generateClone( modelClass, cloneLocations ? locationTrackerClass : null );
            if ( cloneMethods.length > 0 )
            {
                jClass.addInterface( Cloneable.class.getName() );
                jClass.addMethods( cloneMethods );
            }

            if ( modelClass.getCodeSegments( getGeneratedVersion() ) != null )
            {
                for ( CodeSegment codeSegment : modelClass.getCodeSegments( getGeneratedVersion() ) )
                {
                    jClass.addSourceCode( codeSegment.getCode() );
                }
            }

            ModelClassMetadata modelClassMetadata =
                (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

            if ( modelClassMetadata != null )
            {
                if ( modelClassMetadata.isRootElement() )
                {
                    ModelField modelEncoding = new ModelField( modelClass, "modelEncoding" );
                    modelEncoding.setType( "String" );
                    modelEncoding.setDefaultValue( "UTF-8" );
                    modelEncoding.addMetadata( new JavaFieldMetadata() );
                    createField( jClass, modelEncoding );
                }
            }

            if ( modelClass == locationTrackerClass )
            {
                jClass.addInterface( locationTrackerInterface );

                generateLocationBean( jClass, modelClass, sourceTrackerClass );

                generateLocationTracking( jClass, modelClass, locationTrackerClass );
            }
            else if ( locationTrackerClass != null && modelClass != sourceTrackerClass && !superClassInModel )
            {
                jClass.addInterface( locationTrackerInterface );

                generateLocationTracking( jClass, modelClass, locationTrackerClass );
            }

            jClass.print( sourceWriter );

            sourceWriter.close();
        }
    }

    private void generateInterface( ModelInterface modelInterface )
        throws ModelloException, IOException
    {
        Model objectModel = modelInterface.getModel();

        String packageName = modelInterface.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, modelInterface.getName() );

        JInterface jInterface = new JInterface( packageName + '.' + modelInterface.getName() );

        initHeader( jInterface );

        suppressAllWarnings( objectModel, jInterface );

        if ( modelInterface.getSuperInterface() != null )
        {
            // check if we need an import: if it is a generated superInterface in another package
            try
            {
                ModelInterface superInterface =
                    objectModel.getInterface( modelInterface.getSuperInterface(), getGeneratedVersion() );
                String superPackageName =
                    superInterface.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

                if ( !packageName.equals( superPackageName ) )
                {
                    jInterface.addImport( superPackageName + '.' + superInterface.getName() );
                }
            }
            catch ( ModelloRuntimeException mre )
            {
                // no problem if the interface does not exist in the model, it can be in the jdk
            }

            jInterface.addInterface( modelInterface.getSuperInterface() );
        }

        if ( modelInterface.getCodeSegments( getGeneratedVersion() ) != null )
        {
            for ( CodeSegment codeSegment : modelInterface.getCodeSegments( getGeneratedVersion() ) )
            {
                jInterface.addSourceCode( codeSegment.getCode() );
            }
        }

        if ( useJava5 && !modelInterface.getAnnotations().isEmpty() )
        {
            for ( String annotation : modelInterface.getAnnotations() )
            {
                jInterface.appendAnnotation( annotation );
            }
        }

        jInterface.print( sourceWriter );

        sourceWriter.close();
    }

    private JMethod generateEquals( ModelClass modelClass )
    {
        JMethod equals = new JMethod( "equals", JType.BOOLEAN, null );

        equals.addParameter( new JParameter( new JClass( "Object" ), "other" ) );

        JSourceCode sc = equals.getSourceCode();

        sc.add( "if ( this == other )" );
        sc.add( "{" );
        sc.addIndented( "return true;" );
        sc.add( "}" );
        sc.add( "" );
        sc.add( "if ( !( other instanceof " + modelClass.getName() + " ) )" );
        sc.add( "{" );
        sc.addIndented( "return false;" );
        sc.add( "}" );
        sc.add( "" );
        sc.add( modelClass.getName() + " that = (" + modelClass.getName() + ") other;" );
        sc.add( "boolean result = true;" );

        sc.add( "" );

        for ( ModelField identifier : modelClass.getIdentifierFields( getGeneratedVersion() ) )
        {
            String name = identifier.getName();
            if ( "boolean".equals( identifier.getType() ) || "byte".equals( identifier.getType() ) || "char".equals(
                identifier.getType() ) || "double".equals( identifier.getType() ) || "float".equals(
                identifier.getType() ) || "int".equals( identifier.getType() ) || "short".equals( identifier.getType() )
                || "long".equals( identifier.getType() ) )
            {
                sc.add( "result = result && " + name + " == that." + name + ";" );
            }
            else
            {
                name = "get" + capitalise( name ) + "()";
                sc.add(
                    "result = result && ( " + name + " == null ? that." + name + " == null : " + name + ".equals( that."
                        + name + " ) );" );
            }
        }

        if ( modelClass.getSuperClass() != null )
        {
            sc.add( "result = result && ( super.equals( other ) );" );
        }

        sc.add( "" );

        sc.add( "return result;" );

        return equals;
    }

    private JMethod generateToString( ModelClass modelClass, boolean onlyIdentifierFields )
    {
        JMethod toString = new JMethod( "toString", new JType( String.class.getName() ), null );

        List<ModelField> fields = onlyIdentifierFields ? modelClass.getIdentifierFields( getGeneratedVersion() ) : modelClass.getFields( getGeneratedVersion() );

        JSourceCode sc = toString.getSourceCode();

        if ( fields.size() == 0 )
        {
            sc.add( "return super.toString();" );

            return toString;
        }

        if ( useJava5 )
        {
            sc.add( "StringBuilder buf = new StringBuilder( 128 );" );
        }
        else
        {
            sc.add( "StringBuffer buf = new StringBuffer( 128 );" );
        }

        sc.add( "" );

        for ( Iterator<ModelField> j = fields.iterator(); j.hasNext(); )
        {
            ModelField identifier = j.next();

            String getter = "boolean".equals( identifier.getType() ) ? "is" : "get";

            sc.add( "buf.append( \"" + identifier.getName() + " = '\" );" );
            sc.add( "buf.append( " + getter + capitalise( identifier.getName() ) + "() );" );
            sc.add( "buf.append( \"'\" );" );

            if ( j.hasNext() )
            {
                sc.add( "buf.append( \"\\n\" ); " );
            }
        }

        if ( modelClass.getSuperClass() != null )
        {
            sc.add( "buf.append( \"\\n\" );" );
            sc.add( "buf.append( super.toString() );" );
        }

        sc.add( "" );

        sc.add( "return buf.toString();" );

        return toString;
    }

    private JMethod generateHashCode( ModelClass modelClass )
    {
        JMethod hashCode = new JMethod( "hashCode", JType.INT, null );

        List<ModelField> identifierFields = modelClass.getIdentifierFields( getGeneratedVersion() );

        JSourceCode sc = hashCode.getSourceCode();

        if ( identifierFields.size() == 0 )
        {
            sc.add( "return super.hashCode();" );

            return hashCode;
        }

        sc.add( "int result = 17;" );

        sc.add( "" );

        for ( ModelField identifier : identifierFields )
        {
            sc.add( "result = 37 * result + " + createHashCodeForField( identifier ) + ";" );
        }

        if ( modelClass.getSuperClass() != null )
        {
            sc.add( "result = 37 * result + super.hashCode();" );
        }

        sc.add( "" );

        sc.add( "return result;" );

        return hashCode;
    }

    private JMethod[] generateClone( ModelClass modelClass, ModelClass locationClass )
        throws ModelloException
    {
        String cloneModeClass = getCloneMode( modelClass );

        if ( JavaClassMetadata.CLONE_NONE.equals( cloneModeClass ) )
        {
            return new JMethod[0];
        }

        JType returnType;
        if ( useJava5 )
        {
            returnType = new JClass( modelClass.getName() );
        }
        else
        {
            returnType = new JClass( "Object" );
        }

        JMethod cloneMethod = new JMethod( "clone", returnType, null );

        JSourceCode sc = cloneMethod.getSourceCode();

        sc.add( "try" );
        sc.add( "{" );
        sc.indent();

        sc.add( modelClass.getName() + " copy = (" + modelClass.getName() + ") super.clone();" );

        sc.add( "" );

        for ( ModelField modelField : modelClass.getFields( getGeneratedVersion() ) )
        {
            String thisField = "this." + modelField.getName();
            String copyField = "copy." + modelField.getName();

            if ( "DOM".equals( modelField.getType() ) )
            {
                sc.add( "if ( " + thisField + " != null )" );
                sc.add( "{" );
                if ( domAsXpp3 )
                {
                    sc.addIndented( copyField
                                        + " = new org.codehaus.plexus.util.xml.Xpp3Dom( (org.codehaus.plexus.util.xml.Xpp3Dom) "
                                        + thisField + " );" );
                }
                else
                {
                    sc.addIndented( copyField + " = ( (org.w3c.dom.Node) " + thisField + ").cloneNode( true );" );
                }
                sc.add( "}" );
                sc.add( "" );
            }
            else if ( "Date".equalsIgnoreCase( modelField.getType() ) || "java.util.Date".equals(
                modelField.getType() ) )
            {
                sc.add( "if ( " + thisField + " != null )" );
                sc.add( "{" );
                sc.addIndented( copyField + " = (java.util.Date) " + thisField + ".clone();" );
                sc.add( "}" );
                sc.add( "" );
            }
            else if ( ModelDefault.PROPERTIES.equals( modelField.getType() ) )
            {
                sc.add( "if ( " + thisField + " != null )" );
                sc.add( "{" );
                sc.addIndented( copyField + " = (" + ModelDefault.PROPERTIES + ") " + thisField + ".clone();" );
                sc.add( "}" );
                sc.add( "" );
            }
            else if ( modelField instanceof ModelAssociation )
            {
                ModelAssociation modelAssociation = (ModelAssociation) modelField;

                String cloneModeAssoc = getCloneMode( modelAssociation, cloneModeClass );

                boolean deepClone =
                    JavaAssociationMetadata.CLONE_DEEP.equals( cloneModeAssoc ) && !immutableTypes.contains(
                        modelAssociation.getTo() );

                if ( modelAssociation.isOneMultiplicity() )
                {
                    if ( deepClone )
                    {
                        sc.add( "if ( " + thisField + " != null )" );
                        sc.add( "{" );
                        sc.addIndented(
                            copyField + " = (" + modelAssociation.getTo() + ") " + thisField + ".clone();" );
                        sc.add( "}" );
                        sc.add( "" );
                    }
                }
                else
                {
                    sc.add( "if ( " + thisField + " != null )" );
                    sc.add( "{" );
                    sc.indent();

                    JavaAssociationMetadata javaAssociationMetadata = getJavaAssociationMetadata( modelAssociation );
                    JType componentType = getComponentType( modelAssociation, javaAssociationMetadata );

                    sc.add( copyField + " = " + getDefaultValue( modelAssociation, componentType ) + ";" );

                    if ( isCollection( modelField.getType() ) )
                    {
                        if ( deepClone )
                        {
                            if ( useJava5 )
                            {
                                sc.add( "for ( " + componentType.getName() + " item : " + thisField + " )" );
                            }
                            else
                            {
                                sc.add( "for ( java.util.Iterator it = " + thisField + ".iterator(); it.hasNext(); )" );
                            }
                            sc.add( "{" );
                            sc.indent();
                            if ( useJava5 )
                            {
                                sc.add( copyField + ".add( ( (" + modelAssociation.getTo() + ") item).clone() );" );
                            }
                            else
                            {
                                sc.add(
                                    copyField + ".add( ( (" + modelAssociation.getTo() + ") it.next() ).clone() );" );
                            }
                            sc.unindent();
                            sc.add( "}" );
                        }
                        else
                        {
                            sc.add( copyField + ".addAll( " + thisField + " );" );
                        }
                    }
                    else if ( isMap( modelField.getType() ) )
                    {
                        sc.add( copyField + ".clear();" );
                        sc.add( copyField + ".putAll( " + thisField + " );" );
                    }

                    sc.unindent();
                    sc.add( "}" );
                    sc.add( "" );
                }
            }
        }

        if ( locationClass != null )
        {
            String locationField =
                ( (ModelClassMetadata) locationClass.getMetadata( ModelClassMetadata.ID ) ).getLocationTracker();
            sc.add( "if ( copy." + locationField + " != null )" );
            sc.add( "{" );
            sc.indent();
            sc.add( "copy." + locationField + " = new java.util.LinkedHashMap" + "( copy." + locationField + " );" );
            sc.unindent();
            sc.add( "}" );
            sc.add( "" );
        }

        String cloneHook = getCloneHook( modelClass );

        if ( StringUtils.isNotEmpty( cloneHook ) && !"false".equalsIgnoreCase( cloneHook ) )
        {
            if ( "true".equalsIgnoreCase( cloneHook ) )
            {
                cloneHook = "cloneHook";
            }

            sc.add( cloneHook + "( copy );" );
            sc.add( "" );
        }

        sc.add( "return copy;" );

        sc.unindent();
        sc.add( "}" );
        sc.add( "catch ( " + Exception.class.getName() + " ex )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "throw (" + RuntimeException.class.getName() + ") new " + UnsupportedOperationException.class.getName()
                    + "( getClass().getName()" );
        sc.addIndented( "+ \" does not support clone()\" ).initCause( ex );" );
        sc.unindent();
        sc.add( "}" );

        return new JMethod[]{ cloneMethod };
    }

    private String getCloneMode( ModelClass modelClass )
        throws ModelloException
    {
        String cloneMode = null;

        for ( ModelClass currentClass = modelClass; ; )
        {
            JavaClassMetadata javaClassMetadata = (JavaClassMetadata) currentClass.getMetadata( JavaClassMetadata.ID );

            cloneMode = javaClassMetadata.getCloneMode();

            if ( cloneMode != null )
            {
                break;
            }

            String superClass = currentClass.getSuperClass();
            if ( StringUtils.isEmpty( superClass ) || !isClassInModel( superClass, getModel() ) )
            {
                break;
            }

            currentClass = getModel().getClass( superClass, getGeneratedVersion() );
        }

        if ( cloneMode == null )
        {
            cloneMode = JavaClassMetadata.CLONE_NONE;
        }
        else if ( !JavaClassMetadata.CLONE_MODES.contains( cloneMode ) )
        {
            throw new ModelloException(
                "The Java Modello Generator cannot use '" + cloneMode + "' as a value for <class java.clone=\"...\">, "
                    + "only the following values are acceptable " + JavaClassMetadata.CLONE_MODES );
        }

        return cloneMode;
    }

    private String getCloneMode( ModelAssociation modelAssociation, String cloneModeClass )
        throws ModelloException
    {
        JavaAssociationMetadata javaAssociationMetadata =
            (JavaAssociationMetadata) modelAssociation.getAssociationMetadata( JavaAssociationMetadata.ID );

        String cloneModeAssoc = javaAssociationMetadata.getCloneMode();
        if ( cloneModeAssoc == null )
        {
            cloneModeAssoc = cloneModeClass;
        }
        else if ( !JavaAssociationMetadata.CLONE_MODES.contains( cloneModeAssoc ) )
        {
            throw new ModelloException( "The Java Modello Generator cannot use '" + cloneModeAssoc
                                            + "' as a value for <association java.clone=\"...\">, "
                                            + "only the following values are acceptable "
                                            + JavaAssociationMetadata.CLONE_MODES );
        }

        return cloneModeAssoc;
    }

    private String getCloneHook( ModelClass modelClass )
        throws ModelloException
    {
        JavaClassMetadata javaClassMetadata = (JavaClassMetadata) modelClass.getMetadata( JavaClassMetadata.ID );

        return javaClassMetadata.getCloneHook();
    }

    private String generateLocationTracker( Model objectModel, ModelClass locationClass )
        throws ModelloException, IOException
    {
        if ( locationClass == null )
        {
            return null;
        }

        String locationField =
            ( (ModelClassMetadata) locationClass.getMetadata( ModelClassMetadata.ID ) ).getLocationTracker();

        String propertyName = capitalise( singular( locationField ) );

        String interfaceName = locationClass.getName() + "Tracker";

        String packageName = locationClass.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, interfaceName );

        JInterface jInterface = new JInterface( packageName + '.' + interfaceName );

        initHeader( jInterface );

        suppressAllWarnings( objectModel, jInterface );

        JMethodSignature jMethod = new JMethodSignature( "get" + propertyName, new JType( locationClass.getName() ) );
        jMethod.setComment( "Gets the location of the specified field in the input source." );
        addParameter( jMethod, "Object", "field", "The key of the field, must not be <code>null</code>." );
        String returnDoc = "The location of the field in the input source or <code>null</code> if unknown.";
        jMethod.getJDocComment().addDescriptor( JDocDescriptor.createReturnDesc( returnDoc ) );
        jInterface.addMethod( jMethod );

        jMethod = new JMethodSignature( "set" + propertyName, null );
        jMethod.setComment( "Sets the location of the specified field." );
        addParameter( jMethod, "Object", "field", "The key of the field, must not be <code>null</code>." );
        addParameter( jMethod, locationClass.getName(), singular( locationField ),
                      "The location of the field, may be <code>null</code>." );
        jInterface.addMethod( jMethod );

        jInterface.print( sourceWriter );

        sourceWriter.close();

        return jInterface.getName();
    }

    private void generateLocationTracking( JClass jClass, ModelClass modelClass, ModelClass locationClass )
        throws ModelloException
    {
        if ( locationClass == null )
        {
            return;
        }

        String superClass = modelClass.getSuperClass();
        if ( StringUtils.isNotEmpty( superClass ) && isClassInModel( superClass, getModel() ) )
        {
            return;
        }

        ModelClassMetadata metadata = (ModelClassMetadata) locationClass.getMetadata( ModelClassMetadata.ID );
        String locationField = metadata.getLocationTracker();

        String fieldType = "java.util.Map" + ( useJava5 ? "<Object, " + locationClass.getName() + ">" : "" );
        String fieldImpl = "java.util.LinkedHashMap" + ( useJava5 ? "<Object, " + locationClass.getName() + ">" : "" );

        // private java.util.Map<Object, Location> locations;
        JField jField = new JField( new JType( fieldType ), locationField );
        jClass.addField( jField );

        JMethod jMethod;
        JSourceCode sc;

        // public Location getLocation( Object key )
        jMethod =
            new JMethod( "get" + capitalise( singular( locationField ) ), new JType( locationClass.getName() ), null );
        jMethod.addParameter( new JParameter( new JType( "Object" ), "key" ) );
        sc = jMethod.getSourceCode();
        sc.add( "return ( " + locationField + " != null ) ? " + locationField + ".get( key ) : null;" );
        jMethod.setComment( "" );
        jClass.addMethod( jMethod );

        // public void setLocation( Object key, Location location )
        jMethod = new JMethod( "set" + capitalise( singular( locationField ) ) );
        jMethod.addParameter( new JParameter( new JType( "Object" ), "key" ) );
        jMethod.addParameter( new JParameter( new JType( locationClass.getName() ), singular( locationField ) ) );
        sc = jMethod.getSourceCode();
        sc.add( "if ( " + singular( locationField ) + " != null )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "if ( this." + locationField + " == null )" );
        sc.add( "{" );
        sc.addIndented( "this." + locationField + " = new " + fieldImpl + "();" );
        sc.add( "}" );
        sc.add( "this." + locationField + ".put( key, " + singular( locationField ) + " );" );
        sc.unindent();
        sc.add( "}" );
        jMethod.setComment( "" );
        jClass.addMethod( jMethod );
    }

    private void generateLocationBean( JClass jClass, ModelClass locationClass, ModelClass sourceClass )
        throws ModelloException
    {
        jClass.getModifiers().setFinal( true );

        String locationsField =
            ( (ModelClassMetadata) locationClass.getMetadata( ModelClassMetadata.ID ) ).getLocationTracker();

        JavaFieldMetadata readOnlyField = new JavaFieldMetadata();
        readOnlyField.setSetter( false );

        // int lineNumber;
        ModelField lineNumber = new ModelField( locationClass, "lineNumber" );
        lineNumber.setDescription( "The one-based line number. The value will be non-positive if unknown." );
        lineNumber.setType( "int" );
        lineNumber.setDefaultValue( "-1" );
        lineNumber.addMetadata( readOnlyField );
        createField( jClass, lineNumber );

        // int columnNumber;
        ModelField columnNumber = new ModelField( locationClass, "columnNumber" );
        columnNumber.setDescription( "The one-based column number. The value will be non-positive if unknown." );
        columnNumber.setType( "int" );
        columnNumber.setDefaultValue( "-1" );
        columnNumber.addMetadata( readOnlyField );
        createField( jClass, columnNumber );

        // Source source;
        ModelField source = null;
        if ( sourceClass != null )
        {
            ModelClassMetadata metadata = (ModelClassMetadata) sourceClass.getMetadata( ModelClassMetadata.ID );
            String sourceField = metadata.getSourceTracker();

            source = new ModelField( locationClass, sourceField );
            source.setType( sourceClass.getName() );
            source.addMetadata( readOnlyField );
            createField( jClass, source );
        }

        // Location( int lineNumber, int columnNumber );
        JConstructor jConstructor = jClass.createConstructor();
        JSourceCode sc = jConstructor.getSourceCode();

        jConstructor.addParameter( new JParameter( JType.INT, lineNumber.getName() ) );
        sc.add( "this." + lineNumber.getName() + " = " + lineNumber.getName() + ";" );

        jConstructor.addParameter( new JParameter( JType.INT, columnNumber.getName() ) );
        sc.add( "this." + columnNumber.getName() + " = " + columnNumber.getName() + ";" );

        // Location( int lineNumber, int columnNumber, Source source );
        if ( sourceClass != null )
        {
            jConstructor = jClass.createConstructor( jConstructor.getParameters() );
            sc.copyInto( jConstructor.getSourceCode() );
            sc = jConstructor.getSourceCode();

            jConstructor.addParameter( new JParameter( new JType( sourceClass.getName() ), source.getName() ) );
            sc.add( "this." + source.getName() + " = " + source.getName() + ";" );
        }

        JType fieldType = new JMapType( "java.util.Map", new JType(locationClass.getName()), useJava5 );
        JType fieldImpl = new JMapType("java.util.LinkedHashMap", new JType(locationClass.getName()), useJava5);

        // public Map<Object, Location> getLocations()
        JMethod jMethod = new JMethod( "get" + capitalise( locationsField ), fieldType, null );
        sc = jMethod.getSourceCode();
        sc.add( "return " + locationsField + ";" );
        jMethod.setComment( "" );
        jClass.addMethod( jMethod );

        // public void setLocations( Map<Object, Location> locations )
        jMethod = new JMethod( "set" + capitalise( locationsField ) );
        jMethod.addParameter( new JParameter( fieldType, locationsField ) );
        sc = jMethod.getSourceCode();
        sc.add( "this." + locationsField + " = " + locationsField + ";" );
        jMethod.setComment( "" );
        jClass.addMethod( jMethod );

        // public static Location merge( Location target, Location source, boolean sourceDominant )
        jMethod = new JMethod( "merge", new JType( locationClass.getName() ), null );
        jMethod.getModifiers().setStatic( true );
        jMethod.addParameter( new JParameter( new JType( locationClass.getName() ), "target" ) );
        jMethod.addParameter( new JParameter( new JType( locationClass.getName() ), "source" ) );
        jMethod.addParameter( new JParameter( JType.BOOLEAN, "sourceDominant" ) );
        sc = jMethod.getSourceCode();
        sc.add( "if ( source == null )" );
        sc.add( "{" );
        sc.addIndented( "return target;" );
        sc.add( "}" );
        sc.add( "else if ( target == null )" );
        sc.add( "{" );
        sc.addIndented( "return source;" );
        sc.add( "}" );
        sc.add( "" );
        sc.add( locationClass.getName() + " result =" );
        sc.add( "    new " + locationClass.getName() + "( target.getLineNumber(), target.getColumnNumber()" + (
            sourceClass != null
                ? ", target.get" + capitalise( source.getName() ) + "()"
                : "" ) + " );" );
        sc.add( "" );
        sc.add( fieldType + " locations;" );
        sc.add( fieldType + " sourceLocations = source.get" + capitalise( locationsField ) + "();" );
        sc.add( fieldType + " targetLocations = target.get" + capitalise( locationsField ) + "();" );
        sc.add( "if ( sourceLocations == null )" );
        sc.add( "{" );
        sc.addIndented( "locations = targetLocations;" );
        sc.add( "}" );
        sc.add( "else if ( targetLocations == null )" );
        sc.add( "{" );
        sc.addIndented( "locations = sourceLocations;" );
        sc.add( "}" );
        sc.add( "else" );
        sc.add( "{" );
        sc.addIndented( "locations = new " + fieldImpl.getName() + "();" );
        sc.addIndented( "locations.putAll( sourceDominant ? targetLocations : sourceLocations );" );
        sc.addIndented( "locations.putAll( sourceDominant ? sourceLocations : targetLocations );" );
        sc.add( "}" );
        sc.add( "result.set" + capitalise( locationsField ) + "( locations );" );
        sc.add( "" );
        sc.add( "return result;" );
        jClass.addMethod( jMethod );

        // public static Location merge( Location target, Location source, Collection<Integer> indices )
        jMethod = new JMethod( "merge", new JType( locationClass.getName() ), null );
        jMethod.getModifiers().setStatic( true );
        jMethod.addParameter( new JParameter( new JType( locationClass.getName() ), "target" ) );
        jMethod.addParameter( new JParameter( new JType( locationClass.getName() ), "source" ) );
        jMethod.addParameter(
            new JParameter( new JCollectionType( "java.util.Collection", new JType( "Integer" ), useJava5 ),
                            "indices" ) );
        String intWrap = useJava5 ? "Integer.valueOf" : "new Integer";
        sc = jMethod.getSourceCode();
        sc.add( "if ( source == null )" );
        sc.add( "{" );
        sc.addIndented( "return target;" );
        sc.add( "}" );
        sc.add( "else if ( target == null )" );
        sc.add( "{" );
        sc.addIndented( "return source;" );
        sc.add( "}" );
        sc.add( "" );
        sc.add( locationClass.getName() + " result =" );
        sc.add( "    new " + locationClass.getName() + "( target.getLineNumber(), target.getColumnNumber()" + (
            sourceClass != null
                ? ", target.get" + capitalise( source.getName() ) + "()"
                : "" ) + " );" );
        sc.add( "" );
        sc.add( fieldType + " locations;" );
        sc.add( fieldType + " sourceLocations = source.get" + capitalise( locationsField ) + "();" );
        sc.add( fieldType + " targetLocations = target.get" + capitalise( locationsField ) + "();" );
        sc.add( "if ( sourceLocations == null )" );
        sc.add( "{" );
        sc.addIndented( "locations = targetLocations;" );
        sc.add( "}" );
        sc.add( "else if ( targetLocations == null )" );
        sc.add( "{" );
        sc.addIndented( "locations = sourceLocations;" );
        sc.add( "}" );
        sc.add( "else" );
        sc.add( "{" );
        sc.indent();
        sc.add( "locations = new " + fieldImpl + "();" );
        sc.add( "for ( java.util.Iterator" + ( useJava5 ? "<Integer>" : "" )
                    + " it = indices.iterator(); it.hasNext(); )" );
        sc.add( "{" );
        sc.indent();
        sc.add( locationClass.getName() + " location;" );
        sc.add( "Integer index = " + ( useJava5 ? "" : "(Integer) " ) + "it.next();" );
        sc.add( "if ( index.intValue() < 0 )" );
        sc.add( "{" );
        sc.addIndented( "location = sourceLocations.get( " + intWrap + "( ~index.intValue() ) );" );
        sc.add( "}" );
        sc.add( "else" );
        sc.add( "{" );
        sc.addIndented( "location = targetLocations.get( index );" );
        sc.add( "}" );
        sc.add( "locations.put( " + intWrap + "( locations.size() ), location );" );
        sc.unindent();
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );
        sc.add( "result.set" + capitalise( locationsField ) + "( locations );" );
        sc.add( "" );
        sc.add( "return result;" );
        jClass.addMethod( jMethod );
    }

    /**
     * Utility method that adds a period to the end of a string, if the last non-whitespace character of the string is
     * not a punctuation mark or an end-tag.
     *
     * @param string The string to work with
     * @return The string that came in but with a period at the end
     */
    private String appendPeriod( String string )
    {
        if ( string == null )
        {
            return string;
        }

        String trimmedString = string.trim();
        if ( trimmedString.endsWith( "." ) || trimmedString.endsWith( "!" ) || trimmedString.endsWith( "?" )
            || trimmedString.endsWith( ">" ) )
        {
            return string;
        }
        else
        {
            return string + ".";
        }
    }

    private String createHashCodeForField( ModelField identifier )
    {
        String name = identifier.getName();
        String type = identifier.getType();

        if ( "boolean".equals( type ) )
        {
            return "( " + name + " ? 0 : 1 )";
        }
        else if ( "byte".equals( type ) || "char".equals( type ) || "short".equals( type ) || "int".equals( type ) )
        {
            return "(int) " + name;
        }
        else if ( "long".equals( type ) )
        {
            return "(int) ( " + name + " ^ ( " + name + " >>> 32 ) )";
        }
        else if ( "float".equals( type ) )
        {
            return "Float.floatToIntBits( " + name + " )";
        }
        else if ( "double".equals( type ) )
        {
            return "(int) ( Double.doubleToLongBits( " + identifier.getName() + " ) ^ ( Double.doubleToLongBits( "
                + identifier.getName() + " ) >>> 32 ) )";
        }
        else
        {
            return "( " + name + " != null ? " + name + ".hashCode() : 0 )";
        }
    }

    private JField createField( ModelField modelField )
        throws ModelloException
    {
        JType type;

        String baseType = modelField.getType();
        if ( modelField.isArray() )
        {
            // remove [] at the end of the type
            baseType = baseType.substring( 0, baseType.length() - 2 );
        }

        if ( "boolean".equals( baseType ) )
        {
            type = JType.BOOLEAN;
        }
        else if ( "byte".equals( baseType ) )
        {
            type = JType.BYTE;
        }
        else if ( "char".equals( baseType ) )
        {
            type = JType.CHAR;
        }
        else if ( "double".equals( baseType ) )
        {
            type = JType.DOUBLE;
        }
        else if ( "float".equals( baseType ) )
        {
            type = JType.FLOAT;
        }
        else if ( "int".equals( baseType ) )
        {
            type = JType.INT;
        }
        else if ( "short".equals( baseType ) )
        {
            type = JType.SHORT;
        }
        else if ( "long".equals( baseType ) )
        {
            type = JType.LONG;
        }
        else if ( "Date".equals( baseType ) )
        {
            type = new JClass( "java.util.Date" );
        }
        else if ( "DOM".equals( baseType ) )
        {
            // TODO: maybe DOM is not how to specify it in the model, but just Object and markup Xpp3Dom for the
            // Xpp3Reader?
            // not sure how we'll treat it for the other sources, eg sql.
            type = new JClass( "Object" );
        }
        else
        {
            type = new JClass( baseType );
        }

        if ( modelField.isArray() )
        {
            type = new JArrayType( type, useJava5 );
        }

        JField field = new JField( type, modelField.getName() );

        if ( modelField.isModelVersionField() )
        {
            field.setInitString( "\"" + getGeneratedVersion() + "\"" );
        }

        if ( modelField.getDefaultValue() != null )
        {
            field.setInitString( getJavaDefaultValue( modelField ) );
        }

        if ( StringUtils.isNotEmpty( modelField.getDescription() ) )
        {
            field.setComment( appendPeriod( modelField.getDescription() ) );
        }

        if ( useJava5 && !modelField.getAnnotations().isEmpty() )
        {
            for ( String annotation : modelField.getAnnotations() )
            {
                field.appendAnnotation( annotation );
            }
        }

        return field;
    }

    private void createField( JClass jClass, ModelField modelField )
        throws ModelloException
    {
        JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) modelField.getMetadata( JavaFieldMetadata.ID );

        JField field = createField( modelField );

        jClass.addField( field );

        if ( javaFieldMetadata.isGetter() )
        {
            jClass.addMethod( createGetter( field, modelField ) );
        }

        if ( javaFieldMetadata.isSetter() )
        {
            jClass.addMethod( createSetter( field, modelField ) );
        }
    }

    private JMethod createGetter( JField field, ModelField modelField )
    {
        String propertyName = capitalise( field.getName() );

        JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) modelField.getMetadata( JavaFieldMetadata.ID );

        String prefix = javaFieldMetadata.isBooleanGetter() ? "is" : "get";

        JType returnType = field.getType();
        String interfaceCast = "";

        if ( modelField instanceof ModelAssociation )
        {
            ModelAssociation modelAssociation = (ModelAssociation) modelField;

            JavaAssociationMetadata javaAssociationMetadata =
                (JavaAssociationMetadata) modelAssociation.getAssociationMetadata( JavaAssociationMetadata.ID );

            if ( StringUtils.isNotEmpty( javaAssociationMetadata.getInterfaceName() )
                && !javaFieldMetadata.isBooleanGetter() )
            {
                returnType = new JClass( javaAssociationMetadata.getInterfaceName() );

                interfaceCast = "(" + javaAssociationMetadata.getInterfaceName() + ") ";
            }
        }

        JMethod getter = new JMethod( prefix + propertyName, returnType, null );

        StringBuffer comment = new StringBuffer( "Get " );
        if ( StringUtils.isEmpty( modelField.getDescription() ) )
        {
            comment.append( "the " );
            comment.append( field.getName() );
            comment.append( " field" );
        }
        else
        {
            comment.append( StringUtils.lowercaseFirstLetter( modelField.getDescription().trim() ) );
        }
        getter.getJDocComment().setComment( appendPeriod( comment.toString() ) );

        getter.getSourceCode().add( "return " + interfaceCast + "this." + field.getName() + ";" );

        return getter;
    }

    private JMethod createSetter( JField field, ModelField modelField )
        throws ModelloException
    {
        return createSetter( field, modelField, false );
    }

    // since 1.8
    private JMethod createSetter( JField field, ModelField modelField, boolean isBuilderMethod )
        throws ModelloException
    {
        String propertyName = capitalise( field.getName() );

        JMethod setter;
        if ( isBuilderMethod )
        {
            setter = new JMethod( "set" + propertyName, new JClass( "Builder" ), "this builder instance" );
        }
        else
        {
            setter = new JMethod( "set" + propertyName );
        }

        StringBuffer comment = new StringBuffer( "Set " );
        if ( StringUtils.isEmpty( modelField.getDescription() ) )
        {
            comment.append( "the " );
            comment.append( field.getName() );
            comment.append( " field" );
        }
        else
        {
            comment.append( StringUtils.lowercaseFirstLetter( modelField.getDescription().trim() ) );
        }
        setter.getJDocComment().setComment( appendPeriod( comment.toString() ) );

        JType parameterType = getDesiredType( modelField, false );

        setter.addParameter( new JParameter( parameterType, field.getName() ) );

        JSourceCode sc = setter.getSourceCode();

        if ( modelField instanceof ModelAssociation )
        {
            ModelAssociation modelAssociation = (ModelAssociation) modelField;

            JavaAssociationMetadata javaAssociationMetadata =
                (JavaAssociationMetadata) modelAssociation.getAssociationMetadata( JavaAssociationMetadata.ID );

            boolean isOneMultiplicity =
                isBidirectionalAssociation( modelAssociation ) && modelAssociation.isOneMultiplicity();

            if ( isOneMultiplicity && javaAssociationMetadata.isBidi() )
            {
                sc.add( "if ( this." + field.getName() + " != null )" );

                sc.add( "{" );

                sc.indent();

                sc.add( "this." + field.getName() + ".break" + modelAssociation.getModelClass().getName()
                            + "Association( this );" );

                sc.unindent();

                sc.add( "}" );

                sc.add( "" );
            }

            String interfaceCast = "";

            if ( StringUtils.isNotEmpty( javaAssociationMetadata.getInterfaceName() )
                && modelAssociation.isOneMultiplicity() )
            {
                interfaceCast = "(" + field.getType().getName() + ") ";

                createClassCastAssertion( sc, modelAssociation, "set" );
            }

            sc.add( "this." + field.getName() + " = " + interfaceCast + field.getName() + ";" );

            if ( isOneMultiplicity && javaAssociationMetadata.isBidi() )
            {
                sc.add( "" );

                sc.add( "if ( " + field.getName() + " != null )" );

                sc.add( "{" );

                sc.indent();

                sc.add( "this." + field.getName() + ".create" + modelAssociation.getModelClass().getName()
                            + "Association( this );" );

                sc.unindent();

                sc.add( "}" );
            }
        }
        else
        {
            sc.add( "this." + field.getName() + " = " + field.getName() + ";" );
        }

        if ( isBuilderMethod )
        {
            sc.add( "return this;" );
        }

        return setter;
    }

    private void createClassCastAssertion( JSourceCode sc, ModelAssociation modelAssociation, String crudModifier )
        throws ModelloException
    {
        JavaAssociationMetadata javaAssociationMetadata =
            (JavaAssociationMetadata) modelAssociation.getAssociationMetadata( JavaAssociationMetadata.ID );

        if ( StringUtils.isEmpty( javaAssociationMetadata.getInterfaceName() ) )
        {
            return; // java.useInterface feature not used, no class cast assertion needed
        }

        String propertyName = capitalise( modelAssociation.getName() );

        JField field = createField( modelAssociation );
        String fieldName = field.getName();
        JType type = new JClass( modelAssociation.getTo() );

        if ( modelAssociation.isManyMultiplicity() )
        {
            fieldName = uncapitalise( modelAssociation.getTo() );
        }

        String instanceName = type.getName();

        // Add sane class cast exception message
        // When will sun ever fix this?

        sc.add( "if ( " + fieldName + " != null && !( " + fieldName + " instanceof " + instanceName + " ) )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "throw new ClassCastException( \"" + modelAssociation.getModelClass().getName() + "." + crudModifier
                    + propertyName + "( " + fieldName + " ) parameter must be instanceof \" + " + instanceName
                    + ".class.getName() );" );

        sc.unindent();

        sc.add( "}" );
    }

    private void createAssociation( JClass jClass, ModelAssociation modelAssociation, JSourceCode jConstructorSource )
        throws ModelloException
    {
        JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) modelAssociation.getMetadata( JavaFieldMetadata.ID );

        JavaAssociationMetadata javaAssociationMetadata = getJavaAssociationMetadata( modelAssociation );

        if ( modelAssociation.isManyMultiplicity() )
        {
            JType componentType = getComponentType( modelAssociation, javaAssociationMetadata );

            String defaultValue = getDefaultValue( modelAssociation, componentType );

            JType type;
            if ( modelAssociation.isGenericType() )
            {
                type = new JCollectionType( modelAssociation.getType(), componentType, useJava5 );
            }
            else if ( ModelDefault.MAP.equals( modelAssociation.getType() ) )
            {
                JMapType mapType = new JMapType( modelAssociation.getType(), defaultValue, componentType, useJava5 );
                defaultValue = mapType.getInstanceName();
                type = mapType;
            }
            else
            {
                type = new JClass( modelAssociation.getType() );
            }

            JField jField = new JField( type, modelAssociation.getName() );

            if ( !isEmpty( modelAssociation.getComment() ) )
            {
                jField.setComment( modelAssociation.getComment() );
            }

            if ( useJava5 && !modelAssociation.getAnnotations().isEmpty() )
            {
                for ( String annotation : modelAssociation.getAnnotations() )
                {
                    jField.appendAnnotation( annotation );
                }
            }

            if ( StringUtils.equals( javaAssociationMetadata.getInitializationMode(),
                                     JavaAssociationMetadata.FIELD_INIT ) )
            {
                jField.setInitString( defaultValue );
            }

            if ( StringUtils.equals( javaAssociationMetadata.getInitializationMode(),
                                     JavaAssociationMetadata.CONSTRUCTOR_INIT ) )
            {
                jConstructorSource.add( "this." + jField.getName() + " = " + defaultValue + ";" );
            }

            jClass.addField( jField );

            if ( javaFieldMetadata.isGetter() )
            {
                String propertyName = capitalise( jField.getName() );

                JMethod getter = new JMethod( "get" + propertyName, jField.getType(), null );

                JSourceCode sc = getter.getSourceCode();

                if ( StringUtils.equals( javaAssociationMetadata.getInitializationMode(),
                                         JavaAssociationMetadata.LAZY_INIT ) )
                {
                    sc.add( "if ( this." + jField.getName() + " == null )" );

                    sc.add( "{" );

                    sc.indent();

                    sc.add( "this." + jField.getName() + " = " + defaultValue + ";" );

                    sc.unindent();

                    sc.add( "}" );

                    sc.add( "" );
                }

                sc.add( "return this." + jField.getName() + ";" );

                jClass.addMethod( getter );
            }

            if ( javaFieldMetadata.isSetter() )
            {
                jClass.addMethod( createSetter( jField, modelAssociation ) );
            }

            if ( javaAssociationMetadata.isAdder() )
            {
                createAdder( modelAssociation, jClass );
            }
        }
        else
        {
            createField( jClass, modelAssociation );
        }

        if ( isBidirectionalAssociation( modelAssociation ) )
        {
            if ( javaAssociationMetadata.isBidi() )
            {
                createCreateAssociation( jClass, modelAssociation );
                createBreakAssociation( jClass, modelAssociation );
            }
        }
    }

    private String getDefaultValue( ModelAssociation modelAssociation, JType componentType )
    {
        String defaultValue = getDefaultValue( modelAssociation );

        if ( modelAssociation.isGenericType() )
        {
            ModelDefault modelDefault = getModel().getDefault( modelAssociation.getType() );

            if ( useJava5 )
            {
                defaultValue =
                    StringUtils.replace( modelDefault.getValue(), "<?>", "<" + componentType.getName() + ">" );
            }
            else
            {
                defaultValue =
                    StringUtils.replace( modelDefault.getValue(), "<?>", "/*<" + componentType.getName() + ">*/" );
            }
        }
        return defaultValue;
    }

    private JavaAssociationMetadata getJavaAssociationMetadata( ModelAssociation modelAssociation )
        throws ModelloException
    {
        JavaAssociationMetadata javaAssociationMetadata =
            (JavaAssociationMetadata) modelAssociation.getAssociationMetadata( JavaAssociationMetadata.ID );

        if ( !JavaAssociationMetadata.INIT_TYPES.contains( javaAssociationMetadata.getInitializationMode() ) )
        {
            throw new ModelloException(
                "The Java Modello Generator cannot use '" + javaAssociationMetadata.getInitializationMode()
                    + "' as a <association java.init=\"" + javaAssociationMetadata.getInitializationMode() + "\"> "
                    + "value, the only the following are acceptable " + JavaAssociationMetadata.INIT_TYPES );
        }
        return javaAssociationMetadata;
    }

    private JType getComponentType( ModelAssociation modelAssociation, JavaAssociationMetadata javaAssociationMetadata )
    {
        JType componentType;
        if ( javaAssociationMetadata.getInterfaceName() != null )
        {
            componentType = new JClass( javaAssociationMetadata.getInterfaceName() );
        }
        else
        {
            componentType = new JClass( modelAssociation.getTo() );
        }
        return componentType;
    }

    private void createCreateAssociation( JClass jClass, ModelAssociation modelAssociation )
    {
        JMethod createMethod = new JMethod( "create" + modelAssociation.getTo() + "Association" );

        JavaAssociationMetadata javaAssociationMetadata =
            (JavaAssociationMetadata) modelAssociation.getAssociationMetadata( JavaAssociationMetadata.ID );

        createMethod.addParameter(
            new JParameter( new JClass( modelAssociation.getTo() ), uncapitalise( modelAssociation.getTo() ) ) );

        // TODO: remove after tested
        // createMethod.addException( new JClass( "Exception" ) );

        JSourceCode sc = createMethod.getSourceCode();

        if ( modelAssociation.isOneMultiplicity() )
        {
            if ( javaAssociationMetadata.isBidi() )
            {
                sc.add( "if ( this." + modelAssociation.getName() + " != null )" );

                sc.add( "{" );

                sc.indent();

                sc.add(
                    "break" + modelAssociation.getTo() + "Association( this." + modelAssociation.getName() + " );" );

                sc.unindent();

                sc.add( "}" );

                sc.add( "" );
            }

            sc.add( "this." + modelAssociation.getName() + " = " + uncapitalise( modelAssociation.getTo() ) + ";" );
        }
        else
        {
            jClass.addImport( "java.util.Collection" );

            sc.add( "Collection " + modelAssociation.getName() + " = get" + capitalise( modelAssociation.getName() )
                        + "();" );

            sc.add( "" );

            sc.add( "if ( " + modelAssociation.getName() + ".contains( " + uncapitalise( modelAssociation.getTo() )
                        + " ) )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "throw new IllegalStateException( \"" + uncapitalise( modelAssociation.getTo() )
                        + " is already assigned.\" );" );

            sc.unindent();

            sc.add( "}" );

            sc.add( "" );

            sc.add( modelAssociation.getName() + ".add( " + uncapitalise( modelAssociation.getTo() ) + " );" );
        }

        jClass.addMethod( createMethod );
    }

    private void createBreakAssociation( JClass jClass, ModelAssociation modelAssociation )
    {
        JSourceCode sc;
        JMethod breakMethod = new JMethod( "break" + modelAssociation.getTo() + "Association" );

        breakMethod.addParameter(
            new JParameter( new JClass( modelAssociation.getTo() ), uncapitalise( modelAssociation.getTo() ) ) );

        // TODO: remove after tested
        // breakMethod.addException( new JClass( "Exception" ) );

        sc = breakMethod.getSourceCode();

        if ( modelAssociation.isOneMultiplicity() )
        {
            sc.add(
                "if ( this." + modelAssociation.getName() + " != " + uncapitalise( modelAssociation.getTo() ) + " )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "throw new IllegalStateException( \"" + uncapitalise( modelAssociation.getTo() )
                        + " isn't associated.\" );" );

            sc.unindent();

            sc.add( "}" );

            sc.add( "" );

            sc.add( "this." + modelAssociation.getName() + " = null;" );
        }
        else
        {
            JavaAssociationMetadata javaAssociationMetadata =
                            (JavaAssociationMetadata) modelAssociation.getAssociationMetadata( JavaAssociationMetadata.ID );

            String reference;

            if ( JavaAssociationMetadata.LAZY_INIT.equals( javaAssociationMetadata.getInitializationMode() ) )
            {
                reference = "get" + capitalise( modelAssociation.getName() ) + "()";
            }
            else
            {
                reference = modelAssociation.getName();
            }

            sc.add( "if ( !" + reference + ".contains( " + uncapitalise(
                modelAssociation.getTo() ) + " ) )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "throw new IllegalStateException( \"" + uncapitalise( modelAssociation.getTo() )
                        + " isn't associated.\" );" );

            sc.unindent();

            sc.add( "}" );

            sc.add( "" );

            sc.add( reference + ".remove( " + uncapitalise( modelAssociation.getTo() ) + " );" );
        }

        jClass.addMethod( breakMethod );
    }

    private void createAdder( ModelAssociation modelAssociation, JClass jClass )
        throws ModelloException
    {
        createAdder( modelAssociation, jClass, false );
    }

    /*
     * since 1.8
     */
    private void createAdder( ModelAssociation modelAssociation, JClass jClass, boolean isBuilderMethod )
        throws ModelloException
    {
        String fieldName = modelAssociation.getName();

        JavaAssociationMetadata javaAssociationMetadata =
            (JavaAssociationMetadata) modelAssociation.getAssociationMetadata( JavaAssociationMetadata.ID );

        String parameterName = uncapitalise( modelAssociation.getTo() );
        String implementationParameterName = parameterName;

        boolean bidirectionalAssociation = isBidirectionalAssociation( modelAssociation );

        JType addType;

        if ( StringUtils.isNotEmpty( javaAssociationMetadata.getInterfaceName() ) )
        {
            addType = new JClass( javaAssociationMetadata.getInterfaceName() );
            implementationParameterName = "( (" + modelAssociation.getTo() + ") " + parameterName + " )";
        }
        else if ( modelAssociation.getToClass() != null )
        {
            addType = new JClass( modelAssociation.getToClass().getName() );
        }
        else
        {
            addType = new JClass( "String" );
        }

        if ( modelAssociation.getType().equals( ModelDefault.PROPERTIES ) || modelAssociation.getType().equals(
            ModelDefault.MAP ) )
        {
            String adderName = "add" + capitalise( singular( fieldName ) );

            JMethod adder;
            if ( isBuilderMethod )
            {
                adder = new JMethod( adderName, new JClass( "Builder" ), "this builder instance" );
            }
            else
            {
                adder = new JMethod( adderName );
            }

            if ( modelAssociation.getType().equals( ModelDefault.MAP ) )
            {
                adder.addParameter( new JParameter( new JClass( "Object" ), "key" ) );
            }
            else
            {
                adder.addParameter( new JParameter( new JClass( "String" ), "key" ) );
            }

            adder.addParameter( new JParameter( new JClass( modelAssociation.getTo() ), "value" ) );

            StringBuilder adderCode = new StringBuilder();

            if ( JavaAssociationMetadata.LAZY_INIT.equals( javaAssociationMetadata.getInitializationMode() )
                    && !isBuilderMethod )
            {
                adderCode.append( "get" ).append( capitalise( fieldName ) ).append( "()" );
            }
            else
            {
                adderCode.append( fieldName );
            }

            adderCode.append( ".put( key, value );" );

            adder.getSourceCode().add( adderCode.toString() );

            if ( isBuilderMethod )
            {
                adder.getSourceCode().add( "return this;" );
            }

            jClass.addMethod( adder );
        }
        else
        {
            String adderName = "add" + singular( capitalise( singular( fieldName ) ) );

            JMethod adder;
            if ( isBuilderMethod )
            {
                adder = new JMethod( adderName, new JClass( "Builder" ), "this builder instance" );
            }
            else
            {
                adder = new JMethod( adderName );
            }

            adder.addParameter( new JParameter( addType, parameterName ) );

            createClassCastAssertion( adder.getSourceCode(), modelAssociation, "add" );

            StringBuilder adderCode = new StringBuilder();

            if ( JavaAssociationMetadata.LAZY_INIT.equals( javaAssociationMetadata.getInitializationMode() )
                    && !isBuilderMethod )
            {
                adderCode.append( "get" ).append( capitalise( fieldName ) ).append( "()" );
            }
            else
            {
                adderCode.append( fieldName );
            }

            adderCode.append( ".add( " )
                     .append( implementationParameterName )
                     .append( " );" );

            adder.getSourceCode().add( adderCode.toString() );

            if ( bidirectionalAssociation && javaAssociationMetadata.isBidi() && !isBuilderMethod )
            {
                // TODO: remove after tested
                // adder.addException( new JClass( "Exception" ) );

                adder.getSourceCode().add(
                    implementationParameterName + ".create" + modelAssociation.getModelClass().getName()
                        + "Association( this );" );
            }

            if ( isBuilderMethod )
            {
                adder.getSourceCode().add( "return this;" );
            }

            jClass.addMethod( adder );

            // don't add the remover in the inner Builder class
            if ( isBuilderMethod )
            {
                return;
            }

            JMethod remover = new JMethod( "remove" + singular( capitalise( fieldName ) ) );

            remover.addParameter( new JParameter( addType, parameterName ) );

            createClassCastAssertion( remover.getSourceCode(), modelAssociation, "remove" );

            if ( bidirectionalAssociation && javaAssociationMetadata.isBidi() )
            {
                // TODO: remove after tested
                // remover.addException( new JClass( "Exception" ) );

                remover.getSourceCode().add(
                    parameterName + ".break" + modelAssociation.getModelClass().getName() + "Association( this );" );
            }

            String reference;

            if ( JavaAssociationMetadata.LAZY_INIT.equals( javaAssociationMetadata.getInitializationMode() ) )
            {
                reference = "get" + capitalise( fieldName ) + "()";
            }
            else
            {
                reference = fieldName;
            }

            remover.getSourceCode().add( reference + ".remove( " + implementationParameterName + " );" );

            jClass.addMethod( remover );
        }
    }

    private boolean isBidirectionalAssociation( ModelAssociation association )
    {
        Model model = association.getModelClass().getModel();

        if ( !isClassInModel( association.getTo(), model ) )
        {
            return false;
        }

        ModelClass toClass = association.getToClass();

        for ( ModelField modelField : toClass.getFields( getGeneratedVersion() ) )
        {
            if ( !( modelField instanceof ModelAssociation ) )
            {
                continue;
            }

            ModelAssociation modelAssociation = (ModelAssociation) modelField;

            if ( association == modelAssociation )
            {
                continue;
            }

            if ( !isClassInModel( modelAssociation.getTo(), model ) )
            {
                continue;
            }

            ModelClass totoClass = modelAssociation.getToClass();

            if ( association.getModelClass().equals( totoClass ) )
            {
                return true;
            }
        }

        return false;
    }

    private JType getDesiredType( ModelField modelField, boolean useTo )
        throws ModelloException
    {
        JField field = createField( modelField );
        JType type = field.getType();

        if ( modelField instanceof ModelAssociation )
        {
            ModelAssociation modelAssociation = (ModelAssociation) modelField;
            JavaAssociationMetadata javaAssociationMetadata =
                (JavaAssociationMetadata) modelAssociation.getAssociationMetadata( JavaAssociationMetadata.ID );

            if ( StringUtils.isNotEmpty( javaAssociationMetadata.getInterfaceName() )
                && !modelAssociation.isManyMultiplicity() )
            {
                type = new JClass( javaAssociationMetadata.getInterfaceName() );
            }
            else if ( modelAssociation.isManyMultiplicity() && modelAssociation.isGenericType() )
            {
                JType componentType = getComponentType( modelAssociation, javaAssociationMetadata );
                type = new JCollectionType( modelAssociation.getType(), componentType, useJava5 );
            }
            else if ( useTo )
            {
                type = new JClass( modelAssociation.getTo() );
            }
        }

        return type;
    }

    private void addParameter( JMethodSignature jMethod, String type, String name, String comment )
    {
        jMethod.addParameter( new JParameter( new JType( type ), name ) );
        jMethod.getJDocComment().getParamDescriptor( name ).setDescription( comment );
    }

    // ----------------------------------------------------------------------
    // Model.Builder
    // since 1.8
    // ----------------------------------------------------------------------

    private void generateBuilder( ModelClass modelClass, JClass builderClass, JConstructor outherClassConstructor )
        throws ModelloException
    {
        builderClass.getModifiers().setStatic( true );
        builderClass.getModifiers().setFinal( true );

        ModelClass reference = modelClass;

        // traverse the whole modelClass hierarchy to create the nested Builder
        while ( reference != null )
        {
            // create builder setters methods
            for ( ModelField modelField : reference.getFields( getGeneratedVersion() ) )
            {
                if ( modelField instanceof ModelAssociation )
                {
                    createBuilderAssociation( builderClass, (ModelAssociation) modelField );
                }
                else
                {
                    createBuilderField( builderClass, modelField );
                }
            }

            if ( reference.hasSuperClass() )
            {
                reference = reference.getModel().getClass( reference.getSuperClass(), getGeneratedVersion() );
            }
            else
            {
                reference = null;
            }
        }

        // create and add the Model#build() method
        JMethod build = new JMethod( "build", new JClass( modelClass.getName() ), "A new <code>"
                                                                                  + modelClass.getName()
                                                                                  + "</code> instance" );
        build.getJDocComment().setComment( "Creates a new <code>"
                                           + modelClass.getName()
                                           + "</code> instance." );

        JSourceCode sc = build.getSourceCode();

        createInstanceAndSetProperties( modelClass, outherClassConstructor, sc );

        builderClass.addMethod( build );
    }

    private void createInstanceAndSetProperties( ModelClass modelClass, JConstructor constructor, JSourceCode sc )
        throws ModelloException
    {
        final Set<String> ctorArgs = new HashSet<String>();

        StringBuilder ctor = new StringBuilder( modelClass.getName() )
                                 .append( " instance = new " )
                                 .append( modelClass.getName() )
                                 .append( '(' );

        // understand if default empty ctor can be used or if it requires parameters
        if ( constructor != null )
        {
            JParameter[] parameters = constructor.getParameters();
            for ( int i = 0; i < parameters.length; i++ )
            {
                if ( i > 0 )
                {
                    ctor.append( ',' );
                }

                JParameter parameter = parameters[i];

                ctor.append( ' ' )
                    .append( parameter.getName() )
                    .append( ' ' );

                ctorArgs.add( parameter.getName() );
            }
        }

        ctor.append( ");" );

        sc.add( ctor.toString() );

        ModelClass reference = modelClass;

        // traverse the whole modelClass hierarchy to create the nested Builder instance
        while ( reference != null )
        {
            // collect parameters and set them in the instance object
            for ( ModelField modelField : reference.getFields( getGeneratedVersion() ) )
            {
                if ( modelField instanceof ModelAssociation )
                {
                    ModelAssociation modelAssociation = (ModelAssociation) modelField;
                    JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) modelField.getMetadata( JavaFieldMetadata.ID );
                    JavaAssociationMetadata javaAssociationMetadata = getJavaAssociationMetadata( modelAssociation );

                    if ( modelAssociation.isManyMultiplicity()
                         && !javaFieldMetadata.isGetter()
                         && !javaFieldMetadata.isSetter()
                         && !javaAssociationMetadata.isAdder() )
                    {
                        throw new ModelloException( "Exception while generating Java, Model inconsistency found: impossible to generate '"
                                                    + modelClass.getName()
                                                    + ".Builder#build()' method, '"
                                                    + modelClass.getName()
                                                    + "."
                                                    + modelAssociation.getName()
                                                    + "' field ("
                                                    + modelAssociation.getType()
                                                    + ") cannot be set, no getter/setter/adder method available." );
                    }

                    createSetBuilderAssociationToInstance( ctorArgs, modelAssociation, sc );
                }
                else
                {
                    createSetBuilderFieldToInstance( ctorArgs, modelField, sc );
                }
            }

            if ( reference.hasSuperClass() )
            {
                reference = reference.getModel().getClass( reference.getSuperClass(), getGeneratedVersion() );
            }
            else
            {
                reference = null;
            }
        }

        sc.add( "return instance;" );
    }

    private void createBuilderField( JClass jClass, ModelField modelField )
        throws ModelloException
    {
        JField field = createField( modelField );

        jClass.addField( field );

        jClass.addMethod( createSetter( field, modelField, true ) );
    }

    private boolean createSetBuilderFieldToInstance( Set<String> ctorArgs, ModelField modelField, JSourceCode sc )
        throws ModelloException
    {
        JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) modelField.getMetadata( JavaFieldMetadata.ID );

        // if it is not already set by the ctor and if the setter method is available
        if ( !ctorArgs.contains( modelField.getName() ) && javaFieldMetadata.isSetter() )
        {
            sc.add( "instance.set"
                    + capitalise( modelField.getName() )
                    + "( "
                    + modelField.getName()
                    + " );" );
            return true;
        }

        return false;
    }

    private void createBuilderAssociation( JClass jClass, ModelAssociation modelAssociation )
        throws ModelloException
    {
        JavaAssociationMetadata javaAssociationMetadata = getJavaAssociationMetadata( modelAssociation );

        if ( modelAssociation.isManyMultiplicity() )
        {
            JType componentType = getComponentType( modelAssociation, javaAssociationMetadata );

            String defaultValue = getDefaultValue( modelAssociation, componentType );

            JType type;
            if ( modelAssociation.isGenericType() )
            {
                type = new JCollectionType( modelAssociation.getType(), componentType, useJava5 );
            }
            else if ( ModelDefault.MAP.equals( modelAssociation.getType() ) )
            {
                JMapType mapType = new JMapType( modelAssociation.getType(), defaultValue, componentType, useJava5 );
                defaultValue = mapType.getInstanceName();
                type = mapType;
            }
            else
            {
                type = new JClass( modelAssociation.getType() );
            }

            JField jField = new JField( type, modelAssociation.getName() );
            jField.getModifiers().setFinal( true );

            if ( !isEmpty( modelAssociation.getComment() ) )
            {
                jField.setComment( modelAssociation.getComment() );
            }

            if ( useJava5 && !modelAssociation.getAnnotations().isEmpty() )
            {
                for ( String annotation : modelAssociation.getAnnotations() )
                {
                    jField.appendAnnotation( annotation );
                }
            }

            jField.setInitString( defaultValue );

            jClass.addField( jField );

            createAdder( modelAssociation, jClass, true );
        }
        else
        {
            createBuilderField( jClass, modelAssociation );
        }
    }

    private void createSetBuilderAssociationToInstance( Set<String> ctorArgs, ModelAssociation modelAssociation, JSourceCode sc )
        throws ModelloException
    {
        if ( modelAssociation.isManyMultiplicity() )
        {
            // Map/Properties don't have bidi association, they can be directly set
            if ( modelAssociation.getType().equals( ModelDefault.PROPERTIES )
                            || modelAssociation.getType().equals( ModelDefault.MAP ) )
            {
                if ( createSetBuilderFieldToInstance( ctorArgs, modelAssociation, sc ) )
                {
                    return;
                }
            }

            // check if there's no bidi association, so

            JavaAssociationMetadata javaAssociationMetadata = getJavaAssociationMetadata( modelAssociation );

            boolean bidirectionalAssociation = isBidirectionalAssociation( modelAssociation );

            if ( !bidirectionalAssociation || !javaAssociationMetadata.isBidi() )
            {
                JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) modelAssociation.getMetadata( JavaFieldMetadata.ID );

                // just use the plain old setter
                if ( createSetBuilderFieldToInstance( ctorArgs, modelAssociation, sc ) )
                {
                    return;
                }
                // or we can try to set by using the addAll if there is a getter available
                else if ( javaFieldMetadata.isGetter() )
                {
                    String action = isMap( modelAssociation.getType() ) ? "put" : "add";

                    sc.add( "instance.get" + capitalise( modelAssociation.getName() ) + "()." + action + "All( " + modelAssociation.getName() + " );" );
                    return;
                }
            }

            // no previous precondition satisfied
            // or no one of the previous method worked
            // use the adder
            // bidi association can be handled directly by the model, not a Builder task

            String itemType;
            String targetField = modelAssociation.getName();

            if ( StringUtils.isNotEmpty( javaAssociationMetadata.getInterfaceName() ) )
            {
                itemType = javaAssociationMetadata.getInterfaceName();
            }
            else if ( modelAssociation.getToClass() != null )
            {
                itemType = modelAssociation.getToClass().getName();
            }
            else if ( modelAssociation.getType().equals( ModelDefault.PROPERTIES )
                      || modelAssociation.getType().equals( ModelDefault.MAP ) )
            {
                StringBuilder itemTypeBuilder = new StringBuilder( "java.util.Map.Entry" );

                if ( useJava5 )
                {
                    itemTypeBuilder.append( '<' );

                    if ( modelAssociation.getType().equals( ModelDefault.PROPERTIES ) )
                    {
                        itemTypeBuilder.append( "Object, Object" );
                    }
                    else
                    {
                        itemTypeBuilder.append( "String, " ).append( modelAssociation.getTo() );
                    }

                    itemTypeBuilder.append( '>' );
                }

                itemType = itemTypeBuilder.toString();

                targetField += ".entrySet()";
            }
            else
            {
                itemType = "String";
            }

            if ( useJava5 )
            {
                sc.add( "for ( " + itemType + " item : " + targetField + " )" );
            }
            else
            {
                sc.add( "for ( java.util.Iterator it = " + targetField + ".iterator(); it.hasNext(); )" );
            }

            sc.add( "{" );
            sc.indent();

            if ( !useJava5 )
            {
                sc.add( itemType + " item = (" + itemType + ") it.next();" );
            }

            StringBuilder adder = new StringBuilder( "instance.add" )
                        .append( capitalise( singular( modelAssociation.getName() ) ) )
                        .append( "( " );

            if ( modelAssociation.getType().equals( ModelDefault.PROPERTIES )
                 || modelAssociation.getType().equals( ModelDefault.MAP ) )
            {
                appendEntryMethod( "String", "getKey()", adder, modelAssociation );
                adder.append( ", " );
                appendEntryMethod( modelAssociation.getTo(), "getValue()", adder, modelAssociation );
            }
            else
            {
                adder.append( "item" );
            }

            adder.append( " );" );

            sc.add( adder.toString() );

            sc.unindent();
            sc.add( "}" );
        }
        else
        {
            createSetBuilderFieldToInstance( ctorArgs, modelAssociation, sc );
        }
    }

    private void appendEntryMethod( String type, String method, StringBuilder target, ModelAssociation modelAssociation )
    {
        if ( !useJava5 || modelAssociation.getType().equals( ModelDefault.PROPERTIES ) )
        {
            target.append( '(' ).append( type ).append( ") " );
        }

        target.append( "item." ).append( method );
    }

    private void generateStaticCreator( ModelClass modelClass, JClass jClass, JConstructor constructor )
        throws ModelloException
    {
        JMethod creatorMethod = new JMethod( "new" + modelClass.getName() + "Instance",
                                             new JClass( modelClass.getName() ),
                                             "a new <code>" + modelClass.getName() + "</code> instance." );
        creatorMethod.getModifiers().setStatic( true );
        creatorMethod.getJDocComment().setComment( "Creates a new <code>" + modelClass.getName() + "</code> instance." );

        ModelClass reference = modelClass;

        boolean hasDefaults = false;

        // traverse the whole modelClass hierarchy to create the static creator method
        while ( reference != null )
        {
            for ( ModelField modelField : reference.getFields( getGeneratedVersion() ) )
            {
                // this is hacky
                JField field = createField( modelField );
                creatorMethod.addParameter( new JParameter( field.getType(), field.getName() ) );

                if ( !StringUtils.isEmpty( modelField.getDefaultValue() ) )
                {
                    hasDefaults = true;
                }
            }

            if ( reference.hasSuperClass() )
            {
                reference = reference.getModel().getClass( reference.getSuperClass(), getGeneratedVersion() );
            }
            else
            {
                reference = null;
            }
        }

        JSourceCode sc = creatorMethod.getSourceCode();

        createInstanceAndSetProperties( modelClass, constructor, sc );

        jClass.addMethod( creatorMethod );

        // creates a shortcut with default values only if necessary
        if ( !hasDefaults )
        {
            return;
        }

        creatorMethod = new JMethod( "new" + modelClass.getName() + "Instance",
                                     new JClass( modelClass.getName() ),
                                     "a new <code>" + modelClass.getName() + "</code> instance." );
        creatorMethod.getModifiers().setStatic( true );
        creatorMethod.getJDocComment().setComment( "Creates a new <code>" + modelClass.getName() + "</code> instance." );

        StringBuilder shortcutArgs = new StringBuilder();

        reference = modelClass;

        while ( reference != null )
        {
            for ( ModelField modelField : reference.getFields( getGeneratedVersion() ) )
            {
                if ( shortcutArgs.length() > 0 )
                {
                    shortcutArgs.append( ',' );
                }

                shortcutArgs.append( ' ' );

                if ( StringUtils.isEmpty( modelField.getDefaultValue() ) )
                {
                    // this is hacky
                    JField field = createField( modelField );
                    creatorMethod.addParameter( new JParameter( field.getType(), field.getName() ) );

                    shortcutArgs.append( modelField.getName() );
                }
                else
                {
                    shortcutArgs.append( getJavaDefaultValue( modelField ) );
                }

                shortcutArgs.append( ' ' );
            }

            if ( reference.hasSuperClass() )
            {
                reference = reference.getModel().getClass( reference.getSuperClass(), getGeneratedVersion() );
            }
            else
            {
                reference = null;
            }
        }

        sc = creatorMethod.getSourceCode();

        sc.add( "return new" + modelClass.getName() + "Instance(" + shortcutArgs + ");" );

        jClass.addMethod( creatorMethod );
    }

}
