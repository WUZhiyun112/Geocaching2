package com.example.geocaching1.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.geocaching1.fragment.FoundGeocachesFragment;
import com.example.geocaching1.fragment.SearchedNotFoundGeocachesFragment;
public class GeocacheSearchPagerAdapter extends FragmentPagerAdapter {

    private int userId;
    private String jwtToken;

    public GeocacheSearchPagerAdapter(@NonNull FragmentManager fm, int userId, String jwtToken) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.userId = userId;
        this.jwtToken = jwtToken;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            // 传递 userId 和 jwtToken 参数
            return FoundGeocachesFragment.newInstance(userId, jwtToken);
        } else {
            return SearchedNotFoundGeocachesFragment.newInstance(userId, jwtToken);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? "Found it" : "Searched but not found";
    }
}
