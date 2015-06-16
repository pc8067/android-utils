package com.utilsframework.android.threading;

import android.os.AsyncTask;
import android.os.Handler;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;

/**
 * User: Tikhonenko.S
 * Date: 22.11.13
 * Time: 17:26
 */
public final class Threading {
    private static final Handler HANDLER = new Handler();

    public static void runOnBackground(final ThrowingRunnable action, final OnFinish<Throwable> onFinish) {
        new AsyncTask<Void, Void, Throwable>() {
            @Override
            protected Throwable doInBackground(Void... params) {
                try {
                    action.run();
                } catch (Throwable e) {
                    return e;
                }

                return null;
            }

            @Override
            protected void onPostExecute(Throwable throwable) {
                if (onFinish != null) {
                    onFinish.onFinish(throwable);
                } else if (throwable != null) {
                    throw new RuntimeException(throwable);
                }
            }
        }.execute();
    }

    public static void runOnBackground(final ThrowingRunnable action) {
        runOnBackground(action, null);
    }

    public static void runOnBackground(final Runnable runnable, final OnComplete onFinish) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                runnable.run();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (onFinish != null) {
                    onFinish.onFinish();
                }
            }
        }.execute();
    }

    public static void runOnBackground(final Runnable runnable) {
        runOnBackground(runnable, null);
    }

    public static ThreadFactory lowPriorityThreadFactory() {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            }
        };
    }

    public static BlockingQueue<Runnable> highPriorityBlockingQueue() {
        return new PriorityBlockingQueue<Runnable>(11, new Comparator<Runnable>() {
            @Override
            public int compare(Runnable lhs, Runnable rhs) {
                if (lhs instanceof HighPriorityRunnable) {
                    if (rhs instanceof HighPriorityRunnable) {
                        return 0;
                    } else {
                        return -1;
                    }
                } else {
                    if (rhs instanceof HighPriorityRunnable) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            }
        });
    }

    public interface ResultProvider<T> {
        T get();
    }

    public static <T> void getResultAsync(final ResultProvider<T> resultProvider, final OnFinish<T> onFinish) {
        new AsyncTask<Void, Void, T>() {
            @Override
            protected T doInBackground(Void... params) {
                return resultProvider.get();
            }

            @Override
            protected void onPostExecute(T result) {
                onFinish.onFinish(result);
            }
        }.execute();
    }

    public static abstract class Task<ErrorType extends Throwable, Result> {
        public abstract Result runOnBackground() throws ErrorType;
        public void onError(ErrorType error) {}
        public void onSuccess(Result result) {}
        public void onComplete() {}
    }

    public static <ErrorType extends Throwable, Result> void executeAsyncTask(Task<ErrorType, Result> task) {
        new AsyncTask<Void, Void, Result>(){
            ErrorType errorType;

            @Override
            protected Result doInBackground(Void... params) {
                Result result = null;
                try {
                    result = task.runOnBackground();
                } catch (Throwable error) {
                    errorType = (ErrorType) error;
                }

                return result;
            }

            @Override
            protected void onPostExecute(Result result) {
                if (result != null) {
                    task.onSuccess(result);
                } else {
                    task.onError(errorType);
                }

                task.onComplete();
            }
        }.execute();
    }
}
