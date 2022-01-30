package com.example.todooo.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.todooo.Fragment.CalendarFragment;
import com.example.todooo.Fragment.SettingsFragment;
import com.example.todooo.Fragment.TaskFragment;

public class FragmentAdapter extends FragmentStateAdapter {


    public FragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 1:
                return new TaskFragment();
            case 2:
                return new SettingsFragment();
        }

        return new CalendarFragment();
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
