package com.maks_kliuba.rotarydialer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class NumberActivity extends MainActivity {

    ImgButton[] numPad = new ImgButton[12];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number);

        setResources(this);
    }

    @Override
    public void setResources(Activity activity) {
        super.setResources(activity);

        styleButton = new ImgButton(activity, (ImageView) findViewById(R.id.style_button), ImgButton.Type.ACTION,"toRotaryActivity");
        for(int i = 0; i < numPad.length; i++)
        {
            switch (i)
            {
                case 0: numPad[i] = new ImgButton(this, (ImageView) findViewById(R.id.button_0_plus), ImgButton.Type.NUMBER, i + "", "+"); break;
                case 1: numPad[i] = new ImgButton(this, (ImageView) findViewById(R.id.button_1), ImgButton.Type.NUMBER, i + ""); break;
                case 2: numPad[i] = new ImgButton(this, (ImageView) findViewById(R.id.button_2), ImgButton.Type.NUMBER, i + ""); break;
                case 3: numPad[i] = new ImgButton(this, (ImageView) findViewById(R.id.button_3), ImgButton.Type.NUMBER, i + ""); break;
                case 4: numPad[i] = new ImgButton(this, (ImageView) findViewById(R.id.button_4), ImgButton.Type.NUMBER,i + ""); break;
                case 5: numPad[i] = new ImgButton(this, (ImageView) findViewById(R.id.button_5), ImgButton.Type.NUMBER, i + ""); break;
                case 6: numPad[i] = new ImgButton(this, (ImageView) findViewById(R.id.button_6), ImgButton.Type.NUMBER, i + ""); break;
                case 7: numPad[i] = new ImgButton(this, (ImageView) findViewById(R.id.button_7), ImgButton.Type.NUMBER, i + ""); break;
                case 8: numPad[i] = new ImgButton(this, (ImageView) findViewById(R.id.button_8), ImgButton.Type.NUMBER, i + ""); break;
                case 9: numPad[i] = new ImgButton(this, (ImageView) findViewById(R.id.button_9), ImgButton.Type.NUMBER, i + ""); break;
                case 10: numPad[i] = new ImgButton(this, (ImageView) findViewById(R.id.button_star), ImgButton.Type.NUMBER, "*"); break;
                case 11: numPad[i] = new ImgButton(this, (ImageView) findViewById(R.id.button_sharp), ImgButton.Type.NUMBER, "#"); break;
            }
        }
    }
}
