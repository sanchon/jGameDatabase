package xyz.sanchon.jgamedatabase.service;

import xyz.sanchon.jgamedatabase.dto.GgDealsPriceDetails;

/**
 * Helpers for parsing and comparing GG.deals price strings.
 */
public final class GgDealsPriceUtils {

    private GgDealsPriceUtils() {
    }

    /** Returns the lowest current price (retail or keyshops) as a Double, or null if unavailable. */
    public static Double bestCurrent(GgDealsPriceDetails p) {
        if (p == null) {
            return null;
        }
        Double retail = parse(p.getCurrentRetail());
        Double keyshops = parse(p.getCurrentKeyshops());
        if (retail == null) {
            return keyshops;
        }
        if (keyshops == null) {
            return retail;
        }
        return Math.min(retail, keyshops);
    }

    /** Parses a GG.deals price string (e.g. "€49.99" or "49,99") into a Double. */
    public static Double parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String s = raw.replaceAll("[^0-9.,\\-]", "").trim();
        if (s.isEmpty()) {
            return null;
        }
        int lastDot = s.lastIndexOf('.');
        int lastComma = s.lastIndexOf(',');
        if (lastDot > lastComma) {
            s = s.replace(",", "");
        } else if (lastComma > lastDot) {
            s = s.replace(".", "").replace(",", ".");
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
