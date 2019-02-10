package com.example.firebaseblog;

public class Blog {

    private  String title,image,description,userName ;

    public  Blog ()
    {}


    public Blog(String title, String image, String description, String userName) {
        this.title = title;
        this.image = image;
        this.description = description;
        this.userName=userName;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
