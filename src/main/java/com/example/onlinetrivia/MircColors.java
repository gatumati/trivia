package com.example.onlinetrivia;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MircColors {

    private static final int[] MIRC_COLORS = {
            Color.WHITE,      // 0
            Color.BLACK,      // 1
            Color.BLUE,       // 2
            Color.GREEN,      // 3
            Color.RED,        // 4
            0xFF800000,       // MAROON - 5
            0xFF800080,       // PURPLE - 6
            0xFF808000,       // OLIVE - 7
            Color.YELLOW,     // 8
            0xFF00FF00,       // LIME - 9
            0xFF008080,       // TEAL - 10
            0xFF00FFFF,       // AQUA - 11
            Color.BLUE,       // 12
            0xFFFF00FF,       // FUCHSIA - 13
            Color.GRAY,       // 14
            0xFFC0C0C0        // SILVER - 15
    };





    private static final Pattern COLOR_PATTERN = Pattern.compile("(\\d{1,2})");






    public static Spannable toSpannable(String message) {
        SpannableStringBuilder spannable = new SpannableStringBuilder(message);
        Matcher matcher = COLOR_PATTERN.matcher(spannable);
        int lastEnd = 0;

        while (matcher.find()) {
            int colorCode = Integer.parseInt(matcher.group(1));
            if (colorCode < MIRC_COLORS.length) {
                int colorStart = matcher.start();
                int colorEnd = matcher.end();
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(MIRC_COLORS[colorCode]);

                // Apply the color span from the end of the color code to the next color code or end of the message
                spannable.setSpan(colorSpan, colorEnd, spannable.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Remove the color code
                spannable.delete(colorStart, colorEnd);

                // Reset the matcher since the string has changed
                matcher.reset(spannable);
            }
        }

        return spannable;
    }










}
