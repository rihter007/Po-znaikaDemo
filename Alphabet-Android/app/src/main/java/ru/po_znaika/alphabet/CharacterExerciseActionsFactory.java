package ru.po_znaika.alphabet;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.CommonResultCode;

/**
 * Created by Rihter on 06.03.2015.
 * Creates steps fragments for CharacterExercise
 */
public final class CharacterExerciseActionsFactory implements CharacterExerciseStepManager.IExerciseStepFactory
{
    private static enum CustomAction
    {
        ObjectsContainingSound(0),
        SelectPictureWithSound(1),
        FindCharacter(2);

        private static Map<Integer, CustomAction> ValuesMap = new HashMap<Integer, CustomAction>()
        {
            { put(ObjectsContainingSound.getValue(), ObjectsContainingSound); }
            { put(SelectPictureWithSound.getValue(), SelectPictureWithSound); }
            { put(FindCharacter.getValue(), FindCharacter); }
        };

        public static CustomAction parse(int value)
        {
            return ValuesMap.get(value);
        }

        private CustomAction(int _value)
        {
            m_value = _value;
        }

        public int getValue()
        {
            return m_value;
        }

        private int m_value;
    }

    private static class SoundObjectExerciseContainer
    {
        public int soundFlag;
        public int exercisesCount;
        public int exerciseTitleResourceId;

        public SoundObjectExerciseContainer(int _soundFlag, int _exercisesCount, int _exerciseTitleResourceId)
        {
            this.soundFlag = _soundFlag;
            this.exercisesCount = _exercisesCount;
            this.exerciseTitleResourceId = _exerciseTitleResourceId;
        }
    }

    private static final int ContainsSoundImageExercisesCount = 1;
    private static final int BeginsSoundImageExercisesCount = 1;
    private static final int EndsSoundImageExercisesCount = 1;

    private static final int VerseMinSearchCharactersCount = 3;
    private static final int VerseMaxCharactersCount = 100;

    public CharacterExerciseActionsFactory(int _characterExerciseId, @NonNull Context _context,
                                           @NonNull AlphabetDatabase _alphabetDatabase)
    {
        m_characterExerciseId = _characterExerciseId;
        m_context = _context;
        m_alphabetDatabase = _alphabetDatabase;
    }

    public Fragment createExerciseStep(@NonNull AlphabetDatabase.CharacterExerciseActionType actionType,
                                int value) throws CommonException
    {
        Fragment resultFragment = null;
        switch (actionType)
        {
            case CustomAction:
                final CustomAction customAction = CustomAction.parse(value);
                if (customAction == null)
                    throw new CommonException(CommonResultCode.InvalidArgument);

                resultFragment = createCustomActionFragment(customAction);
                break;

            case TheoryPage:
                resultFragment = TheoryPageFragment.createFragment(value);
                break;
        }

        return resultFragment;
    }

    private Fragment createCustomActionFragment(@NonNull CustomAction customAction)
    {
        Fragment resultFragment = null;

        try
        {
            switch (customAction)
            {
                case ObjectsContainingSound:
                {
                    resultFragment = CharacterMultipleObjectsFragment.CreateFragment(m_characterExerciseId);
                }
                break;

                case SelectPictureWithSound:
                {
                    final AlphabetDatabase.CharacterExerciseInfo characterExerciseInfo = m_alphabetDatabase.getCharacterExerciseById(m_characterExerciseId);
                    if (characterExerciseInfo == null)
                        throw new CommonException(CommonResultCode.InvalidExternalSource);

                    ArrayList<ImageSelectionSingleExerciseState> imageSelectionExercises = new ArrayList<>();

                    final SoundObjectExerciseContainer[] soundObjectExercises = new SoundObjectExerciseContainer[]
                            {
                                    new SoundObjectExerciseContainer(AlphabetDatabase.SoundObjectInfo.Contain, ContainsSoundImageExercisesCount, R.string.caption_sound_object_contains_selection),
                                    new SoundObjectExerciseContainer(AlphabetDatabase.SoundObjectInfo.Begin, BeginsSoundImageExercisesCount, R.string.caption_sound_object_begins_selection),
                                    new SoundObjectExerciseContainer(AlphabetDatabase.SoundObjectInfo.End, EndsSoundImageExercisesCount, R.string.caption_sound_object_ends_selection)
                            };

                    Random rand = new Random(System.currentTimeMillis());
                    Resources resources = m_context.getResources();
                    for (SoundObjectExerciseContainer soundObjectExercise : soundObjectExercises)
                    {
                        final int thisSoundObjectsCount = soundObjectExercise.exercisesCount;
                        final int otherSoundObjectsCount = thisSoundObjectsCount * (ImageSelectionFragment.ImagesCount - thisSoundObjectsCount);

                        AlphabetDatabase.SoundObjectInfo thisSoundObjects[] = m_alphabetDatabase.getCharacterSoundObjectsByCharacterExerciseIdAndMatchFlag(
                                m_characterExerciseId, soundObjectExercise.soundFlag, thisSoundObjectsCount);
                        if ((thisSoundObjects == null) || (thisSoundObjects.length != thisSoundObjectsCount))
                            throw new CommonException(CommonResultCode.InvalidExternalSource);

                        AlphabetDatabase.SoundObjectInfo otherSoundObjects[] = m_alphabetDatabase.getCharacterSoundObjectsByCharacterExerciseIdAndNotMatchFlag(
                                m_characterExerciseId, soundObjectExercise.soundFlag, otherSoundObjectsCount);
                        if ((otherSoundObjects == null) || (otherSoundObjects.length != otherSoundObjectsCount))
                            throw new IllegalStateException();

                        for (int subExerciseIndex = 0; subExerciseIndex < thisSoundObjectsCount; ++subExerciseIndex)
                        {
                            ImageSelectionSingleExerciseState singleExerciseState = new ImageSelectionSingleExerciseState();

                            singleExerciseState.exerciseTitle = String.format(resources.getString(soundObjectExercise.exerciseTitleResourceId),
                                    characterExerciseInfo.character);
                            singleExerciseState.selectionVariants = new ObjectDescription[ImageSelectionFragment.ImagesCount];
                            singleExerciseState.answer = rand.nextInt(ImageSelectionFragment.ImagesCount);

                            int otherObjectsIndex = 0;
                            for (int imageIndex = 0; imageIndex < ImageSelectionFragment.ImagesCount; ++imageIndex)
                            {
                                int imageIdentifier, soundIdentifier;
                                if (imageIndex == singleExerciseState.answer)
                                {
                                    imageIdentifier = thisSoundObjects[subExerciseIndex].imageId;
                                    soundIdentifier = thisSoundObjects[subExerciseIndex].soundId;
                                }
                                else
                                {
                                    final AlphabetDatabase.SoundObjectInfo otherSoundObjectInfo = otherSoundObjects[(ImageSelectionFragment.ImagesCount - 1) * subExerciseIndex + otherObjectsIndex++];
                                    imageIdentifier = otherSoundObjectInfo.imageId;
                                    soundIdentifier = otherSoundObjectInfo.soundId;
                                }

                                final int imageResourceId = DatabaseHelpers.getDrawableIdByName(resources, m_alphabetDatabase.getImageFileNameById(imageIdentifier));
                                final int soundResourceId = DatabaseHelpers.getSoundIdByName(resources,m_alphabetDatabase.getSoundFileNameById(soundIdentifier));

                                if ((imageResourceId == 0) || (soundResourceId == 0))
                                    throw new IllegalStateException();

                                singleExerciseState.selectionVariants[imageIndex] = new ObjectDescription(imageResourceId, soundResourceId, null);
                            }

                            imageSelectionExercises.add(singleExerciseState);
                        }
                    }
                    resultFragment = ImageSelectionFragment.createFragment(imageSelectionExercises);
                }
                break;

                case FindCharacter:
                {
                    final AlphabetDatabase.CharacterExerciseInfo exerciseInfo =
                            m_alphabetDatabase.getCharacterExerciseById(m_characterExerciseId);
                    if (exerciseInfo == null)
                        throw new CommonException(CommonResultCode.InvalidExternalSource);

                    final String verseText = m_alphabetDatabase.getVerseTextByAlphabet(exerciseInfo.alphabetId,
                            exerciseInfo.character, VerseMinSearchCharactersCount, VerseMaxCharactersCount);
                    if (TextUtils.isEmpty(verseText))
                        throw new CommonException(CommonResultCode.InvalidExternalSource);

                    resultFragment = FindCharacterFragment.createFragment(verseText, exerciseInfo.character);
                }
                break;
            }
        }
        catch (Exception exp)
        {
            resultFragment = null;
        }

        return resultFragment;
    }

    private int m_characterExerciseId;
    private Context m_context;
    private AlphabetDatabase m_alphabetDatabase;
}
