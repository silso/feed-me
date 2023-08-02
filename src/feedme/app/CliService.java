package feedme.app;

import feedme.domain.tidbit.Tidbit;
import feedme.domain.tidbit.TidbitRepository;
import feedme.domain.tidbit.action.TidbitAction;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CliService {
    private final TidbitRepository store = new TidbitRepository();

    public void start() {
        Scanner in = new Scanner(System.in);
        in.useDelimiter("\\n");
        boolean shouldRun = true;
        while(shouldRun) {
            out("\nMorsels:");
            store.forEach((id, tidbit) -> out(String.format("%d: %s", id, tidbit)));
            String line = in.next();
            Matcher matcher = Pattern.compile("(?<command>\\w*)\\W+(?<remaining>.*)").matcher(line);
            matcher.matches();
            String command = matcher.group("command");
            String remaining = matcher.group("remaining");
            switch (command) {
                case "add" -> add(remaining);
                case "consume" -> consume(remaining);
                case "exit" -> {out("bye"); shouldRun = false;}
            }
        }
        in.close();
    }

    private void add(String remaining) {
        int newId = store.addTidbit(remaining.strip());
        out(String.format("Added %d", newId));
    }

    private void consume(String remaining) {
        Matcher matcher = Pattern.compile("(?:\\W|^)\\d+(?:\\W|$)").matcher(remaining);

        while (matcher.find()) {
            int id = Integer.parseInt(matcher.group().strip());
            if (store.getTidbit(id).isEmpty()){
                out("Couldn't find morsel with id %d", id);
                continue;
            }
            Tidbit tidbit = store.getTidbit(id).get();
            TidbitAction newAction = TidbitAction.Impl.Consume.create();
            if (!newAction.isApplicableTo(tidbit)) {
                out("Action couldn't be applied to morsel %d", id);
                continue;
            }
            if (!store.putTidbit(id, newAction.apply(tidbit))) {
                out("Failed to replace morsel %d", id);
                continue;
            }
            out("Consumed %d", id);
        }
    }

    private void out(String format, Object... objects) {
        System.out.printf((format) + "%n", objects);
    }
}
