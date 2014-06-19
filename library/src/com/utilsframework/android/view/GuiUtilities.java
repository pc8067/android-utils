package com.utilsframework.android.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.*;

import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import com.dbbest.framework.CollectionUtils;
import com.dbbest.framework.Predicate;
import com.dbbest.framework.predicates.InstanceOfPredicate;
import com.utilsframework.android.BuildConfig;
import com.utilsframework.android.R;
import com.utilsframework.android.threading.OnFinish;

/**
 * Created by Tikhonenko.S on 19.09.13.
 */
public class GuiUtilities {

	private static final String TAG = "GuiUtilities";

	public final static boolean isOnline(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			Log.w(TAG, "couldn't get connectivity manager");
			return false;
		}

		NetworkInfo activeInfo = connectivity.getActiveNetworkInfo();
		if (activeInfo == null) {
			if (BuildConfig.DEBUG) {
				Log.v(TAG, "network is not available");
			}
			return false;
		}

		return activeInfo.isAvailable() && activeInfo.isConnected();
	}

    public static List<View> getAllChildrenRecursive(View view){
        if(!(view instanceof ViewGroup)){
            return new ArrayList<View>();
        }

        List<View> result = new ArrayList<View>();
        ViewGroup viewGroup = (ViewGroup)view;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            result.add(child);
            result.addAll(getAllChildrenRecursive(child));
        }

        return result;
    }

    public static List<View> getAllChildrenRecursive(View view, Predicate<View> predicate){
        List<View> views = getAllChildrenRecursive(view);
        return CollectionUtils.findAll(views, predicate);
    }

    public static <T extends View> List<T> getAllChildrenRecursive(View view, Class<T> aClass){
        InstanceOfPredicate<View> predicate = new InstanceOfPredicate<View>(aClass);
        return (List<T>) getAllChildrenRecursive(view, predicate);
    }

    public static <T extends View> T getFirstChildWithTypeRecursive(View view, Class<T> aClass){
        List<T> all = getAllChildrenRecursive(view, aClass);
        if(all.isEmpty()){
            return null;
        }

        if(all.size() > 1){
            throw new RuntimeException("all.size() > 1");
        }

        return all.get(0);
    }

    public static List<View> getAllChildrenRecursive(Activity activity){
        return getAllChildrenRecursive(activity.getWindow().getDecorView().getRootView());
    }

    public static List<View> getAllChildrenRecursive(Activity activity, Predicate<View> predicate){
        List<View> views = getAllChildrenRecursive(activity);
        return CollectionUtils.findAll(views, predicate);
    }

    public static <T extends View> List<T> getAllChildrenRecursive(Activity activity, Class<T> aClass){
        InstanceOfPredicate<View> predicate = new InstanceOfPredicate<View>(aClass);
        return (List<T>) getAllChildrenRecursive(activity, predicate);
    }

    public static List<View> getNonViewGroupChildrenRecursive(View view){
        List<View> children = getAllChildrenRecursive(view);
        CollectionUtils.removeAllWithType(children, ViewGroup.class);
        return children;
    }

    public static void setVisibility(Iterable<View> views, int visibility){
        for(View view : views){
            view.setVisibility(visibility);
        }
    }

    public static void setVisibility(View[] views, int visibility){
        setVisibility(Arrays.asList(views), visibility);
    }

    public static View getContentView(Activity activity){
        return activity.getWindow().getDecorView().getRootView();
    }

    public static View[] resourceIdArrayToViewArray(Activity activity, int... ides) {
        View[] result = new View[ides.length];

        int index = 0;
        for(int id : ides){
            result[index++] = activity.findViewById(id);
        }

        return result;
    }

    @SuppressLint("NewApi")
	public static Point getViewCenter(View view) {
        float x = view.getX();
        float y = view.getY();
        int width = view.getMeasuredWidth();
        int height = view.getMeasuredHeight();

        return new Point(Math.round(x + width / 2), Math.round(y + height / 2));
    }

    public static void lockOrientation(Activity context) {
    	if (Build.VERSION.SDK_INT >= 18) {
    		context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
		} else {
            int orientation;
            int rotation = ((WindowManager) context.getSystemService(
                    Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    break;
                case Surface.ROTATION_180:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    break;
                default:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    break;
            }

            context.setRequestedOrientation(orientation);
    	}

    }

    public static void unlockOrientation(Activity context) {
        context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public static void removeView(View view) {
        ViewParent parent = view.getParent();
        if(parent == null || !(parent instanceof ViewGroup)){
            return;
        }

        ((ViewGroup)parent).removeView(view);
    }

    public static void removeAllViews(Iterable<? extends View> views) {
        for(View view : views){
            removeView(view);
        }
    }

    public static Iterator<View> getChildrenIterator(final ViewGroup viewGroup) {
        return new Iterator<View>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < viewGroup.getChildCount();
            }

            @Override
            public View next() {
                return viewGroup.getChildAt(index++);
            }

            @Override
            public void remove() {
                viewGroup.removeViewAt(index - 1);
            }
        };
    }

    public static Iterable<View> getChildren(final ViewGroup viewGroup) {
        return new Iterable<View>() {
            @Override
            public Iterator<View> iterator() {
                return getChildrenIterator(viewGroup);
            }
        };
    }

    public List<View> getChildrenAsList(final ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        List<View> result = new ArrayList<View>(childCount);
        for (int i = 0; i < childCount; i++) {
            result.add(viewGroup.getChildAt(i));
        }

        return result;
    }

    public static List<View> findChildren(ViewGroup viewGroup, Predicate<View> predicate) {
        return CollectionUtils.findAll(getChildren(viewGroup), predicate);
    }

    public static Bitmap createBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    public static void createBitmapFromViewAsync(final View view, final OnFinish<Bitmap> onFinish) {
        final AsyncTask<Void, Void, Bitmap> asyncTask = new AsyncTask<Void, Void, Bitmap>(){
            @Override
            protected Bitmap doInBackground(Void... params) {
                return Bitmap.createBitmap(view.getMeasuredWidth(),
                        view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                Canvas canvas = new Canvas(bitmap);
                view.draw(canvas);

                if(onFinish != null){
                    onFinish.onFinish(bitmap);
                }
            }
        };

        if(view.getMeasuredHeight() == 0 || view.getMeasuredWidth() == 0){
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    asyncTask.execute();
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        } else {
            asyncTask.execute();
        }
    }

    public interface OnImageViewCreated {
        void onFinish(Bitmap bitmap, ImageView imageView);
    }

    // This method creates an ImageView representation of the given view, converting the view into bitmap,
    // and places the result ImageView in the same position with same width and height on the top
    // level of the current activity
    // *
    // Use GuiUtilities.removeView to delete this view from activity.
    public static void createImageViewCloneOnView(final View view, final OnImageViewCreated onFinish){
        createBitmapFromViewAsync(view, new OnFinish<Bitmap>() {
            @Override
            public void onFinish(final Bitmap bitmap) {
                FrameLayout frameLayout = (FrameLayout) view.getParent();

                final ImageView imageView = new ImageView(view.getContext());
                imageView.setImageBitmap(bitmap);

                frameLayout.addView(imageView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                imageView.setX(view.getX());
                imageView.setY(view.getY());

                imageView.getViewTreeObserver().
                        addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                onFinish.onFinish(bitmap, imageView);
                                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                        });
            }
        });
    }
}
