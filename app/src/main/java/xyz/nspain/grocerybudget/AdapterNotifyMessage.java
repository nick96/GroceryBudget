package xyz.nspain.grocerybudget;

import xyz.nspain.grocerybudget.persistance.Item;

class AdapterNotifyMessage {
    private Item mItem;
    private ChangeType mChangeType;

    public enum ChangeType {
        UPDATE,
        DELETE,
        INSERT
    }

    public AdapterNotifyMessage(Item item, ChangeType changeType) {
        mItem = item;
        mChangeType = changeType;
    }

    public Item getItem() {
        return mItem;
    }

    public ChangeType getChangeType() {
        return mChangeType;
    }
}
