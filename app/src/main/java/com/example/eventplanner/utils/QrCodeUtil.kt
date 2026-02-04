package com.example.eventplanner.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

object QrCodeUtil {

    fun generateQrBitmap(payload: String, sizePx: Int): Bitmap {
        val matrix: BitMatrix = MultiFormatWriter().encode(
            payload,
            BarcodeFormat.QR_CODE,
            sizePx,
            sizePx
        )

        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    fun buildPayload(ticketId: String, eventId: String, token: String): String {
        // simple v1 payload; no personal data
        return "v1|ticketId=$ticketId|eventId=$eventId|token=$token"
    }
}
