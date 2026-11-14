package org.codehaus.modello.plugin.java;

/*
 * Copyright (c) 2026, Codehaus.org
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

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codehaus.modello.Modello;
import org.codehaus.modello.ModelloParameterConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Generates through {@link Modello}, the entry point of the CLI and the Ant task, in a container without a Maven
 * session. plexus-build-api's own {@code BuildContext} cannot be provisioned there, so this fails unless Modello
 * registers its standalone one.
 */
class StandaloneModelloTest {

    @TempDir
    Path outputDirectory;

    @Test
    void generatesJavaOutsideMaven() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ModelloParameterConstants.OUTPUT_DIRECTORY, outputDirectory.toString());
        parameters.put(ModelloParameterConstants.VERSION, "1.0.0");
        parameters.put(ModelloParameterConstants.PACKAGE_WITH_VERSION, Boolean.FALSE.toString());

        try (Reader model = Files.newBufferedReader(Paths.get("src/test/resources/models/builder.mdo"))) {
            new Modello().generate(model, "java", parameters);
        }

        try (Stream<Path> files = Files.walk(outputDirectory)) {
            List<Path> sources =
                    files.filter(path -> path.toString().endsWith(".java")).collect(Collectors.toList());
            assertFalse(sources.isEmpty(), "no Java sources generated");
        }
    }
}
