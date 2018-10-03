package xyz.nspain.grocerybudget;

import android.support.v7.widget.RecyclerView;

import org.jetbrains.annotations.Nullable;

/**
 * Extension of the RecylcerView.Adapter that allows a payload to be sent on notifyItemRemove().
 */
public abstract class RecyclerViewAdapterWithRemovePayload<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
   private Object mRemoveItemPayload;

    public @Nullable Object getRemovedItemPayload() {
        return mRemoveItemPayload;
    }

    /**
     * Notify listeners of an item's remove and allow for a payload to be sent.
     *
     * NOTE: Observers must use getRemovedItemPayload() to get the payload for use in onItemRangeRemoved().
     *
     * @param pos Position of item removed from data set
     * @param msg Payload to send
     */
    public void notifyItemRemove(int pos, Object msg) {
        // Assign the payload before notifying of item removal to ensure that the payload is set
        mRemoveItemPayload = msg;
        notifyItemRemoved(pos);
    }
}
