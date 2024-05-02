package org.codehaus.modello;

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
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.XmlStreamReader;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ModelloCli {
    private static File modelFile;

    private static String outputType;

    private static Map<String, Object> parameters;

    public static void main(String[] args) throws Exception {
        Modello modello = new DefaultPlexusContainer().lookup(Modello.class);

        parseArgumentsFromCommandLine(args);

        modello.generate(new XmlStreamReader(modelFile), outputType, parameters);
    }

    public static void parseArgumentsFromCommandLine(String[] args) throws Exception {
        if (args.length < 6) {
            usage();

            System.exit(1);
        }

        modelFile = new File(args[0]);

        outputType = args[1];

        parameters = new HashMap<>();

        String outputDirectory = args[2];

        if (StringUtils.isEmpty(outputDirectory)) {
            System.err.println("Missing required parameter: output directory");

            usage();

            System.exit(1);
        }

        parameters.put(ModelloParameterConstants.OUTPUT_DIRECTORY, outputDirectory);

        String modelVersion = args[3];

        if (StringUtils.isEmpty(modelVersion)) {
            System.err.println("Missing required parameter: model version");

            usage();

            System.exit(1);
        }

        parameters.put(ModelloParameterConstants.VERSION, modelVersion);

        String packageWithVersion = args[4];

        if (StringUtils.isEmpty(packageWithVersion)) {
            System.err.println("Missing required parameter: package with version");

            usage();

            System.exit(1);
        }

        parameters.put(ModelloParameterConstants.PACKAGE_WITH_VERSION, packageWithVersion);

        String javaSource = args[5];

        if (StringUtils.isEmpty(javaSource)) {
            System.err.println("Missing required parameter: Java Source");

            usage();

            System.exit(1);
        }

        parameters.put(ModelloParameterConstants.OUTPUT_JAVA_SOURCE, javaSource);

        if (args.length > 6) {
            parameters.put(ModelloParameterConstants.ENCODING, args[6]);
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static void usage() {
        System.err.println("Usage: modello <model> <outputType> <output directory> <modelVersion> <packageWithVersion>"
                + "<javaSource> [<encoding>]");
    }
}
