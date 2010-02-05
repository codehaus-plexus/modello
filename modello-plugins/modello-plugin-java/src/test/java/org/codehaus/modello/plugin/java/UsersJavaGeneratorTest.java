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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;

/**
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id: TmpJavaGeneratorTest.java 1125 2009-01-10 20:29:32Z hboutemy $
 */
public class UsersJavaGeneratorTest
    extends AbstractModelloJavaGeneratorTest
{
    private String modelFile = "src/test/resources/models/users.mdo";

    public UsersJavaGeneratorTest()
    {
        super( "users" );
    }

    public void setUp()
        throws Exception
    {
        super.setUp();
        // TODO: Add this to genrate Java with annotations
        // setSourceVersion( "1.5" );
        // setTargetVersion( "1.5" );
        addDependency( "javax.xml.bind", "jaxb-api", "2.1" );
        addDependency( "org.apache.geronimo.specs", "geronimo-jpa_2.0_spec", "1.0" );
    }

    /**
     * MODELLO-83
     *
     * @throws Throwable
     */
    public void testJavaGeneratorWithUsers()
        throws Throwable
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Properties parameters = new Properties();
        parameters.setProperty( ModelloParameterConstants.OUTPUT_DIRECTORY, getOutputDirectory().getAbsolutePath() );
        parameters.setProperty( ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.toString( false ) );
        parameters.setProperty( ModelloParameterConstants.VERSION, "1.0.0" );
        parameters.setProperty( ModelloParameterConstants.USE_JAVA5, Boolean.toString( true ) );
        Model model = modello.loadModel( ReaderFactory.newXmlReader( getTestFile( modelFile ) ) );

        modello.generate( model, "java", parameters );

        try
        {
            compile( getOutputDirectory(), getOutputClasses(), true );
            assertTrue( true );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            fail( e.getMessage() );
        }

        FileInputStream inputStream = null;
        String groupClassText = "";
        try
        {
            inputStream = new FileInputStream( new File( getOutputDirectory().getAbsolutePath(), "model/Group.java" ) );
            groupClassText = IOUtil.toString( inputStream );
        }
        finally
        {
            IOUtil.close( inputStream );
        }

        // we could check a little more robust then this.
        assertTrue( "class:\n" + groupClassText, groupClassText.contains( "@javax.persistence.JoinColumn" ) );
    }
}