package com.wraith.datepickerlibrary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;


import com.wraith.datepickerlibrary.databinding.FragmentDatePickerBinding;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;


public class DatePickerFragment extends DialogFragment {

    private String FORMAT_DATE_PATTERN="yyyy-MM-dd";
    private SimpleDateFormat FORMAT_DATE = new SimpleDateFormat(FORMAT_DATE_PATTERN, Locale.getDefault());
    private static SimpleDateFormat FORMAT_DATE_WEEK = new SimpleDateFormat("EE, MMMM d, yyyy", Locale.getDefault());
    private FragmentDatePickerBinding binding;
    private String title;

    private int selectedDay = 0;
    private int selectedMonth = 0;
    private int selectedYear = 0;
    private final static String MINDATEBYDEFAULT = "1900-1-1";
    public final static String DEFAULT_DATE_KEY = "defaultdatekey";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFORMAT_DATE_PATTERN() {
        return FORMAT_DATE_PATTERN;
    }

    public void setFORMAT_DATE_PATTERN(String FORMAT_DATE_PATTERN) {
        this.FORMAT_DATE_PATTERN = FORMAT_DATE_PATTERN;
    }


    @Override
    public void onStart() {
        super.onStart();
        Window window = Objects.requireNonNull(getDialog()).getWindow();

        WindowManager wm = null;
        DisplayMetrics dm = new DisplayMetrics();
        if (getActivity() != null) {
            wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        }
        if (wm != null) {
            wm.getDefaultDisplay().getMetrics(dm);
        }

        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            if (dm.widthPixels != 0) {
                params.width = (int) (dm.widthPixels * 0.8);
            } else {
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
            }
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_date_picker, container, false);

        binding.btnSelectDate.setOnClickListener(v -> {
            listener.onNumberChanged(selectedYear, selectedMonth+1, selectedDay);
            dismiss();
        });

        setDatePickerDividerColor(binding.dpDate);

        return binding.getRoot();
    }

    private void setDatePickerDividerColor(DatePicker datePicker) {
        // Divider changing:

        // mSpinners
        LinearLayout llFirst = (LinearLayout) datePicker.getChildAt(0);

        // NumberPicker
        LinearLayout mSpinners = (LinearLayout) llFirst.getChildAt(0);
        for (int i = 0; i < mSpinners.getChildCount(); i++) {
            NumberPicker picker = (NumberPicker) mSpinners.getChildAt(i);

            Field[] pickerFields = NumberPicker.class.getDeclaredFields();
            for (Field pf : pickerFields) {
                if (pf.getName().equals("mSelectionDivider")) {
                    pf.setAccessible(true);
                    try {
                        pf.set(picker, new ColorDrawable(Color.parseColor("#E3333A")));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle bundle = getArguments();
        String currentDate = "";
        if (bundle != null) {
            currentDate = bundle.getString(DEFAULT_DATE_KEY);
        }

        if (getTitle()!=null){
            binding.datePickerTitle.setText(getTitle());
        }

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        if (!TextUtils.isEmpty(currentDate)) {
            String[] arrDate = currentDate.split("/");
            selectedYear = Integer.valueOf(arrDate[2]);
            selectedMonth = Integer.valueOf(arrDate[1]) - 1;
            selectedDay = Integer.valueOf(arrDate[0]);

            try {
                currentDate = FORMAT_DATE_WEEK.format(FORMAT_DATE.parse(currentDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        binding.dpDate.setMaxDate(System.currentTimeMillis());
        binding.dpDate.setMinDate(stringToLong(MINDATEBYDEFAULT));

        binding.dpDate.init(selectedYear, selectedMonth, selectedDay, (view, year, monthOfYear, dayOfMonth) -> {
            selectedYear = year;
            selectedMonth = monthOfYear;
            selectedDay = dayOfMonth;
            String strDate = String.valueOf(selectedYear) + "-" + String.valueOf(selectedMonth) + "-" + String.valueOf(selectedDay);
            try {
                strDate = FORMAT_DATE_WEEK.format(FORMAT_DATE.parse(strDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // listener.onNumberChanged(selectedYear, selectedMonth+1, selectedDay);
    }

    private long stringToLong(String strTime) {
        SimpleDateFormat formatter = new SimpleDateFormat(FORMAT_DATE_PATTERN, Locale.getDefault());
        Date date = null;
        try {
            date = formatter.parse(strTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date == null) {
            return 0;
        } else {
            return date.getTime();
        }
    }


    private DatePickerFragment.NumberChangedListener listener;

    public interface NumberChangedListener {
        void onNumberChanged(int year, int monthOfYear, int dayOfMonth);
    }

    public void setOnNumberChangedListener(DatePickerFragment.NumberChangedListener listener) {
        this.listener = listener;
    }
}
