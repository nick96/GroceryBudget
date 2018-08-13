package com.example.nspain.grocerybudget;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

class ShoppingListItem implements Serializable {
    private boolean isBought;
    private String name;
    private BigDecimal cost;


    public ShoppingListItem(boolean isBought, String name, BigDecimal cost) {
        this.isBought = isBought;
        this.name = name;
        this.cost = cost;
    }

    public boolean isBought() {
        return isBought;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public static ArrayList<ShoppingListItem> createShoppingList() {
        return new ArrayList<>(Arrays.asList(
                new ShoppingListItem(true, "Break", new BigDecimal(5.00)),
                new ShoppingListItem(false, "Milk", new BigDecimal(3.00)),
                new ShoppingListItem(false, "cheese", new BigDecimal(10.00))
        ));
    }

    public void setIsBought(boolean isBought) {
        this.isBought = isBought;
    }
}
