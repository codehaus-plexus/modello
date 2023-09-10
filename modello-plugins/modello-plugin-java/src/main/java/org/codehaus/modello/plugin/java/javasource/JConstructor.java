/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio, Inc.  For written permission,
 *    please contact info@codehaus.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio, Inc. Exolab is a registered
 *    trademark of Intalio, Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.codehaus.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO, INC. AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Intalio, Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.codehaus.modello.plugin.java.javasource;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

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
 * A class for handling source code for a constructor of a JClass
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a>
 * @version $Revision$ $Date$
 **/
public class JConstructor {

    /**
     * The set of modifiers for this JMethod
     **/
    private JModifiers modifiers = null;

    /**
     * List of parameters for this Constructor
     **/
    private Map<String, JParameter> params = null;

    /**
     * The Class in this JMember has been declared
     **/
    private JClass declaringClass = null;

    private JSourceCode sourceCode = null;

    private JAnnotations annotations = null;

    /**
     * Creates a new method with the given name and returnType.
     * For "void" return types, simply pass in null as the returnType
     *
     * @param declaringClass the declaring class for this constructor
     **/
    public JConstructor(JClass declaringClass) {
        this.declaringClass = declaringClass;
        this.modifiers = new JModifiers();
        this.params = new LinkedHashMap<>();
        this.sourceCode = new JSourceCode();
    }

    /**
     * Adds the given parameter to this Methods list of parameters
     * @param parameter the parameter to add to the this Methods
     * list of parameters.
     * @exception java.lang.IllegalArgumentException when a parameter already
     * exists for this Method with the same name as the new parameter
     **/
    public void addParameter(JParameter parameter) throws IllegalArgumentException {
        if (parameter == null) return;
        // -- check current params
        if (params.get(parameter.getName()) != null) {
            StringBuilder err = new StringBuilder();
            err.append("A parameter already exists for the constructor, ");
            err.append(this.declaringClass.getName());
            err.append(", with the name: ");
            err.append(parameter.getName());
            throw new IllegalArgumentException(err.toString());
        }

        params.put(parameter.getName(), parameter);
    } // -- addParameter

    /**
     * Returns the class in which this JMember has been declared
     * @return the class in which this JMember has been declared
     **/
    public JClass getDeclaringClass() {
        return this.declaringClass;
    } // -- getDeclaringClass

    /**
     * Returns the modifiers for this JConstructor
     * @return the modifiers for this JConstructor
     **/
    public JModifiers getModifiers() {
        return this.modifiers;
    } // -- getModifiers

    /**
     * Returns an array of JParameters consisting of the parameters
     * of this Method in declared order
     * @return a JParameter array consisting of the parameters
     * of this Method in declared order
     **/
    public JParameter[] getParameters() {
        return params.values().toArray(new JParameter[0]);
    } // -- getParameters

    public JSourceCode getSourceCode() {
        return this.sourceCode;
    } // -- getSourceCode

    public void print(JSourceWriter jsw) {
        JAnnotations annotations = getAnnotations();
        if (annotations != null) annotations.print(jsw);

        if (modifiers.isPrivate()) jsw.write("private");
        else if (modifiers.isProtected()) jsw.write("protected");
        else jsw.write("public");
        jsw.write(' ');
        jsw.write(declaringClass.getLocalName());
        jsw.write('(');

        // -- print parameters
        if (!params.isEmpty()) {
            Enumeration<JParameter> paramEnum = Collections.enumeration(params.values());
            jsw.write(paramEnum.nextElement());
            while (paramEnum.hasMoreElements()) {
                jsw.write(", ");
                jsw.write(paramEnum.nextElement());
            }
        }

        for (int i = 0; i < params.size(); i++) {}
        jsw.writeln(')');
        jsw.writeln('{');
        // jsw.indent();
        sourceCode.print(jsw);
        // jsw.unindent();
        if (!jsw.isNewline()) jsw.writeln();
        jsw.write("} //-- ");
        jsw.writeln(toString());
    } // -- print

    public void setModifiers(JModifiers modifiers) {
        this.modifiers = modifiers.copy();
        this.modifiers.setFinal(false);
    } // -- setModifiers

    public void setSourceCode(String sourceCode) {
        this.sourceCode = new JSourceCode(sourceCode);
    } // -- setSourceCode

    public void setSourceCode(JSourceCode sourceCode) {
        this.sourceCode = sourceCode;
    } // -- setSourceCode

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(declaringClass.getName());
        sb.append('(');

        // -- print parameters
        if (!params.isEmpty()) {
            Enumeration<JParameter> paramEnum = Collections.enumeration(params.values());
            sb.append(paramEnum.nextElement().getType().getName());
            while (paramEnum.hasMoreElements()) {
                sb.append(", ");
                sb.append(paramEnum.nextElement().getType().getName());
            }
        }
        sb.append(')');
        return sb.toString();
    } // -- toString

    /**
     * @return the annotations
     */
    public JAnnotations getAnnotations() {
        return annotations;
    }

    /**
     * @param annotation the annotation to append
     */
    public void appendAnnotation(String annotation) {
        if (annotations == null) {
            annotations = new JAnnotations();
        }
        annotations.appendAnnotation(annotation);
    }

    /**
     * @param annotations the annotations to set
     */
    public void setAnnotations(JAnnotations annotations) {
        this.annotations = annotations;
    }
} // -- JConstructor
