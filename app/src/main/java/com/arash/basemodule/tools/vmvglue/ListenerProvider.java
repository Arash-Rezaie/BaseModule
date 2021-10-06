package com.arash.basemodule.tools.vmvglue;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.arash.basemodule.components.MEditText;
import com.arash.basemodule.contracts.Consumer;
import com.arash.basemodule.tools.vmvglue.contracts.ViewListenerProvider;

import org.codejargon.feather.Provides;

import javax.inject.Named;

public class ListenerProvider {
    public final static String TEXT_VIEW_TEXT_CHANGE_LISTENER = "TextView_textChangeListener";
    public final static String TEXT_VIEW_FOCUS_LOSE_LISTENER = "TextView_focusLooseListener";
    public final static String M_EDIT_TEXT_CHANGE_LISTENER = "MEditText_textChangeListener";
    public final static String M_EDIT_TEXT_FOCUS_LOSE_LISTENER = "MEditText_focusLooseListener";
    public final static String CHECKABLE_CHECK_CHANGE_LISTENER = "Checkable_checkChangeListener";
    public final static String RADIO_GROUP_SELECTED_ITEM_CHANGE_LISTENER = "RadioGroup_selectedItemChangeListener";
    public final static String SPINNER_SELECTED_ITEM_CHANGE_LISTENER = "Spinner_selectedItemChangeListener";

    /*TextView listeners*/
    @Provides
    @Named(TEXT_VIEW_TEXT_CHANGE_LISTENER)
    public ViewListenerProvider getTextViewOnTextChangeListener() {
        return new ViewListenerProvider() {
            private TextWatcher tw;

            @Override
            public void registerListener(View view, Consumer<Object> consumer) {

                tw = new TextWatcher() {
                    String s;

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        s = charSequence.toString();
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String s2 = editable.toString();
                        if (!s2.equals(s))
                            consumer.accept(s2);
                    }
                };
                TextView v = (TextView) view;
                v.addTextChangedListener(tw);
            }

            @Override
            public void unregisterListener(View view, Consumer<Object> consumer) {
                TextView v = (TextView) view;
                v.removeTextChangedListener(tw);
            }
        };
    }

    @Provides
    @Named(TEXT_VIEW_FOCUS_LOSE_LISTENER)
    public ViewListenerProvider getTextViewOnFocusLooseChangeListener() {
        return new ViewListenerProvider() {

            @Override
            public void registerListener(View view, Consumer<Object> consumer) {
                TextView v = (TextView) view;
                v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    String lastValue;

                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if (b) {
                            lastValue = v.getText().toString();
                        } else {
                            String str = v.getText().toString();
                            if (!str.equals(lastValue))
                                consumer.accept(str);
                        }
                    }
                });
            }

            @Override
            public void unregisterListener(View view, Consumer<Object> consumer) {
                TextView v = (TextView) view;
                v.setOnFocusChangeListener(null);
            }
        };
    }

    /*MEditText listeners*/
    @Provides
    @Named(M_EDIT_TEXT_CHANGE_LISTENER)
    public ViewListenerProvider getMEditTextOnTextChangeListener() {
        return new ViewListenerProvider() {
            private TextWatcher tw;

            @Override
            public void registerListener(View view, Consumer<Object> consumer) {
                MEditText v = (MEditText) view;
                tw = new TextWatcher() {
                    String s;

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        s = charSequence.toString();
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        String s2 = editable.toString();
                        if (!s2.equals(s) && v.checkInput(s2, false))
                            consumer.accept(s2);
                    }
                };
                v.addTextChangedListener(tw);
            }

            @Override
            public void unregisterListener(View view, Consumer<Object> consumer) {
                MEditText v = (MEditText) view;
                v.removeTextChangedListener(tw);
            }
        };
    }

    @Provides
    @Named(M_EDIT_TEXT_FOCUS_LOSE_LISTENER)
    public ViewListenerProvider getMEditTextOnFocusLooseChangeListener() {
        return new ViewListenerProvider() {

            @Override
            public void registerListener(View view, Consumer<Object> consumer) {
                MEditText v = (MEditText) view;
                v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    String lastValue;

                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if (b) {
                            lastValue = v.getText().toString();
                        } else {
                            String str = v.getText().toString();
                            if (!str.equals(lastValue) && v.checkInput(str, false))
                                consumer.accept(str);
                        }
                    }
                });
            }

            @Override
            public void unregisterListener(View view, Consumer<Object> consumer) {
                MEditText v = (MEditText) view;
                v.setOnFocusChangeListener(null);
            }
        };
    }

    /*Checkable listeners (CheckBox,ToggleButton)*/
    @Provides
    @Named(CHECKABLE_CHECK_CHANGE_LISTENER)
    public ViewListenerProvider getCheckableCheckChangeListener() {
        return new ViewListenerProvider() {
            @Override
            public void registerListener(View view, Consumer<Object> consumer) {
                CompoundButton v = (CompoundButton) view;
                v.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    Boolean lastValue = null;

                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        try {
                            if (lastValue == null || lastValue != b)
                                consumer.accept(b);
                        } catch (Exception ignored) {
                        } finally {
                            lastValue = b;
                        }
                    }
                });
            }

            @Override
            public void unregisterListener(View view, Consumer<Object> consumer) {
                CompoundButton v = (CompoundButton) view;
                v.setOnCheckedChangeListener(null);
            }
        };
    }

    /*RadioGroup listeners*/
    @Provides
    @Named(RADIO_GROUP_SELECTED_ITEM_CHANGE_LISTENER)
    public ViewListenerProvider getRadioGroupSelectedItemChangeListener() {
        return new ViewListenerProvider() {
            @Override
            public void registerListener(View view, Consumer<Object> consumer) {
                RadioGroup v = (RadioGroup) view;
                v.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    int lastValue = -1;

                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        try {
                            if (lastValue < 0 || lastValue != i)
                                consumer.accept(i);
                        } catch (Exception ignored) {
                        } finally {
                            lastValue = i;
                        }
                    }
                });
            }

            @Override
            public void unregisterListener(View view, Consumer<Object> consumer) {
                RadioGroup v = (RadioGroup) view;
                v.setOnCheckedChangeListener(null);
            }
        };
    }

    /*Spinner listeners*/
    @Provides
    @Named(SPINNER_SELECTED_ITEM_CHANGE_LISTENER)
    public ViewListenerProvider getSpinnerSelectedItemChangeListener() {
        return new ViewListenerProvider() {
            @Override
            public void registerListener(View view, Consumer<Object> consumer) {
                Spinner v = (Spinner) view;
                v.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    int lastValue = -1;

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        try {
                            if (lastValue != i)
                                consumer.accept(i);
                        } catch (Exception ignored) {
                        } finally {
                            lastValue = i;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }

            @Override
            public void unregisterListener(View view, Consumer<Object> consumer) {
                Spinner v = (Spinner) view;
                v.setOnItemSelectedListener(null);
            }
        };
    }
}
