package org.codehaus.modello.generator.xml.stax;

import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.Version;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.ReaderFactory;

import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id: StaxGeneratorTest.java 675 2006-11-16 10:58:59Z brett $
 */
public class StaxGeneratorVersionInNamespaceTest
    extends AbstractStaxGeneratorTestCase
{
    public StaxGeneratorVersionInNamespaceTest()
        throws ComponentLookupException
    {
        super( "stax-version-in-namespace" );
    }

    public void testStaxReaderVersionInField()
        throws Throwable
    {
        Model model =
            modello.loadModel( ReaderFactory.newXmlReader( getTestFile( "src/test/resources/version-in-namespace.mdo" ) ) );

        List classesList = model.getClasses( new Version( "4.0.0" ) );

        assertEquals( 1, classesList.size() );

        ModelClass clazz = (ModelClass) classesList.get( 0 );

        assertEquals( "Model", clazz.getName() );

        verifyModel( model, "org.codehaus.modello.generator.xml.stax.StaxVerifierVersionInNamespace" );
    }

}
