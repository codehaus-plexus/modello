package org.codehaus.modello.plugin.jdom;

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

import javax.inject.Inject;

import java.util.Map;

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;

/**
 * Test for issue where "Root" cannot be used as a class name in JDOM writer.
 * The generated code would have a variable name conflict between the method parameter
 * and a local variable both named "root".
 *
 * @author Copilot
 */
@PlexusTest
public class RootClassnameJDOMGeneratorTest extends AbstractModelloJavaGeneratorTest {
    @Inject
    private ModelloCore modello;

    public RootClassnameJDOMGeneratorTest() {
        super("root-classname");
    }

    @Test
    public void testJavaGenerator() throws Throwable {

        Model model = modello.loadModel(getXmlResourceReader("/root-classname.mdo"));

        Map<String, Object> parameters = getModelloParameters("1.0.0");

        modello.generate(model, "java", parameters);
        modello.generate(model, "jdom-writer", parameters);

        addDependency("org.jdom", "jdom");
        compileGeneratedSources();

        // If the code compiles successfully, the test passes
        // The compilation would fail if there was a variable name conflict
    }
}
