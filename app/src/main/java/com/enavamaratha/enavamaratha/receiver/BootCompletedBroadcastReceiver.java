package  com.enavamaratha.enavamaratha.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import  com.enavamaratha.enavamaratha.service.RefreshService;
import  com.enavamaratha.enavamaratha.utils.PrefUtils;

public class BootCompletedBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PrefUtils.putLong(PrefUtils.LAST_SCHEDULED_REFRESH, 0);
        if (PrefUtils.getBoolean(PrefUtils.REFRESH_ENABLED, true)) {
            context.startService(new Intent(context, RefreshService.class));
        }
    }

}
