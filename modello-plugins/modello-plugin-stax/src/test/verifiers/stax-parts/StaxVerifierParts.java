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

import junit.framework.Assert;
import org.codehaus.modello.test.model.parts.Model;
import org.codehaus.modello.test.model.parts.SingleReference;
import org.codehaus.modello.test.model.parts.Reference;
import org.codehaus.modello.test.model.parts.DummyReference;
import org.codehaus.modello.test.model.parts.DummyIdReference;
import org.codehaus.modello.test.model.parts.io.stax.PartsStaxReader;
import org.codehaus.modello.test.model.parts.io.stax.PartsStaxWriter;
import org.codehaus.modello.verifier.Verifier;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;

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

        Reader reader = ReaderFactory.newXmlReader( file );
        PartsStaxReader modelReader = new PartsStaxReader();

        Model model = modelReader.read( reader );

        Assert.assertNotNull( model.getSingleReference() );
        Assert.assertNotNull( model.getSingleReference().getReference() );
        Assert.assertEquals( "single", model.getSingleReference().getReference().getId() );
        Assert.assertEquals( "Single Reference", model.getSingleReference().getReference().getName() );
        Assert.assertEquals( "single", model.getSecondReference().getReference().getId() );
        Assert.assertEquals( "Single Reference", model.getSecondReference().getReference().getName() );
        Assert.assertEquals( "other", model.getThirdReference().getReference().getId() );
        Assert.assertEquals( "Other Reference", model.getThirdReference().getReference().getName() );
        Assert.assertNull( model.getNullReference().getReference() );
        Assert.assertEquals( "single", model.getDualReference().getFirst().getId() );
        Assert.assertEquals( "Single Reference", model.getDualReference().getFirst().getName() );
        Assert.assertEquals( "other", model.getDualReference().getSecond().getId() );
        Assert.assertEquals( "Other Reference", model.getDualReference().getSecond().getName() );
        Assert.assertEquals( "single", model.getDupeReference().getFirst().getId() );
        Assert.assertEquals( "Single Reference", model.getDupeReference().getFirst().getName() );
        Assert.assertEquals( "single", model.getDupeReference().getSecond().getId() );
        Assert.assertEquals( "Single Reference", model.getDupeReference().getSecond().getName() );
        Assert.assertEquals( "single", ((Reference)model.getReferenceList().getItems().get( 0 )).getId() );
        Assert.assertEquals( "Single Reference", ((Reference)model.getReferenceList().getItems().get( 0 )).getName() );
        Assert.assertEquals( "single", ((Reference)model.getReferenceList().getItems().get( 1 )).getId() );
        Assert.assertEquals( "Single Reference", ((Reference)model.getReferenceList().getItems().get( 1 )).getName() );
        Assert.assertEquals( "other", ((Reference)model.getReferenceList().getItems().get( 2 )).getId() );
        Assert.assertEquals( "Other Reference", ((Reference)model.getReferenceList().getItems().get( 2 )).getName() );
        Assert.assertEquals( "another", ((Reference)model.getReferenceList().getItems().get( 3 )).getId() );
        Assert.assertEquals( "Another Reference", ((Reference)model.getReferenceList().getItems().get( 3 )).getName() );
        Assert.assertEquals( "other", ((SingleReference)model.getSingleReferences().get( 0 )).getReference().getId() );
        Assert.assertEquals( "Other Reference", ((SingleReference)model.getSingleReferences().get( 0 )).getReference().getName() );
        Assert.assertEquals( "single", ((SingleReference)model.getSingleReferences().get( 1 )).getReference().getId() );
        Assert.assertEquals( "Single Reference", ((SingleReference)model.getSingleReferences().get( 1 )).getReference().getName() );
        Assert.assertEquals( "another", ((SingleReference)model.getSingleReferences().get( 2 )).getReference().getId() );
        Assert.assertEquals( "Another Reference", ((SingleReference)model.getSingleReferences().get( 2 )).getReference().getName() );
        Assert.assertEquals( "parent", model.getNestedReference().getId() );
        Assert.assertEquals( model.getNestedReference(), model.getNestedReference().getChildReference().getParentReference() );
        Assert.assertEquals( 3, model.getReferences().size() );
        Assert.assertNotNull( model.getDummyReference() );
        Assert.assertNotNull( model.getDummyReference().getReference() );
        Assert.assertEquals( "Dummy 2", model.getDummyReference().getReference().getName() );
        Assert.assertEquals( "Description 2", model.getDummyReference().getReference().getDescription() );
        Assert.assertNotNull( model.getOtherDummyReference() );
        Assert.assertNotNull( model.getOtherDummyReference().getReference() );
        Assert.assertEquals( "Dummy 1", model.getOtherDummyReference().getReference().getName() );
        Assert.assertEquals( "Description 1", model.getOtherDummyReference().getReference().getDescription() );
        Assert.assertEquals( 3, model.getDummyIdReferences().size() );
        Assert.assertEquals( 4, model.getDummyReferences().size() );
        Assert.assertEquals( "Dummy 3", ((DummyReference)model.getDummyReferences().get( 0 )).getReference().getName() );
        Assert.assertEquals( "Description 3", ((DummyReference)model.getDummyReferences().get( 0 )).getReference().getDescription() );
        Assert.assertEquals( "Dummy 1", ((DummyReference)model.getDummyReferences().get( 1 )).getReference().getName() );
        Assert.assertEquals( "Description 1", ((DummyReference)model.getDummyReferences().get( 1 )).getReference().getDescription() );
        Assert.assertEquals( "Dummy 1", ((DummyReference)model.getDummyReferences().get( 2 )).getReference().getName() );
        Assert.assertEquals( "Description 1", ((DummyReference)model.getDummyReferences().get( 2 )).getReference().getDescription() );
        Assert.assertEquals( "Dummy 2", ((DummyReference)model.getDummyReferences().get( 3 )).getReference().getName() );
        Assert.assertEquals( "Description 2", ((DummyReference)model.getDummyReferences().get( 3 )).getReference().getDescription() );
        Assert.assertEquals( 4, model.getDummyPointers().size() );
        Assert.assertEquals( "Dummy 3", ((DummyIdReference)model.getDummyPointers().get( 0 )).getName() );
        Assert.assertEquals( "Description 3", ((DummyIdReference)model.getDummyPointers().get( 0 )).getDescription() );
        Assert.assertEquals( "Dummy 1", ((DummyIdReference)model.getDummyPointers().get( 1 )).getName() );
        Assert.assertEquals( "Description 1", ((DummyIdReference)model.getDummyPointers().get( 1 )).getDescription() );
        Assert.assertEquals( "Dummy 1", ((DummyIdReference)model.getDummyPointers().get( 2 )).getName() );
        Assert.assertEquals( "Description 1", ((DummyIdReference)model.getDummyPointers().get( 2 )).getDescription() );
        Assert.assertEquals( "Dummy 2", ((DummyIdReference)model.getDummyPointers().get( 3 )).getName() );
        Assert.assertEquals( "Description 2", ((DummyIdReference)model.getDummyPointers().get( 3 )).getDescription() );


        String expected = FileUtils.fileRead( file );

        PartsStaxWriter modelWriter = new PartsStaxWriter();
        StringWriter w = new StringWriter();
        modelWriter.write( w, model );
        Assert.assertEquals( cleanLineEndings( expected ).trim(), scrubXmlDeclQuotes( w.toString() ).trim() );
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
