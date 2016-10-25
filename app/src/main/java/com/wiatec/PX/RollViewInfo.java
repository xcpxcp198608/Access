package com.wiatec.PX;

/**
 * Created by PX on 2016/9/19.
 */
public class RollViewInfo {
    private String imageUrl;
    private String linkUrl;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    @Override
    public String toString() {
        return "RollImageInfo{" +
                "imageUrl='" + imageUrl + '\'' +
                ", linkUrl='" + linkUrl + '\'' +
                '}';
    }
}
