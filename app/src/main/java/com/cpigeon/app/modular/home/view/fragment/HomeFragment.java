package com.cpigeon.app.modular.home.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cpigeon.app.MainActivity;
import com.cpigeon.app.R;
import com.cpigeon.app.commonstandard.presenter.BasePresenter;
import com.cpigeon.app.commonstandard.view.fragment.BaseLazyLoadFragment;
import com.cpigeon.app.modular.home.model.bean.HomeAd;
import com.cpigeon.app.modular.home.presenter.HomePresenter;
import com.cpigeon.app.modular.home.view.activity.WebActivity;
import com.cpigeon.app.modular.home.view.fragment.viewdao.IHomeView;
import com.cpigeon.app.modular.matchlive.model.bean.MatchInfo;
import com.cpigeon.app.utils.CommonTool;
import com.cpigeon.app.utils.ScreenTool;
import com.cpigeon.app.utils.customview.SaActionSheetDialog;
import com.cpigeon.app.utils.customview.SearchEditText;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerClickListener;
import com.youth.banner.loader.ImageLoader;

import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/4/6.
 */

public class HomeFragment extends BaseLazyLoadFragment<HomePresenter> implements IHomeView {
    @BindView(R.id.search_edittext)
    SearchEditText searchEdittext;
    @BindView(R.id.home_banner)
    Banner homeBanner;
    @BindView(R.id.layout_gpzb)
    LinearLayout layoutGpzb;
    @BindView(R.id.layout_xhzb)
    LinearLayout layoutXhzb;
    @BindView(R.id.layout_zhcx)
    LinearLayout layoutZhcx;
    @BindView(R.id.layout_wdsc)
    LinearLayout layoutWdsc;
    @BindView(R.id.recyclerview_home)
    RecyclerView recyclerviewHome;
    private HomeAdapter mAdapter;
    private List<HomeAd> homeAdList;

    @Override
    protected void initView(View view) {
        ViewGroup.LayoutParams lp = homeBanner.getLayoutParams();
        lp.height = ScreenTool.getScreenWidth(getActivity()) / 2;
        homeBanner.setLayoutParams(lp);
        mPresenter.laodAd();
        mPresenter.loadMatchInfo(1);
        mPresenter.loadMatchInfo(0);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.fragment_home;
    }

    @Override
    protected void lazyLoad() {

    }


    @OnClick({R.id.layout_gpzb, R.id.layout_xhzb, R.id.layout_zhcx, R.id.layout_wdsc})
    public void onClick(View view) {
        ((MainActivity) getActivity()).onHomeItemClick(view);
    }

    @Override
    public void showAd(final List<HomeAd> homeAdList) {
        //设置banner样式
        homeBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
        //设置图片加载器
        homeBanner.setImageLoader(new XutilsImageLoader());
        //设置图片集合
        homeBanner.setImages(homeAdList);
        //设置banner动画效果
        homeBanner.setBannerAnimation(Transformer.Default);
        //设置标题集合（当banner样式有显示title时）
        // banner.setBannerTitles(Arrays.asList(titles));
        //设置自动轮播，默认为true
        homeBanner.isAutoPlay(true);
        //设置轮播时间
        homeBanner.setDelayTime(5000);
        //设置指示器位置（当banner模式中有指示器时）
        homeBanner.setIndicatorGravity(BannerConfig.CENTER);
        homeBanner.setOnBannerClickListener(new OnBannerClickListener() {
            @Override
            public void OnBannerClick(int position) {
                //Logger.d("ADINFO:adList.size()=" + adList.size() + ";position=" + position);
                if (homeAdList != null && position > 0 && position <= homeAdList.size()) {
                    //点击广告
                    String url = homeAdList.get(position - 1).getAdUrl();
                    //判断是不是网站URL
                    if (CommonTool.Compile(url, CommonTool.PATTERN_WEB_URL)) {
                        Intent intent = new Intent(getActivity(), WebActivity.class);
                        intent.putExtra(WebActivity.INTENT_DATA_KEY_URL, url);
                        intent.putExtra(WebActivity.INTENT_DATA_KEY_BACKNAME, "首页");
                        startActivity(intent);
                    } else {
                        try {
                            Uri uri = Uri.parse(url);
                            SaActionSheetDialog dialog = new SaActionSheetDialog(getActivity()).builder();

                            if (uri.getScheme().equalsIgnoreCase("cpigeon")
                                    && uri.getHost().equalsIgnoreCase("ad")
                                    ) {

                                final String phone = uri.getQueryParameter("tel");
                                if (uri.getQueryParameter("call") != null && uri.getQueryParameter("call").equals("1")) {
                                    dialog.addSheetItem("拨打电话", new SaActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                                            try {
                                                startActivity(intent);
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                                showTips("拨号失败", TipType.ToastShort);
                                            }
                                        }
                                    });

                                }
                                if (uri.getQueryParameter("sms") != null && uri.getQueryParameter("sms").equals("1")) {
                                    dialog.addSheetItem("发送短信", new SaActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            //发送短信
                                            Uri uri = Uri.parse("smsto:" + phone);
                                            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                                            //intent.putExtra("sms_body", "测试发送短信");
                                            try {
                                                startActivity(intent);
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                                showTips("打开失败", TipType.ToastShort);
                                            }
                                        }
                                    });

                                }
                                if (uri.getQueryParameter("url") != null && !uri.getQueryParameter("url").equals("")) {
                                    final String u = uri.getQueryParameter("url");
                                    dialog.addSheetItem("打开网页", new SaActionSheetDialog.OnSheetItemClickListener() {
                                        @Override
                                        public void onClick(int which) {
                                            Intent intent = new Intent(getActivity(), WebActivity.class);
                                            intent.putExtra(WebActivity.INTENT_DATA_KEY_URL, u);
                                            intent.putExtra(WebActivity.INTENT_DATA_KEY_BACKNAME, "首页");
                                            startActivity(intent);
                                        }
                                    });


                                }
                            }
//                                                                dialog.builder();
                            dialog.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    homeBanner.startAutoPlay();
                                }
                            });
                            dialog.show();
                            homeBanner.stopAutoPlay();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        });
        //banner设置方法全部调用完毕时最后调用
        homeBanner.start();

    }

    @Override
    public void showMatchLiveData(List<MatchInfo> list, int type) {
        mAdapter = new HomeAdapter(list, type);
        recyclerviewHome.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerviewHome.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        homeBanner.startAutoPlay();
    }

    @Override
    public void onStop() {
        super.onStop();
        homeBanner.stopAutoPlay();
    }

    @Override
    protected HomePresenter initPresenter() {
        return new HomePresenter(this);
    }

    @Override
    protected boolean isCanDettach() {
        return true;
    }

    private class XutilsImageLoader extends ImageLoader {

        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {

            ImageOptions options = new ImageOptions.Builder()
                    .setSize(imageView.getWidth(), imageView.getHeight())//图片大小
                    .setImageScaleType(ImageView.ScaleType.CENTER_CROP)//缩放
                    .setLoadingDrawableId(R.mipmap.image_load_failed)//加载中默认显示图片
                    .setUseMemCache(true)//设置使用缓存
                    .setFailureDrawableId(R.mipmap.image_load_failed)//加载失败后默认显示图片
                    .build();
            x.image().bind(imageView, ((HomeAd) path).getAdImageUrl(), options);
        }


    }
}
