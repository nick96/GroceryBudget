package xyz.nspain.grocerybudget.persistance;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.math.BigDecimal;
import java.util.List;

@android.arch.persistence.room.Database(entities = {
        Item.class,
        ShoppingList.class
}, version = 9)
@TypeConverters({Converters.class})
public abstract class ShoppingListDatabase extends RoomDatabase {
    private static final String TAG = ShoppingListDatabase.class.getCanonicalName();

    private static volatile ShoppingListDatabase INSTANCE;

    public abstract ItemDao itemDao();

    public abstract ShoppingListDao shoppingListDao();

    public static ShoppingListDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    ShoppingListDatabase.class, "shopping_list_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(sPopulateCallback)
                    .build();
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sPopulateCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsync(INSTANCE).execute();
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            new MaybePopulateDBAsync(INSTANCE).execute();
        }
    };

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {
        private final ShoppingListDao mShoppingListDao;
        private final ItemDao mItemDao;

        public PopulateDbAsync(ShoppingListDatabase db) {
            mShoppingListDao = db.shoppingListDao();
            mItemDao = db.itemDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ShoppingList list = new ShoppingList(0,"Shopping List", true);
            List<Long> ids = mShoppingListDao.insert(list);
            Long listId = ids.get(0);
            Log.d(TAG, "Inserting items in list with id " + listId);
            mItemDao.insert(
                    new Item(0, "Bread", new BigDecimal("3.00"), false, listId),
                    new Item(0, "Milk", new BigDecimal("1.00"), false, listId),
                    new Item(0, "Cheese", new BigDecimal("5.00"), false, listId));
            return null;
        }
    }

    private static class MaybePopulateDBAsync extends AsyncTask<Void, Void, Void> {
        private final ShoppingListDatabase mDb;
        private final ShoppingListDao mShoppingListDao;
        private final ItemDao mItemDao;

        public MaybePopulateDBAsync(ShoppingListDatabase db) {
            mDb = db;
            mShoppingListDao = db.shoppingListDao();
            mItemDao = db.itemDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mShoppingListDao.isEmpty() && mItemDao.isEmpty()) {
                Log.d(TAG, "Db is empty, inserting dummy data");
                new PopulateDbAsync(mDb).execute();
            } else {
                Log.d(TAG, "Db is not empty, lists: " + mShoppingListDao.getLists().toString() +
                            "items: " + mItemDao.getItems().toString());
            }
            return null;
        }
    }
}
