import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    // IntelliJ's default run working directory is the parent of this project folder,
    // so paths are written as "advent2025/input/...". VSCode's Java runner instead
    // uses this project folder itself as the working directory, so that prefix must
    // be stripped. Try both so the same source works from either tool.
    private static String resolvePath(String filePath) {
        if (new File(filePath).exists()) {
            return filePath;
        }
        String prefix = "advent2025/";
        if (filePath.startsWith(prefix)) {
            String stripped = filePath.substring(prefix.length());
            if (new File(stripped).exists()) {
                return stripped;
            }
        } else {
            String prefixed = prefix + filePath;
            if (new File(prefixed).exists()) {
                return prefixed;
            }
        }
        return filePath;
    }

    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(resolvePath(filePath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return lines;
    }

    public static char[][] readLinesAsCharArray(String filePath) {
        List<String> lines = readLines(filePath);
        char[][] charArray = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            charArray[i] = lines.get(i).toCharArray();
        }
        return charArray;
    }

    public static void printLines(String filePath) {
        for (String line : readLines(filePath)) {
            System.out.println(line);
        }
    }
}
