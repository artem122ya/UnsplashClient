package artem122ya.unsplashclient.photoviewer;

import artem122ya.unsplashclient.di.scopes.ActivityScoped;
import artem122ya.unsplashclient.di.scopes.FragmentScoped;
import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class PhotoViewerModule {

    @FragmentScoped
    @ContributesAndroidInjector
    abstract PhotoViewerFragment photoViewerFragment();

    @ActivityScoped
    @Binds
    abstract PhotoViewerContract.Presenter photoViewerPresenter(PhotoViewerPresenter presenter);

}
