package xyz.nspain.grocerybudget;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;

import xyz.nspain.grocerybudget.persistance.Item;
import xyz.nspain.grocerybudget.persistance.ShoppingList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ShoppingListAdapter mShoppingListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private EditText mTotalCostView;
    private ShoppingListViewModel mShoppingListViewModel;
    private FloatingActionButton mAddItemFAB;
    private DrawerLayout mNavigationDrawer;
    private NavigationView mNavigationView;
    private NumberFormat mCurrencyFormatter;
    private ShoppingListEditAdapter mShoppingListEditAdapter;

    /**
     * Tag for debugging purposes
     */
    public final static String TAG = "GB/MainActivity";
    public final static String LIST_STATE_KEY = "LIST_STATE";

    private ShoppingList shoppingList;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mShoppingListAdapter = new ShoppingListAdapter(this, mCurrencyFormatter);
        mShoppingListEditAdapter = new ShoppingListEditAdapter(this);

        mCurrencyFormatter = NumberFormat.getCurrencyInstance(getCurrentLocale());
        mTotalCostView = findViewById(R.id.totalCost);
        mShoppingListViewModel = ViewModelProviders.of(this).get(ShoppingListViewModel.class);
        mRecyclerView = findViewById(R.id.shoppingList);
        mLayoutManager = new LinearLayoutManager(this);
        mAddItemFAB = findViewById(R.id.fab);
        mNavigationDrawer = findViewById(R.id.main_activity);
        mNavigationView = findViewById(R.id.nav_view);
        mToolbar = findViewById(R.id.toolbar);

        setupToolbar();
        setupRecyclerView();
        setupViewModelObservers();
        setupFloatingActionButton();
        setupNavigationDrawer();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mNavigationDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24px);

        mShoppingListViewModel.getCurrentList().observe(this, new Observer<ShoppingList>() {
            @Override
            public void onChanged(@Nullable ShoppingList list) {
                if (list != null) {
                    mToolbar.setTitle(list.getName());
                }
            }
        });
    }

    private void setupNavigationDrawer() {
        final Menu menu = mNavigationView.getMenu();
        final SubMenu shoppingListMenu = menu.addSubMenu(R.string.shopping_list_menu);

        mShoppingListViewModel.getLists().observe(this, new Observer<List<ShoppingList>>() {
            @Override
            public void onChanged(@Nullable List<ShoppingList> shoppingLists) {
                shoppingListMenu.clear();
                for (ShoppingList shoppingList : shoppingLists) {
                    shoppingListMenu.add(shoppingList.getName());
                }
            }
        });

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                mNavigationDrawer.closeDrawers();

                Log.d(TAG, "Selected " + menuItem);
                if (menuItem.getItemId() == R.id.new_list) {
                    createNewList();
                } else if (menuItem.getItemId() == R.id.edit_lists) {
                    mRecyclerView.setAdapter(mShoppingListEditAdapter);
                } else {
                    // Must've clicked a list name
                    mShoppingListViewModel.updateCurrentList(menuItem.getTitle().toString());
                    mRecyclerView.setAdapter(mShoppingListAdapter);
                }

                return true;
            }
        });
    }

    private void createNewList() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View listTitleView = factory.inflate(R.layout.new_list_dialog_view, null);
        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        alertBuilder.setTitle(R.string.new_list_dialog_title);
        alertBuilder.setView(listTitleView);
        alertBuilder.setPositiveButton(R.string.new_list_dialog_pos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText listTitleText = listTitleView.findViewById(R.id.new_list_edittext);
                String title = listTitleText.getText().toString();
                ShoppingList newList = new ShoppingList(title);
                mShoppingListViewModel.insertList(newList);
                mShoppingListViewModel.updateCurrentList(newList.getName());
            }
        });

        alertBuilder.setNegativeButton(R.string.new_list_dialog_neg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertBuilder.show();
    }

    private void setupRecyclerView() {
        // Setup the recycler view, this is where the shopping list items will show
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mShoppingListAdapter);

        mShoppingListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                AdapterNotifyMessage msg = (AdapterNotifyMessage) payload;
                if (msg != null) {
                    switch (msg.getChangeType()) {
                        case DELETE_ITEM:
                            Log.d(TAG, "Deletion message found, deleting " + msg.getPayload());
                            mShoppingListViewModel.deleteItem((Item)msg.getPayload());
                            break;
                        case INSERT_ITEM:
                            break;
                        case UPDATE_ITEM:
                            Log.d(TAG, "Update message found, updating " + msg.getPayload());
                            mShoppingListViewModel.updateItem((Item)msg.getPayload());
                            break;
                    }
                } else {
                    Log.d(TAG, "Payload is null so not sending update to view model");
                }
                mShoppingListViewModel.setIsCircularUpdate(true);
            }
        });

        mShoppingListEditAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                AdapterNotifyMessage msg = (AdapterNotifyMessage) payload;
                if (msg != null) {
                    switch (msg.getChangeType()) {
                        case DELETE_LIST:
                            mShoppingListViewModel.deleteList((ShoppingList) msg.getPayload());
                            break;
                        case INSERT_LIST:
                            break;
                        case UPDATE_LIST:
                            mShoppingListViewModel.updateList((ShoppingList) msg.getPayload());
                            break;
                    }
                }
            }
        });
    }

    private void setupViewModelObservers() {
        mShoppingListViewModel.getItems().observe(this, new Observer<List<Item>>() {
            @Override
            public void onChanged(@Nullable List<Item> items) {
//                if (!mShoppingListViewModel.isCircularUpdate()) {
                    Log.d(TAG, "Is not circular update");
                    mShoppingListAdapter.setItems(items);
                    mShoppingListViewModel.setIsCircularUpdate(true);
//                } else {
                    mShoppingListViewModel.setIsCircularUpdate(false);
                    Log.d(TAG, "Is circular update");
//                }
            }
        });

        mShoppingListViewModel.getCurrentItems().observe(this, new Observer<List<Item>>() {
            @Override
            public void onChanged(@Nullable List<Item> items) {
                if (items != null) {
                    BigDecimal sum = new BigDecimal(0);
                    for (Item item : items) {
                        if (item.isBought()) {
                            sum = sum.add(item.getCost());
                        }
                    }
                    mTotalCostView.setText(mCurrencyFormatter.format(sum));
                }
            }
        });

        mShoppingListViewModel.getLists().observe(this, new Observer<List<ShoppingList>>() {
            @Override
            public void onChanged(@Nullable List<ShoppingList> shoppingLists) {
                mShoppingListEditAdapter.setLists(shoppingLists);
            }
        });
    }

    private void setupFloatingActionButton() {
        // Setup the floating button to add new items to the shopping list
        mAddItemFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Item item = new Item("", new BigDecimal(0), false);
                Log.d(TAG, "Inserting " + item + " into db");
                mShoppingListViewModel.setIsCircularUpdate(false);
                mShoppingListViewModel.insertItem(item);
            }
        });
    }

    /**
     * Get the current locale for the phone.
     *
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
