package feedme.app.cli;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InputParserTest {
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "        "})
    void checkEmptyStrings(String input) throws InputParser.InputParserException {
        assertIterableEquals(List.of(), (new InputParser(0, false, true)).parse(input));
        assertIterableEquals(List.of(), (new InputParser(0, true, true)).parse(input));
        assertThrows(InputParser.InputParserException.class, () -> (new InputParser(1, true, true)).parse(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"word1 2word 3", "word1   2word 3", " word1 2word     3 "})
    void checkSimple3WordStrings(String input) throws InputParser.InputParserException {
        List<String> expected = List.of("word1", "2word", "3");
        assertIterableEquals(expected, (new InputParser(3, false, true)).parse(input));
        assertIterableEquals(expected, (new InputParser(3, true, true)).parse(input));
        assertThrows(InputParser.InputParserException.class, () -> (new InputParser(2, true, true)).parse(input));
        assertThrows(InputParser.InputParserException.class, () -> (new InputParser(4, false, true)).parse(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"\"", " \"\" \"\"\"\"", " \"\" "})
    void checkQuoteEmptyStrings(String input) throws InputParser.InputParserException {
        assertIterableEquals(List.of(), (new InputParser(0, true, true)).parse(input));
        assertThrows(InputParser.InputParserException.class, () -> (new InputParser(1, true, true)).parse(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\" \" \" \"", "\"word1\"  \"word2\"", " \"word 1\" word2 ", " here's \"a new one\"", " and\"\"\"\" a\"no\"the\"r one\""})
    void checkQuote2WordStrings(String input) throws InputParser.InputParserException {
        assertEquals(2, (new InputParser(2, true, true).parse(input)).size());
        assertThrows(InputParser.InputParserException.class, () -> (new InputParser(1, true, true)).parse(input));
        assertThrows(InputParser.InputParserException.class, () -> (new InputParser(3, true, true)).parse(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"", "\"\" what happened\" "})
    void checkInvalidStrings(String input) {
        assertThrows(InputParser.InputParserException.class, () -> (new InputParser(1, true, true)).parse(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {" word1 \"word 2\" word3 4", "\"\"\"word1\" \"word 2\" word3 4"})
    void checkStopAtParamCount(String input) throws InputParser.InputParserException {
        assertEquals(List.of("word1", "\"word 2\" word3 4"), (new InputParser(2, true, false).parse(input)));
        assertEquals(List.of("word1", "word 2", "word3 4"), (new InputParser(3, true, false).parse(input)));
    }
}