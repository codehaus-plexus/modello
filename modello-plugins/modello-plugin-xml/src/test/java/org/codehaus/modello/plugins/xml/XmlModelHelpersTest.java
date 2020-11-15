package org.codehaus.modello.plugins.xml;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.junit.Test;

public class XmlModelHelpersTest
{
    @Test
    public void getFieldsForXml_defaultOrder()
    {
        Model model = new Model();
        
        ModelClass entityClass = new ModelClass( model, "Entity" );
        ModelField id = new ModelField( new ModelClass( null, "String" ), "id" );
        id.addMetadata( new XmlFieldMetadata() );
        entityClass.addField( id );
        model.addClass( entityClass );
        
        ModelClass vehicleClass = new ModelClass( model, "Vehicle" );
        vehicleClass.setSuperClass( "Entity" );
        ModelField brand = new ModelField( new ModelClass( null, "String" ), "brand" );
        XmlFieldMetadata brandMetadata = new XmlFieldMetadata();
        brandMetadata.setInsertParentFieldsUpTo( "id" );
        brand.addMetadata( brandMetadata );
        vehicleClass.addField( brand );
        model.addClass( vehicleClass );

        ModelClass carClass = new ModelClass( model, "Car" );
        carClass.setSuperClass( "Vehicle" );
        ModelField modelName = new ModelField( new ModelClass( null, "String" ), "modelName" );
        XmlFieldMetadata modeNameMetaData = new XmlFieldMetadata();
        modeNameMetaData.setInsertParentFieldsUpTo( "brand" );
        modelName.addMetadata( modeNameMetaData );
        
        carClass.addField( modelName );
        model.addClass( carClass );
        
        List<ModelField> orderedFields = XmlModelHelpers.getFieldsForXml( carClass, new Version("0") );
        // unexpected order...
        assertThat( orderedFields.get( 2 ).getName(), is( "modelName" ) );
        assertThat( orderedFields.get( 1 ).getName(), is( "brand" ) );
        assertThat( orderedFields.get( 0 ).getName(), is( "id" ) );
    }

    @Test
    public void getFieldsForXml_insertParentFieldsUpTo()
    {
        Model model = new Model();
        
        ModelClass entityClass = new ModelClass( model, "Entity" );
        ModelField id = new ModelField( new ModelClass( null, "String" ), "id" );
        id.addMetadata( new XmlFieldMetadata() );
        entityClass.addField( id );
        model.addClass( entityClass );
        
        ModelClass vehicleClass = new ModelClass( model, "Vehicle" );
        vehicleClass.setSuperClass( "Entity" );
        ModelField brand = new ModelField( new ModelClass( null, "String" ), "brand" );
        XmlFieldMetadata brandMetadata = new XmlFieldMetadata();
        brandMetadata.setInsertParentFieldsUpTo( "id" );
        brand.addMetadata( brandMetadata );
        vehicleClass.addField( brand );
        model.addClass( vehicleClass );

        ModelClass carClass = new ModelClass( model, "Car" );
        carClass.setSuperClass( "Vehicle" );
        ModelField modelName = new ModelField( new ModelClass( null, "String" ), "modelName" );
        XmlFieldMetadata modeNameMetaData = new XmlFieldMetadata();
        modeNameMetaData.setInsertParentFieldsUpTo( "brand" );
        modelName.addMetadata( modeNameMetaData );
        
        carClass.addField( modelName );
        model.addClass( carClass );
        
        List<ModelField> orderedFields = XmlModelHelpers.getFieldsForXml( carClass, new Version("0") );
        assertThat( orderedFields.get( 0 ).getName(), is( "id" ) );
        assertThat( orderedFields.get( 1 ).getName(), is( "brand" ) );
        assertThat( orderedFields.get( 2 ).getName(), is( "modelName" ) );
    }
}
