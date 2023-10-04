package feedme.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class InputParser {
    private final int paramCount;
    private final boolean enableQuotationMarks;
    private final boolean parseEntireInput;

    private final Set<Character> QUOTATION_MARK_CHARS = Set.of('"');
    private final char DELIMITER = ' ';

    public InputParser(int paramCount) {
        this(paramCount, true);
    }

    public InputParser(int paramCount, boolean enableQuotationMarks) {
        this(paramCount, enableQuotationMarks, false);
    }

    public InputParser(int paramCount, boolean enableQuotationMarks, boolean parseEntireInput) {
        this.paramCount = paramCount;
        this.enableQuotationMarks = enableQuotationMarks;
        this.parseEntireInput = parseEntireInput;
    }

    public List<String> parse(String input) throws InputParserException {
        List<String> splitInput;
        if (enableQuotationMarks) {
            splitInput = parseWithQuotationMarks(input);
        } else {
            splitInput = Stream.of(input.trim().split("%s+".formatted(DELIMITER), paramCount)).filter(Predicate.not(String::isBlank)).toList();
        }
        if (splitInput.size() < paramCount || (parseEntireInput && splitInput.size() > paramCount)) {
            throw new InputParserException("Unaccepted input size: '%d'".formatted(splitInput.size()));
        }
        return splitInput;
    }

    private List<String> parseWithQuotationMarks(String input) throws InputParserException {
        List<String> splitInput = new ArrayList<>();
        StringBuilder remaining = new StringBuilder(input);
        StringBuilder currentParam = new StringBuilder();
        boolean inQuote = false;
        char currentQuote = 0;
        for (char currentChar : input.toCharArray()) {
            if (!parseEntireInput && splitInput.size() == paramCount - 1) {
                splitInput.add(remaining.toString());
                break;
            }
            remaining.delete(0, 1);
            if (QUOTATION_MARK_CHARS.contains(currentChar)) {
                if (!inQuote) {
                    inQuote = true;
                    currentQuote = currentChar;
                    continue;
                }
                if (currentChar == currentQuote) {
                    inQuote = false;
                    continue;
                }
            }
            if (!inQuote) {
                // end on space unless there is no current word, then skip
                if (currentChar == DELIMITER) {
                    if (!currentParam.isEmpty()) {
                        splitInput.add(currentParam.toString());
                        currentParam.delete(0, currentParam.length());
                    }
                    continue;
                }
            }
            currentParam.append(currentChar);
        }
        if (inQuote) {
            throw new InputParserException("Unmatched quotation mark");
        }
        if (!currentParam.isEmpty()) {
            splitInput.add(currentParam.toString());
        }
        return Collections.unmodifiableList(splitInput);
    }

    public static class InputParserException extends Exception {
        public InputParserException(String message) {
            super(message);
        }
    }
}
