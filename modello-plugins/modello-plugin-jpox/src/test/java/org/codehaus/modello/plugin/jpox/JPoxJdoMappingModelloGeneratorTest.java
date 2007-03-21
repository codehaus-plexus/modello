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

import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;


/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class JPoxJdoMappingModelloGeneratorTest extends AbstractJpoxGeneratorTestCase
{
    public JPoxJdoMappingModelloGeneratorTest()
    {
        super( "jpox-jdo-mapping" );
    }

    public void testSimpleInvocation() throws Exception
    {
        ModelloCore core = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = core.loadModel( new FileReader( getTestPath( "src/test/resources/mergere-tissue.mdo" ) ) );

        // ----------------------------------------------------------------------
        // Generate the code
        // ----------------------------------------------------------------------

        Properties parameters = new Properties();

        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getGeneratedSources().getAbsolutePath() );

        parameters.setProperty( ModelloParameterConstants.VERSION, "1.0.0" );

        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.FALSE.toString() );

        core.generate( model, "jpox-jdo-mapping", parameters );

        // ----------------------------------------------------------------------
        // Assert
        // ----------------------------------------------------------------------

        assertGeneratedFileExists( "package.jdo" );

        SAXReader reader = new SAXReader();
        reader.setEntityResolver( new JdoEntityResolver() );
        Document jdoDocument = reader.read( new File( "target/" + getName() + "/package.jdo" ) );

        assertNotNull( jdoDocument );

        // Tree should consist of only elements with attributes. NO TEXT.
        assertNoTextNodes( jdoDocument, "//jdo", true );

        assertAttributeEquals( jdoDocument,
                               "//class[@name='TissueModelloMetadata']/field[@name='modelVersion']/column",
                               "default-value", "1.0.0" );

        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='summary']", "persistence-modifier",
                               "none" );

        // -----------------------------------------------------------------------
        // Association Tests.

        //   mdo/association/jpox.dependent-element == false (only on association with "*" multiplicity (default type)
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='friends']/collection",
                               "dependent-element", "false" );

        //   mdo/association (default type) with "1" multiplicity.
        assertElementNotExists( jdoDocument, "//class[@name='Issue']/field[@name='assignee']/collection" );

        //   mdo/association (map) with "*" multiplicity.
        assertElementExists( jdoDocument, "//class[@name='Issue']/field[@name='configuration']/map" );

        // -----------------------------------------------------------------------
        // Fetch Group Tests
        assertAttributeMissing( jdoDocument, "//class[@name='Issue']/field[@name='reporter']", "default-fetch-group" );
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='configuration']",
                               "default-fetch-group", "true" );
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='assignee']", "default-fetch-group",
                               "false" );

        // -----------------------------------------------------------------------
        // Value Strategy Tests
        //   defaulted
        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='id']", "value-strategy",
                               "native" );
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='accountId']", "value-strategy",
                               "native" );
        //   intentionally unset
        assertAttributeMissing( jdoDocument, "//class[@name='User']/field[@name='id']", "value-strategy" );

        // -----------------------------------------------------------------------
        // Superclass Tests
        assertAttributeEquals( jdoDocument, "//class[@name='User']", "persistence-capable-superclass",
                               "org.mergere.user.AbstractUser" );

        // -----------------------------------------------------------------------
        // Primary Key Tests
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='accountId']", "primary-key", "true" );
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='summary']", "primary-key", "false" );

        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='id']", "primary-key", "true" );
        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='username']", "primary-key",
                               "false" );
        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='fullName']", "primary-key",
                               "false" );
        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='email']", "primary-key",
                               "false" );
        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='locked']", "primary-key",
                               "false" );
        assertAttributeEquals( jdoDocument, "//class[@name='ComplexIdentity']/field[@name='lastLoginDate']",
                               "primary-key", "false" );

        // -----------------------------------------------------------------------
        // Alternate Table and Column Names Tests.
        assertAttributeEquals( jdoDocument, "//class[@name='DifferentTable']", "table", "MyTable" );
        assertAttributeEquals( jdoDocument, "//class[@name='Issue']/field[@name='accountId']/column", "name", "id" );
    }
}
