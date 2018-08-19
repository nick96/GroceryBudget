package xyz.nspain.grocerybudget;

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
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
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
                                new ShoppingListItem(false, "", new BigDecimal(0)))),
                                locale),
                dataFileName, locale);
    }

    public ShoppingListItemAdapter(ShoppingList list, String dataFileName, Locale locale) {
        shoppingList = list;
        this.dataFileName = dataFileName;
        currencyFmt = NumberFormat.getCurrencyInstance(locale);
        bindNewItem = false;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
        notifyDataSetChanged();
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

            name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    ShoppingListItem item = shoppingList.getItem(getAdapterPosition());
                    item.setName(charSequence.toString());
                    saveData();
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            cost.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (!hasFocus) {
                        final int pos = getAdapterPosition();
                        Log.d(TAG, "cost EditText has lost focus, will typeset contents");
                        TextView textView = (TextView) view;
                        if (!textView.getText().toString().isEmpty() && shoppingList.getItem(pos) != null) {
                            String costText = currencyFmt.format(shoppingList.getItem(pos).getCost());
                            textView.setText(costText);
                        }
                    }
                }
            });

            cost.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    shoppingList.updateCost(getAdapterPosition(), charSequence);
                    saveData();
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            cost.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    Log.d(TAG,"EditorAction: " + actionId + "; " + keyEvent);
                    if (isUserFinishedTyping(actionId, keyEvent)) {
                        notifyItemChanged(getAdapterPosition());
                    }
                    return true;
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
//        try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(dataFileName))) {
//            outputStream.writeObject(shoppingList);
//            Log.d(TAG, "Data has been saved!");
//        } catch (IOException e) {
//            Log.e(TAG, "Could not save data", e);
//        }
    }

    public void removeItem(int pos) {
        Log.d(TAG, "Removing item at " + pos);
        shoppingList.remove(pos);
        notifyDataSetChanged();
        saveData();
    }
}
