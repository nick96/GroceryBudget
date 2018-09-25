package xyz.nspain.grocerybudget;

import xyz.nspain.grocerybudget.persistance.Item;

class AdapterNotifyMessage {
    private Object mPayload;
    private ChangeType mChangeType;

    public enum ChangeType {
        UPDATE_ITEM,
        DELETE_ITEM,
        INSERT_ITEM,

        UPDATE_LIST,
        DELETE_LIST,
        INSERT_LIST
    }

    public AdapterNotifyMessage(Object item, ChangeType changeType) {
        mPayload = item;
        mChangeType = changeType;
    }

    public Object getPayload() {
        return mPayload;
    }

    public ChangeType getChangeType() {
        return mChangeType;
    }
}
