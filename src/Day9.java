import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class Day9 {

    private static long[][] parsePoints(List<String> lines) {
        long[][] points = new long[lines.size()][2];
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).trim().split(",");
            points[i][0] = Long.parseLong(parts[0].trim());
            points[i][1] = Long.parseLong(parts[1].trim());
        }
        return points;
    }

    private static long largestArea(List<String> lines) {
        long[][] points = parsePoints(lines);
        int n = points.length;

        long best = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                long width = Math.abs(points[i][0] - points[j][0]) + 1;
                long height = Math.abs(points[i][1] - points[j][1]) + 1;
                best = Math.max(best, width * height);
            }
        }
        return best;
    }

    // Coordinate-compressed fill of the rectilinear polygon traced by the (ordered,
    // wrapping) red tile vertices, so we can test whether an arbitrary axis-aligned
    // rectangle lies entirely within the red/green (interior + boundary) region.
    private static class PolygonGrid {
        final long[] xs;
        final long[] ys;
        final Map<Long, Integer> xIndex = new HashMap<>();
        final Map<Long, Integer> yIndex = new HashMap<>();
        final boolean[][] inside;      // [X-1][Y-1] cell interiors
        final long[][] cellPrefix;     // 2D prefix sum over `inside`, size [X][Y]
        final long[][] vBoundaryPrefix; // [X][Y-1+1] prefix over rows, per column-boundary
        final long[][] hBoundaryPrefix; // [Y][X-1+1] prefix over columns, per row-boundary
        final int X, Y;

        PolygonGrid(long[][] points) {
            int n = points.length;
            TreeSet<Long> xSet = new TreeSet<>();
            TreeSet<Long> ySet = new TreeSet<>();
            for (long[] p : points) { xSet.add(p[0]); ySet.add(p[1]); }
            xs = xSet.stream().mapToLong(Long::longValue).toArray();
            ys = ySet.stream().mapToLong(Long::longValue).toArray();
            X = xs.length;
            Y = ys.length;
            for (int i = 0; i < X; i++) xIndex.put(xs[i], i);
            for (int j = 0; j < Y; j++) yIndex.put(ys[j], j);

            // Vertical edges of the polygon (edges with equal x between consecutive vertices).
            List<long[]> vEdges = new ArrayList<>(); // {x, yLow, yHigh}
            for (int k = 0; k < n; k++) {
                long[] a = points[k];
                long[] b = points[(k + 1) % n];
                if (a[0] == b[0]) {
                    vEdges.add(new long[]{a[0], Math.min(a[1], b[1]), Math.max(a[1], b[1])});
                }
            }

            inside = new boolean[X - 1][Y - 1];
            boolean[] toggle = new boolean[X];
            for (int j = 0; j < Y - 1; j++) {
                long rowLo = ys[j], rowHi = ys[j + 1];
                java.util.Arrays.fill(toggle, false);
                for (long[] e : vEdges) {
                    if (e[1] <= rowLo && e[2] >= rowHi) {
                        int idx = xIndex.get(e[0]);
                        toggle[idx] ^= true;
                    }
                }
                boolean state = false;
                for (int i = 0; i < X - 1; i++) {
                    if (toggle[i]) state = !state;
                    inside[i][j] = state;
                }
            }

            // 2D prefix sum over cell interiors, for "fully inside" range queries.
            cellPrefix = new long[X][Y];
            for (int i = 0; i < X - 1; i++) {
                for (int j = 0; j < Y - 1; j++) {
                    cellPrefix[i + 1][j + 1] = cellPrefix[i][j + 1] + cellPrefix[i + 1][j]
                            - cellPrefix[i][j] + (inside[i][j] ? 1 : 0);
                }
            }

            // Per vertical grid-line (column boundary ix), whether each row is "filled"
            // (on the boundary/interior) because at least one adjacent cell is inside.
            vBoundaryPrefix = new long[X][Y];
            for (int ix = 0; ix < X; ix++) {
                for (int j = 0; j < Y - 1; j++) {
                    boolean left = ix - 1 >= 0 && inside[ix - 1][j];
                    boolean right = ix <= X - 2 && inside[ix][j];
                    vBoundaryPrefix[ix][j + 1] = vBoundaryPrefix[ix][j] + ((left || right) ? 1 : 0);
                }
            }

            // Same idea for horizontal grid-lines (row boundary iy) across columns.
            hBoundaryPrefix = new long[Y][X];
            for (int iy = 0; iy < Y; iy++) {
                for (int i = 0; i < X - 1; i++) {
                    boolean below = iy - 1 >= 0 && inside[i][iy - 1];
                    boolean above = iy <= Y - 2 && inside[i][iy];
                    hBoundaryPrefix[iy][i + 1] = hBoundaryPrefix[iy][i] + ((below || above) ? 1 : 0);
                }
            }
        }

        boolean fullyFilled(long x1, long y1, long x2, long y2) {
            long xlo = Math.min(x1, x2), xhi = Math.max(x1, x2);
            long ylo = Math.min(y1, y2), yhi = Math.max(y1, y2);
            int ixLo = xIndex.get(xlo), ixHi = xIndex.get(xhi);
            int iyLo = yIndex.get(ylo), iyHi = yIndex.get(yhi);

            if (ixLo == ixHi && iyLo == iyHi) return true; // single point
            if (ixLo == ixHi) {
                long sum = vBoundaryPrefix[ixLo][iyHi] - vBoundaryPrefix[ixLo][iyLo];
                return sum == (iyHi - iyLo);
            }
            if (iyLo == iyHi) {
                long sum = hBoundaryPrefix[iyLo][ixHi] - hBoundaryPrefix[iyLo][ixLo];
                return sum == (ixHi - ixLo);
            }
            long sum = cellPrefix[ixHi][iyHi] - cellPrefix[ixLo][iyHi] - cellPrefix[ixHi][iyLo] + cellPrefix[ixLo][iyLo];
            return sum == (long) (ixHi - ixLo) * (iyHi - iyLo);
        }
    }

    private static long largestFilledArea(List<String> lines) {
        long[][] points = parsePoints(lines);
        int n = points.length;
        PolygonGrid grid = new PolygonGrid(points);

        long best = 0;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                long x1 = points[i][0], y1 = points[i][1];
                long x2 = points[j][0], y2 = points[j][1];
                if (!grid.fullyFilled(x1, y1, x2, y2)) continue;
                long width = Math.abs(x1 - x2) + 1;
                long height = Math.abs(y1 - y2) + 1;
                best = Math.max(best, width * height);
            }
        }
        return best;
    }

    public static void main(String[] args) throws Exception {
        List<String> sampleLines = Utils.readLines("advent2025/input/day9_sample.txt");
        List<String> lines = Utils.readLines("advent2025/input/day9.txt");

        long sampleResult = largestArea(sampleLines);
        System.out.println("Sample largest area: " + sampleResult);

        long result = largestArea(lines);
        System.out.println("Largest area: " + result);

        long sampleFilled = largestFilledArea(sampleLines);
        System.out.println("Sample largest filled area: " + sampleFilled);

        long filled = largestFilledArea(lines);
        System.out.println("Largest filled area: " + filled);
    }
}
