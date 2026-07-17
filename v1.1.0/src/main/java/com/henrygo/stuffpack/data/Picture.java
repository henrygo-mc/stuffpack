package com.henrygo.stuffpack.data;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Picture {
    
    private String id;
    private String url;
    private Location location;
    private boolean allowBreak;
    private int width;
    private int height;
    private int pieces;
    private UUID owner;
    private List<Integer> mapIds;
    private int cols;
    private int rows;
    
    public Picture(String url, Location location, boolean allowBreak, int width, int height, int pieces, UUID owner) {
        this.id = UUID.randomUUID().toString();
        this.url = url;
        this.location = location;
        this.allowBreak = allowBreak;
        this.width = width;
        this.height = height;
        this.pieces = pieces;
        this.owner = owner;
        this.mapIds = new ArrayList<>();
        this.cols = 0;
        this.rows = 0;
    }
    
    public List<Integer> getMapIds() {
        return mapIds;
    }
    
    public void setMapIds(List<Integer> mapIds) {
        this.mapIds = mapIds;
    }
    
    public int getCols() {
        return cols;
    }
    
    public void setCols(int cols) {
        this.cols = cols;
    }
    
    public int getRows() {
        return rows;
    }
    
    public void setRows(int rows) {
        this.rows = rows;
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public void setOwner(UUID owner) {
        this.owner = owner;
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