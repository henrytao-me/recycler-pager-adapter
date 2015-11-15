[ ![Download](https://api.bintray.com/packages/henrytao-me/maven/recycler-pager-adapter/images/download.svg) ](https://bintray.com/henrytao-me/maven/recycler-pager-adapter/_latestVersion) [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-recycler--pager--adapter-green.svg?style=true)](https://android-arsenal.com/details/1/2792)

recycler-pager-adaper
================

Recycling mechanism for pager adapter


## Installation

``` groovy
compile "me.henrytao:recycler-pager-adapter:<latest-version>"
```

`recycler-pager-adapter` is deployed to `jCenter`. Make sure you have `jcenter()` in your project gradle.


## Idea

- Apply RecyclerView mechanism for PagerAdapter.
- Use View instead of Fragment for improving performance. 
- Fix onDataSetChanged issue with PagerAdapter. 


## Usage

``` xml
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
  xmlns:tools="http://schemas.android.com/tools"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  tools:context=".MainActivity">

  <android.support.v7.widget.Toolbar
    android:id="@+id/toolbar"
    style="@style/AppStyle.MdToolbar" />

  <android.support.v4.view.ViewPager
    android:id="@+id/view_pager"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_below="@+id/toolbar" />
</RelativeLayout>
```

``` java
mPagerAdapter = new PagerAdapter();
vViewPager.setAdapter(mPagerAdapter);
```

``` java
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
```


## Contributing

Any contributions are welcome!  
Please check the [CONTRIBUTING](CONTRIBUTING.md) guideline before submitting a new PR.


## License

    Copyright 2015 "Henry Tao <hi@henrytao.me>"

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


