package com.example.windose;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Locale;

public class NoteEditor extends AppCompatActivity {

    static EditText description;
    int noteId;
    String text,path;
    private static final int WRITE_EXTERNAL_STORAGE_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        getSupportActionBar().setTitle("Notes");

        description = (EditText) findViewById(R.id.descriptions);

        Intent intent = getIntent();
        noteId = intent.getIntExtra("noteId", -1);

        if(noteId != -1){
            description.setText(Home.notes.get(noteId));
        }
        else{
            Home.notes.add("");
            noteId = Home.notes.size() -1;
            Home.arrayAdapter.notifyDataSetChanged();
        }

        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                Home.notes.set(noteId, String.valueOf(charSequence));
                Home.arrayAdapter.notifyDataSetChanged();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.windose",
                        Context.MODE_PRIVATE);
                HashSet<String> set = new HashSet(Home.notes);
                sharedPreferences.edit().putStringSet("notes",set).apply();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        path = getFilesDir().getAbsolutePath();
        File folder = new File(path);
        folder.mkdir();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.menu:
            Intent home = new Intent(getApplicationContext(),Home.class);
            startActivity(home);
            return true;

            case R.id.newnote:
            Intent newNote = new Intent(getApplicationContext(),NoteEditor.class);
            startActivity(newNote);
            return true;

            case R.id.save:

            String str = description.getText().toString();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
            String filename = "Note_" + timestamp;
            text = description.getText().toString().trim();
            if(text.isEmpty()){
                Toast.makeText(NoteEditor.this,"Please enter something...",Toast.LENGTH_LONG).show();
            }else{
                try {
                    FileWriter myFileWriter = new FileWriter(path+filename+".txt");
                    myFileWriter.write(str);
                    Toast.makeText(NoteEditor.this,"Saved to" + path,Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

                return true;

            case R.id.exportsd:
                text = description.getText().toString().trim();
                if(text.isEmpty()){
                    Toast.makeText(NoteEditor.this,"Please enter something...",Toast.LENGTH_LONG).show();
                }else{
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_DENIED){
                            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(permissions, WRITE_EXTERNAL_STORAGE_CODE);
                        }
                        else{
                            saveToTextFile(text);
                        }
                    }
                    else{
                        saveToTextFile(text);
                    }
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public void saveToTextFile(String text){

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
    try {
        File path = Environment.getExternalStorageDirectory();
        File dir = new File(path + "/Notes/");
        dir.mkdir();

        String filename = "Note_" + timestamp + ".txt";

        File file = new File(dir,filename);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());

        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(text);
        bw.close();

        Toast.makeText(NoteEditor.this,filename + " is saved to\n" + dir,Toast.LENGTH_LONG).show();

    }catch (Exception e){

        Toast.makeText(NoteEditor.this,e.getMessage(),Toast.LENGTH_LONG).show();

    }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveToTextFile(text);
                } else {
                    Toast.makeText(NoteEditor.this, "Storage permission is required", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        Intent home = new Intent(getApplicationContext(),Home.class);
        startActivity(home);
        super.onBackPressed();
    }
}