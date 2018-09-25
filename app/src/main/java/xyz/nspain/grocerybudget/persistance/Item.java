package xyz.nspain.grocerybudget.persistance;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.util.Log;

import java.math.BigDecimal;
import java.util.Locale;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "shopping_list_item",
        foreignKeys = @ForeignKey(entity = ShoppingList.class,
                                  parentColumns = {"id"},
                                  childColumns = {"list_id"},
                                  onDelete = CASCADE
        ),
        indices = {@Index("list_id")})
@TypeConverters({Converters.class})
public class Item implements ShoppingListDatabaseEntity {
    private static final String TAG = Item.class.getCanonicalName();
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    private long mId;

    @ColumnInfo(name = "name")
    private String mName;

    @ColumnInfo(name = "cost")
    private BigDecimal mCost;

    @ColumnInfo(name = "is_bought")
    private boolean mIsBought;

    @ColumnInfo(name = "list_id")
    private long mListId;

    @Ignore
    public Item(String name, BigDecimal cost, boolean isBought) {
        this(0, name, cost, isBought,0);
    }

    public Item(long id, String name, BigDecimal cost, boolean isBought, long listId) {
        mId = id;
        mName = name;
        mCost = cost;
        mIsBought = isBought;
        mListId = listId;
    }

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public BigDecimal getCost() {
        return mCost;
    }

    public void setCost(BigDecimal mCost) {
        this.mCost = mCost;
    }

    public long getListId() {
        return mListId;
    }

    public void setListId(long mListId) {
        this.mListId = mListId;
    }

    public boolean isBought() {
        return mIsBought;
    }

    public void setIsBought(boolean mIsBought) {
        this.mIsBought = mIsBought;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s [id=%d; name=%s; cost=%s, is_bought=%b; list_id=%d]",
                super.toString(), getId(), getName(), getCost().toString(), isBought(), getListId());
    }

    @Override
    public boolean equals(Object o) {
        Item other = (Item) o;
        return mId == other.getId()
                && mName.equals(other.getName())
                && mCost.equals(other.getCost())
                && mIsBought == other.isBought()
                && mListId == other.getListId();
    }
}
