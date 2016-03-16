package com.epitech.wepleb.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ParseRecyclerQueryAdapter<T extends ParseObject> extends RecyclerView.Adapter {

    protected Context mContext;
    protected QueryFactory queryFactory;
    protected List<T> objects;
    protected ParseObject object;
    protected int objectsPerPage;
    protected int currentPage;
    protected boolean hasNextPage;
    private Set<ParseQuery> runningQueries = Collections.newSetFromMap(new ConcurrentHashMap<ParseQuery, Boolean>());
    private List<OnQueryLoadListener<T>> listeners = new ArrayList<>();
    private boolean paginationEnabled = false;
    private boolean autoload = true;

    public ParseRecyclerQueryAdapter(Context context, final String className) {
        this(context, new QueryFactory() {
            public ParseQuery<T> create() {
                ParseQuery query = ParseQuery.getQuery(className);
                query.orderByDescending("createdAt");
                return query;
            }
        });
        if (className == null) {
            throw new RuntimeException("You need to specify a className for the ParseRecyclerQueryAdapter");
        }
    }

    public ParseRecyclerQueryAdapter(Context context, final QueryFactory queryFactory) {
        this.mContext = context;
        this.objects = new ArrayList<>();
        this.queryFactory = queryFactory;
        this.objectsPerPage = 25;
    }

    public void setQueryFactory(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
        cancelAllQueries();
    }

    public void setAutoLoad(boolean autoLoad) {
        this.autoload = autoLoad;
    }

    public boolean isQueryRunning() {
        return runningQueries.size() > 0;
    }

    @Override
    abstract public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    public void clear() {
        this.objects.clear();
        this.notifyDataSetChanged();
        this.currentPage = 0;
    }

    private void cancelAllQueries() {
        for (ParseQuery q : runningQueries) {
            q.cancel();
        }
        runningQueries.clear();
    }

    public T getItem(int index) {
        return this.objects.get(index);
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public void removeItem(T object) {
        int index = objects.indexOf(object);
        if (index >= 0) {
            objects.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void addItem(T object) {
        objects.add(object);
    }

    public void reload() {
        this.loadParseData(0, true);
    }

    public void loadNextPage() {
        this.loadParseData(this.currentPage + 1, false);
    }

    public void loadParseData(final int page, final boolean shouldClear) {
        final ParseQuery query = this.queryFactory.create();

        if (this.objectsPerPage > 0 && this.paginationEnabled) {
            this.setPageOnQuery(page, query);
        }

        runningQueries.add(query);

        this.notifyOnLoadingListeners();
        query.findInBackground(new FindCallback<T>() {
            @Override
            public void done(List<T> list, ParseException e) {
                if (!runningQueries.contains(query)) {
                    return;
                }

                runningQueries.remove(query);
                if (query.getCachePolicy() != ParseQuery.CachePolicy.CACHE_ONLY || e == null || e.getCode() != 120) {
                    if (e == null || e.getCode() != 100 && e.getCode() == 120) {
                        if (list != null) {
                            ParseRecyclerQueryAdapter.this.currentPage = page;
                            ParseRecyclerQueryAdapter.this.hasNextPage = list.size() > ParseRecyclerQueryAdapter.this.objectsPerPage;
                            if (ParseRecyclerQueryAdapter.this.paginationEnabled && ParseRecyclerQueryAdapter.this.hasNextPage) {
                                list.remove(ParseRecyclerQueryAdapter.this.objectsPerPage);
                            }

                            if (shouldClear) {
                                ParseRecyclerQueryAdapter.this.objects.clear();
                            }
                            ParseRecyclerQueryAdapter.this.objects.addAll(list);

                            ParseRecyclerQueryAdapter.this.notifyDataSetChanged();
                        }
                    } else {
                        ParseRecyclerQueryAdapter.this.hasNextPage = true;
                    }

                    ParseRecyclerQueryAdapter.this.notifyOnLoadedListeners(list, e);
                }
            }
        });
    }

    @Override
    abstract public void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(observer);
        if (this.autoload) {
            this.autoload = false;
            loadParseData(0, true);
        }
    }

    @Override
    public int getItemCount() {
        return this.objects.size();
    }

    public void setPaginationEnabled(boolean paginationEnabled) {
        this.paginationEnabled = paginationEnabled;
    }

    protected void setPageOnQuery(int page, ParseQuery<T> query) {
        query.setLimit(this.objectsPerPage + 1);
        query.setSkip(page * this.objectsPerPage);
    }

    public void addOnQueryLoadListener(OnQueryLoadListener<T> listener) {
        this.listeners.add(listener);
    }

    public void removeOnQueryLoadListener(OnQueryLoadListener<T> listener) {
        this.listeners.remove(listener);
    }

    private void notifyOnLoadingListeners() {
        Iterator i$ = this.listeners.iterator();

        while (i$.hasNext()) {
            OnQueryLoadListener listener = (OnQueryLoadListener) i$.next();
            listener.onLoading(this);
        }

    }

    private void notifyOnLoadedListeners(List<T> objects, Exception e) {
        Iterator i$ = this.listeners.iterator();

        while (i$.hasNext()) {
            OnQueryLoadListener listener = (OnQueryLoadListener) i$.next();
            listener.onLoaded(this, objects, e);
        }

    }

    /* ==================== Interfaces ==========================*/
    public interface OnQueryLoadListener<T> {
        void onLoading(ParseRecyclerQueryAdapter adapter);

        void onLoaded(ParseRecyclerQueryAdapter adapter, List<T> objects, Exception e);
    }

    public interface QueryFactory<T extends ParseObject> {
        ParseQuery<T> create();
    }
}
