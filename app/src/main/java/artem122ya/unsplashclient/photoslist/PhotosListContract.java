package artem122ya.unsplashclient.photoslist;

import java.util.List;

import artem122ya.unsplashclient.BasePresenter;
import artem122ya.unsplashclient.BaseView;
import artem122ya.unsplashclient.models.Photo;

public interface PhotosListContract {

    interface View extends BaseView<Presenter> {

        void setPhotos(List<Photo> photos, boolean rewriteDataSet);

        void openPhoto(String photoUrl);

        void showProgressbar();

        void hideProgressbar();

        void showNoNetworkMessage();

        void hideNoNetworkMessage();

        void showNoResultsLayout();

        void hideNoResultsLayout();

        void showNetworkErrorLayout();

        void hideNetworkErrorLayout();

        void showHttpExceptionLayout(int code);

    }

    interface Presenter extends BasePresenter<View> {

        void getPhotos(String searchQuery, boolean resetPageCount);

        void onNetworkConnected(String searchQuery);

        void onNetworkDisconnected();

        void openPhoto(Photo photo);
    }

}
