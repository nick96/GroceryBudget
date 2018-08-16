package com.example.nspain.grocerybudget;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ShoppingListItemAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private BottomSheetBehavior bottomSheetBehavior;

    /**
     * Tag for debugging purposes
     */
    public final static String TAG = "GB/MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String dataFileName = getApplicationContext().getFilesDir() + "shoppingListItems";

        setContentView(R.layout.activity_main);


        // Setup the recycler view, this is where the shopping list items will show
        recyclerView = findViewById(R.id.shoppingList);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        ShoppingList data = loadData(savedInstanceState, dataFileName);
        if (data != null) {
            adapter = new ShoppingListItemAdapter(data, dataFileName, getCurrentLocale());
        } else {
            adapter = new ShoppingListItemAdapter(dataFileName, getCurrentLocale());
        }

        recyclerView.setAdapter(adapter);

        // Setup the floating button to add new items to the shopping list
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.addItem(new ShoppingListItem(false, "", new BigDecimal(0)));
            }
        });

        View bottomSheet = findViewById(R.id.bottomsheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        final EditText totalCost = findViewById(R.id.totalCost);
        totalCost.setFocusable(false);
        ShoppingList shoppingList = adapter.getShoppingList();
        totalCost.setText(getFmtTotalCost());
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                super.onChanged();
                totalCost.setText(getFmtTotalCost());
            }

            @Override
            public void onItemRangeChanged(int posStart, int itemCount) {
                super.onItemRangeChanged(posStart, itemCount);
                totalCost.setText(getFmtTotalCost());
            }

            @Override
            public void onItemRangeInserted(int posStart, int itemCount) {
                super.onItemRangeInserted(posStart, itemCount);
                totalCost.setText(getFmtTotalCost());
            }

            @Override
            public void onItemRangeRemoved(int posStart, int itemCount) {
                super.onItemRangeChanged(posStart, itemCount);
                totalCost.setText(getFmtTotalCost());
            }
        });

    }

    private ShoppingList loadData(Bundle savedInstanceState, String dataFileName) throws RuntimeException{
        ShoppingList data = null;
        if (savedInstanceState != null) {
            Log.d(TAG, "Saved instance state is not null, getting the data");
            data = (ShoppingList) savedInstanceState.get("shoppingList");
        } else {
            Log.d(TAG, "No saved instance state, reading from stored file");
            // Try to get shopping list data from file, it this fails then we just initialise the adapter
            // with not data
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(dataFileName))) {
                data = (ShoppingList) objectInputStream.readObject();
            } catch (IOException e) {
                Log.d(TAG, "No data file found, not using any data to initialise app");
            } catch (ClassNotFoundException e) {
                Log.d(TAG, "Something has gone horribly wrong", e);
                throw new RuntimeException("Too big of a problem to continue");
            }
        }
        return data;
    }

    private String getFmtTotalCost() {
        BigDecimal cost = adapter.getShoppingList().getShoppingCost();
        Log.d(TAG, "Total cost is now " + cost);
        return NumberFormat.getCurrencyInstance(getCurrentLocale()).format(cost);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "Saving instance state, " + adapter.getShoppingList().toString());
        savedInstanceState.putSerializable("shoppingList", adapter.getShoppingList());
    }

    /**
     * Get the current locale for the phone.
     * @return Current locale
     */
    private Locale getCurrentLocale() {
        Context context = getApplicationContext();
        Locale locale = Locale.getDefault();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // HACK: Australians will probably use the GB language settings but this will show the £
        // symbol which is wrong (we use $). Here we check if their settings are for GB but their
        // sim is from Australia and set the locale accordingly.
        if (telephonyManager != null &&
                locale.getCountry().equals("GB") && telephonyManager.getSimCountryIso().equals("au")) {
            Log.d(TAG, "User in Australia, using Australian locale");
            locale = new Locale(locale.getLanguage(), "AU");
        }
        return locale;
    }
}
