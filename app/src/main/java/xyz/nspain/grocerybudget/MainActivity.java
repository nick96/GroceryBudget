package xyz.nspain.grocerybudget;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import xyz.nspain.grocerybudget.persistance.Item;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ShoppingListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private BottomSheetBehavior bottomSheetBehavior;
    private EditText mTotalCostView;
    private ShoppingListViewModel mShoppingListViewModel;
    private DrawerLayout navigationDrawer;
    private Parcelable mListState;
    private NumberFormat mCurrencyFormatter;

    /**
     * Tag for debugging purposes
     */
    public final static String TAG = "GB/MainActivity";
    public final static String LIST_STATE_KEY = "LIST_STATE";

    private ShoppingList shoppingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mCurrencyFormatter = NumberFormat.getCurrencyInstance(getCurrentLocale());

        // Setup the recycler view, this is where the shopping list items will show
        mRecyclerView = findViewById(R.id.shoppingList);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new ShoppingListAdapter(this, mCurrencyFormatter);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mShoppingListViewModel = ViewModelProviders.of(this).get(ShoppingListViewModel.class);

        mShoppingListViewModel.getItems().observe(this, new Observer<List<Item>>() {
            @Override
            public void onChanged(@Nullable List<Item> items) {
                if (!mShoppingListViewModel.isCircularUpdate()) {
                    Log.d(TAG, "Is not circular update");
                    mAdapter.setItems(items);
                    mShoppingListViewModel.setIsCircularUpdate(true);
                } else {
                    mShoppingListViewModel.setIsCircularUpdate(false);
                    Log.d(TAG, "Is circular update");
                }
            }
        });

        mTotalCostView = findViewById(R.id.totalCost);
        mShoppingListViewModel.getCurrentListTotal().observe(this, new Observer<BigDecimal>() {
            @Override
            public void onChanged(@Nullable BigDecimal totalCost) {
                Log.d(TAG, "Updating total cost");
                mTotalCostView.setText(mCurrencyFormatter.format(totalCost));
            }
        });

        // Setup the floating button to add new items to the shopping list
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Item item = new Item("", new BigDecimal(0), false);
                Log.d(TAG, "Inserting " + item + " into db");
                mShoppingListViewModel.setIsCircularUpdate(false);
                mShoppingListViewModel.insertItem(item);
            }
        });

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                AdapterNotifyMessage msg = (AdapterNotifyMessage) payload;
                if (msg != null) {
                    switch (msg.getChangeType()) {
                        case DELETE:
                            Log.d(TAG, "Deletion message found, deleting " + msg.getItem());
                            mShoppingListViewModel.deleteItem(msg.getItem());
                            break;
                        case INSERT:
                            break;
                        case UPDATE:
                            Log.d(TAG, "Update message found, updating " + msg.getItem());
                            mShoppingListViewModel.updateItem(msg.getItem());
                            break;
                    }
                } else {
                    Log.d(TAG, "Payload is null so not sending update to view model");
                }
                mShoppingListViewModel.setIsCircularUpdate(true);
            }
        });
    }

    /**
     * Get the current locale for the phone.
     * @return Current locale
     */
    private java.util.Locale getCurrentLocale() {
        Context context = getApplicationContext();
        java.util.Locale locale = java.util.Locale.getDefault();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // HACK: Australians will probably use the GB language settings but this will show the £
        // symbol which is wrong (we use $). Here we check if their settings are for GB but their
        // sim is from Australia and set the locale accordingly.
        if (telephonyManager != null &&
                locale.getCountry().equals("GB") && telephonyManager.getSimCountryIso().equals("au")) {
            Log.d(TAG, "User in Australia, using Australian locale");
            locale = new java.util.Locale(locale.getLanguage(), "AU");
        }
        return locale;
    }
}
