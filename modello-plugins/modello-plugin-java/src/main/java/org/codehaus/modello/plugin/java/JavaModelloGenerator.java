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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.CodeSegment;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.ModelInterface;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JConstructor;
import org.codehaus.modello.plugin.java.javasource.JField;
import org.codehaus.modello.plugin.java.javasource.JInterface;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JType;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * @author <a href="mailto:jason@modello.org">Jason van Zyl </a>
 * @version $Id$
 */
public class JavaModelloGenerator
    extends AbstractJavaModelloGenerator
{
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

        // ----------------------------------------------------------------------
        // Generate the interfaces.
        // ----------------------------------------------------------------------

        for ( Iterator i = objectModel.getInterfaces( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelInterface modelInterface = (ModelInterface) i.next();

            String packageName = modelInterface.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

            JSourceWriter sourceWriter = newJSourceWriter( packageName, modelInterface.getName() );

            JInterface jInterface = new JInterface( packageName + '.' + modelInterface.getName() );

            if ( modelInterface.getSuperInterface() != null )
            {
                jInterface.addInterface( modelInterface.getSuperInterface() );
            }

            if ( modelInterface.getCodeSegments( getGeneratedVersion() ) != null )
            {
                for ( Iterator iterator = modelInterface.getCodeSegments( getGeneratedVersion() ).iterator(); iterator.hasNext(); )
                {
                    //TODO : add this method to jInterface or remove
                    // codeSegments and add method tag

                    // CodeSegment codeSegment = (CodeSegment) iterator.next();

                    //jInterface.addSourceCode( codeSegment.getCode() );
                }
            }

            jInterface.print( sourceWriter );

            sourceWriter.close();
        }

        // ----------------------------------------------------------------------
        // Generate the classes.
        // ----------------------------------------------------------------------

        for ( Iterator i = objectModel.getClasses( getGeneratedVersion() ).iterator(); i.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) i.next();

            JavaClassMetadata javaClassMetadata = (JavaClassMetadata) modelClass.getMetadata( JavaClassMetadata.ID );

            if ( !javaClassMetadata.isEnabled() )
            {
                // Skip generation of those classes that are not enabled for the java plugin.
                continue;
            }

            String packageName = modelClass.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

            JSourceWriter sourceWriter = newJSourceWriter( packageName, modelClass.getName() );

            JClass jClass = new JClass( packageName + '.' + modelClass.getName() );

            jClass.addImport( "java.util.Date" );

            if ( StringUtils.isNotEmpty( modelClass.getDescription() ) )
            {
                jClass.getJDocComment().setComment( appendPeriod( modelClass.getDescription() ) );
            }

            addModelImports( jClass, modelClass );

            jClass.getModifiers().setAbstract( javaClassMetadata.isAbstract() );

            if ( modelClass.getSuperClass() != null )
            {
                jClass.setSuperClass( modelClass.getSuperClass() );
            }

            for ( Iterator j = modelClass.getInterfaces().iterator(); j.hasNext(); )
            {
                String implementedInterface = (String) j.next();

                jClass.addInterface( implementedInterface );
            }

            jClass.addInterface( Serializable.class.getName() );

            JSourceCode jConstructorSource = new JSourceCode();

            for ( Iterator j = modelClass.getFields( getGeneratedVersion() ).iterator(); j.hasNext(); )
            {
                ModelField modelField = (ModelField) j.next();

                if ( modelField instanceof ModelAssociation )
                {
                    createAssociation( jClass, (ModelAssociation) modelField, jConstructorSource );
                }
                else
                {
                    createField( jClass, modelField );
                }
            }

            if ( !jConstructorSource.isEmpty() )
            {
                // Ironic that we are doing lazy init huh?
                JConstructor jConstructor = jClass.createConstructor();
                jConstructor.setSourceCode( jConstructorSource );
                jClass.addConstructor( jConstructor );
            }

            // ----------------------------------------------------------------------
            // equals() / hashCode() / toString()
            // ----------------------------------------------------------------------

            List identifierFields = modelClass.getIdentifierFields( getGeneratedVersion() );

            if ( identifierFields.size() != 0 )
            {
                JMethod equals = generateEquals( modelClass );

                jClass.addMethod( equals );

                JMethod hashCode = generateHashCode( modelClass );

                jClass.addMethod( hashCode );

                JMethod toString = generateToString( modelClass );

                jClass.addMethod( toString );
            }

            if ( modelClass.getCodeSegments( getGeneratedVersion() ) != null )
            {
                for ( Iterator iterator = modelClass.getCodeSegments( getGeneratedVersion() ).iterator();
                      iterator.hasNext(); )
                {
                    CodeSegment codeSegment = (CodeSegment) iterator.next();

                    jClass.addSourceCode( codeSegment.getCode() );
                }
            }

            ModelClassMetadata metadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

            if ( ( metadata != null ) && metadata.isRootElement() )
            {
                JField modelEncoding = new JField( new JType( "String" ), "modelEncoding" );
                modelEncoding.setInitString( "\"UTF-8\"" );
                jClass.addField( modelEncoding );

                // setModelEncoding(String) method
                JMethod setModelEncoding = new JMethod( "setModelEncoding" );
                setModelEncoding.addParameter( new JParameter( new JClass( "String" ), "modelEncoding" ) );

                setModelEncoding.getSourceCode().add( "this.modelEncoding = modelEncoding;" );

                setModelEncoding.getJDocComment().setComment( "Set an encoding used for reading/writing the model." );

                jClass.addMethod( setModelEncoding );

                // getModelEncoding() method
                JMethod getModelEncoding = new JMethod( "getModelEncoding", new JType( "String" ),
                                                        "the current encoding used when reading/writing this model" );

                getModelEncoding.getSourceCode().add( "return modelEncoding;" );

                jClass.addMethod( getModelEncoding );
            }

            jClass.print( sourceWriter );

            sourceWriter.close();
        }
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

        for ( Iterator j = modelClass.getIdentifierFields( getGeneratedVersion() ).iterator(); j.hasNext(); )
        {
            ModelField identifier = (ModelField) j.next();

            String name = identifier.getName();
            if ( "boolean".equals( identifier.getType() ) || "byte".equals( identifier.getType() )
                 || "char".equals( identifier.getType() ) || "double".equals( identifier.getType() )
                 || "float".equals( identifier.getType() ) || "int".equals( identifier.getType() )
                 || "short".equals( identifier.getType() ) || "long".equals( identifier.getType() ) )
            {
                sc.add( "result = result && " + name + " == that." + name + ";" );
            }
            else
            {
                name = "get" + capitalise( name ) + "()";
                sc.add( "result = result && ( " + name + " == null ? that." + name + " == null : " + name
                        + ".equals( that." + name + " ) );" );
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

    private JMethod generateToString( ModelClass modelClass )
    {
        JMethod toString = new JMethod( "toString", new JType( String.class.getName() ), null );

        List identifierFields = modelClass.getIdentifierFields( getGeneratedVersion() );

        JSourceCode sc = toString.getSourceCode();

        if ( identifierFields.size() == 0 )
        {
            sc.add( "return super.toString();" );

            return toString;
        }

        sc.add( "StringBuffer buf = new StringBuffer();" );

        sc.add( "" );

        for ( Iterator j = identifierFields.iterator(); j.hasNext(); )
        {
            ModelField identifier = (ModelField) j.next();

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

        List identifierFields = modelClass.getIdentifierFields( getGeneratedVersion() );

        JSourceCode sc = hashCode.getSourceCode();

        if ( identifierFields.size() == 0 )
        {
            sc.add( "return super.hashCode();" );

            return hashCode;
        }

        sc.add( "int result = 17;" );

        sc.add( "" );

        for ( Iterator j = identifierFields.iterator(); j.hasNext(); )
        {
            ModelField identifier = (ModelField) j.next();

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

    /**
     * Utility method that adds a period to the end of a string, if the last
     * non-whitespace character of the string is not a punctuation mark or an
     * end-tag.
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
            return "(int) ( Double.doubleToLongBits( " + identifier.getName() + " ) ^ ( Double.doubleToLongBits( " + identifier.getName() + " ) >>> 32 ) )";
        }
        else
        {
            return "( " + name + " != null ? " + name + ".hashCode() : 0 )";
        }
    }

    private JField createField( ModelField modelField )
    {
        JType type;

        String baseType = modelField.getType();
        if ( modelField.isArray() )
        {
            // remove [] at the end of the type
            baseType = baseType.substring( 0, baseType.length() - 2 );
        }

        if ( baseType.equals( "boolean" ) )
        {
            type = JType.BOOLEAN;
        }
        else if ( baseType.equals( "byte" ) )
        {
            type = JType.BYTE;
        }
        else if ( baseType.equals( "char" ) )
        {
            type = JType.CHAR;
        }
        else if ( baseType.equals( "double" ) )
        {
            type = JType.DOUBLE;
        }
        else if ( baseType.equals( "float" ) )
        {
            type = JType.FLOAT;
        }
        else if ( baseType.equals( "int" ) )
        {
            type = JType.INT;
        }
        else if ( baseType.equals( "short" ) )
        {
            type = JType.SHORT;
        }
        else if ( baseType.equals( "long" ) )
        {
            type = JType.LONG;
        }
        else if ( baseType.equals( "Date" ) )
        {
            type = new JClass( "java.util.Date" );
        }
        else if ( baseType.equals( "DOM" ) )
        {
            // TODO: maybe DOM is not how to specify it in the model, but just Object and markup Xpp3Dom for the Xpp3Reader?
            //   not sure how we'll treat it for the other sources, eg sql.
            type = new JClass( "Object" );
        }
        else if ( baseType.equals( "Content" ) )
        {
            type = new JClass( "String" );
        }
        else
        {
            type = new JClass( baseType );
        }

        if ( modelField.isArray() )
        {
            type = type.createArray();
        }

        JField field = new JField( type, modelField.getName() );

        if ( modelField.isModelVersionField() )
        {
            field.setInitString( "\"" + getGeneratedVersion() + "\"" );
        }

        if ( modelField.getDefaultValue() != null )
        {
            if ( modelField.getType().equals( "String" ) )
            {
                field.setInitString( "\"" + modelField.getDefaultValue() + "\"" );
            }
            else if ( modelField.getType().equals( "char" ) )
            {
                field.setInitString( "'" + modelField.getDefaultValue() + "'" );
            }
            else if ( modelField.getType().equals( "long" ) )
            {
                field.setInitString( modelField.getDefaultValue() + "L" );
            }
            else if ( modelField.getType().equals( "float" ) )
            {
                field.setInitString( modelField.getDefaultValue() + "f" );
            }
            else
            {
                field.setInitString( modelField.getDefaultValue() );
            }
        }

        if ( StringUtils.isNotEmpty( modelField.getDescription() ) )
        {
            field.setComment( appendPeriod( modelField.getDescription() ) );
        }

        return field;
    }

    private void createField( JClass jClass, ModelField modelField )
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

            JavaAssociationMetadata javaAssociationMetadata = (JavaAssociationMetadata) modelAssociation
                .getAssociationMetadata( JavaAssociationMetadata.ID );

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
            comment.append( StringUtils.lowercaseFirstLetter( modelField.getDescription() ) );
        }
        getter.getJDocComment().setComment( appendPeriod( comment.toString() ) );

        getter.getSourceCode().add( "return " + interfaceCast + "this." + field.getName() + ";" );

        return getter;
    }

    private JMethod createSetter( JField field, ModelField modelField )
    {
        String propertyName = capitalise( field.getName() );

        JMethod setter = new JMethod( "set" + propertyName );

        StringBuffer comment = new StringBuffer( "Set " );
        if ( StringUtils.isEmpty( modelField.getDescription() ) )
        {
            comment.append( "the " );
            comment.append( field.getName() );
            comment.append( " field" );
        }
        else
        {
            comment.append( StringUtils.lowercaseFirstLetter( modelField.getDescription() ) );
        }
        setter.getJDocComment().setComment( appendPeriod( comment.toString() ) );

        JType parameterType = getDesiredType( modelField, false );

        setter.addParameter( new JParameter( parameterType, field.getName() ) );

        JSourceCode sc = setter.getSourceCode();

        if ( modelField instanceof ModelAssociation )
        {
            ModelAssociation modelAssociation = (ModelAssociation) modelField;

            JavaAssociationMetadata javaAssociationMetadata = (JavaAssociationMetadata) modelAssociation
                .getAssociationMetadata( JavaAssociationMetadata.ID );

            boolean isOneMultiplicity = isBidirectionalAssociation( modelAssociation )
                 && ModelAssociation.ONE_MULTIPLICITY.equals( modelAssociation.getMultiplicity() );

            if ( isOneMultiplicity && javaAssociationMetadata.isGenerateBreak() )
            {
                sc.add( "if ( this." + field.getName() + " != null )" );

                sc.add( "{" );

                sc.indent();

                sc.add( "this." + field.getName() + ".break" + modelAssociation.getModelClass().getName() +
                    "Association( this );" );

                sc.unindent();

                sc.add( "}" );

                sc.add( "" );
            }

            String interfaceCast = "";

            if ( StringUtils.isNotEmpty( javaAssociationMetadata.getInterfaceName() )
                 && ModelAssociation.ONE_MULTIPLICITY.equals( modelAssociation.getMultiplicity() ) )
            {
                interfaceCast = "(" + field.getType().getName() + ") ";

                createClassCastAssertion( sc, modelField, "set" );
            }

            sc.add( "this." + field.getName() + " = " + interfaceCast + field.getName() + ";" );

            if ( isOneMultiplicity && javaAssociationMetadata.isGenerateCreate() )
            {
                sc.add( "" );

                sc.add( "if ( " + field.getName() + " != null )" );

                sc.add( "{" );

                sc.indent();

                sc.add( "this." + field.getName() + ".create" + modelAssociation.getModelClass().getName() +
                    "Association( this );" );

                sc.unindent();

                sc.add( "}" );
            }
        }
        else
        {
            sc.add( "this." + field.getName() + " = " + field.getName() + ";" );
        }

        return setter;
    }

    private void createClassCastAssertion( JSourceCode sc, ModelField modelField, String crudModifier )
    {
        String propertyName = capitalise( modelField.getName() );

        JField field = createField( modelField );
        String fieldName = field.getName();
        JType type = field.getType();

        if ( modelField instanceof ModelAssociation )
        {
            ModelAssociation modelAssociation = (ModelAssociation) modelField;
            JavaAssociationMetadata javaAssociationMetadata = (JavaAssociationMetadata) modelAssociation
                .getAssociationMetadata( JavaAssociationMetadata.ID );

            if ( StringUtils.isNotEmpty( javaAssociationMetadata.getInterfaceName() )
                 && ModelAssociation.ONE_MULTIPLICITY.equals( modelAssociation.getMultiplicity() ) )
            {
                type = new JClass( javaAssociationMetadata.getInterfaceName() );
            }
            else
            {
                fieldName = uncapitalise( modelAssociation.getTo() );
                type = new JClass( modelAssociation.getTo() );
            }
        }

        String instanceName = type.getName();

        // Add sane class cast exception message
        // When will sun ever fix this?

        sc.add( "if ( !(" + fieldName + " instanceof " + instanceName + ") )" );

        sc.add( "{" );

        sc.indent();

        sc.add( "throw new ClassCastException( \"" + modelField.getModelClass().getName() + "." + crudModifier
            + propertyName + "(" + fieldName + ") parameter must be instanceof \" + " + instanceName +
            ".class.getName() );" );

        sc.unindent();

        sc.add( "}" );
    }

    private void createAssociation( JClass jClass, ModelAssociation modelAssociation, JSourceCode jConstructorSource )
        throws ModelloException
    {
        JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) modelAssociation.getMetadata( JavaFieldMetadata.ID );

        JavaAssociationMetadata javaAssociationMetadata =
            (JavaAssociationMetadata) modelAssociation.getAssociationMetadata( JavaAssociationMetadata.ID );

        if ( !JavaAssociationMetadata.INIT_TYPES.contains( javaAssociationMetadata.getInitializationMode() ) )
        {
            throw new ModelloException( "The Java Modello Generator cannot use '"
                + javaAssociationMetadata.getInitializationMode() + "' as a <association java.init=\""
                + javaAssociationMetadata.getInitializationMode() + "\"> "
                + "value, the only the following are acceptable " + JavaAssociationMetadata.INIT_TYPES );
        }

        if ( ModelAssociation.MANY_MULTIPLICITY.equals( modelAssociation.getMultiplicity() ) )
        {
            JType type = new JClass( modelAssociation.getType() );

            JField jField = new JField( type, modelAssociation.getName() );

            if ( !isEmpty( modelAssociation.getComment() ) )
            {
                jField.setComment( modelAssociation.getComment() );
            }

            if ( StringUtils.equals( javaAssociationMetadata.getInitializationMode(),
                                     JavaAssociationMetadata.FIELD_INIT ) )
            {
                jField.setInitString( modelAssociation.getDefaultValue() );
            }

            if ( StringUtils.equals( javaAssociationMetadata.getInitializationMode(),
                                     JavaAssociationMetadata.CONSTRUCTOR_INIT ) )
            {
                jConstructorSource.add( "this." + jField.getName() + " = " + modelAssociation.getDefaultValue() + ";" );
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

                    sc.add( "this." + jField.getName() + " = " + modelAssociation.getDefaultValue() + ";" );

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

            if ( javaFieldMetadata.isAdder() )
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
            if ( javaAssociationMetadata.isGenerateCreate() )
            {
                createCreateAssociation( jClass, modelAssociation );
            }

            if ( javaAssociationMetadata.isGenerateBreak() )
            {
                createBreakAssociation( jClass, modelAssociation );
            }
        }
    }

    private void createCreateAssociation( JClass jClass, ModelAssociation modelAssociation )
    {
        JMethod createMethod = new JMethod( "create" + modelAssociation.getTo() + "Association" );

        JavaAssociationMetadata javaAssociationMetadata =
            (JavaAssociationMetadata) modelAssociation.getAssociationMetadata( JavaAssociationMetadata.ID );

        createMethod.addParameter(
            new JParameter( new JClass( modelAssociation.getTo() ), uncapitalise( modelAssociation.getTo() ) ) );

        // TODO: remove after tested
//            createMethod.addException( new JClass( "Exception" ) );

        JSourceCode sc = createMethod.getSourceCode();

        if ( ModelAssociation.ONE_MULTIPLICITY.equals( modelAssociation.getMultiplicity() ) )
        {
            if ( javaAssociationMetadata.isGenerateBreak() )
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

            sc.add( "if ( get" + capitalise( modelAssociation.getName() ) + "().contains("
                    + uncapitalise( modelAssociation.getTo() ) + ") )" );

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
//            breakMethod.addException( new JClass( "Exception" ) );

        sc = breakMethod.getSourceCode();

        if ( ModelAssociation.ONE_MULTIPLICITY.equals( modelAssociation.getMultiplicity() ) )
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
            sc.add( "if ( ! get" + capitalise( modelAssociation.getName() ) + "().contains( "
                    + uncapitalise( modelAssociation.getTo() ) + " ) )" );

            sc.add( "{" );

            sc.indent();

            sc.add( "throw new IllegalStateException( \"" + uncapitalise( modelAssociation.getTo() )
                    + " isn't associated.\" );" );

            sc.unindent();

            sc.add( "}" );

            sc.add( "" );

            sc.add( "get" + capitalise( modelAssociation.getName() ) + "().remove( "
                    + uncapitalise( modelAssociation.getTo() ) + " );" );
        }

        jClass.addMethod( breakMethod );
    }

    private void createAdder( ModelAssociation modelAssociation, JClass jClass )
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

        if ( modelAssociation.getType().equals( ModelDefault.PROPERTIES )
             || modelAssociation.getType().equals( ModelDefault.MAP ) )
        {
            JMethod adder = new JMethod( "add" + capitalise( singular( fieldName ) ) );

            if ( modelAssociation.getType().equals( ModelDefault.MAP ) )
            {
                adder.addParameter( new JParameter( new JClass( "Object" ), "key" ) );
            }
            else
            {
                adder.addParameter( new JParameter( new JClass( "String" ), "key" ) );
            }

            adder.addParameter( new JParameter( new JClass( modelAssociation.getTo() ), "value" ) );

            adder.getSourceCode().add( "get" + capitalise( fieldName ) + "().put( key, value );" );

            jClass.addMethod( adder );
        }
        else
        {
            JMethod adder = new JMethod( "add" + singular( capitalise( fieldName ) ) );

            adder.addParameter( new JParameter( addType, parameterName ) );

            createClassCastAssertion( adder.getSourceCode(), modelAssociation, "add" );

            adder.getSourceCode().add(
                "get" + capitalise( fieldName ) + "().add( " + implementationParameterName + " );" );

            if ( bidirectionalAssociation && javaAssociationMetadata.isGenerateCreate() )
            {
                // TODO: remove after tested
//                adder.addException( new JClass( "Exception" ) );

                adder.getSourceCode().add( implementationParameterName + ".create"
                    + modelAssociation.getModelClass().getName() + "Association( this );" );
            }

            jClass.addMethod( adder );

            JMethod remover = new JMethod( "remove" + singular( capitalise( fieldName ) ) );

            remover.addParameter( new JParameter( addType, parameterName ) );

            createClassCastAssertion( remover.getSourceCode(), modelAssociation, "remove" );

            if ( bidirectionalAssociation && javaAssociationMetadata.isGenerateBreak() )
            {
                // TODO: remove after tested
//                remover.addException( new JClass( "Exception" ) );

                remover.getSourceCode().add(
                    parameterName + ".break" + modelAssociation.getModelClass().getName() + "Association( this );" );
            }

            remover.getSourceCode().add(
                "get" + capitalise( fieldName ) + "().remove( " + implementationParameterName + " );" );

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

        for ( Iterator j = toClass.getFields( getGeneratedVersion() ).iterator(); j.hasNext(); )
        {
            ModelField modelField = (ModelField) j.next();

            if ( !( modelField instanceof ModelAssociation ) )
            {
                continue;
            }

            ModelAssociation modelAssociation = (ModelAssociation) modelField;

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
    {
        JField field = createField( modelField );
        JType type = field.getType();

        if ( modelField instanceof ModelAssociation )
        {
            ModelAssociation modelAssociation = (ModelAssociation) modelField;
            JavaAssociationMetadata javaAssociationMetadata = (JavaAssociationMetadata) modelAssociation
                .getAssociationMetadata( JavaAssociationMetadata.ID );

            if ( StringUtils.isNotEmpty( javaAssociationMetadata.getInterfaceName() )
                 && ModelAssociation.ONE_MULTIPLICITY.equals( modelAssociation.getMultiplicity() ) )
            {
                type = new JClass( javaAssociationMetadata.getInterfaceName() );
            }
            else if ( useTo )
            {
                type = new JClass( modelAssociation.getTo() );
            }
        }

        return type;
    }
}
