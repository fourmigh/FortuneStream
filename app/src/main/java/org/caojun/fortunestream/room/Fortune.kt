package org.caojun.fortunestream.room

import android.arch.persistence.room.Entity
import android.os.Parcel
import android.os.Parcelable

@Entity(primaryKeys = ["account", "date"])
class Fortune : Parcelable {

    var account = ""//账号名称
    var date = ""//yyyy-MM-DD

    var fortune = 0.0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(account)
        dest.writeString(date)
        dest.writeDouble(fortune)
    }

    override fun describeContents(): Int {
        return 0
    }

    constructor()

    constructor(account: String, date: String, fortune: Double) : this() {
        this.account = account
        this.date = date
        this.fortune = fortune
    }

    constructor(_in: Parcel) : this() {
        account = _in.readString()
        date = _in.readString()
        fortune = _in.readDouble()
    }

    companion object CREATOR : Parcelable.Creator<Fortune> {
        override fun createFromParcel(_in: Parcel): Fortune {
            return Fortune(_in)
        }

        override fun newArray(size: Int): Array<Fortune?> {
            return arrayOfNulls(size)
        }
    }
}