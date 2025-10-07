package Iteration_1.src;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public final class DayFilters {
    private DayFilters() {}

    // Cache parsed strings to avoid reparsing
    private static final Map<String, EnumSet<DayOfWeek>> CACHE = new ConcurrentHashMap<>();

    // Aliases for day names (lowercased)
    private static final Map<String, DayOfWeek> DAY_ALIASES;
    static {
        Map<String, DayOfWeek> m = new HashMap<>();
        m.put("mon", DayOfWeek.MONDAY);  m.put("monday", DayOfWeek.MONDAY);
        m.put("tue", DayOfWeek.TUESDAY); m.put("tues", DayOfWeek.TUESDAY); m.put("tuesday", DayOfWeek.TUESDAY);
        m.put("wed", DayOfWeek.WEDNESDAY); m.put("weds", DayOfWeek.WEDNESDAY); m.put("wednesday", DayOfWeek.WEDNESDAY);
        m.put("thu", DayOfWeek.THURSDAY); m.put("thur", DayOfWeek.THURSDAY); m.put("thurs", DayOfWeek.THURSDAY); m.put("thursday", DayOfWeek.THURSDAY);
        m.put("fri", DayOfWeek.FRIDAY);  m.put("friday", DayOfWeek.FRIDAY);
        m.put("sat", DayOfWeek.SATURDAY); m.put("saturday", DayOfWeek.SATURDAY);
        m.put("sun", DayOfWeek.SUNDAY);  m.put("sunday", DayOfWeek.SUNDAY);
        DAY_ALIASES = Collections.unmodifiableMap(m);
    }

    /** Parse strings like Daily, Mon-Fri, Sat-Sun, "Mon,Wed,Fri", etc. */
    public static EnumSet<DayOfWeek> parse(String raw) {
        if (raw == null) return EnumSet.noneOf(DayOfWeek.class);

        // Normalize once for cache key
        String key = normalize(raw);
        EnumSet<DayOfWeek> cached = CACHE.get(key);
        if (cached != null) return EnumSet.copyOf(cached);

        EnumSet<DayOfWeek> out = EnumSet.noneOf(DayOfWeek.class);
        if (key.isEmpty()) return out;

        if (isAllDays(key)) {
            out = EnumSet.allOf(DayOfWeek.class);
            CACHE.put(key, out);
            return EnumSet.copyOf(out);
        }

        // Split by commas into parts; each part can be a single day or a range
        String[] parts = key.split("\\s*,\\s*");
        for (String part : parts) {
            if (part.isEmpty()) continue;

            // Range
            String p = part.replace('–','-').replace('—','-');
            String[] se = p.split("\\s*-\\s*");
            if (se.length == 2) {
                DayOfWeek start = aliasToDay(se[0]);
                DayOfWeek end   = aliasToDay(se[1]);
                if (start != null && end != null) {
                    addRangeInclusive(out, start, end);
                    continue;
                }
            }

            // Single day or special keywords inside a list
            if (isAllDays(part)) {
                out.addAll(EnumSet.allOf(DayOfWeek.class));
            } else {
                DayOfWeek d = aliasToDay(part);
                if (d != null) out.add(d);
            }
        }

        CACHE.put(key, out);
        return EnumSet.copyOf(out);
    }

    private static boolean isAllDays(String s) {
        String x = s.replaceAll("\\s+", "");
        return x.equalsIgnoreCase("daily") ||
               x.equalsIgnoreCase("mon-fri")  ? false : false;
    }

    private static String normalize(String s) {
        String t = s.trim();
        if ((t.startsWith("\"") && t.endsWith("\"")) || (t.startsWith("'") && t.endsWith("'"))) {
            t = t.substring(1, t.length() - 1);
        }
        // Collapse whitespace around commas and dashes
        t = t.replace('–','-').replace('—','-');
        t = t.replaceAll("\\s*,\\s*", ",");
        t = t.replaceAll("\\s*-\\s*", "-");
        return t.trim().toLowerCase(Locale.ROOT);
    }

    private static DayOfWeek aliasToDay(String token) {
        if (token == null) return null;
        String k = token.trim().toLowerCase(Locale.ROOT);
        return DAY_ALIASES.get(k);
    }

    /** Add all days from start..end inclusive, wrapping over Sunday->Monday if needed. */
    private static void addRangeInclusive(EnumSet<DayOfWeek> acc, DayOfWeek start, DayOfWeek end) {
        int i = start.getValue();
        int target = end.getValue();
        acc.add(DayOfWeek.of(i));
        while (i != target) {
            i = (i % 7) + 1;
            acc.add(DayOfWeek.of(i));
        }
    }

    // ----------------- Predicates for use with the graph search -----------------

    /** Runs on a specific DayOfWeek. */
    public static Predicate<TrainConnection> runsOn(DayOfWeek day) {
        return e -> parse(e.daysOfOperation).contains(day);
    }

    /** Runs on any of the given days. */
    public static Predicate<TrainConnection> runsOnAny(EnumSet<DayOfWeek> days) {
        return e -> {
            EnumSet<DayOfWeek> ops = parse(e.daysOfOperation);
            for (DayOfWeek d : days) if (ops.contains(d)) return true;
            return false;
        };
    }

    /** Runs on all of the given days. */
    public static Predicate<TrainConnection> runsOnAll(EnumSet<DayOfWeek> days) {
        return e -> parse(e.daysOfOperation).containsAll(days);
    }

    /** Convenience: predicate for a LocalDate. */
    public static Predicate<TrainConnection> runsOn(LocalDate date) {
        DayOfWeek d = date.getDayOfWeek();
        return runsOn(d);
    }

    /** Helper to build an EnumSet easily. */
    public static EnumSet<DayOfWeek> set(DayOfWeek... days) {
        if (days == null || days.length == 0) return EnumSet.noneOf(DayOfWeek.class);
        EnumSet<DayOfWeek> s = EnumSet.noneOf(DayOfWeek.class);
        for (DayOfWeek d : days) s.add(d);
        return s;
    }
}
