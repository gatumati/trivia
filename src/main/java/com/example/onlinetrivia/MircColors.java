package com.example.onlinetrivia;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MircColors {

    private static final int[] colors = {
            0xFFFFFF,  // White
            0x000000,  // Black
            0x00007F,  // Blue (navy)
            0x009300,  // Green
            0xFC0000,  // Red
            0x7F0000,  // Brown (maroon)
            0x9C009C,  // Purple
            0xFC7F00,  // Orange (olive)
            0xFFFF00,  // Yellow
            0x00FC00,  // Light Green (lime)
            0x008080,  // Teal (a green/blue cyan)
            0x00FFFF,  // Light Cyan (cyan) (aqua)
            0x0000FF,  // Light Blue (royal)
            0xFF00FF,  // Pink (light purple) (fuchsia)
            0x7F7F7F,  // Grey
            0xD2D2D2   // Light Grey (silver)
    };

    private static final Pattern boldPattern = Pattern.compile("\\x02([^\\x02\\x0F]*)(\\x02|(\\x0F))?");
    private static final Pattern underlinePattern = Pattern.compile("\\x1F([^\\x1F\\x0F]*)(\\x1F|(\\x0F))?");
    private static final Pattern italicPattern = Pattern.compile("\\x1D([^\\x1D\\x0F]*)(\\x1D|(\\x0F))?");
    private static final Pattern inversePattern = Pattern.compile("\\x16([^\\x16\\x0F]*)(\\x16|(\\x0F))?");
    private static final Pattern colorPattern = Pattern.compile("\\x03(\\d{1,2})(?:,(\\d{1,2}))?([^\\x03\\x0F]*)(\\x03|\\x0F)?");
    private static final Pattern cleanupPattern = Pattern.compile("(?:\\x02|\\x1F|\\x1D|\\x0F|\\x16|\\x03(?:(?:\\d{1,2})(?:,\\d{1,2})?)?)");

    public static SpannableString toSpannable(SpannableString text) {
        // Your original logic here

        // After the logic, return the text
        return text;
    }

    public static SpannableString toSpannable(String text) {
        Matcher colorMatcher = colorPattern.matcher(text);
        SpannableString spannable = new SpannableString(text);

        while (colorMatcher.find()) {
            // Extract the color code, if present
            int colorCode = -1;
            if (colorMatcher.group(1) != null) {
                colorCode = Integer.parseInt(Objects.requireNonNull(colorMatcher.group(1)));
            }

            // If a valid color code was found, apply the color
            if (colorCode >= 0 && colorCode < colors.length) {
                int textColor = colors[colorCode];
                spannable.setSpan(new ForegroundColorSpan(textColor), colorMatcher.start(3), colorMatcher.end(3), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        // Remove mIRC codes for display
        return new SpannableString(cleanupPattern.matcher(spannable).replaceAll(""));
    }


    private void appendIrcMessage(String fullMessage) {
        Log.d("IRCMessage", fullMessage); // <-- Add this

    }
    private static void replaceControlCodes(Matcher m, SpannableStringBuilder ssb, CharacterStyle style) {
        // Your original logic here
    }

    public static String removeStyleAndColors(String text) {
        return cleanupPattern.matcher(text).replaceAll("");
    }

    public static SpannableStringBuilder removeStyleAndColors(SpannableStringBuilder text) {
        // Your original logic here

        // After the logic, return the text
        return text;
    }
}
