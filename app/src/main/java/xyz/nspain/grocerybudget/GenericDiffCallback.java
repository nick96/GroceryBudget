package xyz.nspain.grocerybudget;

import android.support.v7.util.DiffUtil;

import java.util.List;

import xyz.nspain.grocerybudget.persistance.ShoppingListDatabaseEntity;

class GenericDiffCallBack<T extends ShoppingListDatabaseEntity> extends DiffUtil.Callback {
    private List<T> mOldList;
    private List<T> mNewList;

    public GenericDiffCallBack(List<T> oldList, List<T> newList) {
        mOldList = oldList;
        mNewList = newList;
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPos, int newItemPos) {
        return mOldList.get(oldItemPos).getId() == mNewList.get(newItemPos).getId();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPos, int newItemPos) {
        return mOldList.get(oldItemPos).equals(mNewList.get(newItemPos));
    }
}