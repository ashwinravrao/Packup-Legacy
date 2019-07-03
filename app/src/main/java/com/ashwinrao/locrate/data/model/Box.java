package com.ashwinrao.locrate.data.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "boxes",
        indices = @Index("id"),
        foreignKeys = @ForeignKey(entity = Move.class,
                parentColumns = "id",
                childColumns = "move_id",
                onDelete = CASCADE,
                onUpdate = CASCADE))

public class Box {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private String id = UUID.randomUUID().toString();

    @ColumnInfo(name = "move_id")
    private String moveId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "created")
    private Date createdDate = new Date();

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "contents")
    private List<String> contents;

    @ColumnInfo(name = "num_items")
    private String numItems;

    @ColumnInfo(name = "is_full")
    private boolean isFull;

    @ColumnInfo(name = "is_moved")
    private boolean isMoved = false;

    @ColumnInfo(name = "is_unpacked")
    private boolean isUnpacked = false;

    public Box() { }

    public String getMoveId() {
        return moveId;
    }

    public void setMoveId(@NonNull String moveId) {
        this.moveId = moveId;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @Ignore
    public void setIdFromUUID(@NonNull UUID uuid) {
        this.id = uuid.toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setNumItems(String numItems) {
        this.numItems = numItems;
    }

    public void setContents(List<String> contents) {
        this.contents = contents;
        if (this.contents.size() == 0) {
            setNumItems("Empty");
        } else if (this.contents.size() == 1) {
            setNumItems("1 item");
        } else {
            setNumItems(String.format(Locale.US, "%d items", this.contents.size()));
        }
    }

    public boolean isFull() {
        return isFull;
    }

    public boolean isMoved() {
        return isMoved;
    }

    public void setUnpacked(boolean unpacked) {
        isUnpacked = unpacked;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description == null ? "No description" : description;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public List<String> getContents() {
        return contents;
    }

    public String getNumItems() {
        return this.numItems == null ? "Empty box" : this.numItems;
    }

    public void setFull(boolean full) {
        isFull = full;
    }

    public void setMoved(boolean moved) {
        isMoved = moved;
    }

    public boolean isUnpacked() {
        return isUnpacked;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}