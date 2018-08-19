package xyz.nspain.grocerybudget;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
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
import android.text.Layout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ShoppingListItemAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private BottomSheetBehavior bottomSheetBehavior;
    private EditText totalCost;
    private DrawerLayout navigationDrawer;

    /**
     * Tag for debugging purposes
     */
    public final static String TAG = "GB/MainActivity";
    private HashMap<String, ShoppingList> shoppingListMap;
    private String dataFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataFileName = getApplicationContext().getFilesDir() + "shoppingListItems";

        setContentView(R.layout.activity_main);

        navigationDrawer = findViewById(R.id.main_activity);
        NavigationView navView = findViewById(R.id.nav_view);
        Log.d(TAG, "navView: " + navView);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                menuItem.setChecked(true);
                navigationDrawer.closeDrawers();

                if (menuItem.getItemId() == R.id.new_list) {
                    newShoppingList();
                }

                // Change to a new list

                return true;
            }
        });

        Menu menu = navView.getMenu();
        SubMenu subMenu = menu.addSubMenu("Your Lists");
        subMenu.add("Coles");
        navView.invalidate();


        Toolbar toolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_dehaze_black_24dp);

        // Setup the recycler view, this is where the shopping list items will show
        recyclerView = findViewById(R.id.shoppingList);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        HashMap<String, ShoppingList> data = loadData(savedInstanceState, dataFileName);
        if (data != null) {
            ArrayList<String> listNames = new ArrayList<>(data.keySet());
            ShoppingList defaultList = data.get(listNames.get(0));
            adapter = new ShoppingListItemAdapter(defaultList, dataFileName, getCurrentLocale());
        } else {
            shoppingListMap = new HashMap<>();
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

        totalCost = findViewById(R.id.totalCost);
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

    private void newShoppingList() {
        final View dialogView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.new_list_dialog_view, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.AppCompatAlertDialogStyle);
        builder.setTitle(R.string.new_list_dialog_title);
        builder.setView(dialogView);
        builder.setPositiveButton(R.string.new_list_dialog_pos, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                EditText listTitleText = dialogView.findViewById(R.id.new_list_edit_text);
                String title = listTitleText.getText().toString();
                shoppingListMap.put(title, new ShoppingList(
                        new ArrayList<>(Collections.singletonList(
                                new ShoppingListItem(false, null, null)))));
                adapter.setShoppingList(shoppingListMap.get(title));
            }
        });
        builder.setNegativeButton(R.string.new_list_dialog_neg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.show();
    }

    private void setAdapterToNewList(String title) {
        ShoppingList list = shoppingListMap.get(title);
        adapter.setShoppingList(list);
    }

    private HashMap<String, ShoppingList> loadData(Bundle savedInstanceState, String dataFileName) throws RuntimeException{
        HashMap<String, ShoppingList> data = null;
        if (savedInstanceState != null) {
            Log.d(TAG, "Saved instance state is not null, getting the data");
            data = (HashMap<String, ShoppingList>) savedInstanceState.get("shoppingList");
        } else {
            Log.d(TAG, "No saved instance state, reading from stored file");
            // Try to get shopping list data from file, it this fails then we just initialise the adapter
            // with not data
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(dataFileName))) {
                data = (HashMap<String, ShoppingList>) objectInputStream.readObject();
                Log.d(TAG, "Read shopping list data from file");
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
        Log.d(TAG, "Saving instance state, " + shoppingListMap.toString());
        savedInstanceState.putSerializable("shoppingList", shoppingListMap);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigationDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
