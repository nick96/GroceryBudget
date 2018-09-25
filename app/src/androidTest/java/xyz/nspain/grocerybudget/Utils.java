package xyz.nspain.grocerybudget;

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
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Utils {

    public static RecyclerViewMatcher withRecyclerView(final int id) {
        return new RecyclerViewMatcher(id);
    }

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
            RecyclerView recyclerView = (RecyclerView)view;
            RecyclerView.Adapter adapter = recyclerView.getAdapter();
            assertThat(adapter.getItemCount(), is(assertedCount));
        }
    }
    /**
     * From https://stackoverflow.com/questions/28476507/using-espresso-to-click-view-inside-recyclerview-item
     */
    public static class RecyclerViewMatcher {
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
