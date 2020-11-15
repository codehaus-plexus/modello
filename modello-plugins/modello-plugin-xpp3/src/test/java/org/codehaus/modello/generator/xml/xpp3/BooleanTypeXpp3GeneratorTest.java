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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Properties;

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;

public class BooleanTypeXpp3GeneratorTest
    extends AbstractModelloJavaGeneratorTest
{
    public BooleanTypeXpp3GeneratorTest()
    {
        super( "testBooleanType" );
    }

    public void testBooleanType()
        throws Throwable
    {
        ModelloCore modello = (ModelloCore) lookup( ModelloCore.ROLE );

        Model model = modello.loadModel( getXmlResourceReader( "/boolean.mdo" ) );

        Properties parameters = getModelloParameters( "1.0.0", 5 ); // autoboxing

        modello.generate( model, "java", parameters );
        modello.generate( model, "xpp3-reader", parameters );

        compileGeneratedSources( 5 );
    }
}
