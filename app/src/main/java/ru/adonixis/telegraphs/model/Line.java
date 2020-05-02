package ru.adonixis.telegraphs.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Line implements Parcelable {

    private boolean enabled;
    private String label;
    private long[] yCoords;
    private String name;
    private int color;

    public Line() {
    }

    public Line(boolean enabled, String label, long[] yCoords, String name, int color) {
        this.enabled = enabled;
        this.label = label;
        this.yCoords = yCoords;
        this.name = name;
        this.color = color;
    }

    protected Line(Parcel in) {
        enabled = in.readByte() != 0;
        label = in.readString();
        yCoords = in.createLongArray();
        name = in.readString();
        color = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeString(label);
        dest.writeLongArray(yCoords);
        dest.writeString(name);
        dest.writeInt(color);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Line> CREATOR = new Creator<Line>() {
        @Override
        public Line createFromParcel(Parcel in) {
            return new Line(in);
        }

        @Override
        public Line[] newArray(int size) {
            return new Line[size];
        }
    };

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long[] getYCoords() {
        return yCoords;
    }

    public void setYCoords(long[] yCoords) {
        this.yCoords = yCoords;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
