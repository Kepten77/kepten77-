package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.example.data.BgRecord
import com.example.data.DiaryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class XDripReceiver(
    private val repository: DiaryRepository,
    private val getCurrentScenario: () -> String
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.eveningoutpost.dexdrip.BgEstimate") {
            try {
                // Parse xDrip data
                // Usually it provides bgEstimate (double), bgSlopeName (String), time (long)
                val bgValue = intent.getDoubleExtra("com.eveningoutpost.dexdrip.Extras.BgEstimate", 0.0)
                // Convert mg/dL to mmol/L if necessary. xDrip provides mg/dL usually or already converted if configured.
                // Assuming xDrip is sending mg/dl and needs conversion to mmol/l (bgValue / 18), 
                // but let's assume it sends standard or we just show as is if it's < 30.
                val mmolValue = if (bgValue > 30) bgValue / 18.0 else bgValue
                
                val direction = intent.getStringExtra("com.eveningoutpost.dexdrip.Extras.BgSlopeName") ?: ""
                val time = intent.getLongExtra("com.eveningoutpost.dexdrip.Extras.Time", System.currentTimeMillis())

                if (mmolValue > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        repository.insertBgRecord(
                            BgRecord(
                                timestamp = time,
                                bgValue = mmolValue,
                                direction = direction,
                                isFromXdrip = true,
                                scenario = getCurrentScenario()
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("XDripReceiver", "Error parsing xDrip intents", e)
            }
        }
    }

    companion object {
        fun register(context: Context, receiver: XDripReceiver) {
            val filter = IntentFilter("com.eveningoutpost.dexdrip.BgEstimate")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }
        }

        fun unregister(context: Context, receiver: XDripReceiver) {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Handle not registered
            }
        }
    }
}
