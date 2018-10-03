package xyz.nspain.grocerybudget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import xyz.nspain.grocerybudget.AdapterNotifyMessage.ChangeType;
import xyz.nspain.grocerybudget.persistance.Item;

public class ShoppingListAdapter extends RecyclerViewAdapterWithRemovePayload<ShoppingListAdapter.ItemViewHolder> {
    private static final String TAG = ShoppingListAdapter.class.getCanonicalName();
    private final LayoutInflater mInflator;
    private List<Item> mItems = Collections.emptyList();

    private boolean mOnBind = false;
    private final NumberFormat mCurrencyFormatter;

    private enum CursorLocation {ITEM_NAME_VIEW, ITEM_COST_VIEW};
    private CursorLocation mCursorLocation = null;
    private int mCursorPos = -1;


    public ShoppingListAdapter(Context context, NumberFormat numberFormatter)  {
        mInflator = LayoutInflater.from(context);
        mCurrencyFormatter = numberFormatter;
    }

    class ItemViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        private final EditText mItemNameView;
        private final EditText mItemCostView;
        private final CheckBox mItemIsBoughtView;
        private final AppCompatImageButton mItemDeleteButton;

        private final TextWatcher mItemCostViewTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mOnBind) {
                    mCursorPos = mItemCostView.getSelectionStart();
                    final int pos = getAdapterPosition();
                    Item currItem = mItems.get(pos);
                    BigDecimal cost = parseItemCostText(s.toString());
                    currItem.setCost(cost);
                    AdapterNotifyMessage msg = new AdapterNotifyMessage(currItem, ChangeType.UPDATE_ITEM);
                    notifyItemChanged(pos, msg);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        private final View.OnFocusChangeListener mItemCostViewFocusChangeListener = new View.OnFocusChangeListener() {
            private String mCurrentText = "";
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mItemCostView.removeTextChangedListener(mItemCostViewTextWatcher);
                String s = mItemCostView.getText().toString();
                if (hasFocus) {
                    mCursorLocation = CursorLocation.ITEM_COST_VIEW;
                    mCursorPos = mItemCostView.getSelectionStart();
                } else {
                    mCursorLocation = null;
                    if (!s.equals(mCurrentText)) {
                        String cleanString = s;
                        try {
                            cleanString = mCurrencyFormatter.parse(s).toString();
                        } catch (ParseException|NumberFormatException e) {
                        }
                        String fmtedText = mCurrencyFormatter.format(new BigDecimal(cleanString));
                        mCurrentText = fmtedText;
                        mItemCostView.setText(fmtedText);
                        mItemCostView.setSelection(mCursorPos);
                    }

                }
                mItemCostView.addTextChangedListener(mItemCostViewTextWatcher);
            }
        };

        private final TextWatcher mItemNameViewTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mOnBind) {
                    final int pos = getAdapterPosition();
                    Item currItem = mItems.get(pos);
                    currItem.setName(s.toString());
                    mCursorPos = mItemNameView.getSelectionStart();
                    AdapterNotifyMessage msg = new AdapterNotifyMessage(currItem, ChangeType.UPDATE_ITEM);
                    notifyItemChanged(pos, msg);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        private final View.OnFocusChangeListener mItemNameViewFocusChangeListener = new View.OnFocusChangeListener() {
            private final String x = "";
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mCursorLocation = CursorLocation.ITEM_NAME_VIEW;
                    mCursorPos = mItemNameView.getSelectionStart();
                } else {
                    mCursorLocation = null;
                }
            }
        };

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mItemNameView = itemView.findViewById(R.id.item_name);
            mItemCostView = itemView.findViewById(R.id.item_cost);
            mItemIsBoughtView = itemView.findViewById(R.id.item_is_bought);
            mItemDeleteButton = itemView.findViewById(R.id.deleteItemBtn);

            mItemNameView.addTextChangedListener(mItemNameViewTextWatcher);
            mItemNameView.setOnFocusChangeListener(mItemNameViewFocusChangeListener);

            mItemCostView.addTextChangedListener(mItemCostViewTextWatcher);
            mItemCostView.setOnFocusChangeListener(mItemCostViewFocusChangeListener);

            mItemIsBoughtView.setOnCheckedChangeListener(this);
            mItemDeleteButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == mItemDeleteButton.getId() && !mOnBind) {
                final int pos = getAdapterPosition();
                removeItemAt(pos);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView.getId() == mItemIsBoughtView.getId() && !mOnBind) {
                final int pos = getAdapterPosition();
                mItems.get(pos).setIsBought(isChecked);
                AdapterNotifyMessage msg = new AdapterNotifyMessage(mItems.get(pos), ChangeType.UPDATE_ITEM);
                notifyItemChanged(pos, msg);
            }
        }
    }

    private BigDecimal parseItemCostText(String costText) {
        BigDecimal cost = new BigDecimal(0);
        try {
            if (mCurrencyFormatter != null) {
                cost = new BigDecimal(mCurrencyFormatter.parse(costText).toString());
            } else {
                cost = new BigDecimal(costText);
            }

        } catch (ParseException parserException) {
            try {
                cost = new BigDecimal(costText);
            } catch (NumberFormatException numberFormatException) {
            }
        }
        return cost;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflator.inflate(R.layout.shopping_list_item, parent, false);
        return new ItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder itemViewHolder, int position) {
        mOnBind = true;
        Item currItem = mItems.get(position);
        String itemName = currItem.getName() != null ? currItem.getName() : "";
        BigDecimal itemCost = currItem.getCost() != null ? currItem.getCost() : new BigDecimal(0);

        String itemCostTxt = itemCost.toString();
        if (mCurrencyFormatter != null) {
            itemCostTxt = mCurrencyFormatter.format(itemCost);
        }

        itemViewHolder.mItemNameView.setText(itemName);
        itemViewHolder.mItemCostView.setText(itemCostTxt);
        itemViewHolder.mItemIsBoughtView.setChecked(currItem.isBought());
        if (mCursorLocation != null) {
            int textLen;
            switch (mCursorLocation) {
                case ITEM_COST_VIEW:
                    textLen = itemViewHolder.mItemCostView.getText().length();
                    if (mCursorPos >= 0 && mCursorPos <= textLen) {
                        itemViewHolder.mItemCostView.setSelection(mCursorPos);
                    }
                    break;
                case ITEM_NAME_VIEW:
                    textLen = itemViewHolder.mItemNameView.getText().length();
                    if (mCursorPos >= 0 && mCursorPos <= textLen) {
                        itemViewHolder.mItemNameView.setSelection(mCursorPos);
                    }
                    break;
            }
        }
        mOnBind = false;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public List<Item> getItems() {
        return mItems;
    }

    void setItems(List<Item> items) {
        if (mItems == null) {
            mItems = items;
        } else {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new GenericDiffCallBack<>(mItems, items));
            mItems.clear();
            mItems = new ArrayList<>();
            mItems.addAll(items);
            diffResult.dispatchUpdatesTo(this);
        }
    }

    private void removeItemAt(int pos) {
        if (pos >= 0 && pos < mItems.size()) {
            Item removedItem = mItems.remove(pos);
            AdapterNotifyMessage msg = new AdapterNotifyMessage(removedItem, ChangeType.DELETE_ITEM);
            notifyItemRemove(pos, msg);
            notifyItemRangeChanged(pos, mItems.size());
        }
    }
}
