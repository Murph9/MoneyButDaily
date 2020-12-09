package com.murph9.moneybutdaily.service;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Random;

public class CategoryColourService {

    private static final HashMap<String, Integer> categoryColourMap = new HashMap<>();
    private static final Random colRnd = new Random();
    public static Integer colourForCategory(String value) {
        if (categoryColourMap.containsKey(value))
            return categoryColourMap.get(value);

        int col = Color.rgb(colRnd.nextInt(256), colRnd.nextInt(256), colRnd.nextInt(256));
        categoryColourMap.put(value, col);
        return col;
    }
}
