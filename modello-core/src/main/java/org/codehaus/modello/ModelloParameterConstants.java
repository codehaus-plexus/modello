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
public class ModelloParameterConstants {
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
     * Replaces USE_JAVA5
     * @since 2.0
     */
    public static final String OUTPUT_JAVA_SOURCE = "modello.output.java.source";

    public static final String OUTPUT_JAVA_SOURCE_DEFAULT = "8";

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

    /**
     * @since 1.10
     */
    public static final String EXTENDED_CLASSNAME_SUFFIX = "modello.xpp3.extended.suffix";

    /**
     * Boolean flag enforcing existence of mandatory elements in the XSD.
     * If set to {@code false} will not require mandatory elements in the XML which can be useful if the XML is post processed (e.g. POM merging with parents)
     * where mandatory elements might be contributed by sources outside the XML.
     * @since 2.1
     */
    public static final String XSD_ENFORCE_MANDATORY_ELEMENTS = "modello.xsd.enforce.mandatory.element";

    /**
     * The license text as list of strings, to be added to generated files, if needed.
     *
     * @since 2.3.1
     */
    public static final String LICENSE_TEXT = "modello.license.text";

    /**
     * Additional plural to singular exceptions
     * @since 2.5.0
     */
    public static final String PLURAL_EXCEPTIONS = "modello.plural.exceptions";

    private ModelloParameterConstants() {}
}
