package com.lethanh.ql_com_dao_bk.utils;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class LocalHideManager {
    private static final String PREF_NAME = "HiddenItemsPrefs";
    private static final String KEY_HIDDEN_PRODUCTS = "hidden_products";
    private static final String KEY_HIDDEN_CATEGORIES = "hidden_categories";

    public static void hideProduct(Context context, int id) {
        hideItem(context, KEY_HIDDEN_PRODUCTS, id);
    }

    public static void hideCategory(Context context, int id) {
        hideItem(context, KEY_HIDDEN_CATEGORIES, id);
    }

    public static boolean isProductHidden(Context context, int id) {
        return isHidden(context, KEY_HIDDEN_PRODUCTS, id);
    }

    public static boolean isCategoryHidden(Context context, int id) {
        return isHidden(context, KEY_HIDDEN_CATEGORIES, id);
    }

    private static void hideItem(Context context, String key, int id) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> hiddenIds = new HashSet<>(prefs.getStringSet(key, new HashSet<>()));
        hiddenIds.add(String.valueOf(id));
        prefs.edit().putStringSet(key, hiddenIds).apply();
    }

    private static boolean isHidden(Context context, String key, int id) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> hiddenIds = prefs.getStringSet(key, new HashSet<>());
        return hiddenIds.contains(String.valueOf(id));
    }
    
    public static void clearAll(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
