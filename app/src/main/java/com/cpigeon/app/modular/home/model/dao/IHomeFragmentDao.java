package com.cpigeon.app.modular.home.model.dao;

import com.cpigeon.app.modular.home.model.bean.HomeAd;
import com.cpigeon.app.modular.matchlive.model.bean.MatchInfo;

import java.util.List;

/**
 * Created by Administrator on 2017/4/12.
 */

public interface IHomeFragmentDao {
    void loadHomeAd(OnLoadCompleteListener onLoadCompleteListener);
    interface OnLoadCompleteListener{
        void onLoadSuccess(List<HomeAd> adList);
        void onLoadFailed(String msg);
    }
    void loadMatchInfo(int loadType,OnReadCompleteListenr listenr);

    interface OnReadCompleteListenr{
        void onLoadSuccess(List<MatchInfo> matchInfos);
        void onLoadFailed();
    }

}