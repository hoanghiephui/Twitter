/*
 *  Twidere X
 *
 *  Copyright (C) 2020-2021 Tlaster <tlaster@outlook.com>
 * 
 *  This file is part of Twidere X.
 * 
 *  Twidere X is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  Twidere X is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with Twidere X. If not, see <http://www.gnu.org/licenses/>.
 */
package com.twidere.twiderex.kmp

import android.Manifest
import android.location.Criteria
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.annotation.RequiresPermission
import com.twidere.twiderex.model.kmp.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow

actual class LocationProvider(
    private val locationManager: LocationManager,
) : LocationListener {
    private val _location = MutableStateFlow<Location?>(null)
    actual val location: Flow<Location?>
        get() = _location.asSharedFlow()

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION])
    actual fun enable() {
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_FINE
        val provider = locationManager.getBestProvider(criteria, true) ?: return
        locationManager.requestLocationUpdates(provider, 0, 0f, this)
        locationManager.getCachedLocation()?.let {
            _location.value = Location(
                latitude = it.latitude,
                longitude = it.longitude,
            )
        }
    }

    actual fun disable() {
        locationManager.removeUpdates(this)
    }

    override fun onLocationChanged(p0: android.location.Location) {
        _location.value = p0.let {
            Location(
                latitude = it.latitude,
                longitude = it.longitude,
            )
        }
    }

    // compatibility fix for Api < 22
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }
}

private fun LocationManager.getCachedLocation(): android.location.Location? {
    var location: android.location.Location? = null
    try {
        location = getLastKnownLocation(LocationManager.GPS_PROVIDER)
    } catch (ignore: SecurityException) {
    }

    if (location != null) return location
    try {
        location = getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
    } catch (ignore: SecurityException) {
    }

    return location
}