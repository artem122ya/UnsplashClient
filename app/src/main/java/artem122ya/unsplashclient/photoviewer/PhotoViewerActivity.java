package artem122ya.unsplashclient.photoviewer;

import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import javax.inject.Inject;

import artem122ya.unsplashclient.R;
import dagger.Lazy;
import dagger.android.support.DaggerAppCompatActivity;

public class PhotoViewerActivity extends DaggerAppCompatActivity {

    public static final String PHOTO_URL_STRING_EXTRA = "photo url extra";

    @Inject
    Lazy<PhotoViewerFragment> photoViewerFragmentProvider;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_viewer_activity);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        PhotoViewerFragment photoViewerFragment = (PhotoViewerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);
        if (photoViewerFragment == null) {
            photoViewerFragment = photoViewerFragmentProvider.get();
            getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, photoViewerFragment)
                    .commit();
        }

        //set touchListener for toggling ui
        GestureDetectorCompat gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleSystemUI();
                return true;
            }
        });
        photoViewerFragment.setOnTouchListener((view, event) -> {
            view.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);
            return true;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void toggleSystemUI() {
        boolean visible = (getWindow().getDecorView().getSystemUiVisibility()
                & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0;
        if (visible) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

}
