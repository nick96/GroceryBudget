package com.example.nspain.grocerybudget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

class ShoppingListItemAdapter extends RecyclerView.Adapter<ShoppingListItemAdapter.ViewHolder> {

    private static final String TAG = "GB/ShoppingListItemAdapter";
    public ArrayList<ShoppingListItem> shoppingList;
    private boolean onBind;

    public ShoppingListItemAdapter() {
        this(new ArrayList<ShoppingListItem>());
    }

    public ShoppingListItemAdapter(ArrayList<ShoppingListItem> items) {
        shoppingList = items;
    }

    public BigDecimal getTotalCost() {
        BigDecimal total = new BigDecimal(0);
        for (ShoppingListItem item: shoppingList) {
            if (item.isBought()) {
                total = total.add(item.getCost());
            }
        }
        return total;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox isBought;
        public EditText name;
        public EditText cost;
        private View view;

        public ViewHolder(View v) {
            super(v);
            view = v;
            isBought = v.findViewById(R.id.isBought);
            name = v.findViewById(R.id.name);
            cost = v.findViewById(R.id.cost);

            isBought.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    final int pos = getAdapterPosition();
                    shoppingList.get(pos).setIsBought(b);
                    if (!onBind) {
                        notifyItemChanged(pos);
                    }
                }
            });

            name.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    shoppingList.get(getAdapterPosition()).setName(charSequence.toString());
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void afterTextChanged(Editable editable) { }
            });

            name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (isUserFinishedTyping(actionId, keyEvent)) {
                        notifyItemChanged(getAdapterPosition());
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            cost.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.toString().isEmpty()) {
                        shoppingList.get(getAdapterPosition()).setCost(new BigDecimal("0"));
                    } else {
                        shoppingList.get(getAdapterPosition()).setCost(new BigDecimal(charSequence.toString()));
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

                @Override
                public void afterTextChanged(Editable editable) { }
            });

            cost.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (isUserFinishedTyping(actionId, keyEvent)) {
                        notifyItemChanged(getAdapterPosition());
                        return true;
                    } else {
                        return false;
                    }
                }
            });
        }

        private boolean isUserFinishedTyping(int actionId, KeyEvent keyEvent) {
            //noinspection SimplifiableIfStatement
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    keyEvent != null ||
                    (keyEvent != null && (keyEvent.getAction() == KeyEvent.ACTION_DOWN ||
                    keyEvent.getAction() == KeyEvent.KEYCODE_ENTER))) {
                
                if (keyEvent == null || !keyEvent.isShiftPressed()) {
                    Log.d(TAG, "User has finished typing");
                    return true;
                }
            }
            return false;
        }

    }

    @NonNull
    public ShoppingListItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View shoppingListItemView = inflater.inflate(R.layout.shopping_list_item, parent, false);
        return new ViewHolder(shoppingListItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int pos) {
        Log.d(TAG, "Binding view holder");

        onBind = true;
        ShoppingListItem item = shoppingList.get(pos);
        viewHolder.isBought.setChecked(item.isBought());
        viewHolder.name.setText(item.getName());
        viewHolder.cost.setText(item.getCost().toString());
        onBind = false;
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    public void addItem(ShoppingListItem item) {
        shoppingList.add(item);
        notifyItemInserted(shoppingList.size() - 1);
    }

    public ArrayList<ShoppingListItem> getShoppingList() {
        return shoppingList;
    }
}
