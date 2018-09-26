package xyz.nspain.grocerybudget;

import android.support.test.filters.LargeTest;

import org.junit.Test;

/**
 * Instrumentation tests for the UI of individual shopping lists
 */
@LargeTest
public class ShoppingListTests {
    /**
     * Pressing the FAB should create a new item in the list and focus should be on the EditText
     * for the items name.
     */
    @Test
    public void createNewItemTest() {

    }

    /**
     * Pressing the delete button should delete the item.
     */
    @Test
    public void deleteItemTest() {

    }

    /**
     * The database should be update when the item field are updated.
     */
    @Test
    public void updateItemInDbTest() {

    }

    /**
     * The database should be updated when items are inserted
     */
    @Test
    public void insertItemInDbTest() {

    }

    /**
     * The database should be updated when items are deleted
     */
    @Test
    public void deleteItemInDbTest() {

    }

    /**
     * Pressing the clear all button should remove all items from the list
     */
    @Test
    public void deleteAllItemsTest() {

    }

    /**
     * The total cost bar should update when an item's cost is updated
     */
    @Test
    public void updateTotalCostOnItemCostUpdateTest() {

    }

    /**
     * The total cost bar should update when an item's bought status is updated
     */
    @Test
    public void updateTotalCostOnItemBoughtStatusChangeTest() {

    }
}
