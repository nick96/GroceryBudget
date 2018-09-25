package xyz.nspain.grocerybudget;

import android.support.v7.util.DiffUtil;

import java.util.List;

import xyz.nspain.grocerybudget.persistance.ShoppingList;

class ShoppingListDiffCallback extends DiffUtil.Callback {
    private List<ShoppingList> mOldLists;
    private List<ShoppingList> mNewLists;

    public ShoppingListDiffCallback(List<ShoppingList> oldLists, List<ShoppingList> newLists) {
        mOldLists = oldLists;
        mNewLists = newLists;
    }

    @Override
    public int getOldListSize() {
        return mOldLists.size();
    }

    @Override
    public int getNewListSize() {
        return mNewLists.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPos, int newItemPos) {
        return mOldLists.get(oldItemPos).getId() == mNewLists.get(newItemPos).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPos, int newItemPos) {
        return mOldLists.get(oldItemPos).equals(mNewLists.get(newItemPos));
    }
}