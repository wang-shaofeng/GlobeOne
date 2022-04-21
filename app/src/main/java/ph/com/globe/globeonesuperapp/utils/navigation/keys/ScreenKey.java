/*
 * Copyright (C) 2018 LotusFlare
 * All Rights Reserved.
 * Unauthorized copying and distribution of this file, via any medium is strictly prohibited.
 */
package ph.com.globe.globeonesuperapp.utils.navigation.keys;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Default impl of {@link IScreenKey}.
 * Every key is identified by id.
 * To generate this id best is to use APPT
 * <pre>
 *     &lt;resources&gt;&lt;id name="home_scree"/&gt;&lt;/resources&gt;
 * </pre>
 *
 * Since APPT generates 32bit numbers and we store longs (64b) you can use upper 32 bit to store
 * more info if you want.
 */
public class ScreenKey implements IScreenKey {

    protected final long id;

    public ScreenKey(long id) {
        this.id = id;
    }

    public ScreenKey(Parcel parcel) {
        id = parcel.readLong();
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScreenKey screenKey = (ScreenKey) o;

        return id == screenKey.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "id=" + id + '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
    }

    public static final Parcelable.Creator<ScreenKey> CREATOR = new Parcelable.Creator<ScreenKey>() {
        @Override
        public ScreenKey createFromParcel(Parcel source) {
            return new ScreenKey(source);
        }

        @Override
        public ScreenKey[] newArray(int size) {
            return new ScreenKey[size];
        }
    };
}
