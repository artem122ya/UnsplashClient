package artem122ya.unsplashclient.photoviewer;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Singleton
public class PhotoViewerPresenter implements PhotoViewerContract.Presenter{

    private PhotoViewerContract.View view;

    private CompositeDisposable savePhotoDisposable;
    private CompositeDisposable setWallpaperDisposable;



    @Inject
    public PhotoViewerPresenter() {
        savePhotoDisposable = new CompositeDisposable();
        setWallpaperDisposable = new CompositeDisposable();
    }

    @Override
    public void takeView(PhotoViewerContract.View view) {
        this.view = view;
    }

    @Override
    public void dropView() {
        view = null;
    }

    @Override
    public void showPhoto(String url) {
        view.showPhoto(url);
    }

    @Override
    public void savePhoto() {
        final Bitmap bitmap = view.getPhotoBitmap();
        if (bitmap == null) return;
        if (!view.isWritePermissionGranted()) return;
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        savePhotoDisposable.clear();
        view.showProgressBar();
        Disposable disposable = Observable.fromCallable(() -> writeBitmapToFile(bitmap, path))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        file -> {
                            if (view != null) {
                                view.hideProgressBar();
                                view.showPhotoSavedMessage(file.getPath());
                            }
                        },
                        throwable -> {
                            if (view != null) {
                                view.hideProgressBar();
                                view.showSaveError((Exception) throwable);
                            }
                        }
                );
        savePhotoDisposable.add(disposable);
    }

    @Override
    public void setWallpaper(WallpaperManager wallpaperManager) {
        Bitmap photoBitmap = view.getPhotoBitmap();
        if (photoBitmap == null) return;
        view.showProgressBar();
        setWallpaperDisposable.clear();
        Disposable disposable = Observable.fromCallable(() -> {
            wallpaperManager.setBitmap(photoBitmap);
            return 0;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            view.hideProgressBar();
                            view.showWallpaperSetSuccessfullyMessage();
                        },
                        throwable -> {
                            view.hideProgressBar();
                            view.showWallpaperSettingError((Exception) throwable);
                        }

                );
        setWallpaperDisposable.add(disposable);
    }

    @Override
    public void showLoadingPhotoError() {
        view.showLoadingPhotoError();
    }

    private File writeBitmapToFile(Bitmap bitmap, File path) throws IOException {
        File file;
        file = new File(path,
                "image_" + System.currentTimeMillis() + ".png");
        FileOutputStream out = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.close();
        return file;
    }

}
