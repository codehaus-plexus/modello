package org.codehaus.modello.generator.xml.xpp3;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import junit.framework.Assert;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Reader;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import org.apache.maven.plugins.changes.model.io.xpp3.*;
import org.apache.maven.plugins.changes.model.*;

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 27 juil. 2008
 * @version $Id$
 */
public class Xpp3ChangesVerifier
    extends Verifier
{
    /**
     * 
     */
    public void verify()
        throws Exception
    {

        verifyReader();
        
        verifyWriter();
    }



    public void verifyReader()
        throws IOException, XmlPullParserException
    {
        ChangesXpp3Reader changesReader = new ChangesXpp3Reader();
        File file = new File( "src/test/verifiers/xpp3-changes/changes.xml" );
        Reader reader = ReaderFactory.newXmlReader( file );
        
        Document actual = changesReader.read( reader );

        
    }
    
    public void verifyWriter()
        throws IOException, XmlPullParserException
    {
        ChangesXpp3Writer changesXpp3Writer = new ChangesXpp3Writer();

        Document document = new Document();
        Body body = new Body();
        document.setBody( body );

        Release release = new Release();
        body.addRelease( release );

        Action action = new Action();
        release.addAction( action );
        action.setAction( "test action content" );
        action.setDev( "olamy" );

        changesXpp3Writer.write( new FileWriter( "target/changes.xml" ), document );

        File changes = new File("target/changes.xml");
        Xpp3Dom xpp3Dom = Xpp3DomBuilder.build( ReaderFactory.newXmlReader( changes ) );
        Xpp3Dom actionElem = xpp3Dom.getChild( "body" ).getChild( "release" ).getChild( "action" ); 
        String dev = actionElem.getAttribute( "dev" );
        Assert.assertEquals( "olamy", dev );
        Assert.assertEquals( "test action content", actionElem.getValue() );          
        
    }    

}
