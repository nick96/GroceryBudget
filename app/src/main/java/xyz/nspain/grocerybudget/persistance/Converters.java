package xyz.nspain.grocerybudget.persistance;

import android.arch.persistence.room.TypeConverter;
import android.util.Log;

import java.math.BigDecimal;

public class Converters {
    private static final String TAG = Converters.class.getCanonicalName();

    @TypeConverter
    public static BigDecimal fromLong(Long val) {
        String whole = Long.toString(val / 100);
        String fractional = Long.toString(val % 100);
        return new BigDecimal(whole +  "." + fractional);
    }

    @TypeConverter
    public static long toLong(BigDecimal val) {
        BigDecimal magnified = val.multiply(new BigDecimal(100));

        // We don't care about anything past the first two decimal places so we can use longValue()
        // rather than longValueExact().
        return magnified.longValue();
    }
}
