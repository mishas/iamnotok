package com.google.imnotok;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class EmergencyButtonWidgetProvider extends AppWidgetProvider {

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    final int numWidgets = appWidgetIds.length;
    
    for (int i = 0; i < numWidgets; i++) {
      int appWidgetId = appWidgetIds[i];
      
      // Create intent to launch the EmergencyNotificationService
      Intent intent = new Intent(context, EmergencyNotificationService.class);
      intent.putExtra(EmergencyNotificationService.SHOW_NOTIFICATION_WITH_DISABLE, true);
      PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

      // Get the remote views and set the pending intent for the emergency button.
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.emergency_button_widget);
      views.setOnClickPendingIntent(R.id.EmergencyButton, pendingIntent);
      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    
    super.onUpdate(context, appWidgetManager, appWidgetIds);
  }

}
