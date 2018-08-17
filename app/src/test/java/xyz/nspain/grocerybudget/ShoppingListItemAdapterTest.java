package xyz.nspain.grocerybudget;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.hamcrest.CoreMatchers.equalTo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 22)
public class ShoppingListItemAdapterTest  {

    private ShoppingListItemAdapter shoppingListItemAdapter;
    private ShoppingListItemAdapter shoppingListItemAdapterSpy;

    @Before
    public void setUp() {
        ShoppingList shoppingList = new ShoppingList(new ArrayList<>(Arrays.asList(
                new ShoppingListItem(false, "bread", new BigDecimal(10)),
                new ShoppingListItem(false, "cheese", new BigDecimal("11.23")),
                new ShoppingListItem(false, "milk", new BigDecimal("100"))
        )), null);
        shoppingListItemAdapter = new ShoppingListItemAdapter(shoppingList,"dataFile", Locale.getDefault());
        shoppingListItemAdapterSpy = spy(shoppingListItemAdapter);
    }

    @Test
    public void getItemCount() {
        when(shoppingListItemAdapterSpy.getItemCount()).thenCallRealMethod();
        assertThat(shoppingListItemAdapter.getItemCount(), is(3));
    }

    @Test
    public void addItem() {
        ShoppingListItem item = new ShoppingListItem(false, "ham" , new BigDecimal("11"));
        ShoppingList newList = new ShoppingList(new ArrayList<>(Arrays.asList(
                new ShoppingListItem(false, "bread", new BigDecimal(10)),
                new ShoppingListItem(false, "cheese", new BigDecimal("11.23")),
                new ShoppingListItem(false, "milk", new BigDecimal("100")),
                item
        )), null);
        shoppingListItemAdapter.addItem(item);
        assertThat(shoppingListItemAdapter.getShoppingList(), equalTo(newList));
    }

    @Test
    public void getShoppingList() {
        ArrayList<ShoppingListItem> items = new ArrayList<>(Arrays.asList(
                new ShoppingListItem(false, "bread", new BigDecimal(10)),
                new ShoppingListItem(false, "cheese", new BigDecimal("11.23")),
                new ShoppingListItem(false, "milk", new BigDecimal("100"))));
        ShoppingList shoppingList = new ShoppingList(items, null);
        assertThat(shoppingListItemAdapter.getShoppingList(), equalTo(shoppingList));

    }

    @Test
    public void removeExistingIndex() {
        ShoppingListItem item = new ShoppingListItem(false, "cheese", new BigDecimal("11.23"));
        shoppingListItemAdapter.removeItem(1);
        assertThat(shoppingListItemAdapter.getItemCount(), is(2));
        assertThat(shoppingListItemAdapter.getShoppingList().getItem(1), not(item));
    }

    @Test
    public void removeNonExistingIndex() {
        shoppingListItemAdapter.removeItem(3);
        assertThat(shoppingListItemAdapter.getItemCount(), is(3));
        }
}