/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.todolist;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.android.todolist.data.TaskContract;


public class AddTaskActivity extends AppCompatActivity {

    // Declare a member variable to keep track of a task's selected mPriority
    private int mPriority;

    // extras from when user edits a task
    private Bundle extras;
    private EditText textGoesHere;
    private Boolean isTextToBeEdited;
    private ContentValues contentValues;
    private int item;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        Intent editIntent = getIntent();
        extras = editIntent.getExtras();
        // where the text from the clicked list item goes
        textGoesHere = findViewById(R.id.editTextTaskDescription);

        // Initialize to highest mPriority by default (mPriority = 1)
        ((RadioButton) findViewById(R.id.radButton1)).setChecked(true);
        mPriority = 1;

        // Initialise boolean to false
        isTextToBeEdited = false;

        // insert the text in the clicked on item, if applicable
        insertTextToBeEdited();


    }

    /**
     * This method checks if there is any text to be added to the EditText field (i.e. if the user
     * clicked on a To-Do list item). If there is, then it adds the text so the user can edit it.
     */
    private void insertTextToBeEdited() {
        // this is an existing task and we need to update it.
        if (extras != null) {
            isTextToBeEdited = true;
            String textToBeEdited = (String) extras.get("Edit");
            item = (int) extras.get("ItemId");
            textGoesHere.setText(textToBeEdited);

        }
    }

    /**
     * onClickAddTask is called when the "ADD" button is clicked.
     * It retrieves user input and inserts that new task data into the underlying database.
     */
    public void onClickAddTask(View view) {
        // Check if EditText is empty, if not retrieve input and store it in a ContentValues object
        // If the EditText input is empty -> don't create an entry
        String input = textGoesHere.getText().toString();

        if (input.length() == 0) return;

        addOrEditTask(input);

        // Finish activity (this returns back to MainActivity)
        finish();

    }

    private void addOrEditTask(String input) {
        Uri uri;
        if (isTextToBeEdited) {
            // the user clicked on a To-Do list item, so we need to update the contentValues instead
            // of creating a new object

            setContentValues(input);
            getContentResolver().update(Uri.parse(TaskContract.TaskEntry.CONTENT_URI + "/" + item), contentValues,
                    TaskContract.TaskEntry._ID + "=?", new String[]{String.valueOf(item)});

        } else {
            // Insert new task data via a ContentResolver
            // Create new empty ContentValues object
            setContentValues(input);

            // Insert the content values via a ContentResolver
            uri = getContentResolver().insert(TaskContract.TaskEntry.CONTENT_URI, contentValues);

            // Display the URI that's returned with a Toast
            // [Hint] Don't forget to call finish() to return to MainActivity after this insert is complete
            sendToast(uri);
        }
    }

    private void sendToast(Uri uri) {
        if (uri != null) {
            //Toast.makeText(getBaseContext(), uri.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(getBaseContext(), "Task saved with priority level "
                    + mPriority, Toast.LENGTH_LONG).show();
        }
    }

    private void setContentValues(String input) {
        contentValues = new ContentValues();
        contentValues.put(TaskContract.TaskEntry.COLUMN_DESCRIPTION, input);
        contentValues.put(TaskContract.TaskEntry.COLUMN_PRIORITY, mPriority);
    }


    /**
     * onPrioritySelected is called whenever a priority button is clicked.
     * It changes the value of mPriority based on the selected button.
     */
    public void onPrioritySelected(View view) {
        if (((RadioButton) findViewById(R.id.radButton1)).isChecked()) mPriority = 1;
        else if (((RadioButton) findViewById(R.id.radButton2)).isChecked()) mPriority = 2;
        else if (((RadioButton) findViewById(R.id.radButton3)).isChecked()) mPriority = 3;
    }
}
