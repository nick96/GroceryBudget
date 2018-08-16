package com.example.nspain.grocerybudget;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.design.widget.Snackbar;
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
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Locale;

class ShoppingListItemAdapter extends RecyclerView.Adapter<ShoppingListItemAdapter.ViewHolder> {

    /**
     * Tag for debugging purposes
     */
    private static final String TAG = "GB/ShoppingListItemAdapter";

    /**
     * Store the shopping list data so that we can save it and have continuity with RecyclerView
     * recycling views.
     */
    private ShoppingList shoppingList;

    /**
     * Prevent mutually recursive calls to update listeners when binding views
     */
    private boolean onBind;

    /**
     * File to save the shopping list data to
     */
    private final String dataFileName;

    /**
     * Format currency according to the locale.
     */
    private final NumberFormat currencyFmt;

    /**
     * Whether or not a new item is being bound. This allows us to change the behaviour when new items
     * are created.
     */
    private boolean bindNewItem;

    public ShoppingListItemAdapter(String dataFileName, Locale locale) {
        this(new ShoppingList(
                new ArrayList<>(Collections.singletonList(
                        new ShoppingListItem(false, "", new BigDecimal(0))))),
                dataFileName, locale);
    }

    public ShoppingListItemAdapter(ShoppingList list, String dataFileName, Locale locale) {
        shoppingList = list;
        this.dataFileName = dataFileName;
        currencyFmt = NumberFormat.getCurrencyInstance(locale);
        bindNewItem = false;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox isBought;
        public EditText name;
        public EditText cost;
        public ImageButton deleteItemBtn;

        public ViewHolder(final View v) {
            super(v);
            isBought = v.findViewById(R.id.isBought);
            name = v.findViewById(R.id.name);
            cost = v.findViewById(R.id.cost);
            deleteItemBtn = v.findViewById(R.id.deleteItemBtn);

            isBought.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    final int pos = getAdapterPosition();
                    Log.d(TAG, "Item: " + shoppingList.getItem(pos));
                    shoppingList.getItem(pos).setIsBought(b);
                    if (!onBind) {
                        notifyItemChanged(pos);
                        saveData();
                    }
                }
            });

//            name.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                    shoppingList.getItem(getAdapterPosition()).setName(charSequence.toString());
//                }
//
//                @Override
//                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                }
//
//                @Override
//                public void afterTextChanged(Editable editable) {
//                }
//            });

            name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (isUserFinishedTyping(actionId, keyEvent)) {
                        notifyItemChanged(getAdapterPosition());
                        saveData();
                        return true;
                    } else {
                        return false;
                    }
                }
            });

//            cost.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                    Log.d(TAG, "Cost TextEdit has been changed");
//                    String input = charSequence.toString();
//
//                    // Fix up formatting of text input so that the BigDecimal parser doesn't fail
//                    input = prepCostText(input);
//
//                    try {
//                        Log.d(TAG, "Trying to parse input, " + input + ", into a BigDecimal");
//                        shoppingList.getItem(getAdapterPosition()).setCost(new BigDecimal(input));
//                    } catch (NumberFormatException e) {
//                        // When we fail to parse the string to a BigDecimal, notify the user.
//                        // Principle of least surprise
//                        e.printStackTrace();
//                        Log.d(TAG, "Cost formatting failed on input " + input, e);
////                        Snackbar.make(itemView, "I can't understand what you've just written", Snackbar.LENGTH_LONG);
//                        Log.d(TAG, "Cannot understand input " + input);
//                    }
//                }
//
//                @Override
//                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                }
//
//                @Override
//                public void afterTextChanged(Editable editable) {
//                }
//            });

            cost.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    // If the user has finished typing then we will update the shopping list
                    // accordingly and typeset the cost they just typed in nicely.
                    if (isUserFinishedTyping(actionId, keyEvent)) {
                        if (!textView.getText().toString().isEmpty()) {
                            final int pos = getAdapterPosition();
                            Log.d(TAG, "Cost is currently typeset as : " + textView.getText());
                            shoppingList.updateCost(pos, textView.getText());
                            String fmtText = currencyFmt.format(shoppingList.getItem(pos).getCost());
                            textView.setText(fmtText);
                            Log.d(TAG, "Cost is now typeset as: " + textView.getText());
                        }
                        notifyItemChanged(getAdapterPosition());
                        saveData();
                        return true;
                    } else {
                        return false;
                    }
                }
            });

            deleteItemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeItem(getAdapterPosition());
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
        ShoppingListItem item = shoppingList.getItem(pos);
        Log.d(TAG, "Binding item: " + item);
        viewHolder.isBought.setChecked(item.isBought());
        viewHolder.name.setText(item.getName());
        if (item.getCost() != null) {
            viewHolder.cost.setText(currencyFmt.format(item.getCost()));
        }

        if (bindNewItem) {
            viewHolder.name.requestFocus();
            bindNewItem = false;
        }
        onBind = false;
    }

    @Override
    public int getItemCount() {
        return shoppingList.getItemCount();
    }

    public void addItem(ShoppingListItem item) {
        shoppingList.addItem(item);
        Log.d(TAG, "Adding item " + item);
        notifyItemInserted(shoppingList.getItemCount() - 1);
        saveData();
        bindNewItem = true;
    }

    public ShoppingList getShoppingList() {
        return shoppingList;
    }

    public void saveData() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(dataFileName))) {
            outputStream.writeObject(shoppingList);
            Log.d(TAG, "Data has been saved!");
        } catch (IOException e) {
            Log.e(TAG, "Could not save data", e);
        }
    }

    public void removeItem(int pos) {
        Log.d(TAG, "Removing item at " + pos);
        shoppingList.remove(pos);
        notifyItemRemoved(pos);
        saveData();
    }
}
