package artem122ya.unsplashclient.photoslist;

import android.os.Bundle;

import javax.inject.Inject;

import artem122ya.unsplashclient.R;
import dagger.Lazy;
import dagger.android.support.DaggerAppCompatActivity;

public class PhotosListActivity extends DaggerAppCompatActivity {

    @Inject
    Lazy<PhotosListFragment> photosListFragmentProvider;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photos_list_activity);

        PhotosListFragment photosListFragment = (PhotosListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.contentFrame);
        if (photosListFragment == null) {
            photosListFragment = photosListFragmentProvider.get();
            getSupportFragmentManager().beginTransaction().add(R.id.contentFrame, photosListFragment)
                    .commit();
        }


    }
}
