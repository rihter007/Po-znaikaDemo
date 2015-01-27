package ru.po_znaika.alphabet;

import java.lang.String;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.database.DatabaseConstant;
import ru.po_znaika.database.alphabet.AlphabetDatabase;

/**
 * Describes theory page
 */
public class TheoryPageFragment extends Fragment
{
    private static final String TheoryTableIndexTag = "theory_table_index";

    private static final String TheoryImageIndexTag = "theory_image_index";
    private static final String TheorySoundIndexTag = "theory_sound_index";
    private static final String TheoryMessageTag = "theory_message";

    public static TheoryPageFragment createFragment(CharacterExerciseItemStepState state)
    {
        assert state != null;

        TheoryPageFragment pageFragment = new TheoryPageFragment();
        Bundle fragmentArguments = new Bundle();
        fragmentArguments.putInt(TheoryTableIndexTag, state.value);
        pageFragment.setArguments(fragmentArguments);

        return pageFragment;
    }

    /**
     * Restores all internal objects
     * @param savedInstanceState activity saved state
     * @throws java.io.IOException
    */
    void restoreInternalState(Bundle savedInstanceState) throws IOException
    {
        m_alphabetDatabase = new AlphabetDatabase(getActivity(), false);

        Bundle fragmentArguments = getArguments();
        assert fragmentArguments != null;
        final Integer TheoryTableIndex = fragmentArguments.getInt(TheoryTableIndexTag);

        // Restore essential internal members
        if (savedInstanceState == null)
        {
            AlphabetDatabase.TheoryPageInfo theoryPageInfo = m_alphabetDatabase.getTheoryPageById(TheoryTableIndex);
            m_theoryImageId = theoryPageInfo.imageId;
            m_theorySoundId = theoryPageInfo.soundId;
            m_theoryMessage = theoryPageInfo.message;
        }
        else
        {
            m_theoryImageId = savedInstanceState.getInt(TheoryImageIndexTag);
            m_theorySoundId = savedInstanceState.getInt(TheorySoundIndexTag);
            m_theoryMessage = savedInstanceState.getString(TheoryMessageTag);
        }
    }

    /**
     * Constructs parts of user interface
     */
    void constructUserInterface(View fragmentView)
    {
        if ((m_theoryImageId == DatabaseConstant.InvalidDatabaseIndex) && (m_theorySoundId == DatabaseConstant.InvalidDatabaseIndex) &&
                (TextUtils.isEmpty(m_theoryMessage)))
            throw new IllegalArgumentException(); // TODO: fatal

        // process image
        if (m_theoryImageId != DatabaseConstant.InvalidDatabaseIndex)
        {
            Resources resources = getResources();

            final String ResourceFileName = m_alphabetDatabase.getImageFileNameById(m_theoryImageId);
            final int ImageResourceId = resources.getIdentifier(ResourceFileName, Constant.DrawableResourcesTag, getActivity().getPackageName());
            if (ImageResourceId == 0)
                throw new IllegalArgumentException();

            ImageView theoryImageView = (ImageView) fragmentView.findViewById(R.id.theoryImageView);
            theoryImageView.setImageDrawable(resources.getDrawable(ImageResourceId));
        }
        else
        {
            ImageView theoryImageView = (ImageView)fragmentView.findViewById(R.id.theoryImageView);
            theoryImageView.setVisibility(View.INVISIBLE);

            ViewGroup.LayoutParams layoutParams = theoryImageView.getLayoutParams();
            layoutParams.height = layoutParams.width = 0;
            theoryImageView.setLayoutParams(layoutParams);
        }

        // process sound
        if (m_theorySoundId != DatabaseConstant.InvalidDatabaseIndex)
        {
            final String ResourceFileName = m_alphabetDatabase.getSoundFileNameById(m_theorySoundId);
            final int SoundResourceId = getResources().getIdentifier(ResourceFileName, Constant.RawResourcesTag, getActivity().getPackageName());
            if (SoundResourceId == 0)
                throw new IllegalArgumentException();

            m_theorySoundPlayer = MediaPlayer.create(getActivity(), SoundResourceId);
            m_theorySoundPlayer.start();
            m_isResumed = false;
        }
        else
        {
            m_theorySoundPlayer = null;

            ImageButton playSoundButton = (ImageButton)fragmentView.findViewById(R.id.playSoundButton);
            playSoundButton.setVisibility(View.INVISIBLE);

            TextView playSoundTextView = (TextView)fragmentView.findViewById(R.id.playSoundTextView);
            playSoundTextView.setVisibility(View.INVISIBLE);
        }

        // process text
        {
            if (!TextUtils.isEmpty(m_theoryMessage))
            {
                TextView theoryTextView = (TextView) fragmentView.findViewById(R.id.theoryTextView);
                theoryTextView.setText(m_theoryMessage);
            }
            else
            {
                TextView theoryTextView = (TextView) fragmentView.findViewById(R.id.theoryTextView);
                theoryTextView.setVisibility(View.INVISIBLE);
            }
        }

        // set buttons callbacks
        {
            {
                final ImageButton playSoundButton = (ImageButton) fragmentView.findViewById(R.id.playSoundButton);
                playSoundButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onPlaySoundButtonClick();
                    }
                });
            }

            {
                Button forwardButton = (Button) fragmentView.findViewById(R.id.forwardButton);
                forwardButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onForwardButtonClick();
                    }
                });
            }

            {
                Button backButton = (Button) fragmentView.findViewById(R.id.backButton);
                backButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onBackButtonClick();
                    }
                });
            }
        }
    }

    private void onPlaySoundButtonClick()
    {
        if (m_theorySoundPlayer == null)
            return;

        //m_theorySoundPlayer.stop();
        m_isResumed = false;

        m_theorySoundPlayer.start();
    }

    private void onForwardButtonClick()
    {
        m_stepsCallback.processNextStep();
    }

    private void onBackButtonClick()
    {
        m_stepsCallback.processPreviousStep();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_theory_page, container, false);

        try
        {
            restoreInternalState(savedInstanceState);
            constructUserInterface(fragmentView);
        }
        catch (Exception exp)
        {
            Resources resources = getResources();
            AlertDialog msgBox = MessageBox.CreateDialog(getActivity(), resources.getString(R.string.failed_exercise_step),
                    resources.getString(R.string.alert_title), false, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            getActivity().finish();
                        }
                    });
            msgBox.show();
        }
        return fragmentView;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        m_stepsCallback = (IExerciseStepCallback) activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        m_stepsCallback = null;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (m_theorySoundPlayer == null)
            return;

        if (m_theorySoundPlayer.isPlaying())
        {
            m_theorySoundPlayer.pause();
            m_isResumed = true;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (m_theorySoundPlayer == null)
            return;

        if (m_isResumed)
        {
            m_isResumed = false;
            m_theorySoundPlayer.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(TheoryImageIndexTag, m_theoryImageId);
        savedInstanceState.putInt(TheorySoundIndexTag, m_theorySoundId);
        savedInstanceState.putString(TheoryMessageTag, m_theoryMessage);
    }

    /* Performs switching between exercise steps */
    private IExerciseStepCallback m_stepsCallback;

    private AlphabetDatabase m_alphabetDatabase;

    private int m_theoryImageId;
    private int m_theorySoundId;
    private String m_theoryMessage;

    private MediaPlayer m_theorySoundPlayer;
    private boolean m_isResumed;
}