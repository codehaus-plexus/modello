package org.codehaus.modello.plugin;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.ModelloParameterConstants;
import org.codehaus.modello.model.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractModelloGeneratorTest {

    @CsvSource({
        ",",
        "'',''",
        "s,s",

        // Known exceptions
        "sheep, sheep",
        "fish, fish",
        "deer, deer",
        "aircraft, aircraft",
        "bison, bison",
        "elk, elk",

        // Regular plural forms with suffixes
        "voes, voe",
        "hoes, hoe",
        "canoes, canoe",
        "toes, toe",
        "foes, foe",
        "oboes, oboe",
        "boxes, box",
        "cars, car",
        "dogs, dog",
        "cats, cat",
        "horses, horse",
        "foxes, fox",

        // Some test cases with different rules
        "archives, archive",
        "otherArchives, otherArchive",
        "Archives, Archive",
        "babies, baby",
        "parties, party",
        "cities, city",
        "boxes, box",
        "churches, church",
        "matches, match",
        "watches, watch",
        "riches, rich",
        "lunches, lunch",
        "relatives, relative",

        // More edge cases
        "phases, phase",
        "shoes, shoe",

        // other examples
        "ids, id",
        "licenses, license",
        "repositories, repository",
        "roles, role",

        // existing non grammar conversions
        "superclasses, superclasse",
        "classes, classe"
    })
    @ParameterizedTest
    void testSingular(String plural, String singular) {
        assertEquals(
                singular,
                AbstractModelloGenerator.singular(plural),
                "singular of: " + plural + " should be: " + singular);
    }

    static class TestGenerator extends AbstractModelloGenerator {
        @Override
        public void generate(Model model, Map<String, Object> parameters) throws ModelloException {
            initialize(model, parameters);
        }
    }

    private static Map<String, Object> parameters(Path dir) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ModelloParameterConstants.OUTPUT_DIRECTORY, dir.toString());
        parameters.put(ModelloParameterConstants.VERSION, "1.0.0");
        parameters.put(ModelloParameterConstants.PACKAGE_WITH_VERSION, "false");
        return parameters;
    }

    private static void write(TestGenerator generator, Path file, String content) throws IOException {
        try (Writer w = generator.newWriter(file)) {
            w.write(content);
        }
    }

    @Test
    void fileRefresherOnlyNotifiedOnChange(@TempDir Path dir) throws Exception {
        List<File> refreshed = new ArrayList<>();
        Map<String, Object> parameters = parameters(dir);
        parameters.put(ModelloParameterConstants.FILE_REFRESHER, (Consumer<File>) refreshed::add);
        TestGenerator generator = new TestGenerator();
        generator.generate(new Model(), parameters);

        Path file = dir.resolve("A.java");
        write(generator, file, "a");
        write(generator, file, "a");
        write(generator, file, "b");

        assertEquals(Arrays.asList(file.toFile(), file.toFile()), refreshed);
    }

    @Test
    void fileRefresherOptional(@TempDir Path dir) throws Exception {
        TestGenerator generator = new TestGenerator();
        generator.generate(new Model(), parameters(dir));
        write(generator, dir.resolve("A.java"), "a");
    }
}
