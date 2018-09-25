package xyz.nspain.grocerybudget.persistance;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Locale;

@Entity(tableName = "shopping_list")
public class ShoppingList {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    private long mId;

    @ColumnInfo(name = "name")
    private String mName;

    @ColumnInfo(name = "is_current")
    private boolean mIsCurrent;

    @Ignore
    public ShoppingList(String name) {
        this(0, name, false);
    }

    @Ignore
    public ShoppingList(long id, String name) {
        this(id, name, false);
    }

    @Ignore
    public ShoppingList(String name, boolean isCurrent) {
        this(0, name, isCurrent);
    }

    public ShoppingList(long id, String name, boolean isCurrent) {
        mId = id;
        mName = name;
        mIsCurrent = isCurrent;
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

    public boolean isIsCurrent() {
        return mIsCurrent;
    }

    public void setIsCurrent(boolean mIsCurrent) {
        this.mIsCurrent = mIsCurrent;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%s: [id=%d; name=%s; is_current=%b",
                super.toString(), getId(), getName(), isIsCurrent());
    }
}
