package org.codehaus.modello.plugin;

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
}
