package com.zynergylabs.forager.app.domain

import com.zynergylabs.forager.app.domain.model.LatLng

/** STUB (tests first): the real codec lands in the implementation commit. */
object TrackPathCodec {
    fun encode(path: List<LatLng>): ByteArray = throw NotImplementedError("TrackPathCodec.encode")

    fun decode(bytes: ByteArray): List<LatLng> = throw NotImplementedError("TrackPathCodec.decode")
}
