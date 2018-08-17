package xyz.nspain.grocerybudget;

import android.util.Log;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class ShoppingListTest {

    private ShoppingList shoppingList;

    @Before
    public void setUp() {
        shoppingList = new ShoppingList(new ArrayList<>(Arrays.asList(
                new ShoppingListItem(false, "bread", new BigDecimal(10)),
                new ShoppingListItem(false, "cheese", new BigDecimal("11.23")),
                new ShoppingListItem(false, "milk", new BigDecimal("100"))
        )), null);
        PowerMockito.mockStatic(Log.class);

    }

    @Test
    public void getShoppingCost() {
        // When nothing is ticked, none of the costs should be included in the sum
        assertThat(shoppingList.getShoppingCost(), is(new BigDecimal(0)));

        // When items are ticked, they should be included in the sum
        shoppingList.getItem(0).setIsBought(true);
        assertThat(shoppingList.getShoppingCost(), is(new BigDecimal(10)));

        shoppingList.getItem(1).setIsBought(true);
        assertThat(shoppingList.getShoppingCost(), is(new BigDecimal("21.23")));

        shoppingList.getItem(2).setIsBought(true);
        assertThat(shoppingList.getShoppingCost(), is(new BigDecimal("121.23")));
    }

    @Test
    public void getExistingIndex() {
        assertThat(shoppingList.getItem(1),
                equalTo(new ShoppingListItem(false, "cheese", new BigDecimal("11.23"))));
    }

    @Test
    public void getNonExistingIndex() {
        assertThat(shoppingList.getItem(3), nullValue());
    }

    @Test
    public void removeExistingIndex() {
        ShoppingList newShoppingList = new ShoppingList(new ArrayList<ShoppingListItem>(Arrays.asList(
                new ShoppingListItem(false, "bread", new BigDecimal(10)),
                new ShoppingListItem(false, "cheese", new BigDecimal("11.23"))
        )), null);

        shoppingList.remove(2);
        assertThat(shoppingList, equalTo(newShoppingList));
    }

    @Test
    public void removeNonExistingIndex() {
        ShoppingList oldShoppingList = new ShoppingList(shoppingList);
        shoppingList.remove(3);
        assertThat(shoppingList, equalTo(oldShoppingList));
    }

    @Test
    public void updateCostUnformatted() {
        shoppingList.updateCost(1, "1.00");
        assertThat(shoppingList.getItem(1),
                equalTo(new ShoppingListItem(false, "cheese", new BigDecimal("1.00"))));
    }

    @Test
    public void updateCostFormatted() {
        NumberFormat fmt = NumberFormat.getCurrencyInstance(shoppingList.getLocale());
        String cost = fmt.format(new BigDecimal("1.00"));
        shoppingList.updateCost(1, cost);
        assertThat(shoppingList.getItem(1),
                equalTo(new ShoppingListItem(false, "cheese", new BigDecimal("1.00"))));
    }

    @Test
    public void getItemCount() {
        assertThat(shoppingList.getItemCount(), is(3));
    }

    @Test
    public void addItem() {
        ShoppingList newList = shoppingList = new ShoppingList(new ArrayList<ShoppingListItem>(Arrays.asList(
                new ShoppingListItem(false, "bread", new BigDecimal(10)),
                new ShoppingListItem(false, "cheese", new BigDecimal("11.23")),
                new ShoppingListItem(false, "milk", new BigDecimal("100")),
                new ShoppingListItem(false, "apples", new BigDecimal("15"))
        )), null);
        shoppingList.addItem(new ShoppingListItem(false, "apples", new BigDecimal("15")));
        assertThat(shoppingList, equalTo(newList));
    }
}