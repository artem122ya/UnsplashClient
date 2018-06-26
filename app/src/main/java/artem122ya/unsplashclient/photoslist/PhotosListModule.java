package artem122ya.unsplashclient.photoslist;

import artem122ya.unsplashclient.di.scopes.ActivityScoped;
import artem122ya.unsplashclient.di.scopes.FragmentScoped;
import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;


@Module
public abstract class PhotosListModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract PhotosListFragment photosListFragment();

    @ActivityScoped
    @Binds
    abstract PhotosListContract.Presenter photosListPresenter(PhotosListPresenter presenter);
}
