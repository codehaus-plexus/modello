package org.codehaus.modello;

/*
 * LICENSE
 */

/**
 * This test uses the maven model as this is the most complete and by far the
 * biggest model using modello.
 * 
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class MavenModelTest
    extends ModelloTest
{
    public void testMavenModel()
        throws ModelloException, ModelValidationException
    {
        ModelBuilder builder = getModelBuilder();

        builder.getModel( getTestFile( "src/test/resources/models/maven.mdo" ) );
    }
}
