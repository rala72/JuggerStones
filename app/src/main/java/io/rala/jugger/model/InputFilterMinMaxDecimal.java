package io.rala.jugger.model;

import android.text.InputFilter;
import android.text.Spanned;

import java.math.BigDecimal;

// https://stackoverflow.com/q/14212518/2715720

/**
 * <b>warning</b><br>
 * this filter allows min value in '0.' range but if just '0.' is entered the value may not as expected
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class InputFilterMinMaxDecimal implements InputFilter {
    private BigDecimal min = BigDecimal.valueOf(-Double.MAX_VALUE), max = BigDecimal.valueOf(Double.MAX_VALUE);

    /**
     * @see #InputFilterMinMaxDecimal(BigDecimal)
     */
    public InputFilterMinMaxDecimal(double min) {
        this(min, Double.MAX_VALUE);
    }

    public InputFilterMinMaxDecimal(BigDecimal min) {
        this(min, BigDecimal.valueOf(Double.MAX_VALUE));
    }

    /**
     * @see #InputFilterMinMaxDecimal(BigDecimal, BigDecimal)
     */
    public InputFilterMinMaxDecimal(double min, double max) {
        this(BigDecimal.valueOf(min), BigDecimal.valueOf(max));
    }

    public InputFilterMinMaxDecimal(BigDecimal min, BigDecimal max) {
        this.min = min.compareTo(this.min) >= 0 && min.compareTo(max) <= 0 ? min : // min < max
            max.compareTo(this.min) > 0 && max.compareTo(min) > 0 ? max : this.min; // max < min
        this.max = max.compareTo(this.max) <= 0 && min.compareTo(max) <= 0 ? max : // min < max
            min.compareTo(this.max) > 0 && max.compareTo(min) > 0 ? min : this.max; // max < min
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        final String oldValue = dest.toString();
        final String newValue = dest.toString().substring(0, dstart) + source.toString().substring(start, end) + dest.toString().substring(dend);
        if (newValue.equals("-") && min.compareTo(BigDecimal.valueOf(0)) <= 0 || newValue.matches("0[.,]?") && min.toString().startsWith("0")) return null;
        try {
            if (isInRange(new BigDecimal(newValue))) return null;
            else if (isInRange(new BigDecimal(oldValue))) return oldValue;
            else if (oldValue.equals(newValue)) return min.toString();
        } catch (NumberFormatException ignored) {
        }
        return "";
    }

    private boolean isInRange(final BigDecimal value) {
        return isInRange(min, max, value);
    }

    private static boolean isInRange(final BigDecimal min, final BigDecimal max, final BigDecimal value) {
        return min.compareTo(value) <= 0 && value.compareTo(max) <= 0;
    }
}