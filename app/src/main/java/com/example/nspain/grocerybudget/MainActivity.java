package com.example.nspain.grocerybudget;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ShoppingListItemAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private BottomSheetBehavior bottomSheetBehavior;
    private String dataFileName = "shoppingListItems";

    /**
     * Tag for debugging purposes
     */
    public final static String TAG = "GB/MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Setup the recycler view, this is where the shopping list items will show
        recyclerView = findViewById(R.id.shoppingList);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        File dataFile = new File(getApplicationContext().getFilesDir(), dataFileName);

        ArrayList<ShoppingListItem> data;
        if (savedInstanceState != null) {
            Log.d(TAG, "Saved instance state is not null, getting the data");
            data = (ArrayList<ShoppingListItem>) savedInstanceState.get("shoppingList");
            adapter = new ShoppingListItemAdapter(data, dataFile, getCurrentLocale());
        } else {
            Log.d(TAG, "No saved instance state, reading from stored file");
            // Try to get shopping list data from file, it this fails then we just initialise the adapter
            // with not data
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(dataFileName))) {
                data = (ArrayList<ShoppingListItem>) objectInputStream.readObject();
                adapter = new ShoppingListItemAdapter(
                        data,
                        dataFile,
                        getCurrentLocale());
            } catch (IOException e) {
                Log.d(TAG, "No data file found, not using any data to initialise app");
                adapter = new ShoppingListItemAdapter(dataFile, getCurrentLocale());
            } catch (ClassNotFoundException e) {
                // There's a bigger problem going on so just fail
                e.printStackTrace();
                return;
            }
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

        View bottomsheet = findViewById(R.id.bottomsheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        final EditText totalCost = findViewById(R.id.totalCost);
        totalCost.setFocusable(false);
        totalCost.setText(String.format("%s%s",
                Currency.getInstance(getCurrentLocale()).getSymbol(),
                adapter.getTotalCost().toString()));

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeChanged(int posStart, int itemCount) {
                super.onItemRangeChanged(posStart, itemCount);
                Log.d(TAG, "Total cost is now " + adapter.getTotalCost().toString());
                totalCost.setText(String.format("%s%s",
                        Currency.getInstance(getCurrentLocale()).getSymbol(),
                        adapter.getTotalCost().toString()));
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.d(TAG, "Saving instance state, " + adapter.getShoppingList().toString());
        savedInstanceState.putSerializable("shoppingList", adapter.getShoppingList());
    }

    /**
     * Get the current locale for the phone.
     * The use of the /locale/ field is deprecated, however, as phones with and SDK version less than
     * 24, we still have to use it.
     *
     * @return Current locale
     */
    private Locale getCurrentLocale() {
        return getResources().getConfiguration().locale;
    }

}
