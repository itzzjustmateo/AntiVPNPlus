package rip.snake.antivpn.spigot.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Color {

    private static final Pattern HEX_PATTERN =
            Pattern.compile("&#([A-Fa-f0-9]{6})");

    private Color() {
        // utility class
    }

    /**
     * Colorizes a string using legacy (&) and hex (&#RRGGBB) color codes.
     *
     * @param text input text
     * @return colorized string, or empty string if null
     */
    public static String colorize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String result = translateHex(text);
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    /* ---------------------------------------------------------------------- */
    /* Internal Logic                                                         */
    /* ---------------------------------------------------------------------- */

    private static String translateHex(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
