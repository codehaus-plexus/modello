package org.codehaus.modello.plugins.xml;

import java.util.List;

import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.plugins.xml.metadata.XmlFieldMetadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class XmlModelHelpersTest {
    @Test
    public void getFieldsForXml_defaultOrder() {
        Model model = new Model();

        ModelClass entityClass = new ModelClass(model, "Entity");
        ModelField id = new ModelField(new ModelClass(null, "String"), "id");
        id.addMetadata(new XmlFieldMetadata());
        entityClass.addField(id);
        model.addClass(entityClass);

        ModelClass vehicleClass = new ModelClass(model, "Vehicle");
        vehicleClass.setSuperClass("Entity");
        ModelField brand = new ModelField(new ModelClass(null, "String"), "brand");
        XmlFieldMetadata brandMetadata = new XmlFieldMetadata();
        brandMetadata.setInsertParentFieldsUpTo("id");
        brand.addMetadata(brandMetadata);
        vehicleClass.addField(brand);
        model.addClass(vehicleClass);

        ModelClass carClass = new ModelClass(model, "Car");
        carClass.setSuperClass("Vehicle");
        ModelField modelName = new ModelField(new ModelClass(null, "String"), "modelName");
        XmlFieldMetadata modeNameMetaData = new XmlFieldMetadata();
        modeNameMetaData.setInsertParentFieldsUpTo("brand");
        modelName.addMetadata(modeNameMetaData);

        carClass.addField(modelName);
        model.addClass(carClass);

        List<ModelField> orderedFields = XmlModelHelpers.getFieldsForXml(carClass, new Version("0"));
        // unexpected order...
        assertEquals("id", orderedFields.get(0).getName());
        assertEquals("brand", orderedFields.get(1).getName());
        assertEquals("modelName", orderedFields.get(2).getName());
    }

    @Test
    public void getFieldsForXml_insertParentFieldsUpTo() {
        Model model = new Model();

        ModelClass entityClass = new ModelClass(model, "Entity");
        ModelField id = new ModelField(new ModelClass(null, "String"), "id");
        id.addMetadata(new XmlFieldMetadata());
        entityClass.addField(id);
        model.addClass(entityClass);

        ModelClass vehicleClass = new ModelClass(model, "Vehicle");
        vehicleClass.setSuperClass("Entity");
        ModelField brand = new ModelField(new ModelClass(null, "String"), "brand");
        XmlFieldMetadata brandMetadata = new XmlFieldMetadata();
        brandMetadata.setInsertParentFieldsUpTo("id");
        brand.addMetadata(brandMetadata);
        vehicleClass.addField(brand);
        model.addClass(vehicleClass);

        ModelClass carClass = new ModelClass(model, "Car");
        carClass.setSuperClass("Vehicle");
        ModelField modelName = new ModelField(new ModelClass(null, "String"), "modelName");
        XmlFieldMetadata modeNameMetaData = new XmlFieldMetadata();
        modeNameMetaData.setInsertParentFieldsUpTo("brand");
        modelName.addMetadata(modeNameMetaData);

        carClass.addField(modelName);
        model.addClass(carClass);

        List<ModelField> orderedFields = XmlModelHelpers.getFieldsForXml(carClass, new Version("0"));
        assertEquals("id", orderedFields.get(0).getName());
        assertEquals("brand", orderedFields.get(1).getName());
        assertEquals("modelName", orderedFields.get(2).getName());
    }
}
