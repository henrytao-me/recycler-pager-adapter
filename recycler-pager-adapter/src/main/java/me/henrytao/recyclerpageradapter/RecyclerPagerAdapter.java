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

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by henrytao on 11/13/15.
 */
public abstract class RecyclerPagerAdapter<VH extends RecyclerPagerAdapter.ViewHolder> extends PagerAdapter {

  public abstract int getItemCount();

  public abstract void onBindViewHolder(VH holder, int position);

  public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

  private static final boolean DEBUG = true;

  private static final String TAG = "RecyclerPagerAdapter";

  private Map<Integer, RecycleCache> mRecycleCacheMap = new HashMap<>();

  @Override
  public void destroyItem(ViewGroup parent, int position, Object object) {
    if (DEBUG) {
      Log.i(TAG, String.format(Locale.US, "destroyItem | position: %d | instanceOfViewHolder: %b", position, object instanceof ViewHolder));
    }
    if (object instanceof ViewHolder) {
      ViewHolder viewHolder = (ViewHolder) object;
      viewHolder.mIsAttached = false;
      viewHolder.mCurrentPosition = position;
      parent.removeView(viewHolder.getItemView());
    }
  }

  @Override
  public int getCount() {
    return getItemCount();
  }

  @Override
  public int getItemPosition(Object object) {
    if (DEBUG) {
      Log.i(TAG, String.format(Locale.US, "getItemPosition | instanceOfViewHolder: %b | currentPosition: %d | isAttached: %b",
          object instanceof ViewHolder,
          object instanceof ViewHolder ? ((ViewHolder) object).mCurrentPosition : -1,
          object instanceof ViewHolder && ((ViewHolder) object).mIsAttached));
    }
    return POSITION_NONE;
  }

  @Override
  public Object instantiateItem(ViewGroup parent, int position) {
    int viewType = getItemViewType(position);
    if (!mRecycleCacheMap.containsKey(viewType)) {
      mRecycleCacheMap.put(viewType, new RecycleCache(this, parent, viewType));
    }
    ViewHolder viewHolder = mRecycleCacheMap.get(viewType).getFreeViewHolder();
    viewHolder.mIsAttached = true;
    onBindViewHolder((VH) viewHolder, position);
    parent.addView(viewHolder.mItemView);
    if (DEBUG) {
      Log.i(TAG, String.format(Locale.US, "instantiateItem | position: %d | viewType: %d | cacheCount: %d",
          position, viewType, mRecycleCacheMap.get(viewType).mCaches.size()));
    }
    return viewHolder;
  }

  @Override
  public boolean isViewFromObject(View view, Object object) {
    return object == view || (object instanceof ViewHolder && ((ViewHolder) object).getItemView() == view);
  }

  @Override
  public void notifyDataSetChanged() {
    if (DEBUG) {
      Log.i(TAG, String.format(Locale.US, "notifyDataSetChanged"));
    }
    super.notifyDataSetChanged();
    List<ViewHolder> attachedViewHolders = getAttachedViewHolders();
    int i = 0;
    for (int n = attachedViewHolders.size(); i < n; i++) {
      onNotifyItemChanged(attachedViewHolders.get(i));
    }
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

    private final View mItemView;

    private int mCurrentPosition;

    private boolean mIsAttached;

    public ViewHolder(View itemView) {
      if (itemView == null) {
        throw new IllegalArgumentException("itemView may not be null");
      }
      mItemView = itemView;
    }

    public View getItemView() {
      return mItemView;
    }
  }
}