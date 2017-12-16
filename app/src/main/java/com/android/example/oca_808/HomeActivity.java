package com.android.example.oca_808;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.example.oca_808.adapter.Objective;
import com.android.example.oca_808.adapter.ObjectivesAdapter;
import com.android.example.oca_808.adapter.TestHistoryAdapter;
import com.android.example.oca_808.db.AppDatabase;
import com.android.example.oca_808.helper.TestGenerator;
import com.android.example.oca_808.view_model.QuestionViewModelFactory;
import com.android.example.oca_808.view_model.QuestionsViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = HomeActivity.class.getSimpleName();
    private static final int TEST_SIM = 1;
    private static final int PRACTICE_TEST = 0;
    private AppDatabase mDb;
    private PopupWindow mPopUpWindow;
    private View mPopUpView;
    private Context mContext;
    private ConstraintLayout mMainLayout;
    private LayoutInflater mLayoutInflater;
    private TestHistoryAdapter mTestHistoryAdapter;
    private QuestionsViewModel mQuestionViewModel;
    private static int mTestType;
    private Button mTestButton;
    private Button mPracticeButton;
    private Button mTrainButton;
    private Button mStatsButton;
    private static final String[] OBJECTIVES = {"z", "1. Java Basics",
    "2. Data Types", "3. Operators and Decision Constructs",
    "4. Arrays", "5. Loop Constructs","6. Methods and Encapsulation",
    "7. Inheritance", "8. Handling Exceptions", "9. Java API Classes"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_2);

        mQuestionViewModel = ViewModelProviders.of(this, new QuestionViewModelFactory(this.getApplication())).get(QuestionsViewModel.class);
        mQuestionViewModel.setQVM(mQuestionViewModel);

        // instantiate objects
        mDb = mQuestionViewModel.getDb();
        mMainLayout = findViewById(R.id.home_activity);
        mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mContext = this;

        setupSharedPref();

        TestGenerator.addQs(mContext); // TODO: only run once

        // get buttons and set onClickListener
        mTestButton = findViewById(R.id.test_button);
        mPracticeButton = findViewById(R.id.practice_button);
        mTrainButton = findViewById(R.id.train_button);
        mStatsButton = findViewById(R.id.stats_button);
        mTestButton.setOnClickListener(this);
        mPracticeButton.setOnClickListener(this);
        mTrainButton.setOnClickListener(this);
        mStatsButton.setOnClickListener(this);
    }

    private void setupSharedPref() {
        SharedPreferences shPref = PreferenceManager.getDefaultSharedPreferences(this);
        int checkSP = shPref.getInt(getResources().getString(R.string.sp_test_num_key), -99);
        if (checkSP < 0) {
            SharedPreferences.Editor editor = shPref.edit();
            editor.putInt(getResources().getString(R.string.sp_test_num_key), 0);
            editor.apply();
        }

    }

    @Override
    public void onClick(View v) {
//        Log.w(LOG_TAG, "view id: " + v.getId());
        switch (v.getId()) {
            case R.id.test_button:
                mTestType = TEST_SIM;
                inflateTestPopUp(v);
                break;
            case R.id.new_test_tv:
                if (mTestType == TEST_SIM) {
                    TestGenerator.createTestSim(this, TEST_SIM);
                } else {
                    TestGenerator.createTestSim(this, PRACTICE_TEST);
                }
                startActivity(new Intent(getApplicationContext(), QuestionsActivity.class));
                break;
            case R.id.practice_button:
                mTestType = PRACTICE_TEST;
                inflateTestPopUp(v);
                break;
            case R.id.train_button:
                inflateTrainingPopup(v);


        }
    }

    private void inflateTrainingPopup(View v) {
        mPopUpView = mLayoutInflater.inflate(R.layout.popup_training_2, (ViewGroup) v.getRootView(), false);
        Spinner spinner = mPopUpView.findViewById(R.id.objectives_spinner);

        List<Objective> objList = new ArrayList<>();

        for (int i = 0; i < OBJECTIVES.length; i++) {
            Objective obj = new Objective(i, OBJECTIVES[i]);
            objList.add(obj);
        }

        ObjectivesAdapter adapter = new ObjectivesAdapter(this, 0, objList);

        spinner.setAdapter(adapter);

        // Initialize new instance of popup window
        mPopUpWindow = new PopupWindow(
                mPopUpView,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            mPopUpWindow.setElevation(5.0f);
        }

        // show the popup
        mPopUpWindow.showAtLocation(mMainLayout, Gravity.CENTER, 0, 0);

        // dim popup background
        dimBehind(mPopUpWindow);
    }

    private void inflateTestPopUp(View v) {

        // inflate layout
        mPopUpView = mLayoutInflater.inflate(R.layout.popup_test_2, (ViewGroup) v.getRootView(), false);

        // if practice test, show options else hide options
        if (mTestType == PRACTICE_TEST) {
            NumberPicker picker = mPopUpView.findViewById(R.id.question_count_picker);
            String[] data = new String[]{"10", "25", "50", "70"};
            picker.setMinValue(0);
            picker.setMaxValue(data.length - 1);
            picker.setDisplayedValues(data);
            picker.setWrapSelectorWheel(false);
        } else {
            View includeView = mPopUpView.findViewById(R.id.practice_test_view_options);
            includeView.setVisibility(View.GONE);
        }
//        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                // set recycler view
//                RecyclerView mRecyclerView = mPopUpView.findViewById(R.id.rv_incomplete_tests);
//                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
//                mRecyclerView.setLayoutManager(layoutManager);
//                mRecyclerView.setAdapter(mTestHistoryAdapter);
//            }
//        };
//        mainHandler.post(r);


        // Initialize new instance of popup window
        mPopUpWindow = new PopupWindow(
                mPopUpView,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        // Set an elevation value for popup window
        // Call requires API level 21
        if (Build.VERSION.SDK_INT >= 21) {
            mPopUpWindow.setElevation(5.0f);
        }

        // show the popup
        mPopUpWindow.showAtLocation(mMainLayout, Gravity.CENTER, 0, 0);

        // dim popup background
        dimBehind(mPopUpWindow);

        // set onClickListener
        TextView newTest = mPopUpView.findViewById(R.id.new_test_tv);
        newTest.setOnClickListener(this);


    }

    public static void dimBehind(PopupWindow popupWindow) {
        View container = popupWindow.getContentView().getRootView();
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = 0.7f;
        wm.updateViewLayout(container, p);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPopUpWindow != null) mPopUpWindow.dismiss();
    }
}
