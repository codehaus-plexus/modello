package org.codehaus.modello.generator.xml.stax;

import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.Version;

import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 */
public class StaxGeneratorVersionInFieldTest
    extends AbstractStaxGeneratorTestCase
{
    public StaxGeneratorVersionInFieldTest()
    {
        super( "stax-version-in-field" );
    }

    public void testStaxReaderVersionInField()
        throws Throwable
    {
        Model model = modello.loadModel( getXmlResourceReader( "/version-in-field.mdo" ) );

        List<ModelClass> classesList = model.getClasses( new Version( "4.0.0" ) );

        assertEquals( 1, classesList.size() );

        ModelClass clazz = classesList.get( 0 );

        assertEquals( "Model", clazz.getName() );

        verifyModel( model, "org.codehaus.modello.generator.xml.stax.StaxVerifierVersionInField" );
    }

}
