package xyz.nspain.grocerybudget.persistance;

import android.arch.persistence.room.TypeConverter;

import java.math.BigDecimal;

public class Converters {
    @TypeConverter
    public static BigDecimal fromString(String val) {
        return new BigDecimal(val);
    }

    @TypeConverter
    public static String toString(BigDecimal val) {
        return val.toString();
    }
}
