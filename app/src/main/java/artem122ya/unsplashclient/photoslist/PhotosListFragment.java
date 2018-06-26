package artem122ya.unsplashclient.photoslist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import artem122ya.unsplashclient.R;
import artem122ya.unsplashclient.di.scopes.ActivityScoped;
import artem122ya.unsplashclient.models.Photo;
import artem122ya.unsplashclient.photoviewer.PhotoViewerActivity;
import artem122ya.unsplashclient.utils.NetworkUtils;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerFragment;

@ActivityScoped
public class PhotosListFragment extends DaggerFragment implements PhotosListContract.View {

    @Inject
    PhotosListContract.Presenter presenter;

    @BindView(R.id.photos_list_view)
    RecyclerView photosListRecyclerView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.no_connection_textview)
    TextView noConnectionTextView;
    @BindView(R.id.no_results_layout)
    LinearLayout noResultsLayout;
    @BindView(R.id.network_error_layout)
    LinearLayout networkErrorLayout;
    @BindView(R.id.network_error_textview)
    TextView networkErrorTextView;

    BroadcastReceiver broadcastReceiver;

    public static final int LIST_SPAN_COUNT = 2;

    private PhotosListAdapter photosListAdapter;


    private boolean isLoading = false;
    public String searchQuery = "";

    @Inject
    public PhotosListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        photosListAdapter = new PhotosListAdapter();
        presenter.takeView(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.photos_list_fragment, container, false);
        ButterKnife.bind(this, view);


        swipeRefreshLayout.setOnRefreshListener(() -> {
            isLoading = true;
            presenter.getPhotos(searchQuery, true);
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);


        photosListRecyclerView.setHasFixedSize(true);
        photosListRecyclerView.setAdapter(photosListAdapter);

        GridLayoutManager layoutManager =
                new GridLayoutManager(getContext(), LIST_SPAN_COUNT, LinearLayoutManager.VERTICAL, false);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(photosListAdapter.getItemViewType(position)){
                    case PhotosListAdapter.VIEW_ITEM:
                        return 1;
                    case PhotosListAdapter.VIEW_PROGRESS:
                        return LIST_SPAN_COUNT;
                    default:
                        return -1;
                }
            }
        });

        photosListRecyclerView.setLayoutManager(layoutManager);

        photosListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int last = layoutManager.findLastVisibleItemPosition();
                if (dy > 0 && !isLoading && last + 2 >= photosListAdapter.getItemCount()) {
                    isLoading = true;
                    presenter.getPhotos(searchQuery, false);
                }
            }
        });


        photosListAdapter.setOnItemClickListener(photo -> {
            presenter.openPhoto(photo);
        });


        setHasOptionsMenu(true);
        setRetainInstance(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.photos_list_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);

        SearchView searchView = (SearchView) menuItem.getActionView();

        if (!searchQuery.equals("")) {
            // retain searchview state
            menuItem.expandActionView();
            searchView.setQuery(searchQuery, true);
            searchView.clearFocus();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!searchQuery.equals(newText)) {
                    searchQuery = newText;
                    presenter.getPhotos(newText, true);
                }
                return true;
            }
        });

        searchView.setOnCloseListener(() -> {
            searchQuery = "";
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (NetworkUtils.isNetworkAvailable(context)) {
                    isLoading = true;
                    presenter.onNetworkConnected(searchQuery);
                } else presenter.onNetworkDisconnected();
            }
        };
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onDestroy() {
        presenter.dropView();
        super.onDestroy();
    }

    @Override
    public void setPhotos(List<Photo> photos, boolean rewriteDataSet) {
        photosListAdapter.setData(photos, rewriteDataSet);
        isLoading = false;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void openPhoto(String photoUrl) {
        Intent intent = new Intent(getContext(), PhotoViewerActivity.class);
        intent.putExtra(PhotoViewerActivity.PHOTO_URL_STRING_EXTRA, photoUrl);
        startActivity(intent);
    }

    @Override
    public void showProgressbar() {
        photosListAdapter.showProgressBar();
    }

    @Override
    public void hideProgressbar() {
        photosListAdapter.hideProgressBar();
    }

    @Override
    public void showNoNetworkMessage() {
        if (noConnectionTextView != null) noConnectionTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoNetworkMessage() {
        if (noConnectionTextView != null) noConnectionTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showNoResultsLayout() {
        if (noResultsLayout != null) noResultsLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideNoResultsLayout() {
        if (noResultsLayout != null) noResultsLayout.setVisibility(View.INVISIBLE);
    }


    @Override
    public void showHttpExceptionLayout(int code) {
        if (networkErrorLayout != null) networkErrorLayout.setVisibility(View.VISIBLE);
        if (networkErrorTextView != null) networkErrorTextView.setText(
                getString(R.string.http_exception_textview_text) + String.valueOf(code));
    }

    @Override
    public void showNetworkErrorLayout() {
        if (networkErrorLayout != null) networkErrorLayout.setVisibility(View.VISIBLE);
        if (networkErrorTextView != null) networkErrorTextView.setText(getString(R.string.network_error_textview_text));
    }

    @Override
    public void hideNetworkErrorLayout() {
        if (networkErrorLayout != null) networkErrorLayout.setVisibility(View.INVISIBLE);
        if (networkErrorTextView != null) networkErrorTextView.setText("");
    }
}
