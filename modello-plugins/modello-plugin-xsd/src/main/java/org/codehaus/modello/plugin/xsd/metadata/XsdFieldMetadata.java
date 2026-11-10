package org.codehaus.modello.plugin.xsd.metadata;

/*
 * Copyright 2001-2007 The Codehaus.
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

import java.util.Collections;
import java.util.List;

import org.codehaus.modello.metadata.FieldMetadata;

/**
 * XsdFieldMetadata
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 */
public class XsdFieldMetadata implements FieldMetadata {
    public static final String ID = XsdFieldMetadata.class.getName();

    /**
     * Comma-separated allowed values for a strict {@code xs:restriction/xs:enumeration} constraint.
     * When set, the generated XSD uses a {@code simpleType} with {@code restriction base="xs:string"}
     * containing one {@code xs:enumeration} element per value. Instance documents that contain values
     * outside this list are schema-invalid.
     * <p>
     * Mutually exclusive with {@link #suggestions} and {@link #pattern}.
     * </p>
     * <p>
     * Modello syntax:
     * <pre>{@code
     * <field xsd.enumerations="compile,runtime,provided,test,system">
     *   <name>scope</name>
     *   <type>String</type>
     * </field>
     * }</pre>
     */
    private List<String> enumerations = Collections.emptyList();

    /**
     * Comma-separated suggested values for a non-strict {@code xs:union} completion hint.
     * When set, the generated XSD uses a {@code simpleType} with {@code union} of a fixed
     * enumeration and {@code xs:string}, so IDEs offer autocompletion while still accepting
     * any custom value.
     * <p>
     * Mutually exclusive with {@link #enumerations} and {@link #pattern}.
     * </p>
     * <p>
     * Modello syntax:
     * <pre>{@code
     * <field xsd.suggestions="jar,pom,war,ear,maven-plugin">
     *   <name>packaging</name>
     *   <type>String</type>
     * </field>
     * }</pre>
     */
    private List<String> suggestions = Collections.emptyList();

    /**
     * A regex pattern for a {@code xs:restriction/xs:pattern} constraint.
     * When set, the generated XSD uses a {@code simpleType} with {@code restriction base="xs:string"}
     * containing an {@code xs:pattern} element.
     * <p>
     * Mutually exclusive with {@link #enumerations} and {@link #suggestions}.
     * </p>
     * <p>
     * Modello syntax:
     * <pre>{@code
     * <field xsd.pattern="[a-zA-Z0-9._\-]+">
     *   <name>groupId</name>
     *   <type>String</type>
     * </field>
     * }</pre>
     */
    private String pattern;

    public List<String> getEnumerations() {
        return Collections.unmodifiableList(enumerations);
    }

    public void setEnumerations(List<String> enumerations) {
        this.enumerations = enumerations != null ? enumerations : Collections.emptyList();
    }

    public List<String> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions != null ? suggestions : Collections.emptyList();
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
