import java.io.*;

File readerClass = new File( basedir, "target/classes/test/io/stax/ThingStaxReader.class" );
if ( !readerClass.isFile() )
{
    throw new FileNotFoundException( "Missing " + readerClass );
}

File writerClass = new File( basedir, "target/classes/test/io/stax/ThingStaxWriter.class" );
if ( !writerClass.isFile() )
{
    throw new FileNotFoundException( "Missing " + writerClass );
}
