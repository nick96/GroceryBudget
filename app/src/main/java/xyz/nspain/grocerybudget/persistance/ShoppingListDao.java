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
    abstract LiveData<List<ShoppingList>> getLists();

    @Query("SELECT * FROM shopping_list")
    abstract List<ShoppingList> getListsBlocking();

    @Query("SELECT id FROM shopping_list WHERE name LIKE :name")
    abstract long getListIdByName(String name);

    @Query("SELECT id FROM shopping_list WHERE is_current")
    abstract long getCurrentListId();

    boolean isEmpty() {
        return getListsBlocking().isEmpty();
    }

    @Insert
    abstract List<Long> insert(ShoppingList... lists);

    @Update
    abstract void update(ShoppingList... lists);

    @Delete
    abstract void delete(ShoppingList... lists);

    @Query("UPDATE shopping_list set is_current = 0")
    public abstract void unsetCurrentList();

    @Query("UPDATE shopping_list set is_current = 1 WHERE name LIKE :listName")
    public abstract void setCurrentListByName(String listName);

    @Query("UPDATE shopping_list SET is_current = (name LIKE :listName)")
    public abstract void updateCurrentList(String listName);

    @Query("SELECT * FROM shopping_list WHERE is_current")
    public abstract LiveData<ShoppingList> getCurrentList();

    @Query("SELECT * FROM shopping_list WHERE is_current")
    public abstract List<ShoppingList> getCurrentLists();

    @Query("DELETE FROM shopping_list")
    public abstract void deleteAll();
}
