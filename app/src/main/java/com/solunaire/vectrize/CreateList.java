package com.solunaire.vectrize;

public class CreateList {

    private String image_title;
    private Integer image_id;
    private String image_file;

    public String getImage_title() {
        return image_title;
    }

    public String getImage_file() {
        return image_file;
    }

    public void setImage_title(String android_version_name) {
        this.image_title = android_version_name;
    }

    public Integer getImage_ID() {
        return image_id;
    }

    public void setImage_ID(Integer android_image_url) {
        this.image_id = android_image_url;
    }

    public void setImage_Location(String flocation) {
        this.image_file = flocation;
    }
}
