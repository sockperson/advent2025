import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day10 {

    private static final Pattern GROUP_PATTERN = Pattern.compile("\\(([^)]*)\\)");

    private static class Machine {
        long target;      // bit i set => light i must end up on
        long[] buttons;    // bit i set => this button toggles light i
        int numLights;
        long[] joltage;    // per-counter target increment amount
    }

    private static Machine parseMachine(String line) {
        Machine m = new Machine();
        int bracketStart = line.indexOf('[');
        int bracketEnd = line.indexOf(']');
        String pattern = line.substring(bracketStart + 1, bracketEnd);
        m.numLights = pattern.length();
        long target = 0;
        for (int i = 0; i < pattern.length(); i++) {
            if (pattern.charAt(i) == '#') target |= (1L << i);
        }
        m.target = target;

        String afterBracket = line.substring(bracketEnd + 1, line.indexOf('{'));
        List<Long> buttons = new ArrayList<>();
        Matcher matcher = GROUP_PATTERN.matcher(afterBracket);
        while (matcher.find()) {
            String[] parts = matcher.group(1).split(",");
            long mask = 0;
            for (String p : parts) {
                mask |= (1L << Integer.parseInt(p.trim()));
            }
            buttons.add(mask);
        }
        m.buttons = new long[buttons.size()];
        for (int i = 0; i < buttons.size(); i++) m.buttons[i] = buttons.get(i);

        String joltagePart = line.substring(line.indexOf('{') + 1, line.indexOf('}'));
        String[] joltParts = joltagePart.split(",");
        m.joltage = new long[joltParts.length];
        for (int i = 0; i < joltParts.length; i++) m.joltage[i] = Long.parseLong(joltParts[i].trim());

        return m;
    }

    // Minimum number of buttons that XOR together to exactly the target light mask.
    private static int minPresses(Machine m) {
        int numButtons = m.buttons.length;
        int numLights = m.numLights;

        // Row i: coefficients over buttons (bitmask, bit j = button j appears) and rhs bit.
        long[] rowMask = new long[numLights];
        boolean[] rowRhs = new boolean[numLights];
        for (int light = 0; light < numLights; light++) {
            long mask = 0;
            for (int b = 0; b < numButtons; b++) {
                if ((m.buttons[b] & (1L << light)) != 0) mask |= (1L << b);
            }
            rowMask[light] = mask;
            rowRhs[light] = (m.target & (1L << light)) != 0;
        }

        int pivotRow = 0;
        int[] pivotColForRow = new int[numLights];
        java.util.Arrays.fill(pivotColForRow, -1);
        boolean[] isPivotCol = new boolean[numButtons];

        for (int col = 0; col < numButtons && pivotRow < numLights; col++) {
            int sel = -1;
            for (int r = pivotRow; r < numLights; r++) {
                if ((rowMask[r] & (1L << col)) != 0) { sel = r; break; }
            }
            if (sel == -1) continue;

            long tmpMask = rowMask[sel]; rowMask[sel] = rowMask[pivotRow]; rowMask[pivotRow] = tmpMask;
            boolean tmpRhs = rowRhs[sel]; rowRhs[sel] = rowRhs[pivotRow]; rowRhs[pivotRow] = tmpRhs;

            for (int r = 0; r < numLights; r++) {
                if (r != pivotRow && (rowMask[r] & (1L << col)) != 0) {
                    rowMask[r] ^= rowMask[pivotRow];
                    rowRhs[r] ^= rowRhs[pivotRow];
                }
            }

            isPivotCol[col] = true;
            pivotColForRow[pivotRow] = col;
            pivotRow++;
        }

        // Consistency check: any all-zero row with rhs=true means no solution.
        for (int r = pivotRow; r < numLights; r++) {
            if (rowMask[r] == 0 && rowRhs[r]) {
                throw new IllegalStateException("No solution for machine");
            }
        }

        List<Integer> freeCols = new ArrayList<>();
        for (int c = 0; c < numButtons; c++) if (!isPivotCol[c]) freeCols.add(c);
        int numFree = freeCols.size();

        int best = Integer.MAX_VALUE;
        long combos = 1L << numFree;
        for (long assignment = 0; assignment < combos; assignment++) {
            long freeMask = 0;
            for (int i = 0; i < numFree; i++) {
                if ((assignment & (1L << i)) != 0) freeMask |= (1L << freeCols.get(i));
            }

            long full = freeMask;
            for (int r = 0; r < pivotRow; r++) {
                int col = pivotColForRow[r];
                boolean bit = rowRhs[r] ^ ((Long.bitCount(rowMask[r] & freeMask) & 1) != 0);
                if (bit) full |= (1L << col);
            }

            int weight = Long.bitCount(full);
            if (weight < best) best = weight;
        }
        return best;
    }

    // A single linear constraint: coeffs . x {<=, =, >=} rhs, encoded by relation
    // '<' , '=', or '>'.
    private static class Constraint {
        double[] coeffs;
        char relation;
        double rhs;

        Constraint(double[] coeffs, char relation, double rhs) {
            this.coeffs = coeffs;
            this.relation = relation;
            this.rhs = rhs;
        }
    }

    private static class LpResult {
        boolean feasible;
        double[] x;
        double objective;
    }

    private static final double BIG_M = 1_000_000.0;
    private static final double EPS = 1e-7;

    // General Big-M simplex: minimize cost . x subject to the given constraints, x >= 0.
    private static LpResult solveLp(int numVars, List<Constraint> constraints, double[] cost) {
        int m = constraints.size();
        double[][] a = new double[m][numVars];
        double[] b = new double[m];
        char[] rel = new char[m];
        for (int i = 0; i < m; i++) {
            Constraint c = constraints.get(i);
            double[] coeffs = c.coeffs.clone();
            double rhs = c.rhs;
            char r = c.relation;
            if (rhs < 0) {
                for (int j = 0; j < numVars; j++) coeffs[j] = -coeffs[j];
                rhs = -rhs;
                if (r == '<') r = '>'; else if (r == '>') r = '<';
            }
            a[i] = coeffs;
            b[i] = rhs;
            rel[i] = r;
        }

        int[] slackCol = new int[m], surplusCol = new int[m], artCol = new int[m];
        java.util.Arrays.fill(slackCol, -1);
        java.util.Arrays.fill(surplusCol, -1);
        java.util.Arrays.fill(artCol, -1);
        int extra = 0;
        for (int i = 0; i < m; i++) {
            if (rel[i] == '<') {
                slackCol[i] = numVars + extra++;
            } else if (rel[i] == '>') {
                surplusCol[i] = numVars + extra++;
                artCol[i] = numVars + extra++;
            } else {
                artCol[i] = numVars + extra++;
            }
        }
        int totalCols = numVars + extra;

        double[][] tableau = new double[m + 1][totalCols + 1];
        int[] basis = new int[m];
        boolean[] isArtificial = new boolean[totalCols];
        for (int i = 0; i < m; i++) {
            System.arraycopy(a[i], 0, tableau[i], 0, numVars);
            if (slackCol[i] != -1) tableau[i][slackCol[i]] = 1;
            if (surplusCol[i] != -1) tableau[i][surplusCol[i]] = -1;
            if (artCol[i] != -1) { tableau[i][artCol[i]] = 1; isArtificial[artCol[i]] = true; }
            tableau[i][totalCols] = b[i];
            basis[i] = artCol[i] != -1 ? artCol[i] : slackCol[i];
        }

        double[] fullCost = new double[totalCols];
        System.arraycopy(cost, 0, fullCost, 0, numVars);
        for (int i = 0; i < m; i++) if (artCol[i] != -1) fullCost[artCol[i]] = BIG_M;

        System.arraycopy(fullCost, 0, tableau[m], 0, totalCols);
        for (int i = 0; i < m; i++) {
            double basisCost = fullCost[basis[i]];
            if (basisCost != 0) {
                for (int j = 0; j <= totalCols; j++) tableau[m][j] -= basisCost * tableau[i][j];
            }
        }

        while (true) {
            int enter = -1;
            for (int j = 0; j < totalCols; j++) {
                if (tableau[m][j] < -EPS && (enter == -1 || tableau[m][j] < tableau[m][enter])) enter = j;
            }
            if (enter == -1) break;

            int leave = -1;
            double bestRatio = Double.MAX_VALUE;
            for (int i = 0; i < m; i++) {
                if (tableau[i][enter] > EPS) {
                    double ratio = tableau[i][totalCols] / tableau[i][enter];
                    if (ratio < bestRatio - EPS || (ratio < bestRatio + EPS && (leave == -1 || basis[i] < basis[leave]))) {
                        bestRatio = ratio;
                        leave = i;
                    }
                }
            }
            if (leave == -1) {
                LpResult res = new LpResult();
                res.feasible = false;
                return res; // unbounded, treat as infeasible for our purposes
            }

            double pivot = tableau[leave][enter];
            for (int j = 0; j <= totalCols; j++) tableau[leave][j] /= pivot;
            for (int i = 0; i <= m; i++) {
                if (i == leave) continue;
                double factor = tableau[i][enter];
                if (factor == 0) continue;
                for (int j = 0; j <= totalCols; j++) tableau[i][j] -= factor * tableau[leave][j];
            }
            basis[leave] = enter;
        }

        LpResult res = new LpResult();
        for (int i = 0; i < m; i++) {
            if (isArtificial[basis[i]] && tableau[i][totalCols] > EPS) {
                res.feasible = false;
                return res;
            }
        }
        double[] x = new double[numVars];
        for (int i = 0; i < m; i++) if (basis[i] < numVars) x[basis[i]] = tableau[i][totalCols];
        double obj = 0;
        for (int j = 0; j < numVars; j++) obj += cost[j] * x[j];
        res.feasible = true;
        res.x = x;
        res.objective = obj;
        return res;
    }

    // Branch-and-bound on top of the LP relaxation: the relaxation alone can return a
    // fractional solution with a lower objective than any achievable integer press count,
    // so we must branch on fractional variables until we find the true integer optimum.
    private static long branchAndBound(int numVars, List<Constraint> baseConstraints, double[] cost) {
        java.util.Deque<List<Constraint>> stack = new java.util.ArrayDeque<>();
        stack.push(new ArrayList<>());
        long best = Long.MAX_VALUE;

        while (!stack.isEmpty()) {
            List<Constraint> extra = stack.pop();
            List<Constraint> all = new ArrayList<>(baseConstraints);
            all.addAll(extra);
            LpResult result = solveLp(numVars, all, cost);
            if (!result.feasible) continue;
            if (result.objective >= best - EPS) continue;

            int branchVar = -1;
            double bestFrac = EPS;
            for (int j = 0; j < numVars; j++) {
                double v = result.x[j];
                double frac = v - Math.floor(v);
                double dist = Math.min(frac, 1 - frac);
                if (dist > bestFrac) { bestFrac = dist; branchVar = j; }
            }

            if (branchVar == -1) {
                best = Math.min(best, Math.round(result.objective));
                continue;
            }

            double v = result.x[branchVar];
            double[] unit = new double[numVars];
            unit[branchVar] = 1;

            List<Constraint> lower = new ArrayList<>(extra);
            lower.add(new Constraint(unit, '<', Math.floor(v)));
            stack.push(lower);

            List<Constraint> upper = new ArrayList<>(extra);
            upper.add(new Constraint(unit, '>', Math.ceil(v)));
            stack.push(upper);
        }

        return best;
    }

    private static long minPressesJoltage(Machine m) {
        int numButtons = m.buttons.length;
        int numCounters = m.joltage.length;

        List<Constraint> constraints = new ArrayList<>();
        for (int i = 0; i < numCounters; i++) {
            double[] coeffs = new double[numButtons];
            for (int j = 0; j < numButtons; j++) {
                if ((m.buttons[j] & (1L << i)) != 0) coeffs[j] = 1;
            }
            constraints.add(new Constraint(coeffs, '=', m.joltage[i]));
        }

        double[] cost = new double[numButtons];
        java.util.Arrays.fill(cost, 1.0);

        return branchAndBound(numButtons, constraints, cost);
    }

    private static long totalMinPressesJoltage(List<String> lines) {
        long total = 0;
        for (String line : lines) {
            if (line.isBlank()) continue;
            Machine m = parseMachine(line);
            total += minPressesJoltage(m);
        }
        return total;
    }

    private static long totalMinPresses(List<String> lines) {
        long total = 0;
        for (String line : lines) {
            if (line.isBlank()) continue;
            Machine m = parseMachine(line);
            total += minPresses(m);
        }
        return total;
    }

    public static void main(String[] args) throws Exception {
        List<String> sampleLines = Utils.readLines("advent2025/input/day10_sample.txt");
        List<String> lines = Utils.readLines("advent2025/input/day10.txt");

        long sampleResult = totalMinPresses(sampleLines);
        System.out.println("Sample total presses: " + sampleResult);

        long result = totalMinPresses(lines);
        System.out.println("Total presses: " + result);

        long sampleJoltage = totalMinPressesJoltage(sampleLines);
        System.out.println("Sample total joltage presses: " + sampleJoltage);

        long joltage = totalMinPressesJoltage(lines);
        System.out.println("Total joltage presses: " + joltage);
    }
}
