package com.herewhite.sdk.domain;

import com.google.gson.annotations.SerializedName;

/**
 * Created by buhe on 2018/8/15.
 */

/**
 * `PptPage` 类，用于在初始化场景时配置场景的背景图。
 */
public class PptPage extends WhiteObject {

    @SerializedName(value = "src", alternate = {"conversionFileUrl"})
    private String src;
    private Double width;
    private Double height;
    @SerializedName(value = "previewURL", alternate = {"preview"})
    private String preview;

    /**
     * `PptPage` 构造方法，用于创建背景图实例。
     * <p>
     * 该方法只能在场景初始化的时候调用。
     * <p>
     * 场景背景图的中心点默认为白板内部坐标系得原点，背景图无法移动，即无法改变背景图在白板内部的位置。
     *
     * @param src    图片的 URL 地址。
     * @param width  图片在白板中的宽度，单位为像素。
     * @param height 图片在白板中的高度，单位为像素。
     */
    public PptPage(String src, Double width, Double height) {
        this(src, width, height, null);
    }

    /**
     * `PptPage` 构造方法，用于创建背景图实例。
     * <p>
     * 该方法只能在场景初始化的时候调用。
     * <p>
     * 场景背景图的中心点默认为白板内部坐标系得原点，背景图无法移动，即无法改变背景图在白板内部的位置。
     *
     * @param src     图片的 URL 地址。
     * @param width   图片在白板中的宽度，单位为像素。
     * @param height  图片在白板中的高度，单位为像素。
     * @param preview 图片预览图。
     */
    public PptPage(String src, Double width, Double height, String preview) {
        this.src = src;
        this.width = width;
        this.height = height;
        this.preview = preview;
    }


    /**
     * 获取场景背景图的 URL 地址。
     *
     * @return 背景图的 URL 地址。
     */
    public String getSrc() {
        return src;
    }

    /**
     * 设置背景图的 URL 地址。
     *
     * @param src 背景图的 URL 地址。
     */
    public void setSrc(String src) {
        this.src = src;
    }

    /**
     * 获取背景图在白板中的宽度。
     *
     * @return 背景图在白板中的宽度，单位为像素。
     */
    public double getWidth() {
        return width;
    }

    /**
     * 设置背景图在白板中的宽度。
     *
     * @param width 背景图在白板中的宽度，单位为像素。
     */
    public void setWidth(double width) {
        this.width = width;
    }

    /**
     * 获取背景图在白板中的高度。
     *
     * @return 背景图在白板中的高度，单位为像素。
     */
    public double getHeight() {
        return height;
    }

    /**
     * 设置背景图在白板中的高度。
     *
     * @return 背景图在白板中的高度，单位为像素。
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * 获取背景预览图。
     *
     * @return 背景预览图的 URL 地址。
     */
    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }
}
