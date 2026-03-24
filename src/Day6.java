import java.util.ArrayList;
import java.util.List;

public class Day6 {
    
    private static long solveWorksheet(List<String> lines) {
        int rows = lines.size();
        String[] operatorRow = lines.get(rows - 1).trim().split("\\s+");
        int columns = operatorRow.length;

        // initialize column lists
        List<List<Long>> values = new ArrayList<>();
        for (int j = 0; j < columns; j++) {
            values.add(new ArrayList<>());
        }

        // parse value rows (all except last)
        for (int i = 0; i < rows - 1; i++) {
            String[] parts = lines.get(i).trim().split("\\s+");
            for (int j = 0; j < parts.length; j++) {
                values.get(j).add(Long.parseLong(parts[j]));
            }
        }

        // apply operator per column and sum results
        long out = 0;
        for (int j = 0; j < columns; j++) {
            List<Long> column = values.get(j);
            String operator = operatorRow[j];
            long result = 0;
            switch (operator) {
                case "*":
                    result = 1;
                    for (long value : column) result *= value;
                    break;
                case "+":
                    for (long value : column) result += value;
                    break;
                case "-":
                    result = column.get(0);
                    for (int k = 1; k < column.size(); k++) result -= column.get(k);
                    break;
            }
            out += result;
        }
        return out;
    }

    private static long solveWorksheet2(List<String> lines) {
        int rows = lines.size();
        int valueRows = rows - 1;

        // pad all lines to the same length
        int maxLen = lines.stream().mapToInt(String::length).max().orElse(0);
        String[] grid = new String[rows];
        for (int i = 0; i < rows; i++) {
            String line = lines.get(i);
            grid[i] = line + " ".repeat(maxLen - line.length());
        }
        String opRow = grid[valueRows];

        // find all-space columns (separators between problems)
        List<Boolean> isSeparator = new ArrayList<>();
        for (int col = 0; col < maxLen; col++) {
            boolean allSpace = true;
            for (int row = 0; row < rows; row++) {
                if (grid[row].charAt(col) != ' ') { 
                    allSpace = false; break; 
                }
            }
            isSeparator.add(allSpace);
        }

        // build problem groups as [startCol, endCol] ranges
        List<int[]> groups = new ArrayList<>();
        int start = -1;
        for (int col = 0; col <= maxLen; col++) {
            boolean sep = col == maxLen || isSeparator.get(col);
            if (!sep && start == -1) start = col;
            else if (sep && start != -1) { groups.add(new int[]{start, col - 1}); start = -1; }
        }

        long total = 0;
        for (int[] group : groups) {
            String operator = "+";
            List<Long> numbers = new ArrayList<>();

            for (int col = group[0]; col <= group[1]; col++) {
                // check operator row for operator symbol
                char opChar = opRow.charAt(col);
                if (opChar == '*' || opChar == '+' || opChar == '-') {
                    operator = String.valueOf(opChar);
                }
                // build number from value rows (top = most significant)
                StringBuilder sb = new StringBuilder();
                for (int row = 0; row < valueRows; row++) {
                    char c = grid[row].charAt(col);
                    if (c != ' ') sb.append(c);
                }
                if (sb.length() > 0) numbers.add(Long.parseLong(sb.toString()));
            }

            long result = 0;
            switch (operator) {
                case "*": 
                    result = 1; 
                    for (long n : numbers) result *= n; break;
                case "+": 
                    for (long n : numbers) result += n; break;
                case "-":
                    if (!numbers.isEmpty()) {
                        result = numbers.get(0);
                        for (int k = 1; k < numbers.size(); k++) result -= numbers.get(k);
                    }
                    break;
            }
            total += result;
        }
        return total;
    }

    public static void main(String[] args) throws Exception {
        // Sample
        List<String> sampleLines = Utils.readLines("advent2025/input/day6_sample.txt");
        List<String> lines = Utils.readLines("advent2025/input/day6.txt");

        long out = solveWorksheet(sampleLines);
        System.out.println("Sample worksheet result: " + out);
        
        long out2 = solveWorksheet(lines);
        System.out.println("Worksheet result: " + out2);

        long out3 = solveWorksheet2(sampleLines);
        System.out.println("Sample worksheet result: " + out3);

        long out4 = solveWorksheet2(lines);
        System.out.println("Worksheet result: " + out4);
        
    }
}