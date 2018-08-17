package xyz.nspain.grocerybudget;

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

    @Override
    public String toString() {
        return String.format("name: %s; cost: %s; isBought: %s", name, cost, isBought);
    }

    @Override
    public boolean equals(Object object) {
        ShoppingListItem item = (ShoppingListItem) object;
        boolean boughtEqual = item.isBought == isBought,
                // Use compareTo() over equals() because equals() will say that 1 and 1.00 are not
                // equal but for our purposes they are
                costEqual = item.getCost().compareTo(cost) == 0,
                nameEqual = item.getName().equals(name);

        return boughtEqual && costEqual && nameEqual;
    }
}
