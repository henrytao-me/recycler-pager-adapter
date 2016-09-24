/*
 * Copyright 2015 "Henry Tao <hi@henrytao.me>"
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.henrytao.recyclerpageradapter;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by henrytao on 11/13/15.
 */
public abstract class RecyclerPagerAdapter<VH extends RecyclerPagerAdapter.ViewHolder> extends PagerAdapter {

  public abstract int getItemCount();

  public abstract void onBindViewHolder(VH holder, int position);

  public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

  private static final String STATE = "RECYCLER_PAGER_ADAPTER_STATE";

  private static final String TAG = "RecyclerPagerAdapter";

  public static boolean DEBUG = false;

  Map<Integer, RecycleCache> mRecycleCacheMap = new HashMap<>();

  private Logger mLogger;

  private SparseArray<Parcelable> mSavedState = new SparseArray<>();

  public RecyclerPagerAdapter() {
    mLogger = Logger.newInstance(TAG, DEBUG ? Logger.LogLevel.VERBOSE : Logger.LogLevel.NONE);
  }

  @Override
  public void destroyItem(ViewGroup parent, int position, Object object) {
    mLogger.d("destroyItem | position: %d | instanceOfViewHolder: %b", position, object instanceof ViewHolder);
    if (object instanceof ViewHolder) {
      ViewHolder viewHolder = (ViewHolder) object;
      viewHolder.mIsAttached = false;
      viewHolder.mCurrentPosition = position;
      parent.removeView(viewHolder.itemView);
    }
  }

  @Override
  public void finishUpdate(ViewGroup container) {
    super.finishUpdate(container);
  }

  @Override
  public int getCount() {
    return getItemCount();
  }

  @Override
  public int getItemPosition(Object object) {
    mLogger.d("getItemPosition | instanceOfViewHolder: %b | currentPosition: %d | isAttached: %b",
        object instanceof ViewHolder,
        object instanceof ViewHolder ? ((ViewHolder) object).mCurrentPosition : -1,
        object instanceof ViewHolder && ((ViewHolder) object).mIsAttached);
    return POSITION_NONE;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object instantiateItem(ViewGroup parent, int position) {
    int viewType = getItemViewType(position);
    if (!mRecycleCacheMap.containsKey(viewType)) {
      mRecycleCacheMap.put(viewType, new RecycleCache(this, parent, viewType));
    }
    ViewHolder viewHolder = mRecycleCacheMap.get(viewType).getFreeViewHolder();
    viewHolder.mIsAttached = true;
    viewHolder.mCurrentPosition = position;
    onBindViewHolder((VH) viewHolder, position);
    parent.addView(viewHolder.itemView);
    mLogger.d("instantiateItem | position: %d | viewType: %d | cacheCount: %d",
        position, viewType, mRecycleCacheMap.get(viewType).mCaches.size());
    viewHolder.restoreState(mSavedState.get(position));
    return viewHolder;
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return object instanceof ViewHolder && ((ViewHolder) object).itemView == view;
  }

  @Override
  public void notifyDataSetChanged() {
    mLogger.d("notifyDataSetChanged");
    super.notifyDataSetChanged();
    for (ViewHolder viewHolder : getAttachedViewHolders()) {
      onNotifyItemChanged(viewHolder);
    }
  }

  @Override
  public void restoreState(Parcelable state, ClassLoader loader) {
    if (state instanceof Bundle) {
      Bundle bundle = (Bundle) state;
      bundle.setClassLoader(loader);
      SparseArray<Parcelable> ss = bundle.containsKey(STATE) ? bundle.getSparseParcelableArray(STATE) : null;
      mSavedState = ss != null ? ss : new SparseArray<Parcelable>();
    }
    super.restoreState(state, loader);
  }

  @Override
  public Parcelable saveState() {
    Bundle bundle = new Bundle();
    int size = mRecycleCacheMap.size();
    for (int index = 0; index < size; index++) {
      RecycleCache recycleCache = mRecycleCacheMap.get(index);
      int n = recycleCache.mCaches.size();
      for (int i = 0; i < n; i++) {
        ViewHolder viewHolder = recycleCache.mCaches.get(i);
        mSavedState.put(viewHolder.mCurrentPosition, viewHolder.saveState());
      }
    }
    bundle.putSparseParcelableArray(STATE, mSavedState);
    return bundle;
  }

  public int getItemViewType(int position) {
    return 0;
  }

  protected void onNotifyItemChanged(ViewHolder viewHolder) {
  }

  private List<ViewHolder> getAttachedViewHolders() {
    List<ViewHolder> viewHolders = new ArrayList<>();
    for (Map.Entry<Integer, RecycleCache> entry : mRecycleCacheMap.entrySet()) {
      List<ViewHolder> cache = entry.getValue().mCaches;
      int i = 0;
      for (int n = cache.size(); i < n; i++) {
        if (cache.get(i).mIsAttached) {
          viewHolders.add(cache.get(i));
        }
      }
    }
    return viewHolders;
  }

  protected static class RecycleCache {

    private final RecyclerPagerAdapter mAdapter;

    private final ViewGroup mParent;

    private final int mViewType;

    private List<ViewHolder> mCaches;

    public RecycleCache(RecyclerPagerAdapter adapter, ViewGroup parent, int viewType) {
      mAdapter = adapter;
      mParent = parent;
      mViewType = viewType;
      mCaches = new ArrayList<>();
    }

    public ViewHolder getFreeViewHolder() {
      int i = 0;
      ViewHolder viewHolder;
      for (int n = mCaches.size(); i < n; i++) {
        viewHolder = mCaches.get(i);
        if (!viewHolder.mIsAttached) {
          return viewHolder;
        }
      }
      viewHolder = mAdapter.onCreateViewHolder(mParent, mViewType);
      mCaches.add(viewHolder);
      return viewHolder;
    }
  }

  public static abstract class ViewHolder {

    private static final String STATE = "VIEW_HOLDER_STATE";

    public final View itemView;

    private int mCurrentPosition;

    private boolean mIsAttached;

    public ViewHolder(View itemView) {
      if (itemView == null) {
        throw new IllegalArgumentException("itemView may not be null");
      }
      this.itemView = itemView;
    }

    public void restoreState(Parcelable state) {
      if (state instanceof Bundle) {
        Bundle bundle = (Bundle) state;
        SparseArray<Parcelable> ss = bundle.containsKey(STATE) ? bundle.getSparseParcelableArray(STATE) : null;
        if (ss != null) {
          itemView.restoreHierarchyState(ss);
        }
      }
    }

    public Parcelable saveState() {
      SparseArray<Parcelable> state = new SparseArray<>();
      itemView.saveHierarchyState(state);
      Bundle bundle = new Bundle();
      bundle.putSparseParcelableArray(STATE, state);
      return bundle;
    }

    public static class SavedState extends View.BaseSavedState {

      public static final Parcelable.Creator<ViewHolder.SavedState> CREATOR = ParcelableCompat
          .newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
            @Override
            public ViewHolder.SavedState createFromParcel(Parcel in, ClassLoader loader) {
              return new ViewHolder.SavedState(in, loader);
            }

            @Override
            public ViewHolder.SavedState[] newArray(int size) {
              return new ViewHolder.SavedState[size];
            }
          });

      ClassLoader loader;

      SparseArray<Parcelable> viewState;

      public SavedState(Parcelable superState) {
        super(superState);
      }

      SavedState(Parcel in, ClassLoader loader) {
        super(in);
        if (loader == null) {
          loader = getClass().getClassLoader();
        }
        viewState = new SparseArray<>();
        SparseArray data = in.readSparseArray(loader);
        int n = data != null ? data.size() : 0;
        for (int i = 0; i < n; i++) {
          viewState.put(data.keyAt(i), (Parcelable) data.get(i));
        }
        this.loader = loader;
      }

      @Override
      public String toString() {
        return "ViewHolder.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + "}";
      }

      @Override
      public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        SparseArray<Object> data = new SparseArray<>();
        int n = viewState.size();
        for (int i = 0; i < n; i++) {
          data.put(viewState.keyAt(i), viewState.get(i));
        }
        out.writeSparseArray(data);
      }
    }
  }
}
