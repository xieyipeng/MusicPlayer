package com.example.musicplayer.Activity;

import com.example.musicplayer.Fragment.PlayLocalMusicFragment;
import com.example.musicplayer.R;
import com.example.musicplayer.base.FragmentContainerActivity;

public class MainActivity extends FragmentContainerActivity {

    @Override
    protected android.support.v4.app.Fragment createFragment() {
        return new PlayLocalMusicFragment();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getFragmentContainerId() {
        return R.id.Fragment_container;
    }

}
