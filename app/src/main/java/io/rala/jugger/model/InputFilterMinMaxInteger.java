package io.rala.jugger.model;

import android.text.InputFilter;
import android.text.Spanned;

import java.math.BigInteger;

// https://stackoverflow.com/q/14212518/2715720
@SuppressWarnings({"WeakerAccess", "unused"})
public class InputFilterMinMaxInteger implements InputFilter {
    private BigInteger min = BigInteger.valueOf(Long.MIN_VALUE), max = BigInteger.valueOf(Long.MAX_VALUE);

    /**
     * @see #InputFilterMinMaxInteger(BigInteger)
     */
    public InputFilterMinMaxInteger(long min) {
        this(min, Long.MAX_VALUE);
    }

    public InputFilterMinMaxInteger(BigInteger min) {
        this(min, BigInteger.valueOf(Long.MAX_VALUE));
    }

    /**
     * @see #InputFilterMinMaxInteger(BigInteger, BigInteger)
     */
    public InputFilterMinMaxInteger(long min, long max) {
        this(BigInteger.valueOf(min), BigInteger.valueOf(max));
    }

    public InputFilterMinMaxInteger(BigInteger min, BigInteger max) {
        this.min = min.compareTo(this.min) >= 0 && min.compareTo(max) <= 0 ? min : // min < max
            max.compareTo(this.min) > 0 && max.compareTo(min) > 0 ? max : this.min; // max < min
        this.max = max.compareTo(this.max) <= 0 && min.compareTo(max) <= 0 ? max : // min < max
            min.compareTo(this.max) > 0 && max.compareTo(min) > 0 ? min : this.max; // max < min
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        final String oldValue = dest.toString();
        final String newValue = dest.toString().substring(0, dstart) + source.toString().substring(start, end) + dest.toString().substring(dend);
        if (newValue.equals("-") && min.compareTo(BigInteger.valueOf(0)) <= 0) return null;
        try {
            if (isInRange(new BigInteger(newValue))) return null;
            else if (isInRange(new BigInteger(oldValue))) return oldValue;
            else if (oldValue.equals(newValue)) return min.toString();
        } catch (NumberFormatException ignored) {
        }
        return "";
    }

    private boolean isInRange(final BigInteger value) {
        return isInRange(min, max, value);
    }

    private static boolean isInRange(final BigInteger min, final BigInteger max, final BigInteger value) {
        return min.compareTo(value) <= 0 && value.compareTo(max) <= 0;
    }
}