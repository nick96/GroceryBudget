package xyz.nspain.grocerybudget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.nspain.grocerybudget.persistance.Item;
import xyz.nspain.grocerybudget.persistance.ShoppingList;

class ShoppingListEditAdapter extends RecyclerView.Adapter<ShoppingListEditAdapter.ViewHolder> {

    private static final String TAG = ShoppingListEditAdapter.class.getCanonicalName();
    private LayoutInflater mInflator;
    private List<ShoppingList> mLists;
    private boolean mOnBind = false;

    public ShoppingListEditAdapter(Context context) {
        this(context, Collections.<ShoppingList>emptyList());
    }

    public ShoppingListEditAdapter(Context context, List<ShoppingList> lists) {
        mInflator = LayoutInflater.from(context);
        mLists = lists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflator.inflate(R.layout.shopping_list_edit_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int pos) {
        mOnBind = true;
        ShoppingList list = mLists.get(pos);
        viewHolder.mListNameView.setText(list.getName());
        mOnBind = false;
    }

    @Override
    public int getItemCount() {
        return mLists.size();
    }

    public void setLists(List<ShoppingList> lists) {
        if (mLists == null) {
            mLists = lists;
        } else {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new GenericDiffCallBack<>(mLists, lists));
            mLists.clear();
            mLists = new ArrayList<>();
            mLists.addAll(lists);
            diffResult.dispatchUpdatesTo(this);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private EditText mListNameView;
        private AppCompatImageButton mDeleteListBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mListNameView = itemView.findViewById(R.id.list_name_edittext);
            mDeleteListBtn = itemView.findViewById(R.id.delete_list_button);

            mDeleteListBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = getAdapterPosition();
                    removeItemAt(pos);
                }
            });
        }
    }

    private void removeItemAt(int pos) {
        if (pos >= 0 && pos < mLists.size()) {
            ShoppingList removedList = mLists.remove(pos);
            notifyItemRemoved(pos);
            Log.d(TAG, "Removed " + removedList + " from list");
            AdapterNotifyMessage msg = new AdapterNotifyMessage(removedList, AdapterNotifyMessage.ChangeType.DELETE_LIST);
            Log.d(TAG, "Sending " + msg);
            notifyItemChanged(pos, msg);
        }
    }
}
