package org.caojun.fortunestream.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity
class Date : Parcelable {

    @PrimaryKey
    var date = ""//yyyy-MM-DD

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    constructor()

    constructor(date: String) : this() {
        this.date = date
    }

    constructor(_in: Parcel) : this() {
        date = _in.readString()
    }

    companion object CREATOR : Parcelable.Creator<Date> {
        override fun createFromParcel(_in: Parcel): Date {
            return Date(_in)
        }

        override fun newArray(size: Int): Array<Date?> {
            return arrayOfNulls(size)
        }
    }
}