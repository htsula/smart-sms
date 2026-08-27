package org.fossify.messages.receivers

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import org.fossify.commons.extensions.copyToClipboard
import org.fossify.commons.extensions.notificationManager
import org.fossify.commons.helpers.isTiramisuPlus
import org.fossify.messages.R
import org.fossify.messages.helpers.COPY_OTP
import org.fossify.messages.helpers.OTP_CODE
import org.fossify.messages.helpers.THREAD_ID

class CopyOtpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            COPY_OTP -> {
                val code = intent.getStringExtra(OTP_CODE) ?: return
                val threadId = intent.getLongExtra(THREAD_ID, 0L)
                context.notificationManager.cancel(threadId.hashCode())
                if (isTiramisuPlus()) {
                    val clipboardManager =
                        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val label = context.getString(R.string.copy_code)
                    clipboardManager.setPrimaryClip(ClipData.newPlainText(label, code))
                } else {
                    context.copyToClipboard(code)
                }
            }
        }
    }
}
