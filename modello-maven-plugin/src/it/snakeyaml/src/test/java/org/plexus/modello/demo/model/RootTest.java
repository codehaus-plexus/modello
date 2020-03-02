package org.plexus.modello.demo.model;

import org.plexus.modello.demo.model.io.snakeyaml.*;

import java.io.*;

public class RootTest {
    private static Root createRoot(String fieldValue) {
        Root r = new Root();
        r.setSimpleField(fieldValue);
        return r;
    }

    private static String asYamlString(Root root) throws IOException {
        ModelSnakeYamlWriter writer = new ModelSnakeYamlWriter();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos)) {
            writer.write(osw, root);
            return baos.toString();
        }
    }

    public void testWritingYaml() throws IOException {
        Root root = createRoot("modello IT");

        String rootAsYaml = asYamlString(root);

        String expected = "%YAML 1.1" + "\n"                        // directive used to identify the version of YAML
                        + "---" + "\n"                              // document separator
                        + "\"simpleField\": \"modello IT\"" + "\n"; // actual Root
        assert expected.equals(rootAsYaml): "Actual: [" + rootAsYaml + "]";
    }
}

