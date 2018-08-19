package xyz.nspain.grocerybudget;

import android.util.Log;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

public class ShoppingList implements Serializable {
    private static final String TAG = "GB/ShoppingList";
    private ArrayList<ShoppingListItem> items;
    private Locale locale;

    public ShoppingList(ArrayList<ShoppingListItem> shoppingListItems) {
        this(new ArrayList<ShoppingListItem>(), null);
    }


    public ShoppingList(ArrayList<ShoppingListItem> items, Locale locale){
        super();
        this.items = items;
        this.locale = locale != null? locale : Locale.getDefault();
    }

    public ShoppingList(ShoppingList list) {
        this.items = (ArrayList<ShoppingListItem>)list.getItems().clone();
        this.locale = (Locale)list.getLocale().clone();
    }

    public ArrayList<ShoppingListItem> getItems() {
        return items;
    }

    public Locale getLocale() {
        return locale;
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
        if (pos >= 0 && pos < items.size()) {
            return items.get(pos);
        }
        return null;
    }

    public void remove(int pos) {
        if (pos > 0 && pos < items.size()) {
            items.remove(pos);
        }
    }

    public void updateCost(int pos, CharSequence text) {
        BigDecimal newCost = costTextToCost(text);
        if (newCost != null) {
            ShoppingListItem item = getItem(pos);
            if (item != null) {
                item.setCost(newCost);
                items.set(pos, item);
            }
        }
    }

    private BigDecimal costTextToCost(CharSequence costText) {
        BigDecimal cost = null;
        String cleaned = costText.toString();

        if (cleaned.startsWith(".")) {
            cleaned = "0" + cleaned;
        }

        Log.d(TAG, "Currency Symbol: " + NumberFormat.getCurrencyInstance(locale).getCurrency().getSymbol());

        try {
            cost = new BigDecimal(cleaned);
        } catch (NumberFormatException numberFormatException) {
            Log.d(TAG, "Cannot convert " + cleaned + " to a BigDecimal, will try using currency parser", numberFormatException);
            try {
                // This could be a source of bugs as the currency parser returns a double which is not
                // really sufficient for representing currency.
                cost = new BigDecimal(NumberFormat.getCurrencyInstance(locale).parse(cleaned).toString());
            } catch (ParseException parseException) {
                Log.d(TAG, "Problem parsing " + cleaned, parseException);
            }
        }

        return cost;
    }

    public int getItemCount() {
        return items.size();
    }

    public void addItem(ShoppingListItem item) {
        items.add(item);
    }

    @Override
    public boolean equals(Object o) {
        ShoppingList other = (ShoppingList)o;

        if (!other.locale.equals(locale)) {
            return false;
        }

        if (other.getItemCount() != getItemCount()) {
            return false;
        }

        for (int i = 0; i < getItemCount(); i++) {
            if (!getItem(i).equals(other.getItem(i))) {
                return false;
            }
        }
        return true;
    }
}
