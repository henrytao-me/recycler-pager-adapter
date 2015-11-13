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

package me.henrytao.recyclerpageradapterdemo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.henrytao.recyclerpageradapter.RecyclerPagerAdapter;
import me.henrytao.recyclerpageradapterdemo.R;

/**
 * Created by henrytao on 11/14/15.
 */
public class PagerAdapter extends RecyclerPagerAdapter<RecyclerPagerAdapter.ViewHolder> {

  private List<Type> mData;

  public PagerAdapter() {
    mData = new ArrayList<>();
    mData.add(Type.LOADING);
    for (int i = 0; i < 50; i++) {
      mData.add(Type.ITEM);
    }
  }

  @Override
  public int getItemCount() {
    return mData.size();
  }

  @Override
  public int getItemViewType(int position) {
    return mData.get(position).getValue();
  }

  @Override
  public void onBindViewHolder(RecyclerPagerAdapter.ViewHolder holder, int position) {
    if (holder instanceof ItemViewHolder) {
      ((ItemViewHolder) holder).bind(position);
    }
  }

  @Override
  public RecyclerPagerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    if (Type.valueOf(viewType) == Type.LOADING) {
      return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false), v -> {
        mData.remove(0);
        notifyDataSetChanged();
      });
    } else {
      return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pager, parent, false));
    }
  }

  public enum Type {
    LOADING(0), ITEM(1);

    public static Type valueOf(int value) {
      if (value == 0) {
        return LOADING;
      } else if (value == 1) {
        return ITEM;
      }
      return null;
    }

    private final int mValue;

    Type(int value) {
      mValue = value;
    }

    public int getValue() {
      return mValue;
    }
  }

  public static class ItemViewHolder extends RecyclerPagerAdapter.ViewHolder {

    @Bind(R.id.text) TextView vText;

    public ItemViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    public void bind(int position) {
      vText.setText(String.format(Locale.US, "Pager %d", position));
    }
  }

  public static class LoadingViewHolder extends RecyclerPagerAdapter.ViewHolder {

    public LoadingViewHolder(View itemView, View.OnClickListener onClickListener) {
      super(itemView);
      itemView.setOnClickListener(onClickListener);
    }
  }
}
