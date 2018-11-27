package org.caojun.fortunestream.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcel
import android.os.Parcelable

@Entity
class Account : Parcelable {

    @PrimaryKey
    var account = ""//账号名称

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(account)
    }

    override fun describeContents(): Int {
        return 0
    }

    constructor()

    constructor(account: String) : this() {
        this.account = account
    }

    constructor(_in: Parcel) : this() {
        account = _in.readString()
    }

    companion object CREATOR : Parcelable.Creator<Account> {
        override fun createFromParcel(_in: Parcel): Account {
            return Account(_in)
        }

        override fun newArray(size: Int): Array<Account?> {
            return arrayOfNulls(size)
        }
    }
}