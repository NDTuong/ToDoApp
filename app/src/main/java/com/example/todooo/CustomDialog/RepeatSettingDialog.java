package com.example.todooo.CustomDialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.todooo.Model.Repeat;
import com.example.todooo.Model.TypeRepeat;
import com.example.todo.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class RepeatSettingDialog extends Dialog {

    private View.OnClickListener btnDoneListener = null;
    private View.OnClickListener btnCancelListener = null;
    private final static String TAG = "RepeatDialog";
    private final int typeSet = 1; // 1: add new || 0: edit

    // repeatState: lưu type repeat hiện tại, mặc định sẽ set = 0
    // 0 (No repeat) | 1 (Daily) | 2 (Weekly) | 3 (Monthly) | 4 (Yearly)
    int repeatState = 0;

    // mảng lưu các ngày trong tuần được chọn để lặp lại nếu type repeat = weekly
    boolean[] dayOfWeekSelected = new boolean[7];

    // biến kiểu Repeat để lưu thông tin người dùng chọn, sau đó cập nhập vào newTask
    Repeat repeat = new Repeat();

    // khởi tạo các list
    // repeatWeekly: lưu các ngày trong tuần sẽ lặp lại thông báo nếu RepeatType = Weekly
    // repeatMonthly: Lưu các ngày trong tháng sẽ lặp lại nếu TypeRepeat = MONTHLY
    List<Integer> repeatWeekly = new ArrayList<>(); // Sun, Mon...,Sat: 0 -> 6

    public RepeatSettingDialog(@NonNull Context context) {
        super(context);
    }

    public RepeatSettingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected RepeatSettingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_repeat);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        //Khởi tạo giá trị/lấy giá trị
        if (this.repeat.getRepeatEvery() == 0) {
            repeat.setRepeatEvery(1);
        }
//        Arrays.fill(dayOfWeekSelected, Boolean.FALSE);
//        Log.d(TAG, "check repeat: " + repeat);
        // Set button DONE và CANCEL
        TextView btnDone = findViewById(R.id.tvBtnDone);
        TextView btnCancel = findViewById(R.id.tvBtnCancel);
        btnDone.setOnClickListener(btnDoneListener);
        btnCancel.setOnClickListener(btnCancelListener);

        // SET UP UI
        // các text view chọn type repeat
        TextView tvNoRepeat = findViewById(R.id.tvNoRepeat);
        TextView tvDaily = findViewById(R.id.tvDaily);
        TextView tvMonthly = findViewById(R.id.tvMonthly);
        TextView tvWeekly = findViewById(R.id.tvWeekly);
        TextView tvYearly = findViewById(R.id.tvYearly);

        // các text view ngày trong tuần
        TextView tvMo = findViewById(R.id.tvRepeatMo);
        TextView tvTu = findViewById(R.id.tvRepeatTu);
        TextView tvWe = findViewById(R.id.tvRepeatWe);
        TextView tvTh = findViewById(R.id.tvRepeatTh);
        TextView tvFr = findViewById(R.id.tvRepeatFr);
        TextView tvSa = findViewById(R.id.tvRepeatSa);
        TextView tvSu = findViewById(R.id.tvRepeatSu);

        // các text view của các mục chọn
        // llRepeatOn: chọn ngày lặp lại nếu Type = Weekly || Monthly
        // llRepeatEvery: chu kì lặp lại
        // llChooseDayOfWeek: linearlayout chứa ngày trong tuần
        LinearLayout llRepeatOn = findViewById(R.id.llRepeatOn);
        LinearLayout llRepeatEvery = findViewById(R.id.llRepeatEvery);
        LinearLayout llChooseDayOfWeek = findViewById(R.id.llChooseDayOfWeek);

        // tvNumRepeatEvery: text view hiển thị chu kì lặp lại
        TextView tvNumRepeatEvery = findViewById(R.id.tvNumRepeatEvery);

        // tạo mảng để đưa vào hàm
        // listRepeatType: các text view chọn repeat type
        // listDayOfWeek: các text view chọn ngày trong tuần
        TextView[] listRepeatType = {tvNoRepeat, tvDaily, tvWeekly, tvMonthly, tvYearly};
        TextView[] listDayOfWeek = {tvMo, tvTu, tvWe, tvTh, tvFr, tvSa, tvSu};

        Calendar c = Calendar.getInstance();
        int dow = c.get(Calendar.DAY_OF_WEEK);

        // set sự kiện khi chọn ngày trong tuần sẽ cập nhật giá trị và màu
        for (TextView tv1 : listDayOfWeek) {
            tv1.setOnClickListener(v1 -> {
                int i = Arrays.asList(listDayOfWeek).indexOf(tv1);
                dayOfWeekSelected[i] = !dayOfWeekSelected[i];
                setRepeatDayOfWeekBg(listDayOfWeek, dayOfWeekSelected);
//                Log.d(TAG, "check day of week selected: " + i + !dayOfWeekSelected[i]);
                getDayOfWeekSelectedFromArr(dayOfWeekSelected, repeatWeekly);
                repeat.setRepeatOnWeek(repeatWeekly);
            });
        }

        // INIT VIEW theo repeat type
        int i1 = repeat.getRepeatEvery();
        String s = "";
        switch (repeat.getType()) {
            case NO_REPEAT:
                repeat.setType(TypeRepeat.NO_REPEAT);
                // repeatState: 0 - NO_REPEAT
                repeatState = 0;
                // cập nhật lại state của các repeat type (màu của các type trên view)
                // type NO_REPEAT sẽ ẩn toàn bộ các phần còn lại của dialog
                int[] visibility1 = {8, 8, 8}; // 8: Visibility = Gone
                updateRepeatTypeSelected(repeatState, visibility1, listRepeatType, tvNumRepeatEvery,
                        llRepeatEvery, llRepeatOn, llChooseDayOfWeek);
                break;
            case DAILY:
                // set type của repeat = DAILY
                this.repeat.setType(TypeRepeat.DAILY);
                s = s + i1 + getContext().getResources().getString(R.string.days);
                if (i1 <= 1) {
                    s = s.substring(0, s.length() - 1);
                }
                // repeatState: 1 - DAILY
                repeatState = 1;
                // cập nhật lại state của các repeat type (màu của các type trên view)
                int[] visibility2 = {0, 8, 8}; // 8: Visibility = Gone
                updateRepeatTypeSelected(repeatState, visibility2, listRepeatType, tvNumRepeatEvery,
                        llRepeatEvery, llRepeatOn, llChooseDayOfWeek, s,
                        R.string.days, R.menu.menu_day_repeat);
                break;
            case WEEKLY:
                // Mỗi lần chọn weekly sẽ reset lại ngày trong tuần đã chọn
                getDayOfWeekSelectedFromList(dayOfWeekSelected, repeat.getRepeatOnWeek());
//                Log.d(TAG, "check weekly: " + repeat.getRepeatOnWeek());
                setRepeatDayOfWeekBg(listDayOfWeek, dayOfWeekSelected);
                s = s + i1 + getContext().getResources().getString(R.string.weeks);
                if (i1 <= 1) {
                    s = s.substring(0, s.length() - 1);
                }
                // set type của repeat = WEEKLY
                this.repeat.setType(TypeRepeat.WEEKLY);
                // repeatState: 2 - WEEKLY
                repeatState = 2;
                // cập nhật lại state của các repeat type (màu của các type trên view)
                int[] visibility = {0, 0, 0}; // 8: Visibility = Gone | 0: Visible
                updateRepeatTypeSelected(repeatState, visibility, listRepeatType, tvNumRepeatEvery,
                        llRepeatEvery, llRepeatOn, llChooseDayOfWeek, s,
                        R.string.weeks, R.menu.menu_week_repeat);
                break;
            case MONTHLY:

                // set type của repeat = MONTHLY
                repeat.setType(TypeRepeat.MONTHLY);
                // repeatState: 3 - MONTHLY
                repeatState = 3;
                s = s + i1 + getContext().getResources().getString(R.string.months);
                if (i1 <= 1) {
                    s = s.substring(0, s.length() - 1);
                }
                // cập nhật lại state của các repeat type (màu của các type trên view)
                int[] visibility3 = {0, 0, 8}; // 8: Visibility = Gone | 0:Visible
                updateRepeatTypeSelected(repeatState, visibility3, listRepeatType, tvNumRepeatEvery,
                        llRepeatEvery, llRepeatOn, llChooseDayOfWeek, s,
                        R.string.months, R.menu.menu_month_repeat);
                break;
            default:
                // set type của repeat = YEARLY
                this.repeat.setType(TypeRepeat.YEARLY);
                // repeatState: 4 - YEARLY
                repeatState = 4;
                s = s + i1 + getContext().getResources().getString(R.string.years);
                if (i1 <= 1) {
                    s = s.substring(0, s.length() - 1);
                }
                // cập nhật lại state của các repeat type (màu của các type trên view)
                int[] visibility4 = {0, 8, 8}; // 8: Visibility = Gone
                updateRepeatTypeSelected(repeatState, visibility4, listRepeatType, tvNumRepeatEvery,
                        llRepeatEvery, llRepeatOn, llChooseDayOfWeek, s,
                        R.string.years, R.menu.menu_year_repeat);

        }


        // SET SỰ KIỆN KHI CHỌN CÁC TYPE
        tvNoRepeat.setOnClickListener(v -> {
            // set type của repeat = NO_REPEAT
            repeat.setType(TypeRepeat.NO_REPEAT);
            // repeatState: 0 - NO_REPEAT
            repeatState = 0;
            // cập nhật lại state của các repeat type (màu của các type trên view)
            // type NO_REPEAT sẽ ẩn toàn bộ các phần còn lại của dialog
            int[] visibility = {8, 8, 8}; // 8: Visibility = Gone
            updateRepeatTypeSelected(repeatState, visibility, listRepeatType, tvNumRepeatEvery,
                    llRepeatEvery, llRepeatOn, llChooseDayOfWeek);
        });

        tvDaily.setOnClickListener(v -> {
            // set type của repeat = DAILY
            this.repeat.setType(TypeRepeat.DAILY);
            // repeatState: 1 - DAILY
            repeatState = 1;
            // cập nhật lại state của các repeat type (màu của các type trên view)
            int[] visibility = {0, 8, 8}; // 8: Visibility = Gone
            updateRepeatTypeSelected(repeatState, visibility, listRepeatType, tvNumRepeatEvery,
                    llRepeatEvery, llRepeatOn, llChooseDayOfWeek,
                    getContext().getResources().getString(R.string._1_day),
                    R.string.days, R.menu.menu_day_repeat);
        });

        tvWeekly.setOnClickListener(v -> {
            // Mỗi lần chọn weekly sẽ reset lại ngày trong tuần đã chọn
            dayOfWeekSelected = new boolean[7];
            Arrays.fill(dayOfWeekSelected, Boolean.FALSE);
            setRepeatDayOfWeekBg(listDayOfWeek, dayOfWeekSelected);
            // set type của repeat = WEEKLY
            this.repeat.setType(TypeRepeat.WEEKLY);
            // repeatState: 2 - WEEKLY
            repeatState = 2;
            // cập nhật lại state của các repeat type (màu của các type trên view)
            int[] visibility = {0, 0, 0}; // 8: Visibility = Gone | 0: Visible
            updateRepeatTypeSelected(repeatState, visibility, listRepeatType, tvNumRepeatEvery,
                    llRepeatEvery, llRepeatOn, llChooseDayOfWeek,
                    getContext().getResources().getString(R.string._1_week),
                    R.string.weeks, R.menu.menu_week_repeat);
        });

        tvMonthly.setOnClickListener(v -> {
            // set type của repeat = MONTHLY
            repeat.setType(TypeRepeat.MONTHLY);
            // repeatState: 3 - MONTHLY
            repeatState = 3;
            // cập nhật lại state của các repeat type (màu của các type trên view)
            int[] visibility = {0, 8, 8}; // 8: Visibility = Gone | 0:Visible
            updateRepeatTypeSelected(repeatState, visibility, listRepeatType, tvNumRepeatEvery,
                    llRepeatEvery, llRepeatOn, llChooseDayOfWeek,
                    getContext().getResources().getString(R.string._1_month),
                    R.string.months, R.menu.menu_month_repeat);
        });

        tvYearly.setOnClickListener(v -> {
            // set type của repeat = YEARLY
            this.repeat.setType(TypeRepeat.YEARLY);
            // repeatState: 4 - YEARLY
            repeatState = 4;
            // cập nhật lại state của các repeat type (màu của các type trên view)
            int[] visibility = {0, 8, 8}; // 8: Visibility = Gone
            updateRepeatTypeSelected(repeatState, visibility, listRepeatType, tvNumRepeatEvery,
                    llRepeatEvery, llRepeatOn, llChooseDayOfWeek,
                    getContext().getResources().getString(R.string._1_year),
                    R.string.years, R.menu.menu_year_repeat);

        });

    }

    public void setPositiveButton(View.OnClickListener onClickListener) {
        dismiss();
        this.btnDoneListener = onClickListener;
    }

    public void setNegativeButton(View.OnClickListener onClickListener) {
        dismiss();
        this.btnCancelListener = onClickListener;
    }

    // SET - LẤY DỮ LIỆU Repeat ĐỂ SHOW LÊN
    // GET LẤY DỮ LIỆU Repeat
    public void setRepeat(Repeat repeat) {
        this.repeat = repeat;
    }

    public Repeat getRepeat() {
        return this.repeat;
    }

    // CẬP NHẬT MÀU CÁC TYPE KHI CÓ 1 TYPE ĐƯỢC CHỌN
    // visibility: 0 - llRepeatEvery | 1 - llRepeatOn | 2 - llChooseDayOfWeek | 3 - mcvChooseDayOfMonth
    private void updateRepeatTypeSelected(int repeatState, int[] visibility, TextView[] listRepeatType,
                                          TextView tvNumRepeatEvery, LinearLayout llRepeatEvery,
                                          LinearLayout llRepeatOn, LinearLayout llChooseDayOfWeek,
                                          String defaultString, int string, int menu) {
        // Update màu các type khi có 1 type được chọn
        setRepeatState(listRepeatType, repeatState);
        // Ẩn/hiện các phần tương ứng với các type
        llRepeatEvery.setVisibility(visibility[0]);
        llRepeatOn.setVisibility(visibility[1]);
        llChooseDayOfWeek.setVisibility(visibility[2]);
        // Default là 1 + Day/Week/Month/Year
        tvNumRepeatEvery.setText(defaultString);
        showMenuRepeatDWMY(tvNumRepeatEvery, menu, string);
    }

    // NHƯ TRÊN NHƯNG K SET STRING VÌ NO_REPEAT
    private void updateRepeatTypeSelected(int repeatState, int[] visibility, TextView[] listRepeatType,
                                          TextView tvNumRepeatEvery, LinearLayout llRepeatEvery,
                                          LinearLayout llRepeatOn, LinearLayout llChooseDayOfWeek) {
        // Update màu các type khi có 1 type được chọn
        setRepeatState(listRepeatType, repeatState);
        // Ẩn/hiện các phần tương ứng với các type
        llRepeatEvery.setVisibility(visibility[0]);
        llRepeatOn.setVisibility(visibility[1]);
        llChooseDayOfWeek.setVisibility(visibility[2]);
    }

    // HÀM ĐỔI MÀU NỀN CÁC TYPE REPEAT
    // ĐƯỢC CHỌN SẼ CÓ MÀU primary_color_tint40
    // KHÔNG ĐƯỢC CHỌN SẼ CÓ MÀU primary_color_tint100
    private void setRepeatState(TextView[] tvList, int repeatState) {
        changeBackgroundTint(tvList[repeatState], R.color.primary_color_tint40);
        for (int i = 0; i < tvList.length; i++) {
            if (i == repeatState) {
                continue;
            }
            changeBackgroundTint(tvList[i], R.color.primary_color_tint100);
        }
    }


    // HÀM SHOW POPUP MENU CHỌN CHU KÌ NGÀY/TUẦN/THÁNG/NĂM
    private void showMenuRepeatDWMY(TextView tv, int menu, int string) {
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUpMenuNumRepeat(getContext(), v, menu, tv, string);
            }
        });
    }


    // HÀM SHOW POPUP MENU CHỌN SỐ NGÀY/TUẦN/THÁNG/NĂM
    // NẾU CHỌN OTHER SẼ MỞ DIALOG ĐỂ NGƯỜI DÙNG TÙY CHỌN SỐ CỤ THỂ
    private void showPopUpMenuNumRepeat(Context context, View view, int menu, TextView tv, int string) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.other) {
                showInputNum(tv, string);
                return false;
            }
            tv.setText(item.getTitle());
            // lấy chu kì repeat
            String repeatEvery = tv.getText().toString().trim();
            String numberOnly = repeatEvery.replaceAll("[^0-9]", "");
            repeat.setRepeatEvery(Integer.parseInt(numberOnly));
            return false;
        });
        popupMenu.show();
    }

    // HÀM SHOW POPUP MENU CHỌN SỐ NGÀY/TUẦN/THÁNG/NĂM TÙY CHỌN
    private void showInputNum(TextView tv, int string) {
        final Dialog dialog = createDialog(R.layout.dialog_num_input);

        EditText numInput = dialog.findViewById(R.id.numInput);
        TextView btnCancel = dialog.findViewById(R.id.tvBtnCancel);
        TextView btnDone = dialog.findViewById(R.id.tvBtnDone);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = numInput.getText().toString().trim();
                if (!TextUtils.isEmpty(input) && !input.equals("0")) {
                    String s = getContext().getResources().getString(string);
                    if (input.equals("1")) {
                        s = s.substring(0, s.length() - 1);
                    }
                    if (Integer.parseInt(input) > 1000) {
                        Toast.makeText(getContext(), R.string.error_input_repeat_every, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    tv.setText(input + " " + s);
                    // lấy chu kì repeat
                    String repeatEvery = tv.getText().toString().trim();
                    String numberOnly = repeatEvery.replaceAll("[^0-9]", "");
                    repeat.setRepeatEvery(Integer.parseInt(numberOnly));
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // HÀM TẠO DIALOG
    private Dialog createDialog(int layout) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);

        Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }

        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttr = window.getAttributes();
        windowAttr.gravity = Gravity.CENTER;
        window.setAttributes(windowAttr);
        dialog.setCancelable(true);
        return dialog;
    }

    // HÀM THAY ĐỔI MÀU CỦA NGÀY ĐƯỢC CHỌN/KHÔNG ĐƯỢC CHỌN
    private void setRepeatDayOfWeekBg(TextView[] listTv, boolean[] dayOfWeek) {
        for (int i = 0; i < 7; i++) {
            if (dayOfWeek[i]) {
                changeBackgroundDrawable(listTv[i], R.drawable.repeat_weekly_slected_1);
            } else {
                changeBackgroundDrawable(listTv[i], R.drawable.repeat_weekly_slected_stroke_1);
            }
        }
    }

    private void getDayOfWeekSelectedFromList(boolean[] arr, List<Integer> lst) {
        if(lst == null){
            Arrays.fill(arr, Boolean.FALSE);
            return;
        }
        for (int i = 0; i < arr.length; i++) {
            if (lst.contains(i)) {
                arr[i] = true;
            } else {
                arr[i] = false;
            }
        }
    }

    private void getDayOfWeekSelectedFromArr(boolean[] arr, List<Integer> lst) {
        lst.clear();
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == true) {
                lst.add(i);
            }
        }
    }

    // HÀM THAY ĐỔI MÀU BACKGROUND
    public void changeBackgroundTint(TextView tv, int color) {
        tv.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), color));
    }

    // HÀM THAY ĐỔI DRAWABLE
    public void changeBackgroundDrawable(TextView tv, int drawable) {
        tv.setBackground((getContext().getResources().getDrawable(drawable)));
    }

    public void changeBackgroundDrawable(ImageView iv, int drawable) {
        iv.setBackground((getContext().getResources().getDrawable(drawable)));
    }
}
