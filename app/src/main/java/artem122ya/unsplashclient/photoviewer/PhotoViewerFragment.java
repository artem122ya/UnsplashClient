package artem122ya.unsplashclient.photoviewer;

import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import javax.inject.Inject;

import artem122ya.unsplashclient.R;
import artem122ya.unsplashclient.di.scopes.ActivityScoped;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerFragment;

@ActivityScoped
public class PhotoViewerFragment extends DaggerFragment implements PhotoViewerContract.View {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Inject
    PhotoViewerContract.Presenter presenter;

    @BindView(R.id.photo_viewer_view)
    ImageView photoView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    View.OnTouchListener photoViewOnTouchListener;

    @Inject
    public PhotoViewerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter.takeView(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.photo_viewer_fragment, container, false);
        ButterKnife.bind(this, view);


        Intent intent = getActivity().getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            presenter.showPhoto(bundle.getString(PhotoViewerActivity.PHOTO_URL_STRING_EXTRA));
        }

        setRetainInstance(true);

        setHasOptionsMenu(true);

        photoView.setOnTouchListener(photoViewOnTouchListener);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.photo_viewer_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_download:
                presenter.savePhoto();
                return true;
            case R.id.app_bar_wallpaper:
                presenter.setWallpaper(WallpaperManager.getInstance(getContext()));
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDestroy() {
        presenter.dropView();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    presenter.savePhoto();
                } else {
                    // permission denied
                    showNoPermissionErrorMessage();
                }
            }
        }
    }

    @Override
    public void showPhoto(String url) {
        Glide.with(getContext())
                .load(url)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        hideProgressBar();
                        presenter.showLoadingPhotoError();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        hideProgressBar();
                        return false;
                    }
                })
                .into(photoView);
    }

    @Override
    public Bitmap getPhotoBitmap() {
        BitmapDrawable drawable = (BitmapDrawable) photoView.getDrawable();
        if (drawable == null) return null;
        else return drawable.getBitmap();
    }

    @Override
    public boolean isWritePermissionGranted() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        } else {
            // Permission has already been granted
            return true;
        }
        return false;
    }

    @Override
    public void showLoadingPhotoError() {
        Snackbar.make(getActivity().findViewById(android.R.id.content),
                getString(R.string.no_results_snackbar_text), Snackbar.LENGTH_INDEFINITE).show();
    }

    @Override
    public void showPhotoSavedMessage(String path) {
        Snackbar.make(getActivity().findViewById(android.R.id.content),
                getString(R.string.photo_saved_snackbar_text) + path, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showSaveError(Exception e) {
        Snackbar.make(getActivity().findViewById(android.R.id.content),
                R.string.saving_error_snackbar_text, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showWallpaperSetSuccessfullyMessage() {
        Snackbar.make(getActivity().findViewById(android.R.id.content),
                R.string.wallpaper_set_successfully_snackbar_text, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showWallpaperSettingError(Exception e) {
        Snackbar.make(getActivity().findViewById(android.R.id.content),
                R.string.wallpaper_setting_error_snackbar_text, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    public void setOnTouchListener(View.OnTouchListener listener) {
        photoViewOnTouchListener = listener;
    }

    private void showNoPermissionErrorMessage() {
        Snackbar.make(getActivity().findViewById(android.R.id.content),
                R.string.no_storage_permission_snackbar_text, Snackbar.LENGTH_LONG).show();
    }

}
