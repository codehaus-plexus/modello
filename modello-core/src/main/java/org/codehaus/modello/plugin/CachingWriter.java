package org.codehaus.modello.plugin;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.sonatype.plexus.build.incremental.BuildContext;

public class CachingWriter extends StringWriter
{
    private final BuildContext buildContext;
    private final Path path;
    private final Charset charset;

    public CachingWriter( BuildContext buildContext, Path path, Charset charset )
    {
        this.buildContext = buildContext;
        this.path = Objects.requireNonNull( path );
        this.charset = Objects.requireNonNull( charset );
    }

    @Override
    public void close() throws IOException
    {
        String str = getBuffer().toString();
        if ( Files.exists( path ) )
        {
            String old = readString( path, charset );
            if ( str.equals( old ) )
            {
                return;
            }
        }
        writeString( path, str, charset );
        if ( buildContext != null )
        {
            buildContext.refresh( path.toFile() );
        }
    }

    private static String readString( Path path, Charset charset ) throws IOException
    {
        byte[] ba = Files.readAllBytes( path );
        return new String( ba, charset );
    }

    private static void writeString( Path path, String str, Charset charset ) throws IOException
    {
        byte[] ba = str.getBytes( charset );
        Files.write( path, ba );
    }
}
