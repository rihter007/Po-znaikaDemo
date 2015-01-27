package ru.po_znaika.alphabet;

import java.io.IOException;
import java.lang.String;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.TreeMap;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import ru.po_znaika.common.IExercise;
import ru.po_znaika.database.alphabet.AlphabetDatabase;

public class MainMenuActivity extends ActionBarActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        try
        {
            restoreInternalState(savedInstanceState);
            constructUserInterface();
        }
        catch (Exception exp)
        {
            MessageBox.Show(this, exp.getMessage(), exp.getMessage());
        }
    }

    void restoreInternalState(Bundle savedInstanceState) throws IOException
    {
        m_alphabetDatabase = new AlphabetDatabase(this, false);
        m_exerciseFactory = new ExerciseFactory(this, m_alphabetDatabase);

        // contains processed exercises in sorted order
        Map<String, ArrayList<IExercise>> collectedExercises = new TreeMap<String, ArrayList<IExercise>>();

        // contains all exercises
        AlphabetDatabase.ExerciseShortInfo[] exercisesShortInfo = m_alphabetDatabase.getAllExercisesShortInfoNotByType(AlphabetDatabase.ExerciseType.Character);
        if (exercisesShortInfo != null)
        {
            for (AlphabetDatabase.ExerciseShortInfo exerciseInfo : exercisesShortInfo)
            {
                IExercise exercise = m_exerciseFactory.CreateExerciseFromId(exerciseInfo.id, exerciseInfo.type);
                if (exercise != null)
                {
                    // Set tracer

                    final String exerciseDisplayName = exercise.getDisplayName();
                    // Add exercise to list
                    ArrayList<IExercise> displayNameExercises = null;
                    if (collectedExercises.containsKey(exerciseDisplayName))
                    {
                        displayNameExercises = collectedExercises.get(exerciseDisplayName);
                    }
                    else
                    {
                        displayNameExercises = new ArrayList<IExercise>();
                        collectedExercises.put(exerciseDisplayName, displayNameExercises);
                    }

                    displayNameExercises.add(exercise);
                }
            }
        }

        // Place exercises in sorted order
        m_menuExercises = new ArrayList<IExercise>();
        for (Map.Entry<String, ArrayList<IExercise>> sortedExercise : collectedExercises.entrySet())
        {
            m_menuExercises.addAll(sortedExercise.getValue());
        }
    }

    private void constructUserInterface()
    {
        ListView uiListView = (ListView) findViewById(R.id.menuListView);
        uiListView.setAdapter(null);

       ImageTextAdapter listViewItemsAdapter = new ImageTextAdapter(this, R.layout.large_image_menu_item);

        // add first element - alphabet
        {
            Resources resources = getResources();
            listViewItemsAdapter.add(resources.getDrawable(R.drawable.alphabet), resources.getString(R.string.caption_abc_book));
        }

        for (IExercise currentExercise : m_menuExercises)
        {
            listViewItemsAdapter.add(currentExercise.getDisplayImage(), currentExercise.getDisplayName());
        }

        uiListView.setAdapter(listViewItemsAdapter);
        uiListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long rowId)
            {
                onListViewItemSelected((int)rowId);
            }
        });
    }

    private void onListViewItemSelected(int itemSelectedIndex)
    {
        try
        {
            if (itemSelectedIndex == 0)
            {
                // ABC-book is selected
                Intent intent = new Intent(this, CharacterExerciseMenuActivity.class);
                startActivity(intent);
            }
            else
            {
                m_menuExercises.get(itemSelectedIndex - 1).process();
            }
        }
        catch (Exception exp)
        {
            Resources resources = getResources();
            MessageBox.Show(this, resources.getString(R.string.failed_exercise_start), resources.getString(R.string.alert_title));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        final int SelectedItemId = item.getItemId();
        switch (SelectedItemId)
        {
            case R.id.action_diary:
                Intent intent = new Intent(this, DiaryActivity.class);
                startActivity(intent);
                break;

            case R.id.action_about:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private AlphabetDatabase m_alphabetDatabase;
    private ExerciseFactory m_exerciseFactory;

    private List<IExercise> m_menuExercises;
}