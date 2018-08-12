package com.example.nspain.grocerybudget;

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

import java.math.BigDecimal;
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
        setContentView(R.layout.activity_main);

        // Setup the recycler view, this is where the shopping list items will show
        recyclerView = findViewById(R.id.shoppingList);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ShoppingListItemAdapter();

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
                Currency.getInstance(Locale.getDefault()).getSymbol(),
                adapter.getTotalCost().toString()));

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {

            @Override
            public void onItemRangeChanged(int posStart, int itemCount) {
                super.onItemRangeChanged(posStart, itemCount);
                Log.d(TAG, "Total cost is now " + adapter.getTotalCost().toString());
                totalCost.setText(String.format("%s%s",
                        Currency.getInstance(Locale.getDefault()).getSymbol(),
                        adapter.getTotalCost().toString()));
            }
        });

    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        //getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
