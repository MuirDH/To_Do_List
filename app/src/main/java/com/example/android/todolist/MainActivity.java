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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.android.todolist.data.TaskContract;
import com.example.android.todolist.data.TaskDbHelper;

import static com.example.android.todolist.data.TaskContract.TaskEntry.TABLE_NAME;


public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, PopupMenu.OnMenuItemClickListener {


    // Constants for logging and referring to a unique loader
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int TASK_LOADER_ID = 0;

    // Member variable for the adapter
    private CustomCursorAdapter mAdapter;

    private View emptyView;

    private String sortOrder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the RecyclerView to its corresponding view
        EmptyRecyclerView mRecyclerView = findViewById(R.id.recyclerViewTasks);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        emptyView = findViewById(R.id.empty_view);
        mRecyclerView.setEmptyView(emptyView);

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new CustomCursorAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        addTouchHelper(mRecyclerView);

        setFabClickListener();

        /*
         Ensure a loader is initialized and active. If the loader doesn't already exist, one is
         created, otherwise the last created loader is re-used.
         */
        getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file to add the menu item(s) to the app bar
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Return true to display menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Use a switch/case in the event that we need to add more menu options in future
        switch (item.getItemId()) {

            // Respond to a click on the "Share" menu option
            case R.id.menu_item_share:
                Intent shareIntent = getShareIntent();

                // Calling createChooser will always display the chooser so the user has an option
                // for which app to use to share their to do list
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share) ));
                return true;

            //Respond to a click on the "Sort" menu option
            case R.id.menu_item_sort:
                showPopup(emptyView);
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    private Intent getShareIntent() {
        Context context = getBaseContext();
        TaskDbHelper taskDbHelper = new TaskDbHelper(context);
        SQLiteDatabase database = taskDbHelper.getReadableDatabase();

        String toDoList = getTableAsString(database, TABLE_NAME);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, toDoList);
        shareIntent.setType("text/plain");
        return shareIntent;
    }

    private void showPopup(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);

        // This implements OnMenuItemClickListener
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.actions);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sortPriority:
                sortOrder = TaskContract.TaskEntry.COLUMN_PRIORITY;

                // restart the loader to re-query
                requeryLoader();
                return true;
            case R.id.sortId:
                sortOrder = TaskContract.TaskEntry._ID;
                requeryLoader();
                return true;
            default:
                return false;
        }
    }



    /**
     * Helper function that parses a given table into a string and returns it for easy printing.
     * The string consists of the table name and then each row is iterated through with column_name:
     * value pairs printed out.
     *
     * @param  database  The databse to get the table from
     * @param  tableName The name of the table to parse
     * @return           The table tableName as a string
     */

    public String getTableAsString(SQLiteDatabase database, String tableName) {
        Log.d(TAG, "getTableAsString called");
        StringBuilder tableString = new StringBuilder(String.format("Table %s:\n", tableName));
        Cursor allRows = database.rawQuery("SELECT * FROM " + tableName, null);

        goThroughList(tableString, allRows);
        allRows.close();

        return tableString.toString();
    }

    private void goThroughList(StringBuilder tableString, Cursor allRows) {
        if (allRows.moveToFirst() ){
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString.append(String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name))));
                }
                tableString.append("\n");
            }while (allRows.moveToNext());
        }
    }

    private void setFabClickListener() {
    /*
     Set the Floating Action Button (FAB) to its corresponding View.
     Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
     to launch the AddTaskActivity.
     */
        FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to start an AddTaskActivity
                Intent addTaskIntent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(addTaskIntent);
            }
        });
    }

    private void addTouchHelper(EmptyRecyclerView mRecyclerView) {
    /*
     Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
     An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
     and uses callbacks to signal when a user is performing these actions.
     */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            static final float ALPHA_FULL = 1.0f;

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                deleteItem(viewHolder);
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                // we only want the active item to change
                changeToSelectedBackgroundColor(viewHolder, actionState);
                super.onSelectedChanged(viewHolder, actionState);
            }

            private void changeToSelectedBackgroundColor(RecyclerView.ViewHolder viewHolder, int actionState) {
                if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                    viewHolder.itemView.setAlpha(ALPHA_FULL / 2);
                    viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                viewHolder.itemView.setAlpha(ALPHA_FULL);
                viewHolder.itemView.setBackgroundColor(0);
                super.clearView(recyclerView, viewHolder);
            }

        }).attachToRecyclerView(mRecyclerView);
    }

    private void deleteItem(RecyclerView.ViewHolder viewHolder) {
        // Construct the URI for the item to delete
        //[Hint] Use getTag (from the adapter code) to get the id of the swiped item
        // Retrieve the id of the task to delete
        int id = (int) viewHolder.itemView.getTag();

        // Build appropriate uri with String row id appended
        String stringId = Integer.toString(id);
        Uri uri = TaskContract.TaskEntry.CONTENT_URI;
        uri = uri.buildUpon().appendPath(stringId).build();

        // Delete a single row of data using a ContentResolver
        getContentResolver().delete(uri, null, null);

        // Restart the loader to re-query for all tasks after a deletion
        requeryLoader();
    }


    /**
     * This method is called after this activity has been paused or restarted.
     * Often, this is after new data has been inserted through an AddTaskActivity,
     * so this restarts the loader to re-query the underlying data for any changes.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // re-queries for all tasks
        requeryLoader();
    }

    private void requeryLoader() {
        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, MainActivity.this);
    }

    /**
     * Instantiates and returns a new AsyncTaskLoader with the given ID.
     * This loader will return task data as a Cursor or null if an error occurs.
     * <p>
     * Implements the required callbacks to take care of loading data at all stages of loading.
     */
    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {

        return new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor, this will hold all the task data
            Cursor mTaskData = null;

            // onStartLoading() is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                // Delivers any previously loaded data immediately
                if (mTaskData != null) deliverResult(mTaskData);
                else forceLoad(); // Force a new load
            }

            // loadInBackground() performs asynchronous loading of data
            @Override
            public Cursor loadInBackground() {
                // Will implement to load data

                // Query and load all task data in the background; sort by priority
                // [Hint] use a try/catch block to catch any errors in loading data

                try {
                    return getContentResolver().query(TaskContract.TaskEntry.CONTENT_URI,
                            null,
                            null,
                            null,
                            sortOrder);

                } catch (Exception e) {
                    Log.e(TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // deliverResult sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };

    }


    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the data that the adapter uses to create ViewHolders
        mAdapter.swapCursor(data);
    }


    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.
     * onLoaderReset removes any references this activity had to the loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }


}

