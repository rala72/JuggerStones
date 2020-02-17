package io.rala.jugger.model;

import android.content.res.ColorStateList;
import android.os.Parcel;
import android.os.Parcelable;

public class Team implements Parcelable {
    private final CharSequence name;
    private final ColorStateList nameColor;
    private final Long points;
    private final ColorStateList pointsColor;

    public static final Creator<Team> CREATOR = new Creator<Team>() {
        @Override
        public Team createFromParcel(Parcel in) {
            return new Team(in);
        }

        @Override
        public Team[] newArray(int size) {
            return new Team[size];
        }
    };

    public Team(CharSequence name, ColorStateList nameColor, Long points, ColorStateList pointsColor) {
        this.name = name;
        this.nameColor = nameColor;
        this.points = points;
        this.pointsColor = pointsColor;
    }

    private Team(Parcel in) {
        name = in.readString();
        nameColor = ColorStateList.CREATOR.createFromParcel(in);
        points = in.readLong();
        pointsColor = ColorStateList.CREATOR.createFromParcel(in);
    }

    public CharSequence getName() {
        return name;
    }

    public Long getPoints() {
        return points;
    }

    public ColorStateList getNameColor() {
        return nameColor;
    }

    public ColorStateList getPointsColor() {
        return pointsColor;
    }

    @Override
    public String toString() {
        return name + ":" + points;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name.toString());
        nameColor.writeToParcel(dest, flags);
        dest.writeLong(points);
        pointsColor.writeToParcel(dest, flags);
    }
}
