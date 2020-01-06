package com.bynq.wallet

import android.app.Application

class BYNQApplication:Application() {

    companion object{
        fun getInstance(): BYNQApplication { return BYNQApplication() }
    }

}