package com.oufme.contact_api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ContactReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("name");
        String number = intent.getStringExtra("number");
        String operation = intent.getStringExtra("operation");

        Log.d("ContactReceiver", "Received values: name = " + name +
                ", number = " + number + ", operation = " + operation);

        if (operation.equals("add")) {
            new ContactHelper(context).addContact(name, number);
        } else if (operation.equals("remove")) {
            new ContactHelper(context).removeContact(name);
        }
    }
}
