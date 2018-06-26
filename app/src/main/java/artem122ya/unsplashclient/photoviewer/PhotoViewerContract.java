package artem122ya.unsplashclient.photoviewer;

import android.app.WallpaperManager;
import android.graphics.Bitmap;

import artem122ya.unsplashclient.BasePresenter;
import artem122ya.unsplashclient.BaseView;

public interface PhotoViewerContract {

    interface View extends BaseView<Presenter> {

        void showPhoto(String url);

        void showLoadingPhotoError();

        Bitmap getPhotoBitmap();

        boolean isWritePermissionGranted();

        void showPhotoSavedMessage(String path);

        void showSaveError(Exception e);

        void showProgressBar();

        void hideProgressBar();

        void showWallpaperSetSuccessfullyMessage();

        void showWallpaperSettingError(Exception e);

    }

    interface Presenter extends BasePresenter<View> {

        void showPhoto(String url);

        void savePhoto();

        void setWallpaper(WallpaperManager wallpaperManager);

        void showLoadingPhotoError();

    }

}
