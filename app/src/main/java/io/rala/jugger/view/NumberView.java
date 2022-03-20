package io.rala.jugger.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

import java.math.BigDecimal;
import java.math.BigInteger;

import androidx.appcompat.widget.AppCompatTextView;

/**
 * this class simplifies the number handling with a {@link AppCompatTextView}
 */
@SuppressWarnings("unused")
public class NumberView extends AppCompatTextView {
    // TO DO: implement min and max
    // TO DO: implement increaseOne and decreaseOne

    // region constructor
    public NumberView(Context context) {
        super(context);
    }

    public NumberView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    // endregion

    // region set
    public void setNumber(int i) {
        setText(String.valueOf(i));
    }

    public void setNumber(long l) {
        setText(String.valueOf(l));
    }

    public void setNumber(float f) {
        setText(String.valueOf(f));
    }

    public void setNumber(double d) {
        setText(String.valueOf(d));
    }

    @SuppressLint("SetTextI18n")
    public void setNumber(BigInteger b) {
        setText(b.toString());
    }

    @SuppressLint("SetTextI18n")
    public void setNumber(BigDecimal b) {
        setText(b.toString());
    }
    // endregion

    // region get
    public int getNumberAsInt() throws NumberFormatException {
        return Integer.parseInt(getText().toString());
    }

    public long getNumberAsLong() throws NumberFormatException {
        return Long.parseLong(getText().toString());
    }

    public float getNumberAsFloat() throws NumberFormatException {
        return Float.parseFloat(getText().toString());
    }

    public double getNumberAsDouble() throws NumberFormatException {
        return Double.parseDouble(getText().toString());
    }

    public BigInteger getNumberAsBigInteger() throws NumberFormatException {
        return new BigInteger(getText().toString());
    }

    public BigDecimal getNumberAsBigDecimal() throws NumberFormatException {
        return new BigDecimal(getText().toString());
    }
    // endregion

    // region increase
    public void increase(int i) {
        setNumber(getNumberAsInt() + i);
    }

    public void increase(long l) {
        setNumber(getNumberAsLong() + l);
    }

    public void increase(float f) {
        setNumber(getNumberAsFloat() + f);
    }

    public void increase(double d) {
        setNumber(getNumberAsDouble() + d);
    }

    public void increase(BigInteger b) {
        setNumber(getNumberAsBigInteger().add(b));
    }

    public void increase(BigDecimal b) {
        setNumber(getNumberAsBigDecimal().add(b));
    }
    // endregion

    // region decrease
    public void decrease(int i) {
        setNumber(getNumberAsInt() - i);
    }

    public void decrease(long l) {
        setNumber(getNumberAsLong() - l);
    }

    public void decrease(float f) {
        setNumber(getNumberAsFloat() - f);
    }

    public void decrease(double d) {
        setNumber(getNumberAsDouble() - d);
    }

    public void decrease(BigInteger b) {
        setNumber(getNumberAsBigInteger().subtract(b));
    }

    public void decrease(BigDecimal b) {
        setNumber(getNumberAsBigDecimal().subtract(b));
    }
    // endregion
}
