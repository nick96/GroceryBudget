package com.example.nspain.grocerybudget;

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
    public ArrayList<ShoppingListItem> shoppingList;

    /**
     * Prevent mutually recursive calls to update listeners when binding views
     */
    private boolean onBind;

    /**
     * File to save the shopping list data to
     */
    private final File dataFile;

    private final Locale locale;

    private final NumberFormat currencyFmt;

    public ShoppingListItemAdapter(File dataFile, Locale locale) {
        this(new ArrayList<ShoppingListItem>(), dataFile, locale);
    }

    public ShoppingListItemAdapter(ArrayList<ShoppingListItem> items, File dataFile, Locale locale) {
        shoppingList = items;
        this.dataFile = dataFile;
        this.locale = locale;
        currencyFmt = NumberFormat.getCurrencyInstance(locale);
    }

    public BigDecimal getTotalCost() {
        BigDecimal total = new BigDecimal(0);
        for (ShoppingListItem item : shoppingList) {
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
                    shoppingList.get(pos).setIsBought(b);
                    if (!onBind) {
                        notifyItemChanged(pos);
                        saveData();
                    }
                }
            });

            name.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    shoppingList.get(getAdapterPosition()).setName(charSequence.toString());
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

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

            cost.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    Log.d(TAG, "Cost TextEdit has been changed");
                    String input = charSequence.toString();

                    // Fix up formatting of text input so that the BigDecimal parser doesn't fail
                    if (input.isEmpty()) {
                        input = "0";
                    } else if (input.charAt(0) == '.') {
                        // We'll allow inputs like ".5" to be short of "0.5"
                        input = "0" + input;
                    } else if (input.charAt(0) == Currency.getInstance(locale).getSymbol().charAt(0)) {
                        input = input.substring(1, input.length());
                    }

                    try {
                        Log.d(TAG, "Trying to parse input into a BigDecimal");
                        shoppingList.get(getAdapterPosition()).setCost(new BigDecimal(input));
                    } catch (Exception e){
                        // When we fail to parse the string to a BigDecimal, notify the user.
                        // Principle of least surprise
                        e.printStackTrace();
                        Log.d(TAG, "Cost formatting failed on input " + charSequence, e);
                        Snackbar.make(v, "I can't understand what you've just written", Snackbar.LENGTH_LONG);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            cost.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (isUserFinishedTyping(actionId, keyEvent)) {
                        if (!textView.getText().toString().isEmpty()) {
                            Log.d(TAG, "Cost is currently typeset as : " + textView.getText());
                            String fmtText = currencyFmt.format(Double.parseDouble(textView.getText().toString()));
                            Log.d(TAG, "User has finished typing, pretty printing cost: " + fmtText);
                            textView.setText(fmtText);
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
        ShoppingListItem item = shoppingList.get(pos);
        viewHolder.isBought.setChecked(item.isBought());
        viewHolder.name.setText(item.getName());
        viewHolder.cost.setText(currencyFmt.format(item.getCost()));
        onBind = false;
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    public void addItem(ShoppingListItem item) {
        shoppingList.add(item);
        notifyItemInserted(shoppingList.size() - 1);
        saveData();
    }

    public ArrayList<ShoppingListItem> getShoppingList() {
        return shoppingList;
    }

    public void saveData() {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            outputStream.writeObject(shoppingList);
            Log.d(TAG, "Data has been saved!");
        } catch (IOException e) {
            Log.e(TAG, "Could not save data", e);
        }
    }
    
    public void removeItem(int pos) {
        shoppingList.remove(pos);
        notifyItemRemoved(pos);
        saveData();
    }
}
