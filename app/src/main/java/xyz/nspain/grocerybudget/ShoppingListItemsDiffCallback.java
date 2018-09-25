package xyz.nspain.grocerybudget;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.List;

import xyz.nspain.grocerybudget.persistance.Item;

class ShoppingListItemsDiffCallback extends DiffUtil.Callback {
    private List<Item> mOldItems;
    private List<Item> mNewItems;

    public ShoppingListItemsDiffCallback(List<Item> oldItems, List<Item> newItems) {
        mOldItems = oldItems;
        mNewItems = newItems;
    }

    @Override
    public int getOldListSize() {
        return mOldItems.size();
    }

    @Override
    public int getNewListSize() {
        return mNewItems.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPos, int newItemPos) {
        return mOldItems.get(oldItemPos).getId() == mNewItems.get(newItemPos).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPos, int newItemPos) {
        return mOldItems.get(oldItemPos).equals(mNewItems.get(newItemPos));
    }
}
