package org.codehaus.modello.plugin.jpox;

/*
 * Copyright (c) 2005, Codehaus.org
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
import org.codehaus.modello.db.SQLReservedWords;
import org.codehaus.modello.db.SQLReservedWords.KeywordSource;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.jpox.metadata.JPoxAssociationMetadata;
import org.codehaus.modello.plugin.jpox.metadata.JPoxClassMetadata;
import org.codehaus.modello.plugin.jpox.metadata.JPoxFieldMetadata;
import org.codehaus.modello.plugin.jpox.metadata.JPoxModelMetadata;
import org.codehaus.modello.plugin.store.metadata.StoreAssociationMetadata;
import org.codehaus.modello.plugin.store.metadata.StoreFieldMetadata;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.modello.plugin.ModelloGenerator"
 *              role-hint="jpox-jdo-mapping"
 */
public class JPoxJdoMappingModelloGenerator
    extends AbstractModelloGenerator
{
    private static final char EOL = '\n';

    private static final String ERROR_LINE = "----------------------------------------------------------------";

    private final static Map PRIMITIVE_IDENTITY_MAP;

    private final static List IDENTITY_TYPES;

    private final static List VALUE_STRATEGY_LIST;
    
    /**
     * @plexus.requirement
     */
    private SQLReservedWords sqlReservedWords;

    private String valueStrategyOverride;

    private String objectIdClassOverride;

    static
    {
        PRIMITIVE_IDENTITY_MAP = new HashMap();

        // TODO: These should be the fully qualified class names
        PRIMITIVE_IDENTITY_MAP.put( "short", "javax.jdo.identity.ShortIdentity" );
        PRIMITIVE_IDENTITY_MAP.put( "Short", "javax.jdo.identity.ShortIdentity" );
        PRIMITIVE_IDENTITY_MAP.put( "int", "javax.jdo.identity.IntIdentity" );
        PRIMITIVE_IDENTITY_MAP.put( "Integer", "javax.jdo.identity.IntIdentity" );
        PRIMITIVE_IDENTITY_MAP.put( "long", "javax.jdo.identity.LongIdentity" );
        PRIMITIVE_IDENTITY_MAP.put( "Long", "javax.jdo.identity.LongIdentity" );
        PRIMITIVE_IDENTITY_MAP.put( "String", "javax.jdo.identity.StringIdentity" );
        PRIMITIVE_IDENTITY_MAP.put( "char", "javax.jdo.identity.CharIdentity" );
        PRIMITIVE_IDENTITY_MAP.put( "Character", "javax.jdo.identity.CharIdentity" );
        PRIMITIVE_IDENTITY_MAP.put( "byte", "javax.jdo.identity.ByteIdentity" );
        PRIMITIVE_IDENTITY_MAP.put( "Byte", "javax.jdo.identity.ByteIdentity" );

        IDENTITY_TYPES = new ArrayList();
        IDENTITY_TYPES.add( "application" );
        IDENTITY_TYPES.add( "datastore" );
        IDENTITY_TYPES.add( "nondurable" );

        VALUE_STRATEGY_LIST = new ArrayList();
        //VALUE_STRATEGY_LIST.add( "off" ); -- this isn't really valid. It turns it on. We use it internally to set an explicit null
        VALUE_STRATEGY_LIST.add( "native" );
        VALUE_STRATEGY_LIST.add( "sequence" );
        VALUE_STRATEGY_LIST.add( "identity" );
        VALUE_STRATEGY_LIST.add( "increment" );
        VALUE_STRATEGY_LIST.add( "uuid-string" );
        VALUE_STRATEGY_LIST.add( "uuid-hex" );
        VALUE_STRATEGY_LIST.add( "datastore-uuid-hex" );
        VALUE_STRATEGY_LIST.add( "max" );
        VALUE_STRATEGY_LIST.add( "auid" );
    }


    protected void initialize( Model model, Properties parameters )
        throws ModelloException
    {
        super.initialize( model, parameters );

        valueStrategyOverride = parameters.getProperty( "JPOX.override.value-strategy" );
        objectIdClassOverride = parameters.getProperty( "JPOX.override.objectid-class" );
    }

    public void generate( Model model, Properties properties )
        throws ModelloException
    {
        initialize( model, properties );

        // ----------------------------------------------------------------------
        // Generate the JDO files
        // ----------------------------------------------------------------------

        try
        {
            String fileName = properties.getProperty( ModelloParameterConstants.FILENAME, "package.jdo" );

            JPoxModelMetadata metadata = (JPoxModelMetadata) model.getMetadata( JPoxModelMetadata.ID );
            File packageJdo = null;
            
            if ( metadata.isMappingInPackage() )
            {
                // Use package name.
                String packageName = model.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() );
                String dir = StringUtils.replace( packageName, '.', '/' );
                File directory = new File( getOutputDirectory(), dir );
                packageJdo = new File( directory, fileName );
            }
            else
            {
                // Use the specified location.
                File directory = getOutputDirectory();
                packageJdo = new File( directory, fileName );
            }

            File parent = packageJdo.getParentFile();

            if ( !parent.exists() )
            {
                if ( !parent.mkdirs() )
                {
                    throw new ModelloException( "Error while creating parent directories for the file " + "'" +
                        packageJdo.getAbsolutePath() + "'." );
                }
            }

            generatePackageJdo( packageJdo, model );
        }
        catch ( IOException e )
        {
            throw new ModelloException( "Error while writing package.jdo.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void generatePackageJdo( File file, Model model )
        throws IOException, ModelloException
    {
        OutputStreamWriter fileWriter = new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" );

        PrintWriter printWriter = new PrintWriter( fileWriter );

        XMLWriter writer = new PrettyPrintXMLWriter( printWriter );

        Map classes = new HashMap();

        for ( Iterator it = model.getClasses( getGeneratedVersion() ).iterator(); it.hasNext(); )
        {
            ModelClass modelClass = (ModelClass) it.next();

            JPoxClassMetadata jpoxMetadata = (JPoxClassMetadata) modelClass.getMetadata( JPoxClassMetadata.ID );

            if ( !jpoxMetadata.isEnabled() )
            {
                // Skip generation of those classes that are not enabled for the jpox plugin.
                continue;
            }

            String packageName = modelClass.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

            List list = (List) classes.get( packageName );

            if ( list == null )
            {
                list = new ArrayList();
            }

            list.add( modelClass );

            classes.put( packageName, list );
        }

        printWriter.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        printWriter.println();
        printWriter.println( "<!DOCTYPE jdo PUBLIC" );
        printWriter.println( "  \"-//Sun Microsystems, Inc.//DTD Java Data Objects Metadata 2.0//EN\"" );
        printWriter.println( "  \"http://java.sun.com/dtd/jdo_2_0.dtd\">" );
        printWriter.println();

        writer.startElement( "jdo" );

        for ( Iterator it = classes.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();

            List list = (List) entry.getValue();

            if ( list.size() == 0 )
            {
                continue;
            }

            String packageName = (String) entry.getKey();

            writer.startElement( "package" );

            writer.addAttribute( "name", packageName );

            for ( Iterator it2 = list.iterator(); it2.hasNext(); )
            {
                ModelClass modelClass = (ModelClass) it2.next();

                writeClass( writer, modelClass );
            }

            if ( packageName.equals( model.getDefaultPackageName( isPackageWithVersion(), getGeneratedVersion() ) ) )
            {
                writeModelloMetadataClass( writer );
            }

            writer.endElement(); // package
        }

        writer.endElement(); // jdo

        printWriter.println();

        printWriter.close();
    }
    
    private void writeClass( XMLWriter writer, ModelClass modelClass )
        throws ModelloException
    {
        JPoxClassMetadata jpoxMetadata = (JPoxClassMetadata) modelClass.getMetadata( JPoxClassMetadata.ID );

        if ( !jpoxMetadata.isEnabled() )
        {
            // Skip generation of those classes that are not enabled for the jpox plugin.
            return;
        }

        writer.startElement( "class" );

        writer.addAttribute( "name", modelClass.getName() );

        ModelClass persistenceCapableSuperclass = null;

        if ( modelClass.hasSuperClass() && modelClass.isInternalSuperClass() )
        {
            persistenceCapableSuperclass = getModel().getClass( modelClass.getSuperClass(), getGeneratedVersion() );
        }

        if ( persistenceCapableSuperclass != null )
        {
            String superPackageName =
                persistenceCapableSuperclass.getPackageName( isPackageWithVersion(), getGeneratedVersion() );

            writer.addAttribute( "persistence-capable-superclass",
                                 superPackageName + "." + persistenceCapableSuperclass.getName() );
        }

        writer.addAttribute( "detachable", String.valueOf( jpoxMetadata.isDetachable() ) );

        writer.addAttribute( "table", getTableName( modelClass, jpoxMetadata ) );
        
        // ----------------------------------------------------------------------
        // If this class has a primary key field mark make jpox manage the id
        // as a autoincrement variable
        // ----------------------------------------------------------------------

        List fields = Collections.unmodifiableList( modelClass.getFields( getGeneratedVersion() ) );

        // TODO: for now, assume that any primary key will be set in the super class
        // While it should be possible to have abstract super classes and have the
        // key defined in the sub class this is not implemented yet.

        boolean needInheritance = false;

        if ( persistenceCapableSuperclass == null )
        {
            if ( StringUtils.isNotEmpty( jpoxMetadata.getIdentityType() ) )
            {
                String identityType = jpoxMetadata.getIdentityType();
                if ( !IDENTITY_TYPES.contains( identityType ) )
                {
                    throw new ModelloException( "The JDO mapping generator does not support the specified " +
                        "class identity type '" + identityType + "'. " + "Supported types: " + IDENTITY_TYPES );
                }
                writer.addAttribute( "identity-type", identityType );
            }
            else if ( isInstantionApplicationType( modelClass ) )
            {
                writer.addAttribute( "identity-type", "application" );
            }
        }
        else
        {
            needInheritance = true;
        }

        if ( objectIdClassOverride != null )
        {
            if ( StringUtils.isNotEmpty( objectIdClassOverride ) )
            {
                writer.addAttribute( "objectid-class", jpoxMetadata.getIdentityClass() );
            }
        }
        else if ( StringUtils.isNotEmpty( jpoxMetadata.getIdentityClass() ) )
        {
            // Use user provided objectId class.
            writer.addAttribute( "objectid-class", jpoxMetadata.getIdentityClass() );
        }
        else
        {
            // Calculate the objectId class.

            List primaryKeys = getPrimaryKeyFields( modelClass );

            // TODO: write generation support for multi-primary-key support.
            //       would likely need to write a java class that can supply an ObjectIdentity
            //       to the jpox/jdo implementation.
            if ( primaryKeys.size() > 1 )
            {
                throw new ModelloException(
                    "The JDO mapping generator does not yet support Object Identifier generation " + "for the " +
                        primaryKeys.size() + " fields specified as <identifier> or " +
                        "with jpox.primary-key=\"true\"" );
            }

            if ( primaryKeys.size() == 1 )
            {
                ModelField modelField = (ModelField) primaryKeys.get( 0 );
                String objectIdClass = (String) PRIMITIVE_IDENTITY_MAP.get( modelField.getType() );

                if ( StringUtils.isNotEmpty( objectIdClass ) )
                {
                    writer.addAttribute( "objectid-class", objectIdClass );
                }
            }
        }

        if ( needInheritance )
        {
            writer.startElement( "inheritance" );

            // TODO: The table strategy should be customizable
            // http://www.jpox.org/docs/1_1/inheritance.html - in particular
            // the strategy="subclass-table" and strategy="new-table" parts

            writer.addAttribute( "strategy", "new-table" );

            writer.endElement();
        }

        // ----------------------------------------------------------------------
        // Write all fields
        // ----------------------------------------------------------------------

        for ( Iterator it = fields.iterator(); it.hasNext(); )
        {
            ModelField modelField = (ModelField) it.next();

            writeModelField( writer, modelField );
        }

        // Write ignored fields.
        List ignoredFields = jpoxMetadata.getNotPersisted();
        if ( ignoredFields != null )
        {
            Iterator it = ignoredFields.iterator();
            while ( it.hasNext() )
            {
                String fieldName = (String) it.next();
                writer.startElement( "field" );
                writer.addAttribute( "name", fieldName );
                writer.addAttribute( "persistence-modifier", "none" );
                writer.endElement();
            }
        }

        // ----------------------------------------------------------------------
        // Write out the "detailed" fetch group. This group will by default
        // contain all fields in a object. The default fetch group will contain
        // all the primitives in a class as by JDO defaults.
        // ----------------------------------------------------------------------

        List detailedFields = new ArrayList();

        for ( Iterator it = fields.iterator(); it.hasNext(); )
        {
            ModelField field = (ModelField) it.next();

            if ( field.isPrimitive() )
            {
                continue;
            }

            if ( field instanceof ModelAssociation )
            {
                StoreAssociationMetadata storeMetadata = getAssociationMetadata( (ModelAssociation) field );

                if ( storeMetadata.isPart() != null && storeMetadata.isPart().booleanValue() )
                {
                    continue;
                }
            }

            detailedFields.add( field );
        }

        // ----------------------------------------------------------------------
        // Write all fetch groups
        // ----------------------------------------------------------------------

        // Write defaut detail fetch group
        writeFetchGroup( writer, modelClass.getName() + "_detail", detailedFields, true );

        // Write user fetch groups
        Map fetchsMap = new HashMap();

        for ( Iterator it = fields.iterator(); it.hasNext(); )
        {
            ModelField field = (ModelField) it.next();

            JPoxFieldMetadata jpoxFieldMetadata = (JPoxFieldMetadata) field.getMetadata( JPoxFieldMetadata.ID );

            List names = jpoxFieldMetadata.getFetchGroupNames();

            if ( names != null )
            {
                for ( Iterator i = names.iterator(); i.hasNext(); )
                {
                    String fetchGroupName = (String) i.next();

                    List fetchList;

                    if ( fetchsMap.get( fetchGroupName ) == null )
                    {
                        fetchList = new ArrayList();
                    }
                    else
                    {
                        fetchList = (List) fetchsMap.get( fetchGroupName );
                    }

                    fetchList.add( field );

                    fetchsMap.put( fetchGroupName, fetchList );
                }
            }
        }

        for ( Iterator it = fetchsMap.keySet().iterator(); it.hasNext(); )
        {
            String fetchName = (String) it.next();

            writeFetchGroup( writer, fetchName, (List) fetchsMap.get( fetchName ), false );
        }

        writer.endElement(); // class
    }

    /**
     * Utility method to obtain the table name from the {@link ModelClass} or {@link JPoxClassMetadata} taking into
     * account the possibility of prefixes in use at the {@link JPoxModelMetadata} level.
     * 
     * @param modelClass the model class to get the class.name from.
     * @param classMetadata the class metadata to get the class-metadata.table name from.
     * @return the table name (with possible prefix applied) 
     * @throws ModelloException if there was a problem with the table name violating a sql reserved word.
     */
    private String getTableName( ModelClass modelClass, JPoxClassMetadata classMetadata )
        throws ModelloException
    {
        JPoxModelMetadata modelMetadata = (JPoxModelMetadata) modelClass.getModel().getMetadata( JPoxModelMetadata.ID );
        
        boolean hasPrefix = StringUtils.isNotEmpty( modelMetadata.getTablePrefix() );
        boolean hasAlternateName = StringUtils.isNotEmpty( classMetadata.getTable() );

        String prefix = "";

        if ( hasPrefix )
        {
            prefix = modelMetadata.getTablePrefix().trim();
        }

        String tableName = null;

        if ( hasAlternateName )
        {
            tableName = prefix + classMetadata.getTable();
        }
        else
        {
            tableName = prefix + modelClass.getName();
        }

        if ( sqlReservedWords.isKeyword( tableName ) )
        {
            StringBuffer emsg = new StringBuffer();
            
            /* ----------------------------------------------------------------
             *   SQL Reserved Word Violation: 'ROLES'
             *   Context: TABLE NAME
             */
            emsg.append( EOL ).append( ERROR_LINE ).append( EOL );
            emsg.append( "  SQL Reserved Word Violation: " ).append( tableName ).append( EOL );
            emsg.append( "  Context: TABLE NAME" ).append( EOL );
            emsg.append( " ").append( EOL );

            /*   In Model:
             *     <model jpox.table-prefix="">
             *       <class jpox.table="">
             *         <name>JdoRole</name>
             *       </class>
             *     </model>
             */
            emsg.append( "  In Model:" ).append( EOL );
            emsg.append( "    <model" );
            if ( hasPrefix )
            {
                emsg.append( " jpox.table-prefix=\"" ).append( modelMetadata.getTablePrefix() ).append( "\"" );
            }
            emsg.append( ">" ).append( EOL );
            emsg.append( "      <class" );
            if ( hasAlternateName )
            {
                emsg.append( " jpox.table=\"" ).append( classMetadata.getTable() ).append( "\"" );
            }
            emsg.append( ">" ).append( EOL );
            emsg.append( "        <name>" ).append( modelClass.getName() ).append( "</name>" ).append( EOL );
            emsg.append( "      </class>" ).append( EOL );
            emsg.append( "    </model>" ).append( EOL );
            emsg.append( " ").append( EOL );

            /*   Violation Source(s): Oracle (WARNING)
             *                        SQL 99 (ERROR)
             *                        
             *   Severity: ERROR - You must change this name for maximum
             *             compatibility amoungst JDBC SQL Servers.
             *             
             *   Severity: WARNING - You are encouraged to change this name
             *             for maximum compatibility amoungst JDBC SQL Servers.
             */
            boolean hasError = appendKeywordSourceViolations( tableName, emsg );

            /*   Suggestions: 1) Use a different prefix in
             *                   <model jpox.table-prefix="DIFFERENT_">
             *                2) Use a different alternate table name using
             *                   <class jpox.table="DIFFERENT">
             *                3) Use a different class name in
             *                   <class>
             *                     <name>DIFFERENT</name>
             *                   </class>
             * ----------------------------------------------------------------                  
             */
            emsg.append( "  Suggestions: 1) Use a different prefix in" ).append( EOL );
            emsg.append( "                  <model jpox.table-prefix=\"DIFFERENT_\">" ).append( EOL );
            emsg.append( "               2) Use a different alternate table name using" ).append( EOL );
            emsg.append( "                  <class jpox.table=\"DIFFERENT\">" ).append( EOL );
            emsg.append( "               3) Use a different class name in" ).append( EOL );
            emsg.append( "                  <class>" ).append( EOL );
            emsg.append( "                    <name>DIFFERENT</name>" ).append( EOL );
            emsg.append( "                  </class>" ).append( EOL );

            emsg.append( ERROR_LINE );

            // Determine possible exception.
            if ( hasError || modelMetadata.getReservedWordStrictness().equals( JPoxModelMetadata.WARNING ) )
            {
                throw new ModelloException( emsg.toString() );
            }
            
            // No exception. use it. But log it.
            getLogger().warn( emsg.toString() );
        }

        return tableName;
    }

    /**
     * Support method for the {@link #getTableName(ModelClass, JPoxClassMetadata)}, 
     * {@link #getColumnName(ModelField, JPoxFieldMetadata)}, and 
     * {@link #getJoinTableName(ModelField, JPoxFieldMetadata)} reserved word tests.
     * 
     * @param word the word in violation.
     * @param emsg the string buffer to append to.
     * @return if this word has any ERROR severity level violations.
     */
    private boolean appendKeywordSourceViolations( String word, StringBuffer emsg )
    {
        List sources = sqlReservedWords.getKeywordSourceList( word );
        boolean hasError = false;
        emsg.append( "  Violation Source(s): " );
        for ( Iterator it = sources.iterator(); it.hasNext(); )
        {
            KeywordSource source = (KeywordSource) it.next();
            emsg.append( source.getName() ).append( " (" ).append( source.getSeverity() ).append( ")" );
            emsg.append( EOL );

            if ( source.getSeverity().equalsIgnoreCase( "ERROR" ) )
            {
                hasError = true;
            }

            if ( it.hasNext() )
            {
                emsg.append( "                       " );
            }
        }
        emsg.append( " ").append( EOL );
        
        emsg.append( "  Severity: " );
        if ( hasError )
        {
            emsg.append( "ERROR - You must change this name for the maximum" ).append( EOL );
            emsg.append( "            compatibility amoungst JDBC SQL Servers." ).append( EOL );
        }
        else
        {
            emsg.append( "WARNING - You are encouraged to change this name" ).append( EOL );
            emsg.append( "            for maximum compatibility amoungst JDBC SQL Servers." ).append( EOL );
        }
        emsg.append( " ").append( EOL );        
        
        return hasError;
    }

    private void writeModelloMetadataClass( XMLWriter writer ) throws ModelloException
    {
        writer.startElement( "class" );

        writer.addAttribute( "name", getModel().getName() + "ModelloMetadata" );

        writer.addAttribute( "detachable", String.valueOf( true ) );

        writer.startElement( "field" );

        writer.addAttribute( "name", "modelVersion" );
        writer.addAttribute( "null-value", "default" );

        writer.startElement( "column" );

        writer.addAttribute( "default-value", getGeneratedVersion().toString() );

        writer.endElement(); // column

        writer.endElement(); // field

        writer.endElement(); // class
    }

    private void writeFetchGroup( XMLWriter writer, String fetchGroupName, List fields, boolean onlyIfIsStashPart )
    {
        if ( !fields.isEmpty() )
        {
            writer.startElement( "fetch-group" );

            writer.addAttribute( "name", fetchGroupName );

            for ( Iterator it = fields.iterator(); it.hasNext(); )
            {
                ModelField field = (ModelField) it.next();

                if ( onlyIfIsStashPart && ( field instanceof ModelAssociation ) )
                {
                    StoreAssociationMetadata storeMetadata = getAssociationMetadata( (ModelAssociation) field );

                    if ( storeMetadata.isPart() != null && storeMetadata.isPart().booleanValue() )
                    {
                        continue;
                    }
                }

                writer.startElement( "field" );

                writer.addAttribute( "name", field.getName() );

                writer.endElement();
            }

            writer.endElement(); // fetch-group
        }
    }

    private void writeModelField( XMLWriter writer, ModelField modelField ) throws ModelloException
    {
        writer.startElement( "field" );

        StoreFieldMetadata storeMetadata = (StoreFieldMetadata) modelField.getMetadata( StoreFieldMetadata.ID );

        JPoxFieldMetadata jpoxMetadata = (JPoxFieldMetadata) modelField.getMetadata( JPoxFieldMetadata.ID );

        writer.addAttribute( "name", modelField.getName() );

        if ( !storeMetadata.isStorable() )
        {
            writer.addAttribute( "persistence-modifier", "none" );
        }
        else if ( StringUtils.isNotEmpty( jpoxMetadata.getPersistenceModifier() ) )
        {
            writer.addAttribute( "persistence-modifier", jpoxMetadata.getPersistenceModifier() );
        }

        if ( modelField.isRequired() )
        {
            writer.addAttribute( "null-value", "exception" );
        }
        else if ( jpoxMetadata.getNullValue() != null )
        {
            writer.addAttribute( "null-value", jpoxMetadata.getNullValue() );
        }

        String columnName = getColumnName( modelField, jpoxMetadata );
        
        if ( StringUtils.isNotEmpty( jpoxMetadata.getJoinTableName() ) )
        {
            writer.addAttribute( "table", getJoinTableName( modelField, jpoxMetadata ) );
        }

        if ( jpoxMetadata.isPrimaryKey() )
        {
            writer.addAttribute( "primary-key", "true" );

            // value-strategy is only useful when you have a primary-key defined for the field.
            if ( StringUtils.isNotEmpty( valueStrategyOverride ) )
            {
                writeValueStrategy( valueStrategyOverride, writer );
            }
            else if ( StringUtils.isNotEmpty( jpoxMetadata.getValueStrategy() ) )
            {
                writeValueStrategy( jpoxMetadata.getValueStrategy(), writer );
            }
        }

        if ( StringUtils.isNotEmpty( jpoxMetadata.getIndexed() ) )
        {
            writer.addAttribute( "indexed", jpoxMetadata.getIndexed() );
        }

        if ( StringUtils.isNotEmpty( jpoxMetadata.getMappedBy() ) )
        {
            writer.addAttribute( "mapped-by", jpoxMetadata.getMappedBy() );
        }

        if ( modelField instanceof ModelAssociation )
        {
            writeAssociation( writer, (ModelAssociation) modelField );
        }
        else
        {
            if ( modelField.isPrimitiveArray() )
            {
                writer.startElement( "array" );
                writer.endElement();
            }
            
            // Store potential column properties.
            Properties columnProps = new Properties();

            if ( !StringUtils.equalsIgnoreCase( columnName, modelField.getName() ) )
            {
                columnProps.setProperty( "name", columnName );
            }

            if ( storeMetadata.getMaxSize() > 0 )
            {
                columnProps.setProperty( "length", String.valueOf( storeMetadata.getMaxSize() ) );
            }

            if ( StringUtils.equals( jpoxMetadata.getNullValue(), "default" ) )
            {
                columnProps.setProperty( "default-value", modelField.getDefaultValue() );
            }

            // Now write the column sub element (if it has properties)
            if ( !columnProps.isEmpty() )
            {
                writer.startElement( "column" );

                for ( Enumeration en = columnProps.propertyNames(); en.hasMoreElements(); )
                {
                    String attributeName = (String) en.nextElement();
                    String attributeValue = columnProps.getProperty( attributeName );
                    writer.addAttribute( attributeName, attributeValue );

                }
                writer.endElement();
            }
        }

        writer.endElement(); // field
    }

    /**
     * Utility method to obtain the join table name from the {@link JPoxFieldMetadata} taking into
     * account the possibility of prefixes in use at the {@link JPoxModelMetadata} level.
     * 
     * @param modelField the model field to get the field.name from.
     * @param fieldMetadata the field metadata to get the field-metadata.join-table name from.
     * @return the join table name (with possible prefix applied) 
     * @throws ModelloException if there was a problem with the table name violating a sql reserved word.
     */
    private String getJoinTableName( ModelField modelField, JPoxFieldMetadata fieldMetadata ) throws ModelloException
    {
        ModelClass modelClass = modelField.getModelClass();
        JPoxModelMetadata modelMetadata = (JPoxModelMetadata) modelClass.getModel().getMetadata( JPoxModelMetadata.ID );
        
        boolean hasPrefix = StringUtils.isNotEmpty( modelMetadata.getTablePrefix() );
        
        String prefix = "";

        if ( hasPrefix )
        {
            prefix = modelMetadata.getTablePrefix().trim();
        }
        
        String joinTableName = prefix + fieldMetadata.getJoinTableName();;

        if ( sqlReservedWords.isKeyword( joinTableName ) )
        {
            StringBuffer emsg = new StringBuffer();
            
            /* ----------------------------------------------------------------
             *   SQL Reserved Word Violation: 'ROLES'
             *   Context: TABLE NAME
             */
            emsg.append( EOL ).append( ERROR_LINE ).append( EOL );
            emsg.append( "  SQL Reserved Word Violation: " ).append( joinTableName ).append( EOL );
            emsg.append( "  Context: JOIN TABLE NAME" ).append( EOL );
            emsg.append( " ").append( EOL );

            /*   In Model:
             *     <model jpox.table-prefix="">
             *       <class jpox.table="">
             *         <name>JdoRole</name>
             *         <fields>
             *           <field jpox.join-table="Foo">
             *             <name>Operation</name>
             *           </field>
             *         </fields>
             *       </class>
             *     </model>
             */
            emsg.append( "  In Model:" ).append( EOL );
            emsg.append( "    <model" );
            if ( hasPrefix )
            {
                emsg.append( " jpox.table-prefix=\"" ).append( modelMetadata.getTablePrefix() ).append( "\"" );
            }
            emsg.append( ">" ).append( EOL );
            emsg.append( "      <class>" ).append( EOL );
            emsg.append( "        <name>" ).append( modelClass.getName() ).append( "</name>" ).append( EOL );
            emsg.append( "        <fields>" ).append( EOL );
            emsg.append( "          <field jpox.join-table=\"" ).append( fieldMetadata.getJoinTableName() );
            emsg.append( "\">" ).append( EOL );
            emsg.append( "            <name>" ).append( modelField.getName() ).append( "</name>" ).append( EOL );
            emsg.append( "          <field>" ).append( EOL );
            emsg.append( "        </fields>" ).append( EOL );
            emsg.append( "      </class>" ).append( EOL );
            emsg.append( "    </model>" ).append( EOL );
            emsg.append( " ").append( EOL );

            /*   Violation Source(s): Oracle (WARNING)
             *                        SQL 99 (ERROR)
             *                        
             *   Severity: ERROR - You must change this name for maximum
             *             compatibility amoungst JDBC SQL Servers.
             *             
             *   Severity: WARNING - You are encouraged to change this name
             *             for maximum compatibility amoungst JDBC SQL Servers.
             */
            boolean hasError = appendKeywordSourceViolations( joinTableName, emsg );

            /*   Suggestions: 1) Use a different table prefix in
             *                   <model jpox.table-prefix="DIFFERENT_">
             *                2) Use a different join table name using
             *                   <field jpox.join-table="DIFFERENT">
             * ----------------------------------------------------------------                  
             */
            emsg.append( "  Suggestions: 1) Use a different table prefix in" ).append( EOL );
            emsg.append( "                  <model jpox.table-prefix=\"DIFFERENT_\">" ).append( EOL );
            emsg.append( "               2) Use a different join table name using" ).append( EOL );
            emsg.append( "                  <field jpox.join-table=\"DIFFERENT\">" ).append( EOL );

            emsg.append( ERROR_LINE );

            // Determine possible exception.
            if ( hasError || modelMetadata.getReservedWordStrictness().equals( JPoxModelMetadata.WARNING ) )
            {
                throw new ModelloException( emsg.toString() );
            }
            
            // No exception. use it. But log it.
            getLogger().warn( emsg.toString() );
        }
        
        return joinTableName;
    }

    /**
     * Utility method to obtain the column name from the {@link ModelField} or {@link JPoxFieldMetadata} taking into
     * account the possibility of prefixes in use at the {@link JPoxModelMetadata} or {@link JPoxClassMetadata} level.
     * 
     * @param modelField the model field to get the field.name from.
     * @param fieldMetadata the field metadata to get the field-metadata.column name from.
     * @return the column name (with possible prefix applied) 
     * @throws ModelloException if there was a problem with the column name violating a sql reserved word.
     */
    private String getColumnName( ModelField modelField, JPoxFieldMetadata fieldMetadata ) throws ModelloException
    {
        boolean hasClassPrefix = false;
        boolean hasModelPrefix = false;
        boolean hasAlternateName = false;

        ModelClass modelClass = modelField.getModelClass();
        JPoxClassMetadata classMetadata = (JPoxClassMetadata) modelClass.getMetadata( JPoxClassMetadata.ID );
        JPoxModelMetadata modelMetadata = (JPoxModelMetadata) modelClass.getModel().getMetadata( JPoxModelMetadata.ID );

        String prefix = "";

        if ( StringUtils.isNotEmpty( modelMetadata.getColumnPrefix() ) )
        {
            prefix = modelMetadata.getColumnPrefix().trim();
            hasModelPrefix = true;
        }

        if ( StringUtils.isNotEmpty( classMetadata.getColumnPrefix() ) )
        {
            prefix = classMetadata.getColumnPrefix();
            hasClassPrefix = true;
        }

        String columnName = "";

        if ( StringUtils.isNotEmpty( fieldMetadata.getColumnName() ) )
        {
            columnName = prefix + fieldMetadata.getColumnName();
            hasAlternateName = true;
        }
        else
        {
            columnName = prefix + modelField.getName();
        }

        if ( sqlReservedWords.isKeyword( columnName ) )
        {
            StringBuffer emsg = new StringBuffer();

            /* ----------------------------------------------------------------
             *   SQL Reserved Word Violation: 'ROLES'
             *   Context: TABLE NAME
             */
            emsg.append( EOL ).append( ERROR_LINE ).append( EOL );
            emsg.append( "  SQL Reserved Word Violation: " ).append( columnName ).append( EOL );
            emsg.append( "  Context: COLUMN NAME" ).append( EOL );
            emsg.append( " " ).append( EOL );

            /*   In Model:
             *     <model jpox.column-prefix="">
             *       <class jpox.column-prefix="">
             *         <name>JdoRole</name>
             *         <fields>
             *           <field jpox.column="">
             *             <name>operation</name>
             *           </field>
             *         </fields>
             *       </class>
             *     </model>
             */
            emsg.append( "  In Model:" ).append( EOL );
            emsg.append( "    <model" );
            if ( hasModelPrefix )
            {
                emsg.append( " jpox.column-prefix=\"" ).append( modelMetadata.getColumnPrefix() ).append( "\"" );
            }
            emsg.append( ">" ).append( EOL );
            emsg.append( "      <class" );
            if ( hasClassPrefix )
            {
                emsg.append( " jpox.column-prefix=\"" ).append( classMetadata.getColumnPrefix() ).append( "\"" );
            }
            emsg.append( ">" ).append( EOL );
            emsg.append( "        <name>" ).append( modelClass.getName() ).append( "</name>" ).append( EOL );
            emsg.append( "        <fields>" ).append( EOL );
            emsg.append( "          <field" );
            if ( hasAlternateName )
            {
                emsg.append( " jpox.column=\"" ).append( fieldMetadata.getColumnName() ).append( "\"" );
            }
            emsg.append( ">" ).append( EOL );
            emsg.append( "            <name>" ).append( modelField.getName() ).append( "</name>" ).append( EOL );
            emsg.append( "          <field>" ).append( EOL );
            emsg.append( "        </fields>" ).append( EOL );
            emsg.append( "      </class>" ).append( EOL );
            emsg.append( "    </model>" ).append( EOL );
            emsg.append( " " ).append( EOL );

            /*   Violation Source(s): Oracle (WARNING)
             *                        SQL 99 (ERROR)
             *                        
             *   Severity: ERROR - You must change this name for maximum
             *             compatibility amoungst JDBC SQL Servers.
             *             
             *   Severity: WARNING - You are encouraged to change this name
             *             for maximum compatibility amoungst JDBC SQL Servers.
             */
            boolean hasError = appendKeywordSourceViolations( columnName, emsg );

            /*   Suggestions: 1) Use a different model column prefix in
             *                   <model jpox.column-prefix="DIFFERENT_">
             *                2) Use a different class column prefix in
             *                   <class jpox.column-prefix="DIFFERENT_">
             *                3) Use a different alternate column name using
             *                   <field jpox.column="DIFFERENT">
             *                4) Use a different field name in
             *                   <class>
             *                     <name>Foo</name>
             *                     <fields>
             *                       <field>
             *                         <name>DIFFERENT</name>
             *                       </field>
             *                     </fields>
             *                   </class>
             * ----------------------------------------------------------------                  
             */
            emsg.append( "  Suggestions: 1) Use a different model column prefix in" ).append( EOL );
            emsg.append( "                  <model jpox.column-prefix=\"DIFFERENT_\">" ).append( EOL );
            emsg.append( "               2) Use a different class column prefix in" ).append( EOL );
            emsg.append( "                  <class jpox.column-prefix=\"DIFFERENT_\">" ).append( EOL );
            emsg.append( "               3) Use a different alternate column name using" ).append( EOL );
            emsg.append( "                  <field jpox.column=\"DIFFERENT\">" ).append( EOL );
            emsg.append( "               4) Use a different field name in" ).append( EOL );
            emsg.append( "                  <class>" ).append( EOL );
            emsg.append( "                    <name>" ).append( modelClass.getName() ).append( "</name>" ).append( EOL );
            emsg.append( "                    <fields>" ).append( EOL );
            emsg.append( "                      <field>" ).append( EOL );
            emsg.append( "                        <name>DIFFERENT</name>" ).append( EOL );
            emsg.append( "                      <field>" ).append( EOL );
            emsg.append( "                    </fields>" ).append( EOL );
            emsg.append( "                  </class>" ).append( EOL );

            emsg.append( ERROR_LINE );

            // Determine possible exception.
            if ( hasError || modelMetadata.getReservedWordStrictness().equals( JPoxModelMetadata.WARNING ) )
            {
                throw new ModelloException( emsg.toString() );
            }
            
            // No exception. use it. But log it.
            getLogger().warn( emsg.toString() );
        }

        return columnName;
    }

    private static void writeValueStrategy( String valueStrategy, XMLWriter writer ) throws ModelloException
    {
        if ( !"off".equals( valueStrategy ) )
        {
            if ( !VALUE_STRATEGY_LIST.contains( valueStrategy ) )
            {
                throw new ModelloException( "The JDO mapping generator does not support the specified "
                                + "value-strategy '" + valueStrategy + "'. " + "Supported types: "
                                + VALUE_STRATEGY_LIST );
            }
            writer.addAttribute( "value-strategy", valueStrategy );
        }
    }

    private void writeAssociation( XMLWriter writer, ModelAssociation association )
    {
        StoreAssociationMetadata am =
            (StoreAssociationMetadata) association.getAssociationMetadata( StoreAssociationMetadata.ID );

        JPoxAssociationMetadata jpoxMetadata =
            (JPoxAssociationMetadata) association.getAssociationMetadata( JPoxAssociationMetadata.ID );

        if ( am.isPart() != null )
        {
            // This gets added onto the <field> element
            writer.addAttribute( "default-fetch-group", am.isPart().toString() );
        }

        if ( association.getType().equals( "java.util.List" ) || association.getType().equals( "java.util.Set" ) )
        {
            // Start <collection> element
            writer.startElement( "collection" );

            if ( association.getTo().equals( "String" ) )
            {
                writer.addAttribute( "element-type", "java.lang.String" );
            }
            else
            {
                writer.addAttribute( "element-type", association.getTo() );
            }

            if ( jpoxMetadata.isDependent() )
            {
                writer.addAttribute( "dependent-element", "true" );
            }
            else
            {
                writer.addAttribute( "dependent-element", "false" );
            }

            // End <collection> element
            writer.endElement();

            if ( jpoxMetadata.isJoin() )
            {
                writer.startElement( "join" );

                writer.endElement();
            }
        }
        else if ( association.getType().equals( "java.util.Map" ) )
        {
            writer.startElement( "map" );

            writer.addAttribute( "key-type", am.getKeyType() );

            if ( association.getTo().equals( "String" ) )
            {
                writer.addAttribute( "value-type", "java.lang.String" );
            }
            else
            {
                writer.addAttribute( "value-type", association.getTo() );
            }

            if ( jpoxMetadata.isDependent() )
            {
                writer.addAttribute( "dependent-key", "true" );

                writer.addAttribute( "dependent-value", "true" );
            }
            else
            {
                writer.addAttribute( "dependent-key", "false" );

                writer.addAttribute( "dependent-value", "false" );
            }

            writer.endElement();

            if ( jpoxMetadata.isJoin() )
            {
                writer.startElement( "join" );

                writer.endElement();
            }
        }
        else if ( association.getType().equals( "java.util.Properties" ) )
        {
            writer.addAttribute( "embedded", "false" );

            writer.startElement( "map" );

            writer.addAttribute( "key-type", "java.lang.String" );

            writer.addAttribute( "value-type", "java.lang.String" );

            writer.addAttribute( "embedded-key", "true" );

            writer.addAttribute( "embedded-value", "true" );

            writer.addAttribute( "dependent-key", "true" );

            writer.addAttribute( "dependent-value", "true" );

            writer.endElement();

            if ( jpoxMetadata.isJoin() )
            {
                writer.startElement( "join" );

                writer.endElement();
            }
        }
        else
        // One association
        {
            if ( jpoxMetadata.isDependent() )
            {
                writer.addAttribute( "dependent", "true" );
            }
        }
    }

    private boolean isInstantionApplicationType( ModelClass modelClass )
    {
        List identifierFields = modelClass.getIdentifierFields( getGeneratedVersion() );

        return identifierFields.size() > 0;
    }

    private List getPrimaryKeyFields( ModelClass modelClass ) throws ModelloException
    {
        List primaryKeys = new ArrayList();
        List fields = modelClass.getFields( getGeneratedVersion() );
        JPoxClassMetadata jpoxClassMetadata = (JPoxClassMetadata) modelClass.getMetadata( JPoxClassMetadata.ID );

        for ( Iterator it = fields.iterator(); it.hasNext(); )
        {
            ModelField modelField = (ModelField) it.next();
            JPoxFieldMetadata jpoxFieldMetadata = (JPoxFieldMetadata) modelField.getMetadata( JPoxFieldMetadata.ID );

            if ( jpoxClassMetadata.useIdentifiersAsPrimaryKey() )
            {
                if ( modelField.isIdentifier() )
                {
                    assertSupportedIdentityPrimitive( modelField );
                    primaryKeys.add( modelField );
                }
            }
            else
            {
                if ( jpoxFieldMetadata.isPrimaryKey() )
                {
                    assertSupportedIdentityPrimitive( modelField );
                    primaryKeys.add( modelField );
                }
            }
        }

        return primaryKeys;
    }

    private void assertSupportedIdentityPrimitive( ModelField modelField ) throws ModelloException
    {
        if ( !PRIMITIVE_IDENTITY_MAP.containsKey( modelField.getType() ) )
        {
            throw new ModelloException( "The JDO mapping generator does not support the specified " + "field type '"
                            + modelField.getType() + "'. " + "Supported types: " + PRIMITIVE_IDENTITY_MAP.keySet() );
        }
    }

    private StoreAssociationMetadata getAssociationMetadata( ModelAssociation association )
    {
        return (StoreAssociationMetadata) association.getAssociationMetadata( StoreAssociationMetadata.ID );
    }
}
