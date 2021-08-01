package com.example.goplot;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.io.InputStream;

public class SingleRouteViewModel extends AndroidViewModel {

    final RouteDao routeDao;
    final SpotDao spotDao;

    // the fields which we update with the details of the Route once we retrieve it from the database
    private final MutableLiveData<boolean[]> rating = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> bitmap = new MutableLiveData<>();
    private final MutableLiveData<String> startDateTimeString = new MutableLiveData<>();
    private final MutableLiveData<String> endDateTimeString = new MutableLiveData<>();
    private final MutableLiveData<String> kilometreDistance = new MutableLiveData<>();
    // ObservableField is necessary in the traditional paradigm of only updating when the user taps 'Save'
    private final ObservableField<String> description = new ObservableField<>();

    // details of this object are displayed in the associated Activity via data binding
    Route route;

    public SingleRouteViewModel(@NonNull Application application) {
        super(application);
        GoPlotRoomDatabase goPlotRoomDatabase = GoPlotRoomDatabase.getDatabase(application);
        routeDao = goPlotRoomDatabase.routeDao();
        spotDao = goPlotRoomDatabase.spotDao();
    }

    // creating my own binding adapter is necessary to load a Bitmap into an ImageView
    // as there is no built-in ability for this - especially a LiveData<Bitmap>
    @BindingAdapter("android:src")
    public static void loadImage(ImageView view, LiveData<Bitmap> bitmapLiveData) {
        view.setImageBitmap(bitmapLiveData.getValue());
    }

    // getter methods for the fields which are bound to their respective Views
    // note that we cast from MutableLiveData (to allow posting/setting values) to LiveData
    public LiveData<String> getStartDateTimeString() {
        return startDateTimeString;
    }
    public LiveData<String> getEndDateTimeString() {
        return endDateTimeString;
    }

    public LiveData<String> getKilometreDistance() {
        return kilometreDistance;
    }

    public LiveData<Bitmap> getBitmap() {
        return bitmap;
    }

    public LiveData<boolean[]> getRating() {
        return rating;
    }

    // ObservableField is a simpler wrapper which suits the description use case
    public ObservableField<String> getDescription() {
        return description;
    }

    public void setRoute(int routeId) {
        GoPlotRoomDatabase.databaseWriteExecutor.execute(() -> {
            route = routeDao.getRouteById(routeId);
            startDateTimeString.postValue("Start: " + route.getStartDayDateTime());
            endDateTimeString.postValue("End: " + route.getEndDayDateTime());
            kilometreDistance.postValue("Distance (km): " + route.getKilometresString());
            description.set(route.getDescription());
            // according to the rating, we set the boolean[] which the Radio Button statuses will map to
            switch (route.getRating()) {
                case 0:
                    rating.postValue(new boolean[]{true, false, false});
                    break;
                case 1:
                    rating.postValue(new boolean[]{false, true, false});
                    break;
                case 2:
                    rating.postValue(new boolean[]{false, false, true});
                    break;
            }
            // if there was already an image associated with the Route, it will be retrieved by this method call
            updateBitmap();
        });
    }

    // called by the Activity in onActivityResult(...) once it receives the image from the implicit Intent
    public void setImageUri(Uri chosenImageUri) {
        GoPlotRoomDatabase.databaseWriteExecutor.execute(() -> {
            route.setImageUri(chosenImageUri);
            routeDao.update(route);
            updateBitmap();
        });
    }

    // this code needed to be extracted as a method because it is used when the existing image is loaded AND when a user chooses a new one
    private void updateBitmap() {
        if (route.getImageUri() != null) {
            try {
                InputStream inputStream = getApplication().getContentResolver().openInputStream(route.getImageUri());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                this.bitmap.postValue(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // bound in XML to the button to save the text in the @+id/editTextDescription
    public void onSaveDescription() {
        // get the value from the ObservableField and set the corresponding Route object field to it
        this.route.setDescription(description.get());
        GoPlotRoomDatabase.databaseWriteExecutor.execute(() -> routeDao.update(route));
    }

    // bound in XML to each of the Radio Buttons which each pass their IDs to be mapped as the newRating
    public void onRate(int newRating) {
        this.route.setRating(newRating);
        GoPlotRoomDatabase.databaseWriteExecutor.execute(() -> routeDao.update(route));
    }

}
