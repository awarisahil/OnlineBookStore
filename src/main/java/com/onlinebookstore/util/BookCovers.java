package com.onlinebookstore.util;

/**
 * Deterministic book-cover color palettes derived from the book barcode, so
 * every book gets a stable, attractive cover without storing image assets.
 */
public final class BookCovers {

    private static final String[][] PALETTES = {
            { "#7f5539", "#b08968" },
            { "#1d3557", "#457b9d" },
            { "#3a5a40", "#588157" },
            { "#6d2e46", "#a26769" },
            { "#432818", "#99582a" },
            { "#354f52", "#52796f" },
            { "#5e503f", "#a98467" },
            { "#6b2737", "#a4243b" },
            { "#2b2d42", "#8d99ae" },
            { "#4a4e69", "#9a8c98" },
            { "#774936", "#c08552" },
            { "#386641", "#6a994e" },
    };

    private BookCovers() {
    }

    public static Palette palette(String barcode) {
        int index = Math.floorMod(barcode.hashCode(), PALETTES.length);
        return new Palette(PALETTES[index][0], PALETTES[index][1]);
    }

    public record Palette(String c1, String c2) {
    }
}
