package org.codehaus.modello.generator.xml.stax;

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

import org.junit.jupiter.api.Assertions;
import org.codehaus.modello.test.model.parts.Model;
import org.codehaus.modello.test.model.parts.SingleReference;
import org.codehaus.modello.test.model.parts.Reference;
import org.codehaus.modello.test.model.parts.DummyReference;
import org.codehaus.modello.test.model.parts.DummyIdReference;
import org.codehaus.modello.test.model.parts.io.stax.PartsStaxReader;
import org.codehaus.modello.test.model.parts.io.stax.PartsStaxWriter;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.xml.XmlStreamReader;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import javax.xml.stream.XMLStreamException;

/**
 */
public class StaxVerifierParts
    extends Verifier
{
    public void verify()
        throws IOException, XMLStreamException
    {
        File file = new File( "src/test/verifiers/stax-parts/parts.xml" );

        Reader reader = new XmlStreamReader( file );
        PartsStaxReader modelReader = new PartsStaxReader();

        Model model = modelReader.read( reader );

        Assertions.assertNotNull( model.getSingleReference() );
        Assertions.assertNotNull( model.getSingleReference().getReference() );
        Assertions.assertEquals( "single", model.getSingleReference().getReference().getId() );
        Assertions.assertEquals( "Single Reference", model.getSingleReference().getReference().getName() );
        Assertions.assertEquals( "single", model.getSecondReference().getReference().getId() );
        Assertions.assertEquals( "Single Reference", model.getSecondReference().getReference().getName() );
        Assertions.assertEquals( "other", model.getThirdReference().getReference().getId() );
        Assertions.assertEquals( "Other Reference", model.getThirdReference().getReference().getName() );
        Assertions.assertNull( model.getNullReference().getReference() );
        Assertions.assertEquals( "single", model.getDualReference().getFirst().getId() );
        Assertions.assertEquals( "Single Reference", model.getDualReference().getFirst().getName() );
        Assertions.assertEquals( "other", model.getDualReference().getSecond().getId() );
        Assertions.assertEquals( "Other Reference", model.getDualReference().getSecond().getName() );
        Assertions.assertEquals( "single", model.getDupeReference().getFirst().getId() );
        Assertions.assertEquals( "Single Reference", model.getDupeReference().getFirst().getName() );
        Assertions.assertEquals( "single", model.getDupeReference().getSecond().getId() );
        Assertions.assertEquals( "Single Reference", model.getDupeReference().getSecond().getName() );
        Assertions.assertEquals( "single", ((Reference)model.getReferenceList().getItems().get( 0 )).getId() );
        Assertions.assertEquals( "Single Reference", ((Reference)model.getReferenceList().getItems().get( 0 )).getName() );
        Assertions.assertEquals( "single", ((Reference)model.getReferenceList().getItems().get( 1 )).getId() );
        Assertions.assertEquals( "Single Reference", ((Reference)model.getReferenceList().getItems().get( 1 )).getName() );
        Assertions.assertEquals( "other", ((Reference)model.getReferenceList().getItems().get( 2 )).getId() );
        Assertions.assertEquals( "Other Reference", ((Reference)model.getReferenceList().getItems().get( 2 )).getName() );
        Assertions.assertEquals( "another", ((Reference)model.getReferenceList().getItems().get( 3 )).getId() );
        Assertions.assertEquals( "Another Reference", ((Reference)model.getReferenceList().getItems().get( 3 )).getName() );
        Assertions.assertEquals( "other", ((SingleReference)model.getSingleReferences().get( 0 )).getReference().getId() );
        Assertions.assertEquals( "Other Reference", ((SingleReference)model.getSingleReferences().get( 0 )).getReference().getName() );
        Assertions.assertEquals( "single", ((SingleReference)model.getSingleReferences().get( 1 )).getReference().getId() );
        Assertions.assertEquals( "Single Reference", ((SingleReference)model.getSingleReferences().get( 1 )).getReference().getName() );
        Assertions.assertEquals( "another", ((SingleReference)model.getSingleReferences().get( 2 )).getReference().getId() );
        Assertions.assertEquals( "Another Reference", ((SingleReference)model.getSingleReferences().get( 2 )).getReference().getName() );
        Assertions.assertEquals( "parent", model.getNestedReference().getId() );
        Assertions.assertEquals( model.getNestedReference(), model.getNestedReference().getChildReference().getParentReference() );
        Assertions.assertEquals( 3, model.getReferences().size() );
        Assertions.assertNotNull( model.getDummyReference() );
        Assertions.assertNotNull( model.getDummyReference().getReference() );
        Assertions.assertEquals( "Dummy 2", model.getDummyReference().getReference().getName() );
        Assertions.assertEquals( "Description 2", model.getDummyReference().getReference().getDescription() );
        Assertions.assertNotNull( model.getOtherDummyReference() );
        Assertions.assertNotNull( model.getOtherDummyReference().getReference() );
        Assertions.assertEquals( "Dummy 1", model.getOtherDummyReference().getReference().getName() );
        Assertions.assertEquals( "Description 1", model.getOtherDummyReference().getReference().getDescription() );
        Assertions.assertEquals( 3, model.getDummyIdReferences().size() );
        Assertions.assertEquals( 4, model.getDummyReferences().size() );
        Assertions.assertEquals( "Dummy 3", ((DummyReference)model.getDummyReferences().get( 0 )).getReference().getName() );
        Assertions.assertEquals( "Description 3", ((DummyReference)model.getDummyReferences().get( 0 )).getReference().getDescription() );
        Assertions.assertEquals( "Dummy 1", ((DummyReference)model.getDummyReferences().get( 1 )).getReference().getName() );
        Assertions.assertEquals( "Description 1", ((DummyReference)model.getDummyReferences().get( 1 )).getReference().getDescription() );
        Assertions.assertEquals( "Dummy 1", ((DummyReference)model.getDummyReferences().get( 2 )).getReference().getName() );
        Assertions.assertEquals( "Description 1", ((DummyReference)model.getDummyReferences().get( 2 )).getReference().getDescription() );
        Assertions.assertEquals( "Dummy 2", ((DummyReference)model.getDummyReferences().get( 3 )).getReference().getName() );
        Assertions.assertEquals( "Description 2", ((DummyReference)model.getDummyReferences().get( 3 )).getReference().getDescription() );
        Assertions.assertEquals( 4, model.getDummyPointers().size() );
        Assertions.assertEquals( "Dummy 3", ((DummyIdReference)model.getDummyPointers().get( 0 )).getName() );
        Assertions.assertEquals( "Description 3", ((DummyIdReference)model.getDummyPointers().get( 0 )).getDescription() );
        Assertions.assertEquals( "Dummy 1", ((DummyIdReference)model.getDummyPointers().get( 1 )).getName() );
        Assertions.assertEquals( "Description 1", ((DummyIdReference)model.getDummyPointers().get( 1 )).getDescription() );
        Assertions.assertEquals( "Dummy 1", ((DummyIdReference)model.getDummyPointers().get( 2 )).getName() );
        Assertions.assertEquals( "Description 1", ((DummyIdReference)model.getDummyPointers().get( 2 )).getDescription() );
        Assertions.assertEquals( "Dummy 2", ((DummyIdReference)model.getDummyPointers().get( 3 )).getName() );
        Assertions.assertEquals( "Description 2", ((DummyIdReference)model.getDummyPointers().get( 3 )).getDescription() );


        String expected = FileUtils.fileRead( file );

        PartsStaxWriter modelWriter = new PartsStaxWriter();
        StringWriter w = new StringWriter();
        modelWriter.write( w, model );
        Assertions.assertEquals( cleanLineEndings( expected ).trim(), scrubXmlDeclQuotes( w.toString() ).trim() );
    }

    private String scrubXmlDeclQuotes( String s )
    {
        s = cleanLineEndings( s );

        if ( s.startsWith( "<?xml version='1.0' encoding='UTF-8'?>"))
        {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + s.substring( "<?xml version='1.0' encoding='UTF-8'?>".length() );
        }
        return s;
    }

    private String cleanLineEndings( String s )
    {
        return s.replaceAll( "\r\n", "\n" );
    }
}
