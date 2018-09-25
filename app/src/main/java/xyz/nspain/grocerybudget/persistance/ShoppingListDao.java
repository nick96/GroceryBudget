package xyz.nspain.grocerybudget.persistance;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public abstract class ShoppingListDao {
    @Query("SELECT * FROM shopping_list")
    abstract List<ShoppingList> getLists();

    @Query("SELECT id FROM shopping_list WHERE name LIKE :name")
    abstract long getListIdByName(String name);

    @Query("SELECT id FROM shopping_list WHERE is_current")
    abstract long getCurrentListId();

    boolean isEmpty() {
        return getLists().isEmpty();
    }

    @Insert
    abstract List<Long> insert(ShoppingList... lists);

    @Update
    abstract void update(ShoppingList... lists);

    @Delete
    abstract void delete(ShoppingList... lists);
}
