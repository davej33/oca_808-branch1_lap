package com.android.example.oca_808;

import android.annotation.TargetApi;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.example.oca_808.fragment.AnswerFragment;
import com.android.example.oca_808.fragment.ExplanationFragment;
import com.android.example.oca_808.fragment.ProgressFragment;
import com.android.example.oca_808.fragment.QuestionFragment;
import com.android.example.oca_808.view_model.QuestionViewModelFactory;
import com.android.example.oca_808.view_model.QuestionsViewModel;

import java.util.ArrayList;


/**
 * Created by charlotte on 11/21/17.
 */

public class QuestionsActivity extends AppCompatActivity implements QuestionFragment.OnFragmentInteractionListener,
        ProgressFragment.OnFragmentInteractionListener, AnswerFragment.OnFragmentInteractionListener, ExplanationFragment.OnFragmentInteractionListener {

    private static final String LOG_TAG = QuestionsActivity.class.getSimpleName();
    private Integer mQuestionNum = 0;
    private static QuestionsViewModel mViewModel;
    private FloatingActionButton mFAB;
    private ToggleButton mShowAnswerButton;
    private FrameLayout mExplanationContainer, mQuestionContainer, mAnswerContainer, mQuestionForSolutionContainer;
    private static boolean mDisplayingExplanation = false;
    private ArrayList<String> mWrongAnswers;
    private String mUserAnswer;

    @TargetApi(Build.VERSION_CODES.M) // TODO: Fix
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        Log.i(LOG_TAG, "onCreate QuestionA run");

        if (mViewModel == null) {
            mViewModel = QuestionsViewModel.getQVM();
//            mViewModel = ViewModelProviders.of(this, new QuestionViewModelFactory(getActivity().getApplication())).get(QuestionsViewModel.class);
        }
        mShowAnswerButton = findViewById(R.id.show_answer);
        mExplanationContainer = findViewById(R.id.explanation_container);
        mQuestionContainer = findViewById(R.id.question_container);
        mAnswerContainer = findViewById(R.id.answer_container);
        mQuestionForSolutionContainer = findViewById(R.id.question_solution_container);


        Toast.makeText(this, mViewModel.getTestTitle(), Toast.LENGTH_SHORT).show();
        // Hide the status bar.
//        View decorView = getWindow().getDecorView();
//        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark, null)));

        // get FAB instance and set onClickListener
        if (mFAB == null) {
            mFAB = findViewById(R.id.floatingActionButton);
            mFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mViewModel.getUserAnswer().equals("")) mViewModel.setUserAnswer("");

//                    String solution = mViewModel.getCurrentQuestion().answer;
                    mWrongAnswers = mViewModel.checkAnswer();

                    // TODO store answer in Test object, create test object

                    if (!mShowAnswerButton.isChecked() || (mShowAnswerButton.isChecked() && mExplanationContainer.getVisibility() == View.VISIBLE)) {
                        mDisplayingExplanation = false;
                        mViewModel.nextQuestion();
                    } else {
                        displayExplanation();
                    }
                }
            });
        }

        subscribe();

        if (savedInstanceState != null){
            Log.i(LOG_TAG, "savedInstanceBundle ====== " + savedInstanceState.getBoolean("display"));
            mDisplayingExplanation = savedInstanceState.getBoolean("display");
            setDisplay();}



    }

    private void setDisplay() {
        if (mDisplayingExplanation) {
            Log.i(LOG_TAG,"if/else displayExp run. mDisplayingExplanation = " + mDisplayingExplanation);
            displayExplanation();
        } else {
            Log.i(LOG_TAG,"if/else displayQuestion run. mDisplayingExplanation = " + mDisplayingExplanation);
            displayQuestion();
        }
    }

    private void displayQuestion() {
        Log.i(LOG_TAG, "displayQuestion run. mDisplayingExplanation = " + mDisplayingExplanation);
        mDisplayingExplanation = false;

        // manage view visibilities
        mExplanationContainer.setVisibility(View.GONE);
        mQuestionForSolutionContainer.setVisibility(View.GONE);
        mQuestionContainer.setVisibility(View.VISIBLE);

        // set new views
        getSupportFragmentManager().beginTransaction().replace(R.id.question_container, QuestionFragment.newInstance(mQuestionNum, null)).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.answer_container, AnswerFragment.newInstance(null, null)).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.progress_container, ProgressFragment.newInstance(null, null)).commit();


        mFAB.setImageResource(android.R.drawable.ic_media_next);
    }


    private void displayExplanation() {
        Log.i(LOG_TAG, "displayExplanation run");
        mDisplayingExplanation = true;

        // manage view visibilities
        mQuestionContainer.setVisibility(View.GONE);
        mExplanationContainer.setVisibility(View.VISIBLE);
        mQuestionForSolutionContainer.setVisibility(View.VISIBLE);

        // set new views
        getSupportFragmentManager().beginTransaction().replace(R.id.answer_container, AnswerFragment.newInstance(mWrongAnswers, null)).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.question_solution_container, QuestionFragment.newInstance(mQuestionNum, null)).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.explanation_container, ExplanationFragment.newInstance(null, null)).commit();
    }

    private void subscribe() {

        final Observer<Integer> questionObserver = new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer qNum) {
                mQuestionNum = qNum;
                setDisplay();
                Log.i(LOG_TAG, "subscriber run. mDisplayingExplanation = " + mDisplayingExplanation);
            }
        };
        mViewModel.newQuestion().observe(this, questionObserver);


    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(LOG_TAG, "onSavedInstanceState run");
        outState.putBoolean("display", mDisplayingExplanation);
    }

    @Override
    public void answerSelected(boolean b) {
        if (b) {
            mFAB.setImageResource(android.R.drawable.checkbox_on_background);
        } else {
            mFAB.setImageResource(android.R.drawable.ic_media_next);
        }
    }

    @Override
    public void loadPreviousQuestion() {
        mViewModel.loadPreviousQuestion();
    }
}
