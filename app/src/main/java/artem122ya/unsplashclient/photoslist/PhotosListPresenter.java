package artem122ya.unsplashclient.photoslist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import artem122ya.unsplashclient.api.PhotosService;
import artem122ya.unsplashclient.models.Photo;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

@Singleton
public class PhotosListPresenter implements PhotosListContract.Presenter {

    @Inject
    PhotosService photosService;

    private PhotosListContract.View view;

    private CompositeDisposable compositeDisposable;

    private int page = 1;

    private int PHOTOS_PER_PAGE = 20;

    @Inject
    PhotosListPresenter() {
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void takeView(PhotosListContract.View view) {
        this.view = view;
    }

    @Override
    public void dropView() {
        compositeDisposable.clear();
        view = null;
    }

    @Override
    public void openPhoto(Photo photo) {
        view.openPhoto(photo.getUrls().getRaw());
    }

    @Override
    public void onNetworkConnected(String searchQuery) {
        view.hideNoNetworkMessage();
        getPhotos(searchQuery, false);
    }

    @Override
    public void onNetworkDisconnected() {
        view.showNoNetworkMessage();
        getPhotos("", false);
    }

    @Override
    public void getPhotos(String searchQuery, boolean resetPageCount) {
        if (resetPageCount) {
            page = 1;
            view.setPhotos(new ArrayList<>(), true);
        }

        view.showProgressbar();
        hideErrorLayouts();
        if (searchQuery.equals("")) loadPhotos(resetPageCount);
        else searchPhotos(searchQuery, resetPageCount);
    }



    private void loadPhotos(boolean rewriteDataSet) {
        compositeDisposable.clear();
        Disposable disposable = photosService
                .getPhotos(page, PHOTOS_PER_PAGE)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            page++;
                            view.hideProgressbar();
                            view.setPhotos(response, rewriteDataSet);
                        },
                        throwable -> {
                            if (page == 1) {
                                showErrorLayout(throwable);
                            }
                        }
                );
       compositeDisposable.add(disposable);
    }

    private void searchPhotos(String searchQuery, boolean rewriteDataSet) {
        compositeDisposable.clear();
        Disposable disposable = photosService
                .searchPhotos(searchQuery ,page, 10)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            page++;
                            view.hideProgressbar();
                            if (response.getPhotos().size() == 0) view.showNoResultsLayout();
                            view.setPhotos(response.getPhotos(), rewriteDataSet);
                        },
                        throwable -> {
                            if (page == 1) {
                                showErrorLayout(throwable);
                            }
                        }
                );
        compositeDisposable.add(disposable);
    }

    private void showErrorLayout(Throwable throwable) {
            view.setPhotos(new ArrayList<>(), true);
            if (throwable instanceof HttpException) {
                HttpException httpException = (HttpException) throwable;
                view.showHttpExceptionLayout(httpException.code());
            }
            if (throwable instanceof IOException) {
                view.showNetworkErrorLayout();
            }
        }

    private void hideErrorLayouts() {
        view.hideNoResultsLayout();
        view.hideNetworkErrorLayout();
    }

}
