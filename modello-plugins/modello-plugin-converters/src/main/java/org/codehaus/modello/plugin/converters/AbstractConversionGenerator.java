package org.codehaus.modello.plugin.converters;

import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.Version;
import org.codehaus.modello.plugin.java.AbstractJavaModelloGenerator;

public abstract class AbstractConversionGenerator
    extends AbstractJavaModelloGenerator
{
    protected static String getSourceClassName( ModelClass modelClass, Version generatedVersion )
    {
        return modelClass.getPackageName( true, generatedVersion ) + "." + modelClass.getName();
    }

}
