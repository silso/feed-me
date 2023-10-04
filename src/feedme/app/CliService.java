package feedme.app;

import feedme.domain.tidbit.TidbitRepository;
import feedme.domain.tidbit.TidbitState;
import feedme.domain.tidbit.seed.Seed;
import feedme.domain.tidbit.seed.SeedRepository;
import feedme.domain.tidbit.seed.impl.TaskSeed;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Scanner;

public class CliService {
    private final TidbitRepository tidbits = new TidbitRepository();
    private final SeedRepository seeds = new SeedRepository();

    public void start() {
        Scanner in = new Scanner(System.in);
        in.useDelimiter("\\n");
        boolean shouldRun = true;
        while(shouldRun) {
            out("\nSeeds:");
            seeds.forEach((id, seed) -> {
                seed.createTidbits(Instant.now());
                out("%d: %s".formatted(id, seed));
            });
            out("\nTidbits:");
            tidbits.forEach((id, tidbit) -> {
                if (tidbit.currentState.equals(TidbitState.Visible)) {
                    out(String.format("%d: %s", id, tidbit));
                }
            });
            try {
                String input = in.next();
                if (input.isBlank()) {
                    continue;
                }
                List<String> splitInput;
                try {
                    splitInput = new InputParser(2).parse(input);
                } catch (InputParser.InputParserException e) {
                    throw new InputException("start", e);
                }
                String command = splitInput.get(0);
                switch (command) {
                    case "add" -> add(splitInput.get(1));
                    case "exit" -> {out("bye"); shouldRun = false;}
                    default -> throw new InputException("start", "unhandled command '%s'".formatted(command));
                }
            } catch (InputException e) {
                out(e.getMessage());
            }
        }
        in.close();
    }

    private void add(String remaining) throws InputException {
        List<String> splitInput;
        try {
            splitInput = new InputParser(2).parse(remaining);
        } catch (InputParser.InputParserException e) {
            throw new InputException("add", e);
        }
        String type = splitInput.get(0);
        switch (type) {
            case "task" -> addTaskSeed(splitInput.get(1));
            default -> throw new InputException("add", "unhandled type '%s'".formatted(type));
        }
    }

    private void addTaskSeed(String remaining) throws InputException {
        List<String> splitInput;
        try {
            splitInput = new InputParser(2, true, true).parse(remaining);
        } catch (InputParser.InputParserException e) {
            throw new InputException("add task", e);
        }
        String instruction = splitInput.get(0);
        String expiresAtInput = splitInput.get(1);
        Instant expiresAt = Instant.now();
        if (expiresAtInput.startsWith("P")) {
            Instant.now().plus(Duration.parse(expiresAtInput));
        } else {
            expiresAt = Instant.parse(expiresAtInput);
        }
        Seed newSeed = new TaskSeed(
            instruction,
            expiresAt,
            tidbits
        );
        seeds.addSeed(newSeed);
    }

    private void out(String format, Object... objects) {
        System.out.printf((format) + "%n", objects);
    }

    private static class InputException extends Exception {
        private InputException(String inputStep, Throwable cause) {
            super("Error in input after '%s': '%s'".formatted(inputStep, cause.getMessage()));
        }

        private InputException(String inputStep, String message) {
            super("Error in input after '%s': '%s'".formatted(inputStep, message));
        }
    }
}
