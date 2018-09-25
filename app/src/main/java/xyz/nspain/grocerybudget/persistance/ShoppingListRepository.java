package xyz.nspain.grocerybudget.persistance;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShoppingListRepository {
    private static final String TAG = ShoppingListRepository.class.getCanonicalName();
    private ItemDao mItemDao;
    private ShoppingListDao mShoppingListDao;

    public ShoppingListRepository(Application application) {
        ShoppingListDatabase db = ShoppingListDatabase.getDatabase(application);
        mItemDao = db.itemDao();
        mShoppingListDao = db.shoppingListDao();
    }

    public void insertItem(final Item item) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                long listId = mShoppingListDao.getCurrentListId();
                item.setListId(listId);
                Log.d(TAG, "Inserting " + item);
                mItemDao.insert(item);
            }
        });
    }

    public void insertItems(final Item... items) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                long listId = mShoppingListDao.getCurrentListId();
                for (Item item : items) {
                    item.setListId(listId);
                }
                mItemDao.insert(items);
            }
        });
    }

    public void insertList(final ShoppingList list) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mShoppingListDao.insert(list);
            }
        });
    }

    public LiveData<List<Item>> getItemsInCurrentList() {
        return mItemDao.getItemsInCurrentList();
    }

    public void deleteItem(final Item item) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mItemDao.delete(item);
            }
        });
    }

    public void updateItem(final Item item) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Updating " + item);
                mItemDao.update(item);
                Log.d(TAG, "Updated " + item);
            }
        });
    }

    public LiveData<BigDecimal> getCurrentListTotal() {
        return mItemDao.getTotalCostForCurrentList();
    }

    public LiveData<List<ShoppingList>> getLists() {
        return mShoppingListDao.getLists();
    }

    public void updateCurrentList(final String listName) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                mShoppingListDao.updateCurrentList(listName);
            }
        });
    }
}
