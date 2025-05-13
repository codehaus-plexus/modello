package org.codehaus.modello.plugin;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractModelloGeneratorTest {

    @CsvSource( {
            ",",
            "'',''",
            "s,s",
            "aliases, alias",
            "babies, baby",
            "fezzes, fez",
            "foxes, fox",
            "ids, id",
            "licenses, license",
            "lunches, lunch",
            "potatoes, potato",
            "repositories, repository",
            "roles, role",
            "rushes, rush",
            "series, series"
    })
    @ParameterizedTest
    public void testSingular(String plural, String singular) {
        assertEquals(
                singular,
                AbstractModelloGenerator.singular(plural),
                "singular of: " + plural + " should be: " + singular);
    }
}
