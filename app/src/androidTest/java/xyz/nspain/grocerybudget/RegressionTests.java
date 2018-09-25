package xyz.nspain.grocerybudget;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import xyz.nspain.grocerybudget.persistance.ItemDao;
import xyz.nspain.grocerybudget.persistance.ShoppingListDao;
import xyz.nspain.grocerybudget.persistance.ShoppingListDatabase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static xyz.nspain.grocerybudget.Utils.withRecyclerView;
import static android.support.test.espresso.action.ViewActions.typeText;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class RegressionTests {
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    private ShoppingListDatabase mDb;
    private ItemDao mItemDao;
    private ShoppingListDao mShoppingListDao;

    @Before
    public void createDB() {
        Context ctx = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(ctx, ShoppingListDatabase.class).build();
    }

    @After
    public void closeDB() {
        mDb.close();
    }

    @Test
    public void cursorStaysInPosAfterTextInsert() {
        onView(withRecyclerView(R.id.shoppingList)
        .atPositionOnView(0, R.id.item_name))
                .perform(clearText())
                .perform(typeText("test"))
                .check(matches(withText("test")));
    }
}
