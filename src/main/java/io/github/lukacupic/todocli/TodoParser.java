package io.github.lukacupic.todocli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TodoParser {

    /**
     * The list of all TODO markers.
     */
    private static final String[] TODOS = new String[]{"@todo", "TODO", "@fixme", "FIXME"};

    /**
     * Patterns and Regexes used for matching comments and lines.
     */
    private static final Pattern TODO_LINE_PATTERN = Pattern.compile("(\\s*[*])(\\s\\s)(.*)");
    private static final String TODO_SINGLE_LINE = "^(\\s*//\\s*)(@todo|TODO|@fixme|FIXME).*$";
    private static final String TODO_MULTI_LINE = "^(\\s*[*]\\s*)(@todo|TODO|@fixme|FIXME).*$";


    /**
     * Finds and returns a list of all TODOs found in the file given by its path.
     */
    public static List<Todo> parse(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));
        List<Todo> todos = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            int headerIndex = getHeaderIndex(line);
            if (headerIndex == -1) continue;

            String[] parts = line.substring(headerIndex).trim().split("\\s", 2);
            if (parts[0].isEmpty()) continue;

            String header = parts[0].trim();

            if (line.matches(TODO_SINGLE_LINE)) {
                String body = parts.length == 2 ? parts[1].trim() : "";
                Todo todo = new Todo(i + 1, i + 1, body, header, path);
                todos.add(todo);
            }

            if (line.matches(TODO_MULTI_LINE)) {
                int start = i + 1;
                String body = parts[1].trim();
                StringBuilder sb = new StringBuilder(body);

                while (true) {
                    line = lines.get(++i);

                    Matcher m = TODO_LINE_PATTERN.matcher(line);

                    if (m.find()) {
                        body = m.group(3);
                        sb.append(" ").append(body);

                    } else {
                        i--;
                        break;
                    }
                }
                Todo todo = new Todo(start, i + 1, sb.toString(), header, path);
                todos.add(todo);
            }
        }
        return todos;
    }

    /**
     * Checks if a TODO is present in the given line and if so, returns
     * the index of its header, i.e. of the header's first character.
     * Header consists of the ticker number and the estimated time.
     *
     * @param line the source code line to check
     * @return index of header or -1 if the line contains no TODO
     */
    private static int getHeaderIndex(String line) {
        for (String todo : TODOS) {
            int todoIndex = line.indexOf(todo);
            if (todoIndex != -1) {
                return todoIndex + todo.length();
            }
        }
        return -1;
    }
}