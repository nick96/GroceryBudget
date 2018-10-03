package xyz.nspain.grocerybudget;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;

import xyz.nspain.grocerybudget.persistance.Item;
import xyz.nspain.grocerybudget.persistance.ShoppingList;

import static xyz.nspain.grocerybudget.AdapterNotifyMessage.*;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ShoppingListAdapter mShoppingListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView mTotalCostView;
    private ShoppingListViewModel mShoppingListViewModel;
    private AppCompatImageButton mAddButton;
    private AppCompatImageButton mClearAllButton;
    private DrawerLayout mNavigationDrawer;
    private NavigationView mNavigationView;
    private NumberFormat mCurrencyFormatter;
    private ShoppingListEditAdapter mShoppingListEditAdapter;
    private ItemTouchHelper mItemTouchHelper;

    /**
     * Tag for debugging purposes
     */
    public final static String TAG = "GB/MainActivity";
    public final static String LIST_STATE_KEY = "LIST_STATE";

    private ShoppingList shoppingList;
    private Toolbar mToolbar;
    private boolean mIsUpdatedFromView = false;

    private View.OnClickListener mCreateItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Item item = new Item("", new BigDecimal(0), false);
            mShoppingListViewModel.insertItem(item);
        }
    };

    private View.OnClickListener mCreateListOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ShoppingList list = new ShoppingList("");
            mShoppingListViewModel.insertList(list);
        }
    };

    private View.OnClickListener mClearListsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mShoppingListViewModel.deleteAllLists();
        }
    };

    private View.OnClickListener mClearItemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mShoppingListViewModel.deleteItemsInCurrentList();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCurrencyFormatter = NumberFormat.getCurrencyInstance(getCurrentLocale());

        mShoppingListAdapter = new ShoppingListAdapter(this, mCurrencyFormatter);
        mShoppingListEditAdapter = new ShoppingListEditAdapter(this);

        mTotalCostView = findViewById(R.id.total_cost);
        mShoppingListViewModel = ViewModelProviders.of(this).get(ShoppingListViewModel.class);
        mRecyclerView = findViewById(R.id.shoppingList);
        mLayoutManager = new LinearLayoutManager(this);
        mAddButton = findViewById(R.id.add_button);
        mClearAllButton = findViewById(R.id.clear_all_button);
        mNavigationDrawer = findViewById(R.id.main_activity);
        mNavigationView = findViewById(R.id.nav_view);
        mToolbar = findViewById(R.id.toolbar);

        mAddButton.setOnClickListener(mCreateItemOnClickListener);
        mClearAllButton.setOnClickListener(mClearItemsClickListener);

        ItemTouchHelper.Callback itemTouchHelperCallback = new SimpleItemTouchHelperCallback(mShoppingListEditAdapter);
        mItemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);


        setupToolbar();
        setupRecyclerView();
        setupViewModelObservers();
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

                if (menuItem.getItemId() == R.id.new_list) {
                    createNewList();
                } else if (menuItem.getItemId() == R.id.edit_lists) {
                    mAddButton.setOnClickListener(mCreateListOnClickListener);
                    mClearAllButton.setOnClickListener(mClearListsClickListener);
                    mRecyclerView.setAdapter(mShoppingListEditAdapter);
                    mToolbar.setTitle(R.string.list_edit_title);
                    mTotalCostView.setText("");
                } else {
                    // Must've clicked a list name
                    mShoppingListViewModel.updateCurrentList(menuItem.getTitle().toString());
                    mAddButton.setOnClickListener(mCreateItemOnClickListener);
                    mClearAllButton.setOnClickListener(mClearItemsClickListener);
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
                if (msg != null && msg.getChangeType() == ChangeType.UPDATE_ITEM) {
                    mIsUpdatedFromView = true;
                    mShoppingListViewModel.updateItem((Item)msg.getPayload());
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                RecyclerViewAdapterWithRemovePayload adapter = (RecyclerViewAdapterWithRemovePayload) mRecyclerView.getAdapter();
                if (adapter != null) {
                    AdapterNotifyMessage msg = (AdapterNotifyMessage) adapter.getRemovedItemPayload();
                    if (msg != null && msg.getChangeType() == ChangeType.DELETE_ITEM) {
                        mIsUpdatedFromView = true;
                        mShoppingListViewModel.deleteItem((Item)msg.getPayload());
                    }
                }
            }
        });

        mShoppingListEditAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                AdapterNotifyMessage msg = (AdapterNotifyMessage) payload;
                if (msg != null) {
                    switch (msg.getChangeType()) {
                        case UPDATE_LIST:
                            mIsUpdatedFromView = true;
                            mShoppingListViewModel.updateList((ShoppingList) msg.getPayload());
                            break;
                        case VIEW_LIST:
                            mShoppingListViewModel.updateCurrentList(((ShoppingList) msg.getPayload()).getName());
                            mAddButton.setOnClickListener(mCreateItemOnClickListener);
                            mRecyclerView.setAdapter(mShoppingListAdapter);
                            break;
                    }

                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                RecyclerViewAdapterWithRemovePayload adapter = (RecyclerViewAdapterWithRemovePayload) mRecyclerView.getAdapter();
                if (adapter != null) {
                    AdapterNotifyMessage msg = (AdapterNotifyMessage) adapter.getRemovedItemPayload();
                    if (msg != null && msg.getChangeType() == ChangeType.DELETE_LIST) {
                        mIsUpdatedFromView = true;
                        mShoppingListViewModel.deleteList((ShoppingList) msg.getPayload());
                    }
                }
            }
        });
    }

    private void setupViewModelObservers() {
        mShoppingListViewModel.getItems().observe(this, new Observer<List<Item>>() {
            @Override
            public void onChanged(@Nullable List<Item> items) {
                if (!mIsUpdatedFromView) {
                    mShoppingListAdapter.setItems(items);
                }
                mIsUpdatedFromView = false;
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
                if (!mIsUpdatedFromView && shoppingLists != null) {
                    mShoppingListEditAdapter.setLists(shoppingLists);
                    mLayoutManager.scrollToPosition(shoppingLists.size() - 1);

                }
                mIsUpdatedFromView = false;
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
