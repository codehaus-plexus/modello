package org.codehaus.modello.plugin.snakeyaml;

/*
 * Copyright (c) 2004-2013, Codehaus.org
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
import java.util.List;
import java.util.Properties;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelDefault;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.java.javasource.JClass;
import org.codehaus.modello.plugin.java.javasource.JMethod;
import org.codehaus.modello.plugin.java.javasource.JParameter;
import org.codehaus.modello.plugin.java.javasource.JSourceCode;
import org.codehaus.modello.plugin.java.javasource.JSourceWriter;
import org.codehaus.modello.plugin.java.javasource.JType;
import org.codehaus.modello.plugin.java.metadata.JavaClassMetadata;
import org.codehaus.modello.plugin.java.metadata.JavaFieldMetadata;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlAssociationMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlClassMetadata;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:simonetripodi@apache.org">Simone Tripodi</a>
 */
public class SnakeYamlReaderGenerator
    extends AbstractSnakeYamlGenerator
{

    private static final String SOURCE_PARAM = "source";

    private static final String LOCATION_VAR = "_location";

    private ModelClass locationTracker;

    private String locationField;

    private ModelClass sourceTracker;

    private String trackingArgs;

    protected boolean isLocationTracking()
    {
        return false;
    }

    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        locationTracker = sourceTracker = null;
        trackingArgs = locationField = "";

        if ( isLocationTracking() )
        {
            locationTracker = model.getLocationTracker( getGeneratedVersion() );
            if ( locationTracker == null )
            {
                throw new ModelloException( "No model class has been marked as location tracker"
                                                + " via the attribute locationTracker=\"locations\""
                                                + ", cannot generate extended reader." );
            }

            locationField =
                ( (ModelClassMetadata) locationTracker.getMetadata( ModelClassMetadata.ID ) ).getLocationTracker();

            sourceTracker = model.getSourceTracker( getGeneratedVersion() );

            if ( sourceTracker != null )
            {
                trackingArgs += ", " + SOURCE_PARAM;
            }
        }

        try
        {
            generateSnakeYamlReader();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating SnakeYaml Reader.", ex );
        }
    }

    private void writeAllClassesReaders( Model objectModel, JClass jClass )
    {
        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        for ( ModelClass clazz : getClasses( objectModel ) )
        {
            if ( isTrackingSupport( clazz ) )
            {
                continue;
            }

            writeClassReaders( clazz, jClass, root.getName().equals( clazz.getName() ) );
        }
    }

    private void writeClassReaders( ModelClass modelClass, JClass jClass, boolean rootElement )
    {
        JavaClassMetadata javaClassMetadata =
            (JavaClassMetadata) modelClass.getMetadata( JavaClassMetadata.class.getName() );

        // Skip abstract classes, no way to parse them out into objects
        if ( javaClassMetadata.isAbstract() )
        {
            return;
        }

        XmlClassMetadata xmlClassMetadata = (XmlClassMetadata) modelClass.getMetadata( XmlClassMetadata.ID );
        if ( !rootElement && !xmlClassMetadata.isStandaloneRead() )
        {
            return;
        }

        String className = modelClass.getName();

        String capClassName = capitalise( className );

        String readerMethodName = "read";
        if ( !rootElement )
        {
            readerMethodName += capClassName;
        }

        // ----------------------------------------------------------------------
        // Write the read(Parser) method which will do the unmarshalling.
        // ----------------------------------------------------------------------

        JMethod unmarshall = new JMethod( readerMethodName, new JClass( className ), null );
        unmarshall.getModifiers().makePrivate();

        unmarshall.addParameter( new JParameter( new JClass( "Parser" ), "parser" ) );
        unmarshall.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );
        addTrackingParameters( unmarshall );

        unmarshall.addException( new JClass( "IOException" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        String variableName = uncapitalise( className );

        sc.add( "Event event;" );

        sc.add( "if ( !( event = parser.getEvent() ).is( Event.ID.StreamStart ) )" );
        sc.add( "{" );
        sc.addIndented( "throw new ParserException( \"Expected Stream Start event\", event.getStartMark(), \"\", null );" );
        sc.add( "}" );

        sc.add( "if ( !( event = parser.getEvent() ).is( Event.ID.DocumentStart ) )" );
        sc.add( "{" );
        sc.addIndented( "throw new ParserException( \"Expected Document Start event\", event.getStartMark(), \"\", null );" );
        sc.add( "}" );

        sc.add( "" );

        sc.add(
            className + ' ' + variableName + " = parse" + capClassName + "( parser, strict" + trackingArgs + " );" );

        if ( rootElement )
        {
            // TODO
            // sc.add( variableName + ".setModelEncoding( parser.getInputEncoding() );" );
        }

        sc.add( "" );

        sc.add( "if ( !( event = parser.getEvent() ).is( Event.ID.DocumentEnd ) )" );
        sc.add( "{" );
        sc.addIndented( "throw new ParserException( \"Expected Document End event\", event.getStartMark(), \"\", null );" );
        sc.add( "}" );

        sc.add( "if ( !( event = parser.getEvent() ).is( Event.ID.StreamEnd ) )" );
        sc.add( "{" );
        sc.addIndented( "throw new ParserException( \"Expected Stream End event\", event.getStartMark(), \"\", null );" );
        sc.add( "}" );

        sc.add( "" );

        sc.add( "return " + variableName + ';' );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the read(Reader[,boolean]) methods which will do the unmarshalling.
        // ----------------------------------------------------------------------

        unmarshall = new JMethod( readerMethodName, new JClass( className ), null );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );
        unmarshall.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );
        addTrackingParameters( unmarshall );

        unmarshall.addException( new JClass( "IOException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "Parser parser = new ParserImpl( new StreamReader( reader ) );" );

        sc.add( "return " + readerMethodName + "( parser, strict );" );

        jClass.addMethod( unmarshall );

        unmarshall = new JMethod( readerMethodName, new JClass( className ), null );

        unmarshall.addParameter( new JParameter( new JClass( "Reader" ), "reader" ) );

        unmarshall.addException( new JClass( "IOException" ) );

        sc = unmarshall.getSourceCode();
        sc.add( "return " + readerMethodName + "( reader, true );" );

        jClass.addMethod( unmarshall );

        // ----------------------------------------------------------------------
        // Write the read(InputStream[,boolean]) methods which will do the unmarshalling.
        // ----------------------------------------------------------------------

        unmarshall = new JMethod( readerMethodName, new JClass( className ), null );

        unmarshall.addParameter( new JParameter( new JClass( "InputStream" ), "in" ) );
        unmarshall.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );
        addTrackingParameters( unmarshall );

        unmarshall.addException( new JClass( "IOException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "return " + readerMethodName + "( new InputStreamReader( in ), strict" + trackingArgs + " );" );

        jClass.addMethod( unmarshall );unmarshall = new JMethod( readerMethodName, new JClass( className ), null );

        unmarshall.addParameter( new JParameter( new JClass( "InputStream" ), "in" ) );

        unmarshall.addException( new JClass( "IOException" ) );

        sc = unmarshall.getSourceCode();

        sc.add( "return " + readerMethodName + "( in, true );" );

        jClass.addMethod( unmarshall );

        // --------------------------------------------------------------------
    }

    private void generateSnakeYamlReader()
        throws ModelloException, IOException
    {
        Model objectModel = getModel();

        String packageName =
            objectModel.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() ) + ".io.snakeyaml";

        String unmarshallerName = getFileName( "SnakeYamlReader" + ( isLocationTracking() ? "Ex" : "" ) );

        JSourceWriter sourceWriter = newJSourceWriter( packageName, unmarshallerName );

        JClass jClass = new JClass( packageName + '.' + unmarshallerName );
        initHeader( jClass );
        suppressAllWarnings( objectModel, jClass );

        jClass.addImport( "org.yaml.snakeyaml.events.DocumentEndEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.DocumentStartEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.Event" );
        jClass.addImport( "org.yaml.snakeyaml.events.ImplicitTuple" );
        jClass.addImport( "org.yaml.snakeyaml.events.MappingEndEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.MappingStartEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.ScalarEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.SequenceEndEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.SequenceStartEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.StreamEndEvent" );
        jClass.addImport( "org.yaml.snakeyaml.events.StreamStartEvent" );
        jClass.addImport( "org.yaml.snakeyaml.parser.Parser" );
        jClass.addImport( "org.yaml.snakeyaml.parser.ParserException" );
        jClass.addImport( "org.yaml.snakeyaml.parser.ParserImpl" );
        jClass.addImport( "org.yaml.snakeyaml.reader.StreamReader" );
        jClass.addImport( "java.io.InputStream" );
        jClass.addImport( "java.io.InputStreamReader" );
        jClass.addImport( "java.io.IOException" );
        jClass.addImport( "java.io.Reader" );
        jClass.addImport( "java.text.DateFormat" );
        jClass.addImport( "java.util.Set" );
        jClass.addImport( "java.util.HashSet" );

        addModelImports( jClass, null );

        // ----------------------------------------------------------------------
        // Write the class parsers
        // ----------------------------------------------------------------------

        writeAllClassesParser( objectModel, jClass );

        // ----------------------------------------------------------------------
        // Write the class readers
        // ----------------------------------------------------------------------

        writeAllClassesReaders( objectModel, jClass );

        // ----------------------------------------------------------------------
        // Write helpers
        // ----------------------------------------------------------------------

        writeHelpers( jClass );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        jClass.print( sourceWriter );

        sourceWriter.close();
    }

    private void writeAllClassesParser( Model objectModel, JClass jClass )
    {
        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        for ( ModelClass clazz : getClasses( objectModel ) )
        {
            if ( isTrackingSupport( clazz ) )
            {
                continue;
            }

            writeClassParser( clazz, jClass, root.getName().equals( clazz.getName() ) );
        }
    }

    private void writeClassParser( ModelClass modelClass, JClass jClass, boolean rootElement )
    {
        JavaClassMetadata javaClassMetadata =
            (JavaClassMetadata) modelClass.getMetadata( JavaClassMetadata.class.getName() );

        // Skip abstract classes, no way to parse them out into objects
        if ( javaClassMetadata.isAbstract() )
        {
            return;
        }

        String className = modelClass.getName();

        String capClassName = capitalise( className );

        String uncapClassName = uncapitalise( className );

        JMethod unmarshall = new JMethod( "parse" + capClassName, new JClass( className ), null );
        unmarshall.getModifiers().makePrivate();

        unmarshall.addParameter( new JParameter( new JClass( "Parser" ), "parser" ) );
        unmarshall.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );
        addTrackingParameters( unmarshall );

        unmarshall.addException( new JClass( "IOException" ) );

        JSourceCode sc = unmarshall.getSourceCode();

        sc.add( "Event event = parser.getEvent();" );

        sc.add( "" );

        sc.add( "if ( !event.is( Event.ID.MappingStart ) )" );
        sc.add( "{" );
        sc.addIndented( "throw new ParserException( \"Expected '"
                        + className
                        + "' data to start with a Mapping\", event.getStartMark(), \"\", null );" );
        sc.add( "}" );

        sc.add( "" );

        sc.add( className + " " + uncapClassName + " = new " + className + "();" );

        if ( locationTracker != null )
        {
            sc.add( locationTracker.getName() + " " + LOCATION_VAR + ";" );
            writeNewSetLocation( "\"\"", uncapClassName, null, sc );
        }

        ModelField contentField = null;

        List<ModelField> modelFields = getFieldsForXml( modelClass, getGeneratedVersion() );

        // read all XML attributes first
        contentField = writeClassAttributesParser( modelFields, uncapClassName, rootElement );

        // then read content, either content field or elements
        if ( contentField != null )
        {
            writePrimitiveField( contentField, contentField.getType(), uncapClassName, uncapClassName, "\"\"",
                                 "set" + capitalise( contentField.getName() ), sc, false );
        }
        else
        {
            //Write other fields

            sc.add( "Set<String> parsed = new HashSet<String>();" );

            sc.add( "" );

            sc.add( "while ( !( event = parser.getEvent() ).is( Event.ID.MappingEnd ) )" );

            sc.add( "{" );
            sc.indent();

            boolean addElse = false;

            for ( ModelField field : modelFields )
            {
                XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

                processField( field, xmlFieldMetadata, addElse, sc, uncapClassName, jClass );

                addElse = true;
            }

            if ( addElse )
            {
                sc.add( "else" );

                sc.add( "{" );
                sc.indent();
            }

            sc.add( "checkUnknownElement( event, parser, strict );" );

            if ( addElse )
            {
                sc.unindent();
                sc.add( "}" );
            }

            sc.unindent();
            sc.add( "}" );
        }

        sc.add( "return " + uncapClassName + ";" );

        jClass.addMethod( unmarshall );
    }

    private ModelField writeClassAttributesParser( List<ModelField> modelFields, String objectName, boolean rootElement )
    {
        ModelField contentField = null;

        for ( ModelField field : modelFields )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            // TODO check if we have already one with this type and throws Exception
            if ( xmlFieldMetadata.isContent() )
            {
                contentField = field;
            }
        }

        return contentField;
    }

    /**
     * Generate code to process a field represented as an XML element.
     *
     * @param field            the field to process
     * @param xmlFieldMetadata its XML metadata
     * @param addElse          add an <code>else</code> statement before generating a new <code>if</code>
     * @param sc               the method source code to add to
     * @param objectName       the object name in the source
     * @param jClass           the generated class source file
     */
    private void processField( ModelField field, XmlFieldMetadata xmlFieldMetadata, boolean addElse, JSourceCode sc,
                               String objectName, JClass jClass )
    {
        String fieldTagName = resolveTagName( field, xmlFieldMetadata );

        String capFieldName = capitalise( field.getName() );

        String singularName = singular( field.getName() );

        String alias;
        if ( StringUtils.isEmpty( field.getAlias() ) )
        {
            alias = "null";
        }
        else
        {
            alias = "\"" + field.getAlias() + "\"";
        }

        String tagComparison =
            ( addElse ? "else " : "" ) + "if ( checkFieldWithDuplicate( event, \"" + fieldTagName + "\", " + alias
                + ", parsed ) )";

        if ( !( field instanceof ModelAssociation ) )
        { // model field
            sc.add( tagComparison );

            sc.add( "{" );
            sc.indent();

            writePrimitiveField( field, field.getType(), objectName, objectName, "\"" + field.getName() + "\"",
                                 "set" + capFieldName, sc, false );

            sc.unindent();
            sc.add( "}" );
        }
        else
        { // model association
            ModelAssociation association = (ModelAssociation) field;

            String associationName = association.getName();

            if ( association.isOneMultiplicity() )
            {
                sc.add( tagComparison );

                sc.add( "{" );
                sc.indent();

                // sc.add( "// consume current key" );
                // sc.add( "parser.getEvent();" );
                sc.add( objectName
                        + ".set"
                        + capFieldName
                        + "( parse"
                        + association.getTo()
                        + "( parser, strict"
                        + trackingArgs
                        + " ) );" );

                sc.unindent();
                sc.add( "}" );
            }
            else
            {
                //MANY_MULTIPLICITY

                XmlAssociationMetadata xmlAssociationMetadata =
                    (XmlAssociationMetadata) association.getAssociationMetadata( XmlAssociationMetadata.ID );

                String type = association.getType();

                if ( ModelDefault.LIST.equals( type ) || ModelDefault.SET.equals( type ) )
                {
                    boolean inModel = isClassInModel( association.getTo(), field.getModelClass().getModel() );

                    sc.add( ( addElse ? "else " : "" )
                            + "if ( checkFieldWithDuplicate( event, \""
                            + fieldTagName
                            + "\", "
                            + alias
                            + ", parsed ) )" );

                    sc.add( "{" );
                    sc.indent();

                    sc.add( "if ( !parser.getEvent().is( Event.ID.SequenceStart ) )" );
                    sc.add( "{" );
                    sc.addIndented( "throw new ParserException( \"Expected '"
                                    + field.getName()
                                    + "' data to start with a Sequence\", event.getStartMark(), \"\", null );" );
                    sc.add( "}" );

                    JavaFieldMetadata javaFieldMetadata = (JavaFieldMetadata) association.getMetadata( JavaFieldMetadata.ID );

                    String adder;

                    if ( javaFieldMetadata.isGetter() && javaFieldMetadata.isSetter() )
                    {
                        sc.add( type + " " + associationName + " = " + objectName + ".get" + capFieldName + "();" );

                        sc.add( "if ( " + associationName + " == null )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( associationName + " = " + association.getDefaultValue() + ";" );

                        sc.add( objectName + ".set" + capFieldName + "( " + associationName + " );" );

                        sc.unindent();
                        sc.add( "}" );

                        adder = associationName + ".add";
                    }
                    else
                    {
                        adder = objectName + ".add" + association.getTo();
                    }

                    if ( !inModel && locationTracker != null )
                    {
                        sc.add( locationTracker.getName() + " " + LOCATION_VAR + "s = " + objectName + ".get"
                                    + capitalise( singular( locationField ) ) + "( \"" + field.getName()
                                    + "\" );" );
                        sc.add( "if ( " + LOCATION_VAR + "s == null )" );
                        sc.add( "{" );
                        sc.indent();
                        writeNewSetLocation( field, objectName, LOCATION_VAR + "s", sc );
                        sc.unindent();
                        sc.add( "}" );
                    }

                    if ( inModel )
                    {
                        sc.add( "while ( !parser.peekEvent().is( Event.ID.SequenceEnd ) )" );
                        sc.add( "{" );

                        sc.addIndented( adder + "( parse" + association.getTo() + "( parser, strict" + trackingArgs + " ) );" );

                        sc.add( "}" );

                        sc.add( "parser.getEvent();" );
                    }
                    else
                    {
                        String key;
                        if ( ModelDefault.SET.equals( type ) )
                        {
                            key = "?";
                        }
                        else
                        {
                            key = ( useJava5 ? "Integer.valueOf" : "new java.lang.Integer" ) + "( " + associationName
                                + ".size() )";
                        }
                        writePrimitiveField( association, association.getTo(), associationName, LOCATION_VAR + "s", key,
                                             "add", sc, false );
                    }

                    sc.unindent();
                    sc.add( "}" );
                }
                else
                {
                    //Map or Properties

                    sc.add( tagComparison );

                    sc.add( "{" );
                    sc.indent();

                    if ( locationTracker != null )
                    {
                        sc.add( locationTracker.getName() + " " + LOCATION_VAR + "s;" );
                        writeNewSetLocation( field, objectName, LOCATION_VAR + "s", sc );
                    }

                    if ( xmlAssociationMetadata.isMapExplode() )
                    {
                        sc.add( "if ( !parser.getEvent().is( Event.ID.SequenceStart ) )" );
                        sc.add( "{" );
                        sc.addIndented( "throw new ParserException( \"Expected '"
                                        + field.getName()
                                        + "' data to start with a Sequence\", event.getStartMark(), \"\", null );" );
                        sc.add( "}" );

                        sc.add( "while ( !parser.peekEvent().is( Event.ID.SequenceEnd ) )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "event = parser.getEvent();" );

                        sc.add( "" );

                        sc.add( "if ( !event.is( Event.ID.MappingStart ) )" );
                        sc.add( "{" );
                        sc.addIndented( "throw new ParserException( \"Expected '"
                                        + fieldTagName
                                        + "' item data to start with a Mapping\", event.getStartMark(), \"\", null );" );
                        sc.add( "}" );

                        sc.add( "String key = null;" );

                        sc.add( "String value = null;" );

                        sc.add( "Set<String> parsedPropertiesElements = new HashSet<String>();" );

                        sc.add( "while ( !( event = parser.getEvent() ).is( Event.ID.MappingEnd ) )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "if ( checkFieldWithDuplicate( event, \"key\", \"\", parsedPropertiesElements ) )" );
                        sc.add( "{" );

                        String parserGetter = "( (ScalarEvent) parser.getEvent() ).getValue()";
                        if ( xmlFieldMetadata.isTrim() )
                        {
                            parserGetter = "getTrimmedValue( " + parserGetter + " )";
                        }

                        sc.addIndented( "key = " + parserGetter + ";" );

                        sc.add( "}" );
                        sc.add( "else if ( checkFieldWithDuplicate( event, \"value\", \"\", parsedPropertiesElements ) )" );
                        sc.add( "{" );

                        parserGetter = "( (ScalarEvent) parser.getEvent() ).getValue()";
                        if ( xmlFieldMetadata.isTrim() )
                        {
                            parserGetter = "getTrimmedValue( " + parserGetter + " )";
                        }

                        sc.addIndented( "value = " + parserGetter + ";" );

                        sc.add( "}" );

                        sc.add( "else" );

                        sc.add( "{" );

                        sc.addIndented( "checkUnknownElement( event, parser, strict );" );

                        sc.add( "}" );

                        sc.unindent();
                        sc.add( "}" );

                        sc.add( objectName + ".add" + capitalise( singularName ) + "( key, value );" );

                        sc.unindent();
                        sc.add( "}" );
                    }
                    else
                    {
                        //INLINE Mode

                        sc.add( "if ( !parser.getEvent().is( Event.ID.MappingStart ) )" );
                        sc.add( "{" );
                        sc.addIndented( "throw new ParserException( \"Expected '"
                                        + field.getName()
                                        + "' data to start with a Mapping\", event.getStartMark(), \"\", null );" );
                        sc.add( "}" );

                        sc.add( "while ( !parser.peekEvent().is( Event.ID.MappingEnd ) )" );

                        sc.add( "{" );
                        sc.indent();

                        sc.add( "String key = ( (ScalarEvent) parser.getEvent() ).getValue();" );

                        writeNewSetLocation( "key", LOCATION_VAR + "s", null, sc );

                        sc.add(
                            "String value = ( (ScalarEvent) parser.getEvent() ).getValue()" + ( xmlFieldMetadata.isTrim() ? ".trim()" : "" ) + ";" );

                        sc.add( objectName + ".add" + capitalise( singularName ) + "( key, value );" );

                        sc.unindent();
                        sc.add( "}" );
                    }

                    sc.add( "parser.getEvent();" );

                    sc.unindent();
                    sc.add( "}" );
                }
            }
        }
    }

    private void writeHelpers( JClass jClass )
    {
        JMethod method = new JMethod( "getTrimmedValue", new JClass( "String" ), null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.addIndented( "s = s.trim();" );
        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "getRequiredAttributeValue", new JClass( "String" ), null );
        method.addException( new JClass( "ParserException" ) );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "Parser" ), "parser" ) );
        method.addParameter( new JParameter( JClass.BOOLEAN, "strict" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s == null )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented(
            "throw new ParserException( \"Missing required value for attribute '\" + attribute + \"'\", parser.peekEvent().getStartMark(), \"\", null );" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "checkFieldWithDuplicate", JType.BOOLEAN, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "Event" ), "event" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "alias" ) );
        method.addParameter( new JParameter( new JClass( "Set" ), "parsed" ) );
        method.addException( new JClass( "IOException" ) );

        sc = method.getSourceCode();

        sc.add( "String currentName = ( (ScalarEvent) event ).getValue();" );

        sc.add( "" );

        sc.add( "if ( !( currentName.equals( tagName ) || currentName.equals( alias ) ) )" );

        sc.add( "{" );
        sc.addIndented( "return false;" );
        sc.add( "}" );

        sc.add( "if ( !parsed.add( tagName ) )" );

        sc.add( "{" );
        sc.addIndented( "throw new ParserException( \"Duplicated tag: '\" + tagName + \"'\", event.getStartMark(), \"\", null );" );
        sc.add( "}" );

        sc.add( "return true;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "checkUnknownElement", null, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "Event" ), "event" ) );
        method.addParameter( new JParameter( new JClass( "Parser" ), "parser" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );
        method.addException( new JClass( "IOException" ) );

        sc = method.getSourceCode();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented(
            "throw new ParserException( \"Unrecognised tag: '\" + ( (ScalarEvent) event ).getValue() + \"'\", event.getStartMark(), \"\", null );" );
        sc.add( "}" );

        sc.add( "" );

        sc.add( "for ( int unrecognizedTagCount = 1; unrecognizedTagCount > 0; )" );
        sc.add( "{" );
        sc.indent();
        sc.add( "event = parser.getEvent();" );
        sc.add( "if ( event.is( Event.ID.MappingStart ) )" );
        sc.add( "{" );
        sc.addIndented( "unrecognizedTagCount++;" );
        sc.add( "}" );
        sc.add( "else if ( event.is( Event.ID.MappingEnd ) )" );
        sc.add( "{" );
        sc.addIndented( "unrecognizedTagCount--;" );
        sc.add( "}" );
        sc.unindent();
        sc.add( "}" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "checkUnknownAttribute", null, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "Parser" ), "parser" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "tagName" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );
        method.addException( new JClass( "IOException" ) );

        sc = method.getSourceCode();

        if ( strictXmlAttributes )
        {
            sc.add(
                "// strictXmlAttributes = true for model: if strict == true, not only elements are checked but attributes too" );
            sc.add( "if ( strict )" );

            sc.add( "{" );
            sc.addIndented(
                "throw new ParserException( \"\", parser.peekEvent().getStartMark(), \"Unknown attribute '\" + attribute + \"' for tag '\" + tagName + \"'\", parser.peekEvent().getEndMark() );" );
            sc.add( "}" );
        }
        else
        {
            sc.add(
                "// strictXmlAttributes = false for model: always ignore unknown XML attribute, even if strict == true" );
        }

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "getBooleanValue", JType.BOOLEAN, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.addIndented( "return Boolean.valueOf( s ).booleanValue();" );
        sc.add( "}" );

        sc.add( "return false;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "getCharacterValue", JType.CHAR, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.addIndented( "return s.charAt( 0 );" );
        sc.add( "}" );

        sc.add( "return 0;" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getIntegerValue", JType.INT, "Integer.valueOf( s ).intValue()", "an integer" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getShortValue", JType.SHORT, "Short.valueOf( s ).shortValue()",
                                       "a short integer" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getByteValue", JType.BYTE, "Byte.valueOf( s ).byteValue()", "a byte" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getLongValue", JType.LONG, "Long.valueOf( s ).longValue()", "a long integer" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getFloatValue", JType.FLOAT, "Float.valueOf( s ).floatValue()",
                                       "a floating point number" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = convertNumericalType( "getDoubleValue", JType.DOUBLE, "Double.valueOf( s ).doubleValue()",
                                       "a floating point number" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "getDateValue", new JClass( "java.util.Date" ), null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "dateFormat" ) );
        method.addParameter( new JParameter( new JClass( "Event" ), "event" ) );

        writeDateParsingHelper( method.getSourceCode(), "new ParserException( \"\", event.getStartMark(), e.getMessage(), event.getEndMark() )" );

        jClass.addMethod( method );

        // --------------------------------------------------------------------

        method = new JMethod( "getDefaultValue", new JClass( "String" ), null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "v" ) );

        sc = method.getSourceCode();

        sc.add( "if ( s == null )" );

        sc.add( "{" );
        sc.addIndented( "s = v;" );
        sc.add( "}" );

        sc.add( "return s;" );

        jClass.addMethod( method );
    }

    private void addTrackingParameters( JMethod method )
    {
        if ( sourceTracker != null )
        {
            method.addParameter( new JParameter( new JClass( sourceTracker.getName() ), SOURCE_PARAM ) );
        }
    }

    private void writeNewSetLocation( ModelField field, String objectName, String trackerVariable, JSourceCode sc )
    {
        writeNewSetLocation( "\"" + field.getName() + "\"", objectName, trackerVariable, sc );
    }

    private void writeNewSetLocation( String key, String objectName, String trackerVariable, JSourceCode sc )
    {
        writeNewLocation( trackerVariable, sc );
        writeSetLocation( key, objectName, trackerVariable, sc );
    }

    private void writeNewLocation( String trackerVariable, JSourceCode sc )
    {
        if ( locationTracker == null )
        {
            return;
        }

        String constr = "new " + locationTracker.getName() + "( parser.getLineNumber(), parser.getColumnNumber()";
        constr += ( sourceTracker != null ) ? ", " + SOURCE_PARAM : "";
        constr += " )";

        sc.add( ( ( trackerVariable != null ) ? trackerVariable : LOCATION_VAR ) + " = " + constr + ";" );
    }

    private void writeSetLocation( String key, String objectName, String trackerVariable, JSourceCode sc )
    {
        if ( locationTracker == null )
        {
            return;
        }

        String variable = ( trackerVariable != null ) ? trackerVariable : LOCATION_VAR;

        sc.add( objectName + ".set" + capitalise( singular( locationField ) ) + "( " + key + ", " + variable + " );" );
    }

    /**
         * Write code to set a primitive field with a value got from the parser, with appropriate default value, trimming
         * and required check logic.
         *
         * @param field the model field to set (either XML attribute or element)
         * @param type the type of the value read from XML
         * @param objectName the object name in source
         * @param setterName the setter method name
         * @param sc the source code to add to
         */
        private void writePrimitiveField( ModelField field, String type, String objectName, String locatorName,
                                          String locationKey, String setterName, JSourceCode sc, boolean wrappedItem )
        {
            XmlFieldMetadata xmlFieldMetadata = (XmlFieldMetadata) field.getMetadata( XmlFieldMetadata.ID );

            String tagName = resolveTagName( field, xmlFieldMetadata );

            String parserGetter = "( (ScalarEvent) parser.getEvent() ).getValue()";

    /* TODO:
            if ( xmlFieldMetadata.isRequired() )
            {
                parserGetter = "getRequiredAttributeValue( " + parserGetter + ", \"" + tagName + "\", parser, strict )";
            }
    */
            if ( field.getDefaultValue() != null )
            {
                parserGetter = "getDefaultValue( " + parserGetter + ", \"" + field.getDefaultValue() + "\" )";
            }

            if ( xmlFieldMetadata.isTrim() )
            {
                parserGetter = "getTrimmedValue( " + parserGetter + " )";
            }

            if ( "boolean".equals( type ) )
            {
                sc.add( objectName + "." + setterName + "( getBooleanValue( " + parserGetter + " ) );" );
            }
            else if ( "char".equals( type ) )
            {
                sc.add( objectName + "." + setterName + "( getCharacterValue( " + parserGetter + ", \"" + tagName
                    + "\" ) );" );
            }
            else if ( "double".equals( type ) )
            {
                sc.add( objectName + "." + setterName + "( getDoubleValue( " + parserGetter + ", \"" + tagName
                    + "\", parser.peekEvent(), strict ) );" );
            }
            else if ( "float".equals( type ) )
            {
                sc.add( objectName + "." + setterName + "( getFloatValue( " + parserGetter + ", \"" + tagName
                    + "\", parser.peekEvent(), strict ) );" );
            }
            else if ( "int".equals( type ) )
            {
                sc.add( objectName + "." + setterName + "( getIntegerValue( " + parserGetter + ", \"" + tagName
                    + "\", parser.peekEvent(), strict ) );" );
            }
            else if ( "long".equals( type ) )
            {
                sc.add( objectName + "." + setterName + "( getLongValue( " + parserGetter + ", \"" + tagName
                    + "\", parser.peekEvent(), strict ) );" );
            }
            else if ( "short".equals( type ) )
            {
                sc.add( objectName + "." + setterName + "( getShortValue( " + parserGetter + ", \"" + tagName
                    + "\", parser.peekEvent(), strict ) );" );
            }
            else if ( "byte".equals( type ) )
            {
                sc.add( objectName + "." + setterName + "( getByteValue( " + parserGetter + ", \"" + tagName
                    + "\", parser.peekEvent(), strict ) );" );
            }
            else if ( "String".equals( type ) || "Boolean".equals( type ) )
            {
                // TODO: other Primitive types
                sc.add( objectName + "." + setterName + "( " + parserGetter + " );" );
            }
            else if ( "Date".equals( type ) )
            {
                sc.add( "String dateFormat = "
                    + ( xmlFieldMetadata.getFormat() != null ? "\"" + xmlFieldMetadata.getFormat() + "\"" : "null" ) + ";" );
                sc.add( objectName + "." + setterName + "( getDateValue( " + parserGetter + ", \"" + tagName
                    + "\", dateFormat, parser.peekEvent() ) );" );
            }
            else
            {
                throw new IllegalArgumentException( "Unknown type "
                                                    + type
                                                    + " for field "
                                                    + field.getModelClass().getName()
                                                    + "."
                                                    + field.getName() );
            }
        }

    private JMethod convertNumericalType( String methodName, JType returnType, String expression, String typeDesc )
    {
        JMethod method = new JMethod( methodName, returnType, null );
        method.getModifiers().makePrivate();

        method.addParameter( new JParameter( new JClass( "String" ), "s" ) );
        method.addParameter( new JParameter( new JClass( "String" ), "attribute" ) );
        method.addParameter( new JParameter( new JClass( "Event" ), "event" ) );
        method.addParameter( new JParameter( JType.BOOLEAN, "strict" ) );

        JSourceCode sc = method.getSourceCode();

        sc.add( "if ( s != null )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "try" );

        sc.add( "{" );
        sc.addIndented( "return " + expression + ";" );
        sc.add( "}" );

        sc.add( "catch ( NumberFormatException nfe )" );

        sc.add( "{" );
        sc.indent();

        sc.add( "if ( strict )" );

        sc.add( "{" );
        sc.addIndented( "throw new ParserException( \"\", event.getStartMark(), \"Unable to parse element '\" + attribute + \"', must be "
                        + typeDesc
                        + " but was '\" + s + \"'\", event.getEndMark() );" );
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.unindent();
        sc.add( "}" );

        sc.add( "return 0;" );

        return method;
    }

}
