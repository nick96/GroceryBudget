package xyz.nspain.grocerybudget.persistance;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.math.BigDecimal;
import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public abstract class ItemDao {
    @Query("SELECT * FROM shopping_list_item")
    abstract List<Item> getItems();

    @Query("SELECT * FROM shopping_list_item WHERE id = :id")
    abstract Item getItem(long id);

    @Query("SELECT * FROM shopping_list_item WHERE list_id = :listId")
    abstract List<Item> getItemsByList(long listId);

    @Query("SELECT * FROM shopping_list_item WHERE name LIKE :name")
    abstract List<Item> getItemByName(String name);

    @Query("SELECT SUM(cost) FROM shopping_list_item WHERE list_id = :listId AND is_bought")
    abstract BigDecimal getTotalCostForList(long listId);

    @Query("SELECT shopping_list_item.* FROM shopping_list_item"
            + " JOIN shopping_list WHERE shopping_list_item.list_id = shopping_list.id"
            + " AND shopping_list.is_current")
    abstract LiveData<List<Item>> getItemsInCurrentList();

    @Query("SELECT SUM(cost) FROM shopping_list_item WHERE is_bought AND"
            + " list_id IN (SELECT list_id FROM shopping_list WHERE is_current)")
    abstract LiveData<BigDecimal> getTotalCostForCurrentList();

    boolean isEmpty() {
        return getItems().isEmpty();
    }

    @Insert(onConflict = REPLACE)
    abstract void insert(Item... items);

    @Update(onConflict = REPLACE)
    abstract void update(Item... items);

    @Delete
    abstract void delete(Item... items);

    @Query("DELETE FROM shopping_list_item WHERE list_id IN (SELECT id FROM shopping_list WHERE is_current)")
    public abstract void deleteItemsInCurrentList();
}
