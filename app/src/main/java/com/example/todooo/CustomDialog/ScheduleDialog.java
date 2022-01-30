package com.example.todooo.CustomDialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.example.todooo.Lib.CustomDateTimePicker;
import com.example.todooo.Model.Reminder;
import com.example.todooo.Model.Repeat;
import com.example.todooo.Model.Task;
import com.example.todooo.Model.TypeNotifications;
import com.example.todooo.Model.TypeRepeat;
import com.example.todo.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;

public class ScheduleDialog extends Dialog {

    // stateNoDate: lưu trạng thái có/không có ngày đến hạn (due date/deadline)
    private boolean stateNoDate = true;

    // repeatState: lưu type repeat hiện tại, mặc định sẽ set = 0
    // 0 (No repeat) | 1 (Daily) | 2 (Weekly) | 3 (Monthly) | 4 (Yearly)
    int repeatState = 0;

    // mảng lưu các ngày trong tuần được chọn để lặp lại nếu type repeat = weekly
    boolean[] dayOfWeekSelected = new boolean[7];

    // biến kiểu Repeat để lưu thông tin người dùng chọn, sau đó cập nhập vào newTask
    Repeat repeat = new Repeat();

    // biến kiểu Reminder để lưu thông tin người dùng chọn, sau đó cập nhập vào newTask
    Reminder reminder = new Reminder();


    private View.OnClickListener btnDoneListener = null;
    private View.OnClickListener btnCancelListener = null;
    private final static String TAG = "ScheduleDialog";

    private Task task;
    private int typeSchedule = 1; // 1: thêm mới task | 0 sửa task

    public ScheduleDialog(Context context) {
        super(context);
    }

    public ScheduleDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ScheduleDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_schedule_task);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Set button DONE và CANCEL
        TextView btnDone = findViewById(R.id.tvBtnDone);
        TextView btnCancel = findViewById(R.id.tvBtnCancel);
        btnDone.setOnClickListener(btnDoneListener);
        btnCancel.setOnClickListener(btnCancelListener);

        // tvNoDate: set trạng thái có deadline hay không?
        TextView tvNoDate = findViewById(R.id.tvNoDate);

        // phần view set reminder date + time
        LinearLayout llReminder = findViewById(R.id.llReminder);
        TextView tvReminder = findViewById(R.id.tvReminder);
        TextView tvShowReminder = findViewById(R.id.tvShowReminder);
        ImageView ivReminder = findViewById(R.id.ivReminder);

        // phần view set reminder type
        LinearLayout llReminderType = findViewById(R.id.llReminderType);
        TextView tvReminderType = findViewById(R.id.tvReminderType);
        TextView tvShowReminderType = findViewById(R.id.tvShowReminderType);
        ImageView ivReminderType = findViewById(R.id.ivReminderType);

        // phần view set repeat
        LinearLayout llRepeat = findViewById(R.id.llRepeat);
        TextView tvRepeat = findViewById(R.id.tvRepeat);
        TextView tvShowRepeat = findViewById(R.id.tvShowRepeat);
        ImageView ivRepeat = findViewById(R.id.ivRepeat);

        // phần view set priority
        LinearLayout llPriority = findViewById(R.id.llPriority);
        TextView tvPriority = findViewById(R.id.tvPriority);
        TextView tvShowPriority = findViewById(R.id.tvShowPriority);
        ImageView ivPriority = findViewById(R.id.ivPriority);
        ImageView ivShowPriority = findViewById(R.id.ivShowPriority);

        // phần view lịch
        MaterialCalendarView mcv = findViewById(R.id.mcv_schedule);
        mcv.state().edit().setMinimumDate(CalendarDay.today())
                .commit();
        mcv.state().edit().setCalendarDisplayMode(CalendarMode.WEEKS).commit();
        // tạo mảng để đưa vào hàm set enable/disable các thành phần của dialog khi chọn
        // "No date": - chọn: không có deadline của task -> không set ngày giờ...
        //            - không chọn: set ngày/giờ - kiểu thông báo....
        TextView[] changeStateNoDateTv = {tvReminder, tvReminderType, tvRepeat, tvPriority,
                tvShowReminder, tvShowReminderType, tvShowRepeat, tvShowPriority};
        ImageView[] changeStateNoDateIv = {ivReminder, ivReminderType, ivRepeat, ivPriority,
                ivShowPriority};

        // INIT VALUE
        if(typeSchedule == 0){
//            tempTask = task;
            stateNoDate = !task.isHasDueDate();
            if(task.isHasDueDate()){
                // lấy ngày đến hạn và chuyển lịch đến tháng của ngày đến hạn
                // ví dụ đang là tháng 1, deadline ở tháng 2
                // không goToNext thì lịch show ra sẽ hiện tháng 1, k thấy ngày đến hạn ở tháng 2
                if(task.getDueDate() != null) {
                    String[] dueDate = task.getDueDate().split("/");
                    mcv.setDateSelected(CalendarDay.from(Integer.parseInt(dueDate[2]), Integer.parseInt(dueDate[1]),
                            Integer.parseInt(dueDate[0])), true);
                    goToSelectedDate(mcv);
                }
                if(task.getReminder() != null){
                    reminder = task.getReminder();
                    if(task.getReminder().getTimeReminder() != null) {
                        tvShowReminder.setText(task.getReminder().getTimeReminder());
                        if(task.getReminder().getTypeNotify() != null) {
                            switch (task.getReminder().getTypeNotify()) {
                                case NOTIFICATIONS:
                                    tvShowReminderType.setText(getContext().getResources().getString(R.string.notification));
                                    break;
                                default:
                                    tvShowReminderType.setText(getContext().getResources().getString(R.string.alarm));
                                    break;
                            }
                        }
                    }
                }
                if(task.getRepeat() != null){
                    if(task.getRepeat().getType() != TypeRepeat.NO_REPEAT) {
                        tvShowRepeat.setText(getContext().getResources().getString(R.string.yes));
                    }
                    else {
                        tvShowRepeat.setText(getContext().getResources().getString(R.string.no));
                    }
                }

                switch (task.getPriority()){
                    case 1:
                        tvShowPriority.setText(getContext().getResources().getString(R.string.high));
                        ivShowPriority.setBackground(getContext().getResources().getDrawable(R.drawable.ic_circle_1));
                        break;
                    case 2:
                        tvShowPriority.setText(getContext().getResources().getString(R.string.medium));
                        ivShowPriority.setBackground(getContext().getResources().getDrawable(R.drawable.ic_circle_2));
                        break;
                    default:
                        tvShowPriority.setText(getContext().getResources().getString(R.string.normal));
                        ivShowPriority.setBackground(getContext().getResources().getDrawable(R.drawable.ic_circle_3));
                        break;
                }

            }
        }

        // Khởi tạo trạng thái lịch theo stateNoDate
        if(stateNoDate){ mcv.setDateSelected(CalendarDay.today(), false);}
        enableDisableSchedule(changeStateNoDateTv, changeStateNoDateIv, stateNoDate, tvNoDate, mcv);


        // khởi tạo lịch, set sự kiện khi bấm chọn ngày trên lịch
        // mặc định khi mở lên chọn ngày hôm nay
        mcv.setOnDateChangedListener((widget, date, selected) -> {
            // enable các mục chọn ngày giờ,....
            stateNoDate = false;
            task.setHasDueDate(true);
            int year = mcv.getSelectedDate().getYear();
            int month = mcv.getSelectedDate().getMonth();
            int day = mcv.getSelectedDate().getDay();
            String dueDate = checkLessThan10(day) + "/" + checkLessThan10(month) + "/" + year;
            task.setDueDate(dueDate);

            enableDisableSchedule(changeStateNoDateTv, changeStateNoDateIv, false, tvNoDate, mcv);
        });

        // set sự kiện khi bấm chọn "No date"
        tvNoDate.setOnClickListener(v -> {
            // sau khi click đảo trạng thái của stateNoDate
            stateNoDate = !stateNoDate;
            task.setHasDueDate(!stateNoDate);
            if(!task.isHasDueDate()){
                task.setDueDate(null);
                if(task.getRepeat() != null){
                    task.getRepeat().setType(TypeRepeat.NO_REPEAT);
                    task.setPriority(3);
                }
            }

            // kiểm tra trạng thái no của no date
            // - Nếu stateNoDate = true: đang ở trạng thái không có ngày đến hạn
            //   khi click sẽ enable chọn giờ,.....
            // - Nếu stateNoDate = true: đang ở trạng thái không có ngày đến hạn
            //   khi click sẽ disable chọn giờ,.....
                enableDisableSchedule(changeStateNoDateTv, changeStateNoDateIv, stateNoDate, tvNoDate, mcv);
        });

        // set sự kiện khi click set ngày giờ thông báo
        // kiểm tra trạng tái của stateNoDate, nếu = true
        // nếu = true -> disable phần này (return mà không làm gì)
        // nếu không mở datetimepicker để chọn ngày giờ
        llReminder.setOnClickListener(v -> {
            if (stateNoDate) { return; }
            showDateTimePicker(tvShowReminder, tvShowReminderType);
        });

        // set sự kiện khi click set độ ưu tiên
        // kiểm tra trạng thái của stateNoDate,
        // nếu = true -> disable phần này (return mà không làm gì)
        // nếu không thì mở popup menu để chọn độ ưu tiên
        llPriority.setOnClickListener(v -> {
            if (stateNoDate) { return; }
            showPopUpMenu(getContext(), v, R.menu.menu_priority, tvShowPriority, ivShowPriority);
        });

        // set sự kiện khi click set loại thông báo
        // kiểm tra trạng thái của stateNoDate,
        // nếu = true -> disable phần này (return mà không làm gì)
        // nếu không thì mở popup menu để chọn loại thông báo
        llReminderType.setOnClickListener(v -> {
            if (stateNoDate) { return; }
            showPopUpMenu(getContext(), v, R.menu.menu_type_notify, tvShowReminderType);
        });

        // set sự kiện khi click set repeat hay không
        // kiểm tra trạng thái của stateNoDate,
        // nếu = true -> disable phần này (return mà không làm gì)
        // nếu không thì mở dialog để chọn thời gian repeat
        llRepeat.setOnClickListener(v -> {
            if (stateNoDate) { return; }
            final RepeatSettingDialog repeatSettingDialog = new RepeatSettingDialog(getContext());
            repeatSettingDialog.setRepeat(task.getRepeat());
            repeatSettingDialog.setPositiveButton(v1 -> {
//                Toast.makeText(getContext(), "DONE" + repeatSettingDialog.getRepeat().toString(), Toast.LENGTH_SHORT).show();
                if(repeatSettingDialog.getRepeat().getType() == TypeRepeat.NO_REPEAT){
                    tvShowRepeat.setText(getContext().getResources().getString(R.string.no));
                } else {
                    tvShowRepeat.setText(getContext().getResources().getString(R.string.yes));
                }
                task.setRepeat(repeatSettingDialog.getRepeat());
                Log.d(TAG, "check repeat set: " + repeatSettingDialog.getRepeat().toString());
                repeatSettingDialog.dismiss();
            });
            repeatSettingDialog.setNegativeButton(v12 -> {
                Toast.makeText(getContext(), "CANCEL", Toast.LENGTH_SHORT).show();
                repeatSettingDialog.dismiss();
            });
            repeatSettingDialog.show();
        });

    }

    // HÀM ENABLE/DISABLE CÁC TÙY CHỌN Ở DIALOG SCHEDULE THEO TRẠNG THÁI NO DATE
    private void enableDisableSchedule(TextView[] tv, ImageView[] iv, boolean stateNoDate, TextView tvNoDate, MaterialCalendarView mcv) {
        int tvColor, ivColor;
        if(stateNoDate){
            tvColor = R.color.grey700;
            ivColor = R.color.primary_color_tint40;
            mcv.clearSelection();
            changeBackgroundDrawable(tvNoDate, R.drawable.round_background_1);
            changeColorTextView(tvNoDate, R.color.black);
            setTextUseResource(tv[4], R.string.default_no_dialog_schedule);
            setTextUseResource(tv[5], R.string.notification);
            setTextUseResource(tv[6], R.string.default_no_dialog_schedule);
            setTextUseResource(tv[7], R.string.normal);
            changeBackgroundDrawable(iv[4], R.drawable.ic_circle_3);
            reminder.setTimeReminder(null);
            reminder.setTypeNotify(TypeNotifications.NOTIFICATIONS);
            repeat.setType(TypeRepeat.NO_REPEAT);

        }
        else {
            changeBackgroundDrawable(tvNoDate, R.drawable.tag_background_1);
            changeColorTextView(tvNoDate, R.color.grey700);
            tvColor = R.color.black;
            ivColor = R.color.primary_color;
            if(mcv.getSelectedDate() != null) {
                mcv.setDateSelected(mcv.getSelectedDate(), true);
            } else {
                mcv.setDateSelected(CalendarDay.today(), true);
            }
            goToSelectedDate(mcv);

        }
        changeColorTextView(tv[0], tvColor);
        changeColorTextView(tv[1], tvColor);
        changeColorTextView(tv[2], tvColor);
        changeColorTextView(tv[3], tvColor);
        changeColorTextView(tv[4], tvColor);
        changeColorTextView(tv[5], tvColor);
        changeColorTextView(tv[6], tvColor);
        changeColorTextView(tv[7], tvColor);

        changeColorImageView(iv[0], ivColor);
        changeColorImageView(iv[1], ivColor);
        changeColorImageView(iv[2], ivColor);
        changeColorImageView(iv[3], ivColor);
    }

    // HÀM SHOW DATETIMEPICKER
    private void showDateTimePicker(TextView tvShowReminderSet, TextView tvShowReminderType) {
        CustomDateTimePicker customDateTimePicker = new CustomDateTimePicker(getContext(), new CustomDateTimePicker.ICustomDateTimeListener() {
            @Override
            public void onSet(@NotNull Dialog dialog, @NotNull Calendar calendar,
                              @NotNull Date date, int year, @NotNull String monthFullName,
                              @NotNull String monthShortName, int monthNumber, int day,
                              @NotNull String weekDayFullName, @NotNull String weekDayShortName,
                              int hour24, int hour12, int min, int sec, @NotNull String AM_PM, boolean isClear) {
                String dDay, mMonth, hHour, mMinute;
                dDay = checkLessThan10(day);
                mMonth = checkLessThan10(monthNumber + 1);
                hHour = checkLessThan10(hour24);
                mMinute = checkLessThan10(min);
                String dateAndTime = dDay + "/" + mMonth + "/" + year + " - " + hHour + ":" + mMinute;
                if (isClear) {
                    setTextUseResource(tvShowReminderSet, R.string.default_no_dialog_schedule);
                    reminder.setTimeReminder(null);
                    tvShowReminderType.setText(getContext().getString(R.string.notification));
                    reminder.setTypeNotify(TypeNotifications.NOTIFICATIONS);
                } else {
                    tvShowReminderSet.setText(dateAndTime);
                    reminder.setTimeReminder(dateAndTime);
                }
                task.setReminder(reminder);
//                Log.d(TAG, "Reminder time set: " + reminder.getTimeReminder());
            }

            @Override
            public void onCancel() {
            }
        });
        customDateTimePicker.set24HourFormat(true);
        customDateTimePicker.showDialog();
    }

    // SHOW POPUPMENU
    private void showPopUpMenu(Context context, View view, int menu, TextView tv) {
        if (reminder.getTimeReminder() == null) {
            Toast.makeText(getContext(), R.string.error_reminder_type, Toast.LENGTH_SHORT).show();
            return;
        }
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            String typeNotify = item.getTitle().toString();
            if (typeNotify.equals(context.getResources().getString(R.string.notification))) {
                reminder.setTypeNotify(TypeNotifications.NOTIFICATIONS);
            }
            if (typeNotify.equals(context.getResources().getString(R.string.alarm))) {
                reminder.setTypeNotify(TypeNotifications.ALARM);
            }
            tv.setText(typeNotify);
            task.setReminder(reminder);
            return false;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }
        popupMenu.show();

    }

    // SHOW POPUP MENU 2
    private void showPopUpMenu(Context context, View view, int menu, TextView tv, ImageView iv) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            tv.setText(item.getTitle());
            String priority = tv.getText().toString().trim();
            String normal = context.getResources().getString(R.string.normal);
            String medium = context.getResources().getString(R.string.medium);
            String high = context.getResources().getString(R.string.high);
            if (priority.equals(normal)) {
                changeBackgroundDrawable(iv,R.drawable.ic_circle_3);
                this.task.setPriority(3);
            }
            if (priority.equals(medium)) {
                changeBackgroundDrawable(iv,R.drawable.ic_circle_2);
                this.task.setPriority(2);
            }
            if (priority.equals(high)) {
                changeBackgroundDrawable(iv,R.drawable.ic_circle_1);
                this.task.setPriority(1);
            }
            return false;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            popupMenu.setForceShowIcon(true);
        }
        popupMenu.show();
    }

    // HÀM THAY ĐỔI DRAWABLE
    public void changeBackgroundDrawable(TextView tv, int drawable) {
        tv.setBackground((getContext().getResources().getDrawable(drawable)));
    }
    public void changeBackgroundDrawable(ImageView iv, int drawable) {
        iv.setBackground((getContext().getResources().getDrawable(drawable)));
    }

    // HÀM THAY ĐỔI MÀU TEXT
    public void changeColorTextView(TextView tv, int color) {
        tv.setTextColor(getContext().getResources().getColor(color));
    }

    // HÀM THAY ĐỔI MÀU CÁC IMAGE VIEW
    public void changeColorImageView(ImageView iv, int color) {
        Drawable drawable = iv.getBackground();
        drawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTint(drawable, getContext().getResources().getColor(color));
        iv.setBackground(drawable);
    }

    // HÀM THAY ĐỔI MÀU BACKGROUND
    public void changeBackgroundTint(TextView tv, int color) {
        tv.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), color));
    }

    // HÀM SET TEXT VIEW
    public void setTextUseResource(TextView tv, int stringID){
        tv.setText(getContext().getResources().getString(stringID));
    }

    // HÀM KIỂM TRA GIÁ TRỊ INT < 10 HAY KHÔNG
    // NẾU NHỎ HƠN TRẢ VỀ STRING 0 + INT
    public String checkLessThan10(int i) {
        String s;
        if (i < 10) { s = "0" + i; }
        else { s = "" + i; }
        return s;
    }

    public void setPositiveButton(View.OnClickListener onClickListener) {
        dismiss();
        this.btnDoneListener = onClickListener;
    }

    public void setNegativeButton(View.OnClickListener onClickListener) {
        dismiss();
        Log.d("Checkkk", "checkkkk click");
        this.btnCancelListener = onClickListener;
    }

    // SET - LẤY DỮ LIỆU TASK ĐỂ SHOW LÊN
    // GET LẤY DỮ LIỆU TASK
    public void setTask(Task task) { this.task = task; }
    public Task getTask() { return this.task;}

    public void setType(int typeSchedule){
        this.typeSchedule = typeSchedule;
    }

    public void goToSelectedDate(MaterialCalendarView mcv) {
        int year1 = mcv.getSelectedDate().getYear();
        int month1 = mcv.getSelectedDate().getMonth();
        int year2 = mcv.getCurrentDate().getYear();
        int month2 = mcv.getCurrentDate().getMonth();
        int next = (year1-year2)*12 + (month1-month2);
        for(int i = 0; i < Math.abs(next); i++){
            if(next > 0){
                mcv.goToNext();
            }
            if(next < 0){
                mcv.goToPrevious();
            }
        }
    }
}
