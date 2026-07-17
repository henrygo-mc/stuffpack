package com.henrygo.stuffpack.data;

import org.bukkit.Location;

import java.util.UUID;

public class Picture {
    
    private String id;
    private String url;
    private Location location;
    private boolean allowBreak;
    private int width;
    private int height;
    private int pieces;
    
    public Picture(String url, Location location, boolean allowBreak, int width, int height, int pieces) {
        this.id = UUID.randomUUID().toString();
        this.url = url;
        this.location = location;
        this.allowBreak = allowBreak;
        this.width = width;
        this.height = height;
        this.pieces = pieces;
    }
    
    public String getId() {
        return id;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public Location getLocation() {
        return location;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }
    
    public boolean isAllowBreak() {
        return allowBreak;
    }
    
    public void setAllowBreak(boolean allowBreak) {
        this.allowBreak = allowBreak;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public int getPieces() {
        return pieces;
    }
    
    public void setPieces(int pieces) {
        this.pieces = pieces;
    }
}