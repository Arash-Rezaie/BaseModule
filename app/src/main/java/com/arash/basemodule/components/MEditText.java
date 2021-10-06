package com.arash.basemodule.components;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.Toast;

import com.arash.basemodule.R;
import com.arash.basemodule.contracts.IView;
import com.arash.basemodule.contracts.Predicate;
import com.arash.basemodule.exceptions.EmptyException;
import com.arash.basemodule.exceptions.InputNotAcceptableException;
import com.arash.basemodule.exceptions.MalformedException;
import com.arash.basemodule.tools.Utils;

@SuppressLint("AppCompatCustomView")
public class MEditText extends EditText implements IView<String> {
    private boolean required;
    private String pattern;
    private Predicate<String> checker;

    public MEditText(Context context) {
        super(context);
        init();
    }

    public MEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setChecker(Predicate<String> checker) {
        this.checker = checker;
    }

    public Predicate<String> getChecker() {
        return checker;
    }

    public boolean checkInput(String txt, boolean throwEx) {
        boolean result = true;
        RuntimeException trb = null;
        if (txt.isEmpty() && isRequired()) {
            Toast.makeText(getContext(), R.string.noEmptyValue, Toast.LENGTH_LONG).show();
            trb = new EmptyException(this);
            result = false;
        }
        if (result && pattern != null && !txt.matches(pattern)) {
            Toast.makeText(getContext(), R.string.wrongPattern, Toast.LENGTH_LONG).show();
            trb = new MalformedException(this);
            result = false;
        }
        if (result && checker != null && !checker.test(txt)) {
            trb = new InputNotAcceptableException(this);
            result = false;
        }
        if (!result) {
            blink();
            if (throwEx)
                throw trb;
        }
        return result;
    }

    @SuppressLint("WrongConstant")
    private void blink() {
        ObjectAnimator anim = ObjectAnimator.ofInt(this, "backgroundColor", Color.WHITE, Color.RED, Color.WHITE);
        anim.setDuration(100);
        anim.setEvaluator(new ArgbEvaluator());
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(3);
        anim.start();
    }

    @Override
    public void setValue(String s) {
        setText(s);
    }

    @Override
    public String getValue() {
        return getText().toString();
    }
}
