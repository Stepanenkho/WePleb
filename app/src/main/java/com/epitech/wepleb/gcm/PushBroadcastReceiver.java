package com.epitech.wepleb.gcm;

import android.content.Context;
import android.content.Intent;

import com.epitech.wepleb.activities.MainActivity;
import com.epitech.wepleb.events.NewMessageEvent;
import com.epitech.wepleb.utils.LifecycleHandler;
import com.parse.ParsePushBroadcastReceiver;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

public class PushBroadcastReceiver extends ParsePushBroadcastReceiver {

    @Override
    protected void onPushReceive(final Context context, final Intent intent) {
        if (LifecycleHandler.isApplicationInForeground()) {
            try {
                JSONObject pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));

                String type = pushData.getString("t");
                if (type != null) {
                    switch (type) {
                        case "message": {
                            String conversationId = pushData.getString("c");
                            if (conversationId != null) {
                                EventBus.getDefault().post(new NewMessageEvent(conversationId, pushData.getString("alert")));
                            }
                            break;
                        }
                        default:
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            super.onPushReceive(context, intent);
        }

    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        try {
            JSONObject pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));

            String type = pushData.getString("t");
            if (type != null) {
                switch (type) {
                    case "message": {
                        String discussionId = pushData.getString("c");
                        final Intent intentDispatch = new Intent(context, MainActivity.class);
                        //intentDispatch.putExtra(MainActivity.EXTRA_DISPATCH_CHAT, discussionId);
                        intentDispatch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intentDispatch);
                        break;
                    }
                    default:
                        super.onPushOpen(context, intent);
                        break;
                }
            } else {
                super.onPushOpen(context, intent);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            super.onPushOpen(context, intent);
        }
    }
}
