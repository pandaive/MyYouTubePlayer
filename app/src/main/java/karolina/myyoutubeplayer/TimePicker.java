package karolina.myyoutubeplayer;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TimePicker extends AppCompatActivity {

    private Integer minutes, seconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_picker);

        final Intent intent = new Intent(this, CategoryPicker.class);
        final Button timePickButton = (Button) findViewById(R.id.timePickerButton);

        timePickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog;
                timePickerDialog = new TimePickerDialog(view.getContext(), TimePickerDialog.THEME_HOLO_DARK,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(android.widget.TimePicker view, int hourOfDay,
                                                  int minute) {

                                timePickButton.setText(createTimeForButton(hourOfDay,minute));
                                minutes = hourOfDay;
                                seconds = minute;
                            }
                        }, 0, 0, true);
                timePickerDialog.show();
            }
        });

        final Button nextButton = (Button) findViewById(R.id.timePickerNextButton);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setAction(Intent.ACTION_SEND);
                intent.putExtra(CategoryPicker.EXTRA_MINUTES, minutes);
                intent.putExtra(CategoryPicker.EXTRA_SECONDS, seconds);

                startActivity(intent);
            }
        });

    }

    private String createTimeForButton(int minutes, int seconds){
        StringBuilder builder = new StringBuilder();
        if(minutes < 10){
            builder.append("0");
        }
        builder.append(minutes + " : ");
        if(seconds<10){
            builder.append("0");
        }
        builder.append(seconds);

        return builder.toString();
    }
}
