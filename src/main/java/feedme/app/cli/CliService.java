package feedme.app.cli;

import feedme.app.cli.action.ActionService;
import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.TidbitRepository;
import feedme.domain.tidbit.seed.Seed;
import feedme.domain.tidbit.seed.SeedRepository;
import feedme.domain.tidbit.seed.SeedService;
import feedme.domain.tidbit.task.SimpleStatefulTaskSeed;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CliService {
    private final TidbitRepository tidbits = new TidbitRepository();
    private final SeedRepository seeds = new SeedRepository();
    private final ActionService actionService = new ActionService();
    private final SeedService seedService = new SeedService(seeds);

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private final Queue<String> stdinLines = new ArrayDeque<>();

    public void start() {
        startReaderThread();
        seedService.start();
        CliStatus status = new CliStatus(true, true);
        while(status.shouldRun()) {
            if (status.shouldPrint() | seeds.hasChanged() | tidbits.hasChanged()) {
                printInfo();
                status = status.withShouldPrint(false);
            }
            while (!stdinLines.isEmpty()) {
                status = processInput(stdinLines.poll());
                if (!status.shouldRun()) {
                    break;
                }
            }
            if (!status.shouldRun()) {
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                out("Exiting...");
                status = status.withShouldRun(false);
            }
        }

        System.exit(0);
    }

    private void startReaderThread() {
        Executors.newSingleThreadScheduledExecutor().schedule(() -> reader.lines().forEach(stdinLines::add), 0, TimeUnit.MILLISECONDS);
    }

    private void printInfo() {
        out("\nSeeds:");
        seeds.forEach((id, seed) -> {
            seed.createTidbits(Instant.now());
            out("%d: %s".formatted(id, seed));
        });
        out("Tidbits:");
        tidbits.forEach((id, tidbit) -> {
            if (tidbit.currentState.isVisible()) {
                out(String.format("%d: %s", id, tidbit));
            }
        });
    }

    private CliStatus processInput(String input) {
        try {
            if (input.isBlank()) {
                return new CliStatus(true, false);
            }
            List<String> splitInput;
            try {
                splitInput = new InputParser(2, true, false, true).parse(input);
            } catch (InputParser.InputParserException e) {
                throw new InputException("start", e);
            }
            String command = splitInput.get(0);
            String remaining = splitInput.size() > 1 ? splitInput.get(1) : "";
            switch (command) {
                case "add" -> add(remaining);
                case "actions" -> printActions();
                case "action" -> doAction(remaining);
                case "print" -> {return new CliStatus(true, true);}
                case "exit" -> {
                    out("bye");
                    return new CliStatus(false, false);
                }
                default -> throw new InputException("start", "unhandled command '%s'".formatted(command));
            }
        } catch (InputException | ActionService.ActionException e) {
            out(e.getMessage());
        }
        return new CliStatus(true, false);
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

    private void printActions() {
        tidbits.forEach(
            (id, tidbit) -> {
                if (tidbit.currentState.isVisible()) {
                    out("%d: %s".formatted(id, actionService.getAllowedActionsForTidbit(tidbit)));
                }
            }
        );
    }

    private void doAction(String remaining) throws InputException, ActionService.ActionException {
        List<String> splitInput;
        try {
            splitInput = new InputParser(2, true, true).parse(remaining);
        } catch (InputParser.InputParserException e) {
            throw new InputException("action", e);
        }
        String idString = splitInput.get(0);
        String actionString = splitInput.get(1);
        int id;
        try {
            id = Integer.parseInt(idString);
        } catch (NumberFormatException e) {
            throw new InputException("action id", e);
        }
        String errorMessage = "Tidbit with ID '%d' doesn't exist".formatted(id);
        Tidbit tidbit = tidbits.getTidbit(id).orElseThrow(() -> new InputException("action id", errorMessage));
        if (!tidbit.currentState.isVisible()) {
            throw new InputException("action id", "Tidbit is invalid: '%s'".formatted(tidbit));
        }
        actionService.applyActionToTidbit(actionString, tidbit);
    }

    private void addTaskSeed(String remaining) throws InputException {
        List<String> splitInput;
        try {
            splitInput = new InputParser(4, true, true).parse(remaining);
        } catch (InputParser.InputParserException e) {
            throw new InputException("add task", e);
        }
        String instruction = splitInput.get(0);
        String expiresAtInput = splitInput.get(1);
        Instant expiresAt;
        if (expiresAtInput.startsWith("+")) {
            expiresAt = Instant.now().plus(Duration.parse("PT%s".formatted(expiresAtInput.substring(1).toUpperCase(Locale.ENGLISH))));
        } else {
            expiresAt = Instant.parse(expiresAtInput.toUpperCase());
        }
        String expirationTimeInput = splitInput.get(2);
        String onItTimeInput = splitInput.get(3);
        Seed newSeed = new SimpleStatefulTaskSeed(
            instruction,
            expiresAt,
            tidbits,
            Duration.parse(expirationTimeInput.toUpperCase()),
            Duration.parse(onItTimeInput.toUpperCase())
        );
        seeds.addSeed(newSeed);
    }

    private void out(String format, Object... objects) {
        System.out.printf((format) + "%n", objects);
    }

    private record CliStatus(boolean shouldRun, boolean shouldPrint) {
        private CliStatus withShouldRun(boolean shouldRun) {
            return new CliStatus(shouldRun, shouldPrint());
        }
        private CliStatus withShouldPrint(boolean shouldPrint) {
            return new CliStatus(shouldRun(), shouldPrint);
        }
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
