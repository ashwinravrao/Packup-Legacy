package com.ashwinrao.locrate.data.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "items",
        indices = @Index("id"),
        foreignKeys = @ForeignKey(entity = Box.class,
                parentColumns = "id",
                childColumns = "box_uuid",
                onUpdate = CASCADE,
                onDelete = CASCADE))

public class Item {

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    private String id = UUID.randomUUID().toString();

    @ColumnInfo(name = "box_uuid")
    private String boxUUID;

    @ColumnInfo(name = "box_number")
    private int boxNumber;

    @ColumnInfo(name = "name")
    private String name = "";

    @ColumnInfo(name = "packed_date")
    private Date packedDate = new Date();

    @ColumnInfo(name = "file_path")
    private String filePath;

    @ColumnInfo(name = "estimated_value")
    private double estimatedValue;

    @ColumnInfo(name = "category")
    private String category;

    @Ignore
    private Boolean isShownWithBoxContext;

    public Item(@NonNull String boxUUID, @NonNull Integer boxNumber, @NonNull String filePath) {
        this.boxUUID = boxUUID;
        this.boxNumber = boxNumber;
        this.filePath = filePath;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @Ignore
    public void setIdFromUUID(@NonNull UUID uuid) {
        this.id = uuid.toString();
    }

    public String getBoxUUID() {
        return boxUUID;
    }

    public void setBoxUUID(String boxUUID) {
        this.boxUUID = boxUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public double getEstimatedValue() {
        return estimatedValue;
    }

    public Date getPackedDate() {
        return packedDate;
    }

    @Ignore
    public void setIsShownWithBoxContext(boolean isShownWithBoxContext) {
        this.isShownWithBoxContext = isShownWithBoxContext;
    }

    public int getBoxNumber() {
        return boxNumber;
    }

    public void setBoxNumber(int boxNumber) {
        this.boxNumber = boxNumber;
    }

    @Ignore
    public String getPackedDateAsString() {
        final DateFormat df = new SimpleDateFormat("M/d/yy", Locale.US);
        String dateAsString = df.format(getPackedDate());
        return isShownWithBoxContext
                ? String.format(Locale.US,"Packed on %s", dateAsString)
                : String.format(Locale.US,"Packed on %s in Box %d", dateAsString, getBoxNumber());
    }

    public void setEstimatedValue(double estimatedValue) {
        this.estimatedValue = estimatedValue;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setPackedDate(Date packedDate) {
        this.packedDate = packedDate;
    }
}
