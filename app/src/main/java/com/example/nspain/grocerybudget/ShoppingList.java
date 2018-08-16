package com.example.nspain.grocerybudget;

import android.util.Log;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShoppingList implements Serializable {
    private static final String TAG = "GB/ShoppingList";
    private ArrayList<ShoppingListItem> items;

    public ShoppingList() {
        this(new ArrayList<ShoppingListItem>());
    }

    public ShoppingList(ArrayList<ShoppingListItem> items){
        this.items = items;
    }

    public BigDecimal getShoppingCost() {
        BigDecimal total = new BigDecimal(0);
        for (ShoppingListItem item : items) {
            if (item.isBought() && item.getCost() != null) {
                total = total.add(item.getCost());
            }
        }
        return total;    }

    public ShoppingListItem getItem(int pos) {
        return items.get(pos);
    }

    public void remove(int pos) {
        items.remove(pos);
    }

    public void updateCost(int pos, CharSequence text) {
        BigDecimal newCost = costTextToCost(text);
        if (newCost != null) {
            ShoppingListItem item = items.get(pos);
            item.setCost(newCost);
            items.set(pos, item);
        }
    }

    private BigDecimal costTextToCost(CharSequence costText) {
        BigDecimal cost = null;
        String cleaned = costText.toString();

        if (cleaned.startsWith(".")) {
            cleaned = "0" + cleaned;
        }

        try {
            // This could be a source of bugs as the currency parser returns a double which is not
            // really sufficient for representing currency.
            cost = new BigDecimal((Double)NumberFormat.getCurrencyInstance().parse(cleaned));
        } catch (ParseException e) {
            Log.d(TAG, "Problem parsing " + cleaned, e);
        }

        return cost;
    }

    public int getItemCount() {
        return items.size();
    }

    public void addItem(ShoppingListItem item) {
        items.add(item);
    }
}
