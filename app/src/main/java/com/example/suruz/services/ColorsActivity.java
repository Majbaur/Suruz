package com.example.suruz.services;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.color.R;

public class ColorsActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener{

    EditText editText_red, editText_green, editText_blue;
    Button generate;
    View screen;
    TextView hexValue, argbValue;
    SeekBar s_alpha, s_red, s_green, s_blue;
    Integer red = 0, green = 0, blue = 0, alpha = 100;

    @SuppressLint({"MissingInflatedId", "DefaultLocale"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colors);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            getSupportActionBar().setTitle("Generate Color");
        }

        editText_red = findViewById(R.id.edit_red);
        editText_green = findViewById(R.id.edit_green);
        editText_blue = findViewById(R.id.edit_blue);

        generate = findViewById(R.id.generate_btn);
        screen = findViewById(R.id.screen);

        hexValue = findViewById(R.id.hex_value);
        argbValue = findViewById(R.id.argb_value);

        s_alpha = findViewById(R.id.alpha_seekBar);
        s_red = findViewById(R.id.red_seekBar);
        s_green = findViewById(R.id.green_seekBar);
        s_blue = findViewById(R.id.blue_seekBar);

        generate.setOnClickListener(v -> {
            red = Integer.parseInt(editText_red.getText().toString());
            green = Integer.parseInt(editText_green.getText().toString());
            blue = Integer.parseInt(editText_blue.getText().toString());

            screen.setBackgroundColor(Color.argb(100, red, green, blue));
            String code = HexCode(100, red, green, blue);
            hexValue.setText(code);
            argbValue.setText(String.format("(%d, %d, %d, %d)", 100, red, green, blue));
        });

        s_alpha.setOnSeekBarChangeListener(this);
        s_red.setOnSeekBarChangeListener(this);
        s_green.setOnSeekBarChangeListener(this);
        s_blue.setOnSeekBarChangeListener(this);
    }

    @SuppressLint({"NonConstantResourceId", "DefaultLocale"})
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.alpha_seekBar:
                alpha = progress;
                break;
            case R.id.red_seekBar:
                red = progress;
                break;
            case R.id.green_seekBar:
                green = progress;
                break;
            case R.id.blue_seekBar:
                blue = progress;
                break;
        }
        screen.setBackgroundColor(Color.argb(alpha, red, green, blue));
        String code = HexCode(alpha, red, green, blue);
        hexValue.setText(code);
        argbValue.setText(String.format("(%d, %d, %d, %d)", 100, red, green, blue));
    }

    private String HexCode(Integer alpha, Integer red, Integer green, Integer blue) {
        String code;
        code = "#";
        code += Integer.toHexString(alpha);
        code += Integer.toHexString(red);
        code += Integer.toHexString(green);
        code += Integer.toHexString(blue);

        return code;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}