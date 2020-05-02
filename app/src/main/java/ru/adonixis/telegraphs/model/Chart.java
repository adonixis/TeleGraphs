package ru.adonixis.telegraphs.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Chart implements Parcelable {

    private long[] xCoords;
    private List<Line> lines;

    public Chart() {
    }

    public Chart(long[] xCoords, List<Line> lines) {
        this.xCoords = xCoords;
        this.lines = lines;
    }

    protected Chart(Parcel in) {
        xCoords = in.createLongArray();
        lines = in.createTypedArrayList(Line.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLongArray(xCoords);
        dest.writeTypedList(lines);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Chart> CREATOR = new Creator<Chart>() {
        @Override
        public Chart createFromParcel(Parcel in) {
            return new Chart(in);
        }

        @Override
        public Chart[] newArray(int size) {
            return new Chart[size];
        }
    };

    public long[] getXCoords() {
        return xCoords;
    }

    public void setXCoords(long[] xCoords) {
        this.xCoords = xCoords;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }
}
