package com.loohp.holomobhealth.utils;

import com.loohp.holomobhealth.HoloMobHealth;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatColorUtils {

    private static final Set<Character> colors = new HashSet<Character>();
    private static final Pattern colorFormating = Pattern.compile("(?=(?<!\\\\)|(?<=\\\\\\\\))\\[[^\\]]*?color=#[0-9a-fA-F]{6}[^\\[]*?\\]");
    private static final Pattern colorEscape = Pattern.compile("\\\\\\[ *?color=#[0-9a-fA-F]{6} *?\\]");

    private static final String validColorHex = "^#[0-9a-fA-F]{6}$";

    static {
        colors.add('0');
        colors.add('1');
        colors.add('2');
        colors.add('3');
        colors.add('4');
        colors.add('5');
        colors.add('6');
        colors.add('7');
        colors.add('8');
        colors.add('9');
        colors.add('a');
        colors.add('b');
        colors.add('c');
        colors.add('d');
        colors.add('e');
        colors.add('f');
        colors.add('k');
        colors.add('l');
        colors.add('m');
        colors.add('n');
        colors.add('o');
        colors.add('r');
    }

    public static String stripColor(String string) {
        return string.replaceAll("\u00a7[0-9A-Fa-fk-orx]", "");
    }

    public static String filterIllegalColorCodes(String string) {
        return HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_16) ? string.replaceAll("\u00a7[^0-9A-Fa-fk-orx]", "") : string.replaceAll("\u00a7[^0-9a-fk-or]", "");
    }

    public static String getLastColors(String input) {
        String result = "";

        for (int i = input.length() - 1; i > 0; i--) {
            if (input.charAt(i - 1) == '\u00a7') {
                String color = String.valueOf(input.charAt(i - 1)) + input.charAt(i);
                if ((i - 13) >= 0 && input.charAt(i - 12) == 'x' && input.charAt(i - 13) == '\u00a7') {
                    color = input.substring(i - 13, i + 1);
                    i -= 13;
                }
                if (isLegal(color)) {
                    result = color + result;
                    if (color.charAt(1) == 'x' || isColor(ChatColor.getByChar(input.charAt(i))) || ChatColor.getByChar(input.charAt(i)).equals(ChatColor.RESET)) {
                        break;
                    }
                }
            }
        }

        return result;
    }

    public static String getFirstColors(String input) {
        String result = "";
        boolean found = false;

        if (input.length() < 2) {
            return "";
        }

        int i = 1;
        String color = "";
        while (i < input.length()) {
            color = String.valueOf(input.charAt(i - 1)) + input.charAt(i);
            if (input.charAt(i - 1) == '\u00a7' && input.charAt(i) == 'x' && input.length() > i + 13) {
                color = input.substring(i - 1, i + 13);
                i += 13;
            }
            if (isLegal(color)) {
                if (!found) {
                    found = true;
                    result = color;
                } else if (color.charAt(1) == 'x' || isColor(ChatColor.getByChar(color.charAt(1)))) {
                    result = color;
                } else {
                    result = result + color;
                }
                i++;
            } else if (found) {
                break;
            }
            i++;
        }

        return result;
    }

    public static boolean isColor(ChatColor color) {
        List<ChatColor> format = new ArrayList<ChatColor>();
        format.add(ChatColor.MAGIC);
        format.add(ChatColor.BOLD);
        format.add(ChatColor.ITALIC);
        format.add(ChatColor.UNDERLINE);
        format.add(ChatColor.STRIKETHROUGH);
        return !format.contains(color) && !color.equals(ChatColor.RESET);
    }

    public static boolean isLegal(String color) {
        if (color.charAt(0) != '\u00a7') {
            return false;
        }
        if (color.matches("\u00a7[0-9a-fk-or]")) {
            return true;
        }
        return color.matches("\u00a7x\u00a7[0-9A-F]\u00a7[0-9A-F]\u00a7[0-9A-F]\u00a7[0-9A-F]\u00a7[0-9A-F]\u00a7[0-9A-F]");
    }

    public static String addColorToEachWord(String text, String leadingColor) {
        StringBuilder sb = new StringBuilder();
        text = leadingColor + text;
        do {
            int pos = text.indexOf(" ") + 1;
            pos = pos <= 0 ? text.length() : pos;
            String before = leadingColor + text.substring(0, pos);
            //Bukkit.getConsoleSender().sendMessage(leadingColor.replace("\u00a7", "&") + " " + text.replace("\u00a7", "&") + " " + before.replace("\u00a7", "&"));
            sb.append(before);
            text = text.substring(pos);
            leadingColor = getLastColors(before);
        } while (text.length() > 0 && !text.equals(leadingColor));
        return sb.toString();
    }

    public static String hexToColorCode(String hex) {
        hex = hex.toUpperCase();
        if (hex == null) {
            return hex;
        }

        int pos = hex.indexOf("#");
        if (!hex.matches(validColorHex) || pos < 0 || hex.length() < (pos + 7)) {
            return "\u00a7x\u00a7F\u00a7F\u00a7F\u00a7F\u00a7F\u00a7F";
        }
        return "\u00a7x\u00a7" + hex.charAt(1) + "\u00a7" + hex.charAt(2) + "\u00a7" + hex.charAt(3) + "\u00a7" + hex.charAt(4) + "\u00a7" + hex.charAt(5) + "\u00a7" + hex.charAt(6);
    }

    public static String translatePluginColorFormatting(String text) {
        while (true) {
            Matcher matcher = colorFormating.matcher(text);

            if (matcher.find()) {
                String foramtedColor = matcher.group().toLowerCase();
                int start = matcher.start();
                int pos = foramtedColor.indexOf("color");
                int absPos = text.indexOf("color", start);
                int end = matcher.end();

                if (pos < 0) {
                    continue;
                }

                String colorCode = hexToColorCode(foramtedColor.substring(pos + 6, pos + 13));

                StringBuilder sb = new StringBuilder(text);
                sb.insert(end, colorCode);

                sb.delete(absPos, absPos + 13);

                while (sb.charAt(absPos) == ',' || sb.charAt(absPos) == ' ') {
                    sb.deleteCharAt(absPos);
                }

                while (sb.charAt(absPos - 1) == ',' || sb.charAt(absPos - 1) == ' ') {
                    sb.deleteCharAt(absPos - 1);
                    absPos--;
                }

                if (sb.charAt(absPos) == ']' && sb.charAt(absPos - 1) == '[') {
                    sb.deleteCharAt(absPos - 1);
                    sb.deleteCharAt(absPos - 1);

                    if (absPos > 2 && sb.charAt(absPos - 2) == '\\' && sb.charAt(absPos - 3) == '\\') {
                        sb.deleteCharAt(absPos - 2);
                    }
                }

                text = sb.toString();
            } else {
                break;
            }
        }

        while (true) {
            Matcher matcher = colorEscape.matcher(text);
            if (matcher.find()) {
                StringBuilder sb = new StringBuilder(text);
                sb.deleteCharAt(matcher.start());
                text = sb.toString();
            } else {
                break;
            }
        }

        return text;
    }

    public static String translateAlternateColorCodes(char code, String text) {
        if (text == null) {
            return text;
        }

        if (text.length() < 2) {
            return text;
        }

        if (HoloMobHealth.version.isNewerOrEqualTo(MCVersion.V1_16)) {
            text = translatePluginColorFormatting(text);
        }

        for (int i = 0; i < text.length() - 1; i++) {
            if (text.charAt(i) == code) {
                if (text.charAt(i + 1) == 'x' && text.length() > (i + 14)) {
                    String section = text.substring(i, i + 14);
                    String translated = section.replace(code, '\u00a7');
                    text = text.replace(section, translated);
                } else if (colors.contains(text.charAt(i + 1))) {
                    StringBuilder sb = new StringBuilder(text);
                    sb.setCharAt(i, '\u00a7');
                    text = sb.toString();
                }
            }
        }

        return text;
    }

}
