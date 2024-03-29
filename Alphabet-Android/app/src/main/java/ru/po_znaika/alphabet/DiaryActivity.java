package ru.po_znaika.alphabet;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.android.AlertDialogHelper;

import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.alphabet.database.diary.DiaryDatabase;
import ru.po_znaika.common.ExerciseScore;
import ru.po_znaika.common.ru.po_znaika.common.helpers.CommonHelpers;
import com.arz_x.NetworkException;

public final class DiaryActivity extends ActionBarActivity
{
    private static final String LogTag = DiaryActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        m_serviceLocator = new CoreServiceLocator(this);
    }

    @Override
    protected void onDestroy()
    {
        new CloseAsyncTask(m_serviceLocator).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.diary, menu);

        try
        {
            constructUserInterface();
        }
        catch (Exception exp)
        {
            return false;
        }
        return true;
    }

    private void constructUserInterface() throws CommonException
    {
        TextAdapter textAdapter;

        // fill grid caption
        {
            final Resources resources = getResources();

            textAdapter = new TextAdapter(this, resources.getDimension(R.dimen.small_text_size), TextAdapter.TextAlign.Center);
            textAdapter.add(resources.getString(R.string.caption_date));
            textAdapter.add(resources.getString(R.string.caption_exercise_name));
            textAdapter.add(resources.getString(R.string.caption_exercise_score));
        }

        AlphabetDatabase alphabetDatabase = new AlphabetDatabase(this, false);

        DiaryDatabase diaryDatabase = m_serviceLocator.getDiaryDatabase();
        ExerciseScore diaryNotes[] = diaryDatabase.getAllDiaryRecordsOrderedByDate();

        if ((diaryNotes != null) && (diaryNotes.length > 0))
        {
            Map<String, String> exerciseNamesMap = new HashMap<>();
            for (ExerciseScore diaryNote : diaryNotes)
            {
                Calendar noteTime = new GregorianCalendar();
                noteTime.setTime(diaryNote.date);

                textAdapter.add(String.format("%02d.%02d.%d %02d:%02d",
                        noteTime.get(Calendar.DAY_OF_MONTH),
                        noteTime.get(Calendar.MONTH) + 1,
                        noteTime.get(Calendar.YEAR),
                        noteTime.get(Calendar.HOUR_OF_DAY),
                        noteTime.get(Calendar.MINUTE)
                ));

                String exerciseTitle;
                if (exerciseNamesMap.containsKey(diaryNote.exerciseName))
                {
                    exerciseTitle = exerciseNamesMap.get(diaryNote.exerciseName);
                }
                else
                {
                    exerciseTitle = null;//alphabetDatabase.getExerciseDisplayNameByIdName(diaryNote.exerciseName);
                    if (exerciseTitle == null)
                        exerciseTitle = alphabetDatabase.getCharacterItemDisplayNameByIdName(diaryNote.exerciseName);
                    exerciseNamesMap.put(diaryNote.exerciseName, exerciseTitle);
                }

                if (!TextUtils.isEmpty(exerciseTitle))
                    textAdapter.add(exerciseTitle);
                else
                    textAdapter.add("");

                textAdapter.add(((Integer)diaryNote.score).toString());
            }
        }

        GridView gridView = (GridView)findViewById(R.id.gridView);
        gridView.setAdapter(textAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        final int ItemId = item.getItemId();
        switch (ItemId)
        {
            case R.id.action_synchronize:
            {
                final Resources resources = getResources();

                View dialogView = getLayoutInflater().inflate(R.layout.dialog_diary_synchronization, null);
                final TextView selectedDaysView = (TextView)dialogView.findViewById(R.id.daysSelectedTextView);
                final SeekBar seekBarView = (SeekBar)dialogView.findViewById(R.id.seekBar);
                seekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
                {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                    {
                        final String captionString = String.format(resources.getString(R.string.caption_days_selected), progress);
                        selectedDaysView.setText(captionString);
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar)
                    {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar)
                    {

                    }
                });

                // Draw initial progress
                {
                    final String initialCaptionString = String.format(resources.getString(R.string.caption_days_selected), seekBarView.getProgress());
                    selectedDaysView.setText(initialCaptionString);
                }

                AlertDialogHelper.showAlertDialog(this, dialogView, resources.getString(R.string.ok)
                        , resources.getString(R.string.caption_cancel)
                        , new AlertDialogHelper.IDialogResultListener()
                {
                    @Override
                    public void onDialogProcessed(@NonNull AlertDialogHelper.DialogResult dialogResult)
                    {
                        if (dialogResult == AlertDialogHelper.DialogResult.NegativeSelected)
                            return;

                        Calendar minExerciseRecordDate = Calendar.getInstance();
                        minExerciseRecordDate.setTime(new Date());
                        minExerciseRecordDate.add(Calendar.DATE, -seekBarView.getProgress());

                        DiaryDatabase diaryDatabase = m_serviceLocator.getDiaryDatabase();
                        try
                        {
                            ExerciseScore[] syncExercises = m_serviceLocator.getServerOperations()
                                    .getExercisesScores("Alphabet.Russian",
                                            CommonHelpers.beginOfTheDay(minExerciseRecordDate.getTime()),
                                            null);

                            // incorrect realization
                            if (syncExercises == null)
                                throw new CommonException(CommonResultCode.InvalidInternalState);

                            for (ExerciseScore exercise : syncExercises)
                            {
                                diaryDatabase.insertExerciseScore(exercise.date, exercise.exerciseName, exercise.score);
                            }
                        }
                        catch (CommonException | NetworkException exp)
                        {
                            AlertDialogHelper.showMessageBox(DiaryActivity.this,
                                    resources.getString(R.string.alert_title),
                                    resources.getString(R.string.failed_sync_diary));
                            return;
                        }

                        diaryDatabase.deleteRecordsOlderThan(minExerciseRecordDate.getTime());
                    }
                });
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private CoreServiceLocator m_serviceLocator;
}
