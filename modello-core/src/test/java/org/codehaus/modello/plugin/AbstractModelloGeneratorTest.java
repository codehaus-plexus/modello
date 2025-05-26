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
        "men, man",
        "women, woman",
        "children,child",
        "mice, mouse",
        "people, person",
        "teeth, tooth",
        "feet, foot",
        "geese, goose",
        "series, series",
        "species, species",
        "sheep, sheep",
        "fish, fish",
        "deer, deer",
        "aircraft, aircraft",
        "heroes, hero",
        "potatoes, potato",
        "tomatoes, tomato",
        "echoes, echo",
        "vetoes, veto",
        "torpedoes, torpedo",
        "cargoes, cargo",
        "haloes, halo",
        "mosquitoes, mosquito",
        "buffaloes, buffalo",
        "bison, bison",
        "elk, elk",

        // Regular plural forms with suffixes
        "voes, voe",
        "hoes, hoe",
        "canoes, canoe",
        "toes, toe",
        "foes, foe",
        "oboes, oboe",
        "noes, no",
        "boxes, box",
        "wishes, wish",
        "dishes, dish",
        "brushes, brush",
        "classes, class",
        "buzzes, buzz",
        "cars, car",
        "dogs, dog",
        "cats, cat",
        "horses, horse",
        "fezzes, fez",
        "whizzes, whiz",
        "foxes, fox",

        // Some test cases with different rules
        "archives, archive",
        "otherArchives, otherArchive",
        "Archives, Archive",
        "wolves, wolf",
        "knives, knife",
        "leaves, leaf",
        "wives, wife",
        "lives, life",
        "babies, baby",
        "parties, party",
        "cities, city",
        "buses, bus",
        "boxes, box",
        "churches, church",
        "matches, match",
        "watches, watch",
        "riches, rich",
        "dresses, dress",
        "crosses, cross",
        "lunches, lunch",
        "relatives, relative",

        // More edge cases
        "heroes, hero",
        "vetoes, veto",
        "torpedoes, torpedo",
        "tomatoes, tomato",
        "potatoes, potato",
        "echoes, echo",
        "mosquitoes, mosquito",
        "buffaloes, buffalo",
        "volcanoes, volcano",
        "goes, go",
        "indices, index",
        "phases, phase",
        "kisses, kiss",
        "movies, movie",
        "shoes, shoe",

        // other examples
        "aliases, alias",
        "ids, id",
        "licenses, license",
        "repositories, repository",
        "roles, role",
    })
    @ParameterizedTest
    public void testSingular(String plural, String singular) {
        assertEquals(
                singular,
                AbstractModelloGenerator.singular(plural),
                "singular of: " + plural + " should be: " + singular);
    }
}
