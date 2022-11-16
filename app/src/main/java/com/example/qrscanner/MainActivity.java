package com.example.qrscanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btnScan, sendEmailButton, saveToFileButton, resetButton;
    TextView txtResult;
    List<String> parts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.btnScan = findViewById(R.id.btnScan);
        this.sendEmailButton = findViewById(R.id.send_email_button);
        this.sendEmailButton.setOnClickListener(view -> displayEmailDialog());
        this.saveToFileButton = findViewById(R.id.save_to_file_button);
        // TODO: 11/11/2022 change
        this.saveToFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveToFile();
            }
        });
        this.resetButton = findViewById(R.id.reset_button);
        this.resetButton.setOnClickListener((view) -> reset());
        this.txtResult = findViewById(R.id.txt1);
        this.btnScan.setOnClickListener(view -> scanCode());
        reset();
    }

    private void reset() {
        this.parts = new ArrayList<>();
        this.sendEmailButton.setEnabled(false);
        this.saveToFileButton.setEnabled(false);
        this.txtResult.setText("");
    }

    private void displayEmailDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Enter your email");
        final EditText email = new EditText(this);
        email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        email.setHint("Email...");
        email.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        alert.setView(email);
        alert.setPositiveButton("Ok", (dialogInterface, i) -> sendEmail(email.getText().toString()));
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    private void sendEmail(String to) {
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
        email.putExtra(Intent.EXTRA_SUBJECT, "QR Code");
        email.putExtra(Intent.EXTRA_TEXT, getResult());
        email.setType("message/rfc822");
        startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }

    private String getResult() {
        String str = "";
        for (String part : this.parts) {
            str += part;
        }
        return str;
    }

    public void scanCode() {
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setPrompt("Volume up to flash on");
        scanOptions.setBeepEnabled(true);
        scanOptions.setOrientationLocked(true);
        scanOptions.setCaptureActivity(CaptureAct.class);
        scanOptions.setOrientationLocked(true);
        barLauncher.launch(scanOptions);
    }

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(String.format("Result - Part %s", MainActivity.this.parts.size() + 1));
            builder.setMessage(result.getContents());
            builder.setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.parts.add(result.getContents());
                    txtResult.setText(String.format("there are %s parts", MainActivity.this.parts.size()));
                    String content = txtResult.getText().toString();
                    //saveToFile();
//                    writeToFile("file.txt", content);
                    displayButtons();
                    //Toast.makeText(MainActivity.this, "file saved", Toast.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                }
            }).setNeutralButton("Retry Part", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    scanCode();
                }
            }).setNegativeButton("Next", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    scanCode();
                    MainActivity.this.parts.add(result.getContents());
                }
            }).show();
        }
    });

    private void saveToFile() {
        byte[] bytes = Base64.decode(getResult(), Base64.DEFAULT);
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        final EditText name = new EditText(this);
        name.setInputType(InputType.TYPE_CLASS_TEXT);
        name.setHint("Name...");
        name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        alert.setView(name);
        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                writeToFile(name.getText().toString()+".png", bytes);
            }
        }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    private void displayButtons() {
        this.sendEmailButton.setEnabled(true);
        this.saveToFileButton.setEnabled(true);
    }

    private void writeToFile(String fileName, byte[] bytes) {
        try {
            File path = getExternalFilesDir("DOWNLOAD");
            FileOutputStream writer = new FileOutputStream(new File(path, fileName));
            writer.write(bytes);
            writer.close();
            Toast.makeText(getApplicationContext(), "The file " + fileName + " is in " + path, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}