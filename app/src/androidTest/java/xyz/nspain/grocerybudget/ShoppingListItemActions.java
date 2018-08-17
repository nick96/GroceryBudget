package xyz.nspain.grocerybudget;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ShoppingListItemActions {
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    private String itemName;
    private String itemCost;
    private String dataFileName;

    @Before
    public void initInputs() {
        itemName = "bread";
        itemCost = "1.50";
        dataFileName = InstrumentationRegistry.getTargetContext()
                .getApplicationContext().getFilesDir() + "shoppingListItems";
        File dataFile = new File(dataFileName);
        assertThat(dataFile.delete(), is(true));
    }

    @After
    public void tearDown() {
        File dataFile = new File(dataFileName);
        if (dataFile.exists()) {
            assertThat(dataFile.delete(), is(true));
        }
    }

    @Test
    public void oneItemToBegin() {
        onView(withId(R.id.shoppingList)).check(new RecyclerViewItemCountAssertion(1));
    }

    @Test
    public void createItemTest() {
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.shoppingList)).check(new RecyclerViewItemCountAssertion(2));
    }

    @Test
    public void tickItemUpdatesTotalCost() {
        // Insert data into the name and cost fields
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.name))
                .perform(typeText(itemName));
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.cost))
                .perform(clearText())
                .perform(typeText(itemCost));

        // Check that the total cost has not been updated as the item is not ticked
        onView(withId(R.id.totalCost)).check(matches(withText("$0.00")));

        // Tick the item
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.isBought))
                .perform(click());

        // Check that the total cost has been updated
        onView(withId(R.id.totalCost)).check(matches(withText("$" + itemCost)));
    }

    @Test
    public void dataSavedWhenItemLosesFocus() {
        assertThat((new File(dataFileName)).exists(), is(false));
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.name))
                .perform(typeText(itemName));
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.cost))
                .perform(click());
        assertThat((new File(dataFileName)).exists(), is(true));

        ShoppingList shoppingList = null;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(dataFileName))) {
            shoppingList = (ShoppingList) objectInputStream.readObject();
        } catch (Exception e) {
            fail();
        }

        ShoppingListItem item = shoppingList.getItem(0);
        assertThat(item.getName(), equalTo(itemName));
    }

    @Test
    public void dataSavedWhenItemTicked() {
        assertThat((new File(dataFileName)).exists(), is(false));
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.name))
                .perform(typeText(itemName));
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.isBought))
                .perform(click());
        assertThat((new File(dataFileName)).exists(), is(true));

        ShoppingList shoppingList = null;
        try (ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(dataFileName))) {
            shoppingList = (ShoppingList) objectInputStream.readObject();
        } catch (Exception e) {
            fail();
        }

        ShoppingListItem item = shoppingList.getItem(0);
        assertThat(item.getName(), equalTo(itemName));
    }

    /**
     * This is a regression test for a case where when an item's cost was changed then ticked
     * straight away, the cost text reverted back to its previous value.
     */
    @Test
    public void costUnchangedWhenItemTicked() {
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.cost))
                .perform(clearText())
                .perform(typeText(itemCost));
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.name))
                .perform(typeText(itemName));
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.cost))
                .perform(clearText())
                .perform(typeText("100"));
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.isBought))
                .perform(click());

        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.cost))
                .check(matches(withText("$100.00")));
    }

    /**
     * This is a regression test for a bug where the cost would revert to its previous value on
     * focus loss.
     */
    @Test
    public void costUnchangedOnFocusLoss() {
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.cost))
                .perform(clearText())
                .perform(typeText("1"));
        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.name))
                .perform(click());

        onView(withRecyclerView(R.id.shoppingList)
                .atPositionOnView(0, R.id.cost))
                .check(matches(withText("$1.00")));
    }

    private RecyclerViewMatcher withRecyclerView(final int id) {
        return new RecyclerViewMatcher(id);
    }

    private class RecyclerViewItemCountAssertion implements ViewAssertion {
        private int assertedCount;

        public RecyclerViewItemCountAssertion(int count) {
            assertedCount = count;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }
            RecyclerView recyclerView = (RecyclerView)view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            assertThat(adapter.getItemCount(), is(assertedCount));
        }
    }

    /**
     * From https://stackoverflow.com/questions/28476507/using-espresso-to-click-view-inside-recyclerview-item
     */
    private class RecyclerViewMatcher {
        private final int recyclerViewId;

        public RecyclerViewMatcher(int recyclerViewId) {
            this.recyclerViewId = recyclerViewId;
        }

        public Matcher<View> atPosition(final int position) {
            return atPositionOnView(position, -1);
        }

        public Matcher<View> atPositionOnView(final int position, final int targetViewId) {
            return new TypeSafeMatcher<View>() {
                Resources resources = null;
                View childView;

                @Override
                protected boolean matchesSafely(View item) {
                    if (childView == null) {
                        RecyclerView recyclerView = item.getRootView().findViewById(recyclerViewId);
                        if (recyclerView != null) {
                            childView = recyclerView.findViewHolderForAdapterPosition(position).itemView;
                        } else {
                            return false;
                        }

                    }

                    if (targetViewId == -1) {
                        return item == childView;
                    } else {
                        View targetView = childView.findViewById(targetViewId);
                        return item == targetView;
                    }
                }

                @Override
                public void describeTo(Description description) {
                    int id = targetViewId == -1 ? recyclerViewId : targetViewId;
                    String idDesc = Integer.toString(id);
                    if (this.resources != null) {
                        try {
                            idDesc = this.resources.getResourceName(id);
                        } catch (Resources.NotFoundException e) {
                            idDesc = String.format("%s (resource name not found)", id);
                        }
                    }
                    description.appendText(idDesc);
                }
            };
        }
    }

    public static int getCountFromRecyclerView(@IdRes int RecyclerViewId) {
        final int[] COUNT = {0};
        Matcher matcher = new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                int itemCount = ((RecyclerView) item).getAdapter().getItemCount();
                COUNT[0] = ((RecyclerView) item).getAdapter().getItemCount();
                return true;
            }
            @Override
            public void describeTo(Description description) {
            }
        };
        onView(allOf(withId(RecyclerViewId),isDisplayed())).check(matches(matcher));
        return COUNT[0];
    }
}

