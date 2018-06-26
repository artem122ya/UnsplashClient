package artem122ya.unsplashclient.di;

import artem122ya.unsplashclient.di.scopes.ActivityScoped;
import artem122ya.unsplashclient.photoslist.PhotosListActivity;
import artem122ya.unsplashclient.photoslist.PhotosListModule;
import artem122ya.unsplashclient.photoviewer.PhotoViewerActivity;
import artem122ya.unsplashclient.photoviewer.PhotoViewerModule;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = PhotosListModule.class)
    abstract PhotosListActivity photosListActivity();

    @ActivityScoped
    @ContributesAndroidInjector(modules = PhotoViewerModule.class)
    abstract PhotoViewerActivity photoViewerActivity();

}
