package artem122ya.unsplashclient.di;

import android.app.Application;

import javax.inject.Singleton;

import artem122ya.unsplashclient.UnsplashClientApp;
import artem122ya.unsplashclient.api.ApiModule;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;


@Singleton
@Component(modules = {
        ApiModule.class,
        ApplicationModule.class,
        ActivityBindingModule.class,
        AndroidSupportInjectionModule.class
})
public interface AppComponent extends AndroidInjector<UnsplashClientApp> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        AppComponent.Builder application(Application application);

        AppComponent build();

    }
}
