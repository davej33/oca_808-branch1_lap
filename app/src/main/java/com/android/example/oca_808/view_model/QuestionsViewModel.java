package com.android.example.oca_808.view_model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.android.example.oca_808.db.AppDatabase;
import com.android.example.oca_808.db.entity.QuestionEntity;
import com.android.example.oca_808.db.entity.TestEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by charlotte on 11/21/17.
 */

public class QuestionsViewModel extends ViewModel {

    private static final String LOG_TAG = QuestionsViewModel.class.getSimpleName();

    // database
    private static AppDatabase mDb;

    // Question vars
    private static ArrayList<QuestionEntity> mQuestionsList;
    private static MutableLiveData<Integer> mQuestionNumber;
    private static QuestionEntity mCurrentQuestion;
    private static int mWhereWeAt;

    // Test vars
    private static TestEntity mCurrentTest;
    private static List<String> mUserAnswerArray;
    private static StringBuilder mUserAnswer;

    private static boolean mInitiated;
    // Timer vars
//    private static final int ONE_SECOND = 1;
//    private long mInitialTime;
//    private MutableLiveData<Long> mElapsedTime = new MutableLiveData<>();

    private static int mLoadTestId;
    private static QuestionsViewModel mQuestionViewModel;

    // constructor
    public QuestionsViewModel(Application mApplication) {

        // instantiate db if null
        if (mDb == null) {
            mDb = AppDatabase.getDb(mApplication);
        }

    }


    public LiveData<Integer> newQuestion() {
        return mQuestionNumber;
    }

    public QuestionEntity getCurrentQuestion() {
        return mCurrentQuestion;
    }

    // TODO: efficient?
    private void startTimer() {
//        mInitialTime = SystemClock.elapsedRealtime();
//        Timer timer = new Timer();
//
//        // Update the elapsed time every second.
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                final long newValue = (SystemClock.elapsedRealtime() - mInitialTime) / 1000;
//                // setValue() cannot be called from a background thread so post to main thread.
//                new Handler(Looper.getMainLooper()).post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mElapsedTime.setValue(newValue);
//
//                    }
//                });
//            }
//        }, ONE_SECOND, ONE_SECOND);

    }

    // called from QuestionActivity on FAB click
    // compares user answer and returns any wrong answers in ArrayList
    public ArrayList<String> checkAnswer() {

        ArrayList<String> wrongAnswers = new ArrayList<>(); // create arraylist to hold wrong answers

        if (mCurrentQuestion.getType() == 1) { // 1 == single answer. if question type is Radio
            if (!mUserAnswer.toString().equals(mCurrentQuestion.answer)) { // if userAnswer [is wrong] doesn't equal correct answer
                wrongAnswers.add(mUserAnswer.toString()); // add the user's answer to the wrongAnswer arraylist
            }
        } else { // if question type is checkbox
            String correctAnswer = mCurrentQuestion.getAnswer(); // store correct answer in String
            for (int i = 0; i < mUserAnswer.length(); i++) { // iterate through each letter in user's answer
                String check = String.valueOf(mUserAnswer.charAt(i)); // store each user answer in String
                if (!correctAnswer.contains(check)) { // if user answer not in answer
                    wrongAnswers.add(check); // add to wrongAnswer Arraylist
                }
            }
        }
        return wrongAnswers;
    }


    public void setUserAnswer(String s) {
        // add to arrayList if unanswered, if changing previous answer then set corresponding element
        if (mUserAnswerArray.size() <= mWhereWeAt) {
            if (s == null) {
                mUserAnswerArray.add(mUserAnswer.toString());
            } else {
                mUserAnswerArray.add("");
            }
            Log.w(LOG_TAG, "add " + mUserAnswer.toString() + " at index " + mWhereWeAt);
        } else {
            if (s == null) {
                mUserAnswerArray.set(mWhereWeAt, mUserAnswer.toString());
            } else {
                mUserAnswerArray.set(mWhereWeAt, "");
            }
            Log.w(LOG_TAG, "set " + mUserAnswer.toString() + " at index " + mWhereWeAt);
        }
    }

    public void nextQuestion() {
        mQuestionNumber.setValue(++mWhereWeAt);
        mCurrentQuestion = mQuestionsList.get(mWhereWeAt);
    }

    public void loadPreviousQuestion() {
        mQuestionNumber.setValue(--mWhereWeAt);
        mCurrentQuestion = mQuestionsList.get(mWhereWeAt);
    }

    public int getmWhereWeAt() {
        return mWhereWeAt;
    }

    public int getQuestionCount() {
        return mQuestionsList.size();
    }

    public String getUserAnswer() {

        if (mUserAnswerArray.size() > mWhereWeAt) { // if loading a question that's already been answered
            Log.w(LOG_TAG, "Question answered. getUserAnswer return = " + mUserAnswerArray.get(mWhereWeAt));
            return mUserAnswerArray.get(mWhereWeAt); // return the answer
        } else {
            Log.w(LOG_TAG, "getUserAnswer = Unanswered" );
            mUserAnswer.delete(0, mUserAnswer.length());
            return ""; // otherwise, return the current user answer
        }
    }

    public void collectUserAnswer(char userAnswer, boolean checkState) {
        Log.i(LOG_TAG, "1. VM uAnswersArray: " + mUserAnswerArray.toString());
        if (mCurrentQuestion.type == 1) {
            if (mUserAnswer.length() == 1) mUserAnswer.deleteCharAt(0);
            mUserAnswer.append(userAnswer);
        } else if (mCurrentQuestion.type == 0 && checkState) {
            if (!mUserAnswer.toString().contains(String.valueOf(userAnswer)))
                mUserAnswer.append(userAnswer);
            Log.i(LOG_TAG, "added to mUserAnswer: " + mUserAnswer);
        } else if (mCurrentQuestion.type == 0) {
            mUserAnswer.deleteCharAt(mUserAnswer.indexOf(String.valueOf(userAnswer)));
            Log.i(LOG_TAG, "removed from mUserAnswer: " + mUserAnswer);
        }
        setUserAnswer(null);
        Log.i(LOG_TAG, "2. VM uAnswersArray: " + mUserAnswerArray.toString());
    }

    // ---------------------------------- get and set test --------------------
    private void setTestAttributes() {
        // get questions
        mQuestionsList = setQuestionsList();

        mUserAnswer = new StringBuilder();
//        startTimer();
//        int index = mQuestionsList.indexOf(mCurrentQuestion);

        // set starting point
        if (mQuestionNumber == null) {
            mWhereWeAt = mCurrentTest.resumeQuestionNum;
            Log.i(LOG_TAG, "WWA: " + mWhereWeAt);
            mQuestionNumber = new MutableLiveData<>();
            mQuestionNumber.setValue(mWhereWeAt);
        }
        if (mCurrentQuestion == null) {
            mCurrentQuestion = mQuestionsList.get(mWhereWeAt);
        }

        // set answer list
        StringBuilder sb = new StringBuilder(mCurrentTest.answerSet);
        sb.deleteCharAt(sb.length() - 1).deleteCharAt(0);
        mUserAnswerArray = Arrays.asList((sb.toString()).split(", "));
        mUserAnswerArray = new ArrayList<>(mUserAnswerArray);
        if (!mUserAnswerArray.get(0).equals("null")) mUserAnswerArray.add(0, null);
        Log.w(LOG_TAG, "user answer array init: " + mUserAnswerArray.toString());
    }

    public ArrayList<QuestionEntity> setQuestionsList() {

        // get question list from the TestEntity
        StringBuilder questionsStringBuilder = new StringBuilder(mCurrentTest.questionSet);
//        Log.w(LOG_TAG, "questionStringBuilder = "  + questionsStringBuilder.toString());
        questionsStringBuilder.deleteCharAt(questionsStringBuilder.length() - 1).deleteCharAt(0);
        String questionsString = questionsStringBuilder.toString();
//        Log.w(LOG_TAG, "questionString = "  + questionsString);

        // convert string to list of question IDs
        List<String> qIdListAsStrings = Arrays.asList(questionsString.split(", "));
//        Log.w(LOG_TAG, "questionStringList = "  + qIdListAsStrings.toString());


//        Log.w(LOG_TAG, "IntList count: " + mQuestionsList.size());
        mQuestionsList = (ArrayList<QuestionEntity>) mDb.questionsDao().getQuestions(qIdListAsStrings);
        Log.i(LOG_TAG, "questionList count: " + mQuestionsList.size());
        mQuestionsList.add(0, null);
        Log.i(LOG_TAG, "questionList count with null added: " + mQuestionsList.size());
        return mQuestionsList;
    }

    public void saveDataToDb() {
        mCurrentTest.setAnswerSet(mUserAnswerArray.toString());
        mCurrentTest.setResumeQuestionNum(mWhereWeAt);
        mCurrentTest.setProgress(calculateProgress());

        int updateCheck = mDb.testsDao().updateTestResults(mCurrentTest);
//        Log.w(LOG_TAG, "^^^^^^^^^ update check: " + updateCheck);
    }

    private int calculateProgress() {
        if (mUserAnswerArray.size() > 0) {
            int questionsAnswered = 0;
            for (String s : mUserAnswerArray) {
                if (s != null && (!(s.equals("") || s.equals("null")))) {
                    questionsAnswered++;
                }
            }
            int testLength = mQuestionsList.size() - 1;
            int progress = (100 * questionsAnswered) / testLength;

//            Log.w(LOG_TAG, "Progress - answered" + questionsAnswered + " of " + (mQuestionsList.size() - 1) + " equaling " + progress + "% complete");

            return progress;
        } else {
            return 0;
        }
    }

    public void setQVM(QuestionsViewModel QVM) {
        mQuestionViewModel = QVM;
    }

    public static QuestionsViewModel getQVM() {
        return mQuestionViewModel;
    }

    public void getTest(int testId) {

//        clearVars();

        // get TestEntity
        mCurrentTest = mDb.testsDao().fetchTest(testId);
        Log.i(LOG_TAG, "current test title: " + mCurrentTest.title);

        if (!mInitiated) {
            mInitiated = true;
            setTestAttributes();
        }

    }

//    private void clearVars() {
//        mCurrentTest = null;
//        mQuestionsList = null;
//        mCurrentQuestion = null;
//        mUserAnswerArray = null;
//        mUserAnswer = null;
//    }

    public AppDatabase getDb() {
        return mDb;
    }

    public String getTestTitle() {
        return mCurrentTest.title;
    }
}