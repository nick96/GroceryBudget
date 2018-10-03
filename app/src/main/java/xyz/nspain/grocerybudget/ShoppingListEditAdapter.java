package xyz.nspain.grocerybudget;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.nspain.grocerybudget.persistance.ShoppingList;

import static xyz.nspain.grocerybudget.AdapterNotifyMessage.ChangeType;

class ShoppingListEditAdapter extends RecyclerViewAdapterWithRemovePayload<ShoppingListEditAdapter.ViewHolder> implements ItemTouchHelperAdapter{

    private static final String TAG = ShoppingListEditAdapter.class.getCanonicalName();
    private LayoutInflater mInflator;
    private List<ShoppingList> mLists;
    private boolean mOnBind = false;
    private enum CursorLocation {LIST_NAME_VIEW};
    private CursorLocation mCursorLocation = null;
    private int mCursorPos = -1;

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

        if (mCursorLocation != null) {
            int textLen;
            switch (mCursorLocation) {
                case LIST_NAME_VIEW:
                    textLen = viewHolder.mListNameView.getText().length();
                    if (mCursorPos >= 0 && mCursorPos <= textLen) {
                        viewHolder.mListNameView.setSelection(mCursorPos);
                    }
                    break;
            }
        }
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

    public class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        private CardView mListCardView;
        private EditText mListNameView;
        private AppCompatImageButton mDeleteListBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mListCardView = itemView.findViewById(R.id.shopping_list_card);
            mListNameView = itemView.findViewById(R.id.list_name_edittext);

            mListNameView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!mOnBind) {
                        final int pos = getAdapterPosition();
                        mCursorPos = mListNameView.getSelectionStart();
                        mLists.get(pos).setName(s.toString());
                        notifyItemChanged(pos, new AdapterNotifyMessage(mLists.get(pos), ChangeType.UPDATE_LIST));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            mListNameView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        mCursorLocation = CursorLocation.LIST_NAME_VIEW;
                        mCursorPos = mListNameView.getSelectionStart();
                    } else {
                        mCursorPos = -1;
                        mCursorLocation = null;
                    }
                }
            });

            mListCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = getAdapterPosition();
                    ShoppingList list = mLists.get(pos);
                    notifyItemChanged(pos, new AdapterNotifyMessage(list, ChangeType.VIEW_LIST));
                }
            });
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }

    private void removeItemAt(int pos) {
        if (pos >= 0 && pos < mLists.size()) {
            ShoppingList removedList = mLists.remove(pos);
            AdapterNotifyMessage msg = new AdapterNotifyMessage(removedList, ChangeType.DELETE_LIST);
            notifyItemRemove(pos, msg);
            notifyItemRangeChanged(pos, mLists.size());
        }
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        ShoppingList prev = mLists.remove(fromPosition);
        mLists.add(toPosition > fromPosition ? toPosition - 1 : toPosition, prev);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        ShoppingList removedList = mLists.remove(position);
        notifyItemRemove(position, new AdapterNotifyMessage(removedList, ChangeType.DELETE_LIST));
    }
}
