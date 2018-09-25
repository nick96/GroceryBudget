package xyz.nspain.grocerybudget;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import xyz.nspain.grocerybudget.persistance.Item;
import xyz.nspain.grocerybudget.persistance.ShoppingList;
import xyz.nspain.grocerybudget.persistance.ShoppingListRepository;

public class ShoppingListViewModel extends AndroidViewModel {
    private ShoppingListRepository mRepository;
    private LiveData<List<Item>> mItems;
    private boolean mIsCircularUpdate;

    private static final String TAG = ShoppingListViewModel.class.getCanonicalName();

    public ShoppingListViewModel(@NonNull Application application) {
        super(application);
        mRepository = new ShoppingListRepository(application);
        mItems = mRepository.getItemsInCurrentList();
        mIsCircularUpdate = false;
    }

    public LiveData<List<Item>> getItems() {
        if (mItems == null) {
            mItems = mRepository.getItemsInCurrentList();
        }
        return mItems;
    }

    public void insertItem(Item item) {
        mRepository.insertItem(item);
    }

    public void insertList(ShoppingList list) {
        mRepository.insertList(list);
    }


    public void deleteItem(Item item) {
        mRepository.deleteItem(item);
    }

    public void updateItem(Item item) {
        Log.d(TAG, "Updating item " + item);
        mRepository.updateItem(item);
    }

    public void updateListItems(List<Item> items) {
        mRepository.insertItems(items.toArray(new Item[items.size()]));
    }

    public boolean isCircularUpdate() {
        return mIsCircularUpdate;
    }

    public void setIsCircularUpdate(boolean val) {
        mIsCircularUpdate = val;
    }
}
