package artem122ya.unsplashclient;

public interface BasePresenter<T> {

    void takeView(T view);

    void dropView();

}
