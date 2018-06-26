package artem122ya.unsplashclient.photoslist;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.List;


import artem122ya.unsplashclient.R;
import artem122ya.unsplashclient.models.Photo;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotosListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_ITEM = 1;
    public static final int VIEW_PROGRESS = 0;

    public class PhotoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.image_view)
        ImageView photoView;

        @BindView(R.id.image_progressbar)
        ProgressBar progressBar;

        @BindView(R.id.image_card_view)
        CardView cardView;

        PhotoViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            cardView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION)
                if (listener != null) {
                    listener.onItemClick(photosList.get(position));
                }
        }
    }

    public class ProgressBarViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progress_bar)
        ProgressBar progressBar;

        ProgressBarViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Photo photo);
    }

    private OnItemClickListener listener;

    private List<Photo> photosList;

    private boolean isProgressBarVisible = false;

    PhotosListAdapter() {
        photosList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.photo_item, parent, false);
            viewHolder = new PhotoViewHolder(v);
        } else if (viewType == VIEW_PROGRESS) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.progressbar_item, parent, false);
            viewHolder = new ProgressBarViewHolder(v);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PhotoViewHolder) {
            PhotoViewHolder photoViewHolder = (PhotoViewHolder) holder;
            String url = photosList.get(position).getUrls().getSmall();
            photoViewHolder.progressBar.setVisibility(View.VISIBLE);
            Glide.with(photoViewHolder.photoView.getContext()).load(url)
                    .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    photoViewHolder.progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(photoViewHolder.photoView);
        } else if (holder instanceof ProgressBarViewHolder) {
            ((ProgressBarViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return photosList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return photosList.get(position) == null ? VIEW_PROGRESS : VIEW_ITEM;
    }

    public void setData(List<Photo> photos, boolean rewriteDataSet) {
        if(isProgressBarVisible) hideProgressBar();
        if (rewriteDataSet) {
            photosList = photos;
        } else {
            photosList.addAll(photos);
        }
        this.notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void showProgressBar() {
        if (!isProgressBarVisible) {
            isProgressBarVisible = true;
            photosList.add(null);
            this.notifyItemInserted(photosList.size() - 1);
        }
    }

    public void hideProgressBar() {
        if (isProgressBarVisible) {
            isProgressBarVisible = false;
            if (photosList.size() >= 1) {
                photosList.remove(photosList.size() - 1);
                this.notifyItemRemoved(photosList.size() - 1);
            }
        }
    }
}
