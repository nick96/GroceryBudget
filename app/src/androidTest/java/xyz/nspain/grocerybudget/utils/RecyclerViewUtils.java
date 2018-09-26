package xyz.nspain.grocerybudget.utils;

import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Utilities for instrumentation tests.
 */
public class RecyclerViewUtils {

    /**
     * Get an Espresso matcher for a given RecyclerView to use for testing.
     *
     * @param id Resource ID to use
     * @return Espresso matcher for the given RecyclerView
     */
    public static RecyclerViewMatcher withRecyclerView(final int id) {
        return new RecyclerViewMatcher(id);
    }

    /**
     * Espresso assertion about the the number of items in a RecyclerView.
     */
    public static class RecyclerViewItemCountAssertion implements ViewAssertion {
        private int assertedCount;

        public RecyclerViewItemCountAssertion(int count) {
            assertedCount = count;
        }

        @Override
        public void check(View view, NoMatchingViewException noViewFoundException) {
            if (noViewFoundException != null) {
                throw noViewFoundException;
            }
            RecyclerView recyclerView = (RecyclerView) view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            if (adapter != null) {
                assertThat(adapter.getItemCount(), is(assertedCount));
            } else {
                fail("Recycler view does not have an adapter");
            }
        }
    }

    /**
     * Espresso matcher for a RecyclerView
     * From https://stackoverflow.com/questions/28476507/using-espresso-to-click-view-inside-recyclerview-item
     */
    public static class RecyclerViewMatcher {
        private final int recyclerViewId;

        public RecyclerViewMatcher(@IdRes int recyclerViewId) {
            this.recyclerViewId = recyclerViewId;
        }

        /**
         * Match an item in the RecyclerView in a given position.
         *
         * @param position Position of item in RecyclerView
         * @return Espresso matcher for item
         */
        public Matcher<View> atPosition(final int position) {
            return atPositionOnView(position, -1);
        }

        /**
         * Get an Espresso matcher for a specific view of a RecyclerView item.
         *
         * @param position     Index of item in RecyclerView
         * @param targetViewId Resource ID of view within item of RecyclerView
         * @return Espresso matcher for item with the given resource ID
         */
        public Matcher<View> atPositionOnView(final int position, @IdRes final int targetViewId) {
            return new TypeSafeMatcher<View>() {
                Resources resources = null;
                View childView;

                @Override
                protected boolean matchesSafely(View item) {
                    if (childView == null) {
                        RecyclerView recyclerView = item.getRootView().findViewById(recyclerViewId);
                        if (recyclerView != null) {
                            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                            if (viewHolder != null) {
                                childView = viewHolder.itemView;
                            } else {
                                return false;
                            }
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

    /**
     * Get the number of items in a recycler view
     *
     * @param RecyclerViewId Resource ID of RecyclerView to count items in
     * @return Item count in RecyclerView
     */
    public static int getCountFromRecyclerView(@IdRes int RecyclerViewId) {
        final int[] COUNT = {0};
        Matcher<View> matcher = new TypeSafeMatcher<View>() {
            @Override
            protected boolean matchesSafely(View item) {
                RecyclerView.Adapter adapter = ((RecyclerView) item).getAdapter();
                if (adapter != null) {
                    COUNT[0] = adapter.getItemCount();
                    return true;
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
            }
        };

        onView(allOf(withId(RecyclerViewId), isDisplayed())).check(matches(matcher));
        return COUNT[0];
    }
}
