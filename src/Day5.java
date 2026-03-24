import java.util.ArrayList;
import java.util.List;

public class Day5 {
    
    private static int getValidIdCount(List<String> lines) {
        List<long[]> ranges = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        for (String line : lines) {
            if (line.contains("-")) {
                String[] parts = line.split("-");
                long start = Long.parseLong(parts[0]);
                long end = Long.parseLong(parts[1]);
                ranges.add(new long[]{start, end});
            } else {
                if (line.length() > 0) {
                    ids.add(Long.parseLong(line));
                }
            }
        }
        int validIdCount = 0;
        for (long id : ids) {
            for (long[] range : ranges) {
                if (id >= range[0] && id <= range[1]) {
                    validIdCount++;
                    break;
                }
            }
        }
        return validIdCount;
    }

    // given a list of ranges, find how many IDs are valid
    public static long getValidIdCount2(List<String> lines) {
        List<long[]> ranges = new ArrayList<>();
        for (String line : lines) {
            if (line.contains("-")) {
                String[] parts = line.split("-");
                long start = Long.parseLong(parts[0]);
                long end = Long.parseLong(parts[1]);
                ranges.add(new long[]{start, end});
            } 
        }
        
        // sort ranges by start value
        ranges.sort((a, b) -> Long.compare(a[0], b[0]));

        // merge overlapping ranges
        List<long[]> merged = new ArrayList<>();
        for (long[] range : ranges) {
            if (merged.isEmpty() || range[0] > merged.get(merged.size() - 1)[1] + 1) {
                merged.add(new long[]{range[0], range[1]});
            } else {
                merged.get(merged.size() - 1)[1] = Math.max(merged.get(merged.size() - 1)[1], range[1]);
            }
        }

        // sum the total length of all merged ranges
        long total = 0;
        for (long[] range : merged) {
            total += range[1] - range[0] + 1;
        }
        return total;
    }
    
    public static void main(String[] args) throws Exception {
        // Sample
        List<String> sampleLines = Utils.readLines("advent2025/input/day5_sample.txt");
        int sampleValidIdCount = getValidIdCount(sampleLines);
        System.out.println("Sample valid ID count: " + sampleValidIdCount);
        
        // Day5
        List<String> lines = Utils.readLines("advent2025/input/day5.txt");
        int validIdCount = getValidIdCount(lines);
        System.out.println("Valid ID count: " + validIdCount);

        // Day5 part 2
        long validIdCount2 = getValidIdCount2(lines);
        System.out.println("Valid ID count: " + validIdCount2);
    }
}
