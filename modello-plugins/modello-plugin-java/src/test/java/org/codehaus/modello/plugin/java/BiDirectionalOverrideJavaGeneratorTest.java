package org.codehaus.modello.plugin.java;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.inject.Inject;

import java.util.Map;

import org.codehaus.modello.AbstractModelloJavaGeneratorTest;
import org.codehaus.modello.core.ModelloCore;
import org.codehaus.modello.model.Model;
import org.codehaus.plexus.testing.PlexusTest;
import org.junit.jupiter.api.Test;

/**
 * BiDirectionalOverrideJavaGeneratorTest
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
@PlexusTest
public class BiDirectionalOverrideJavaGeneratorTest extends AbstractModelloJavaGeneratorTest {
    @Inject
    private ModelloCore modello;

    public BiDirectionalOverrideJavaGeneratorTest() {
        super("bidirectional");
    }

    @Test
    public void testJavaGenerator() throws Throwable {
        Model model = modello.loadModel(getXmlResourceReader("/models/bidirectional-override.mdo"));

        Map<String, Object> parameters = getModelloParameters("1.0.0");

        modello.generate(model, "java", parameters);

        compileGeneratedSources();

        verifyCompiledGeneratedSources("JavaVerifier");
    }
}
