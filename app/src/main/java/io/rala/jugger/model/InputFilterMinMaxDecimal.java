package io.rala.jugger.model;

import android.text.InputFilter;
import android.text.Spanned;

import java.math.BigDecimal;

// https://stackoverflow.com/q/14212518/2715720
@SuppressWarnings("WeakerAccess")
public class InputFilterMinMaxDecimal implements InputFilter {
    private BigDecimal min = BigDecimal.valueOf(-Double.MAX_VALUE), max = BigDecimal.valueOf(Double.MAX_VALUE);

    public InputFilterMinMaxDecimal(BigDecimal min) {
        this(min, BigDecimal.valueOf(Double.MAX_VALUE));
    }

    public InputFilterMinMaxDecimal(BigDecimal min, BigDecimal max) {
        this.min = min.compareTo(this.min) >= 0 && min.compareTo(max) <= 0 ? min : // min < max
                max.compareTo(this.min) > 0 && max.compareTo(min) > 0 ? max : this.min; // max < min
        this.max = max.compareTo(this.max) <= 0 && min.compareTo(max) <= 0 ? max : // min < max
                min.compareTo(this.max) > 0 && max.compareTo(min) > 0 ? min : this.max; // max < min
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.toString().length());
        newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart, newVal.length());
        try {
            if (isInRange(min, max, new BigDecimal(newVal))) return null;
        } catch (NumberFormatException ignored) {
        }
        return "";
    }

    private boolean isInRange(BigDecimal min, BigDecimal max, BigDecimal value) {
        return min.compareTo(value) <= 0 && value.compareTo(max) <= 0;
    }
}