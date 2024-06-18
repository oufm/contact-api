package com.oufme.contact_api;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //new ContactHelper(this).addContact("test", "1234567890");
        setContentView(R.layout.activity_main);
    }
}
