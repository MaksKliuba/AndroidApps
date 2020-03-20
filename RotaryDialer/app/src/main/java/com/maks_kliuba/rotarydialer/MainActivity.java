package com.maks_kliuba.rotarydialer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{

    final String LOG_TAG = "DIAL_LOG";

    ActivityService activityService;
    int soundIdActionClick, soundIdCallClick, soundIdNumClick, soundIdStyleClick;

    String chars = "1234567890*+#";
    ClipboardManager clipboardManager;
    ClipData clipData;

    public static final int REQUEST_CALL = 1;
    public static final int PICK_CONTACT = 1;

    EditText display;

    final static int MENU_COPY = 1;
    final static int MENU_PASTE = 2;

    ImgButton backspace;
    ImgButton callButton;
    ImgButton styleButton;
    ImgButton contacts;
    ImgButton addContact;
    ImgButton sendMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
    }

    public void setResources(Activity activity)
    {
        activityService = new ActivityService(activity);
        clipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);

        soundIdActionClick = R.raw.action_click;
        soundIdCallClick = R.raw.call_click;
        soundIdNumClick = R.raw.num_click;
        soundIdStyleClick = R.raw.style_click;

        display = (EditText) findViewById(R.id.display);
        registerForContextMenu(display);
        //display.setMovementMethod(new ScrollingMovementMethod());

        backspace = new ImgButton(activity, (ImageView) findViewById(R.id.backspace), ImgButton.Type.ACTION, "backspace", "clear");
        callButton = new ImgButton(activity, (ImageView) findViewById(R.id.call_button), ImgButton.Type.ACTION,"call");
        styleButton = new ImgButton(activity, (ImageView) findViewById(R.id.style_button), ImgButton.Type.ACTION,"style");
        contacts = new ImgButton(activity, (ImageView) findViewById(R.id.contacts_button), ImgButton.Type.ACTION,"contacts");
        addContact = new ImgButton(activity, (ImageView) findViewById(R.id.add_contact_button), ImgButton.Type.ACTION,"add");
        sendMessage = new ImgButton(activity, (ImageView) findViewById(R.id.send_message_button), ImgButton.Type.ACTION,"send");

        checkIntent();
    }

    public void checkIntent()
    {
        Intent intent = getIntent();

        if(intent != null && intent.getStringExtra("phoneNumber") != null)
            displaySetText(intent.getStringExtra("phoneNumber"));
        else
            displaySetText(display.getText().toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        finish();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        //super.onCreateContextMenu(menu, v, menuInfo);

        switch (v.getId())
        {
            case R.id.display:
                menu.add(0, MENU_COPY, 0, "Copy");
                menu.add(0, MENU_PASTE, 0, "Paste");
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case MENU_COPY:
                clipData = ClipData.newPlainText("text", display.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(),"Text Copied",Toast.LENGTH_SHORT).show();
                break;
            case MENU_PASTE:
                ClipData data = clipboardManager.getPrimaryClip();
                ClipData.Item itemData = data.getItemAt(0);
                displaySetText(checkText(itemData.getText().toString()));
                Toast.makeText(getApplicationContext(),"Text Pasted",Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onContextItemSelected(item);
    }

    String checkText(String inputText)
    {
        String outputText = "";

        for (int i = 0; i < inputText.length(); i++)
        {
            if(chars.contains(inputText.charAt(i) + ""))
                outputText += inputText.charAt(i);
        }

        return outputText;
    }

    void displaySetText(String text)
    {
        display.setText(text);

        display.setSelection(display.getText().length());
    }

    public void makeCall()
    {
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        else
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(display.getText().toString()))));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CALL)
        {
            if(grantResults.length> 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                makeCall();
        }
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data)
    {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode)
        {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri contctDataVar = data.getData();

                    Cursor contctCursorVar = getContentResolver().query(contctDataVar, null,
                            null, null, null);
                    if (contctCursorVar.getCount() > 0)
                    {
                        while (contctCursorVar.moveToNext())
                        {
                            if (Integer.parseInt(contctCursorVar.getString(contctCursorVar.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                                displaySetText(checkText(contctCursorVar.getString(contctCursorVar.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));
                        }
                    }
                }
                break;
        }
    }

    public void analyzeButton(ImgButton imgButton, boolean flag)
    {
        switch (imgButton.type)
        {
            case ACTION:

                if(imgButton == backspace) {

                    activityService.click(soundIdActionClick);

                    String phoneNumber = display.getText().toString();

                    if(!flag && phoneNumber.length() > 0)
                        phoneNumber = phoneNumber.substring(0, phoneNumber.length() - 1);
                    else
                        phoneNumber = "";

                    displaySetText(phoneNumber);
                }
                else if(imgButton == callButton)
                {
                    activityService.click(soundIdCallClick);
                    makeCall();
                }
                else if(imgButton == contacts)
                {
                    activityService.click(soundIdActionClick);

                    Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                    intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                    startActivityForResult(intent, PICK_CONTACT);
                }
                else if(imgButton == addContact)
                {
                    activityService.click(soundIdActionClick);

                    Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                    intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, display.getText().toString());
                    startActivity(intent);
                }
                else if(imgButton == sendMessage)
                {
                    activityService.click(soundIdActionClick);

                    Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                    smsIntent.setData(Uri.parse("sms:" + Uri.encode(display.getText().toString())));
                    startActivity(smsIntent);
                }
                else if(imgButton == styleButton)
                {
                    activityService.click(soundIdStyleClick);
                    Intent intent;

                    if(imgButton.code.equals("toRotaryActivity"))
                        intent = new Intent(this, RotaryActivity.class);
                    else if(imgButton.code.equals("toNumberActivity"))
                        intent = new Intent(this, NumberActivity.class);
                    else
                        intent = new Intent(this, MainActivity.class);

                    intent.putExtra("phoneNumber", display.getText().toString());
                    startActivity(intent);

                    finish();
                }
                break;
            case NUMBER:

                activityService.click(soundIdNumClick);

                String phoneNumber = display.getText().toString();

                if(flag) {
                    if(imgButton.code2 != null) {
                        phoneNumber = phoneNumber.substring(0, phoneNumber.length() - 1);
                        phoneNumber += imgButton.code2;
                    }
                }
                else
                    phoneNumber += imgButton.code;

                displaySetText(phoneNumber);

                break;
        }
    }
}