import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day7 {

    private static int[] findStart(List<String> lines) {
        for (int r = 0; r < lines.size(); r++) {
            int c = lines.get(r).indexOf('S');
            if (c != -1) return new int[]{r, c};
        }
        throw new IllegalStateException("No start position found");
    }

    private static long countSplits(List<String> lines) {
        int[] start = findStart(lines);
        int rows = lines.size();

        Set<Integer> beams = new HashSet<>();
        beams.add(start[1]);

        long splits = 0;
        for (int r = start[0] + 1; r < rows; r++) {
            String line = lines.get(r);
            Set<Integer> next = new HashSet<>();
            for (int col : beams) {
                if (col < 0 || col >= line.length()) continue;
                if (line.charAt(col) == '^') {
                    splits++;
                    if (col - 1 >= 0) next.add(col - 1);
                    if (col + 1 < line.length()) next.add(col + 1);
                } else {
                    next.add(col);
                }
            }
            beams = next;
        }

        return splits;
    }

    private static long countTimelines(List<String> lines) {
        int[] start = findStart(lines);
        int rows = lines.size();

        Map<Integer, Long> beams = new HashMap<>();
        beams.put(start[1], 1L);

        for (int r = start[0] + 1; r < rows; r++) {
            String line = lines.get(r);
            Map<Integer, Long> next = new HashMap<>();
            for (Map.Entry<Integer, Long> entry : beams.entrySet()) {
                int col = entry.getKey();
                long count = entry.getValue();
                if (col < 0 || col >= line.length()) continue;
                if (line.charAt(col) == '^') {
                    if (col - 1 >= 0) next.merge(col - 1, count, Long::sum);
                    if (col + 1 < line.length()) next.merge(col + 1, count, Long::sum);
                } else {
                    next.merge(col, count, Long::sum);
                }
            }
            beams = next;
        }

        long total = 0;
        for (long count : beams.values()) total += count;
        return total;
    }

    public static void main(String[] args) throws Exception {
        List<String> sampleLines = Utils.readLines("advent2025/input/day7_sample.txt");
        List<String> lines = Utils.readLines("advent2025/input/day7.txt");

        long sampleSplits = countSplits(sampleLines);
        System.out.println("Sample split count: " + sampleSplits);

        long splits = countSplits(lines);
        System.out.println("Split count: " + splits);

        long sampleTimelines = countTimelines(sampleLines);
        System.out.println("Sample timeline count: " + sampleTimelines);

        long timelines = countTimelines(lines);
        System.out.println("Timeline count: " + timelines);
    }
}
