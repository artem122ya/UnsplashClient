package artem122ya.unsplashclient.api;

import java.util.List;

import artem122ya.unsplashclient.models.Photo;
import artem122ya.unsplashclient.models.Search;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PhotosService {

    @GET("photos")
    Observable<List<Photo>> getPhotos(@Query("page") int page,
                                      @Query("per_page") int photosPerPage);

    @GET("/search/photos")
    Observable<Search> searchPhotos(@Query("query") String searchQuery,
                                    @Query("page") int page,
                                    @Query("per_page") int photosPerPage);

}