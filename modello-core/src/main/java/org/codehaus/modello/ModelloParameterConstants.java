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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ModelloParameterConstants
{
    public static final String VERSION = "modello.version";

    public static final String OUTPUT_DIRECTORY = "modello.output.directory";

    public static final String PACKAGE_WITH_VERSION = "modello.package.with.version";

    public static final String STRICT_PARSER = "modello.strict.parser";

    public static final String ALL_VERSIONS = "modello.all.versions";

    public static final String FILENAME = "modello.output.filename";

    public static final String FIRST_VERSION = "modello.first.version";

    /**
     * @since 1.0-alpha-19
     */
    public static final String ENCODING = "modello.output.encoding";

    /**
     * @since 1.0
     */
    public static final String USE_JAVA5 = "modello.output.useJava5";

    public static final String OUTPUT_XDOC_FILE_NAME = "modello.output.xdoc.file";

    public static final String OUTPUT_XSD_FILE_NAME = "modello.output.xsd.file";

    /**
     * @since 1.8
     */
    public static final String OUTPUT_JSONSCHEMA_FILE_NAME = "modello.output.jsonschema.file";

    /**
     * @since 1.6
     */
    public static final String DOM_AS_XPP3 = "modello.dom.xpp3";

    private ModelloParameterConstants()
    {
    }
}
