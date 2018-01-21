package com.javierd.nightlive;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.javierd.nightlive.PlaceStyle.PlaceStyle;
import com.javierd.nightlive.PlaceStyle.StylesGridViewAdapter;
import com.javierd.nightlive.RestUtils.User;
import com.javierd.nightlive.RestUtils.UserData;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    EditText birthdateTextView;
    long birthdateMillis = -1;
    int sex = 0; //0 = man, 1 = woman, 2 = Other

    public static final String SIGNED_IN_USER = "sign_in_user";
    public static final String USER_NAME = "user_name";
    public static final String USER_TOKEN = "user_token";

    FABProgressCircle fabProgressCircle;
    LinearLayout layoutUserMail;
    LinearLayout layoutUserPassword;
    LinearLayout layoutUserPassName;
    RelativeLayout layoutUserInfo;

    ArrayList<PlaceStyle> ITEMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //TODO Use the status codes

        layoutUserMail = (LinearLayout) findViewById(R.id.layout_user_mail);
        layoutUserPassword = (LinearLayout) findViewById(R.id.layout_user_password);
        layoutUserPassName = (LinearLayout) findViewById(R.id.layout_user_pass_name);
        layoutUserInfo = (RelativeLayout) findViewById(R.id.layout_user_info);
        fabProgressCircle = (FABProgressCircle) findViewById(R.id.fabProgressCircle);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manageViews();
            }
        });

        birthdateTextView = (EditText) findViewById(R.id.birthDateEditText);
        birthdateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar now = Calendar.getInstance();

                Calendar minDate = Calendar.getInstance();
                minDate.set(Calendar.YEAR, 1940);
                Calendar maxDate = Calendar.getInstance();
                maxDate.set(Calendar.YEAR, now.get(Calendar.YEAR)- 10);

                DatePickerDialog dpd = DatePickerDialog.newInstance(
                        LoginActivity.this,
                        1990,
                        1,
                        1
                );

                dpd.setVersion(DatePickerDialog.Version.VERSION_2);
                //dpd.setAccentColor();
                //dpd.setTitle("Test");
                dpd.setMinDate(minDate);
                dpd.setMaxDate(maxDate);
                dpd.show(getFragmentManager(), "Datepickerdialog");

            }
        });

        final Spinner sexSpinner = (Spinner) findViewById(R.id.sexSpinner);
        sexSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                sex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter<String> sexDataAdapter = new ArrayAdapter<String>(LoginActivity.this, android.R.layout.simple_spinner_item, LoginActivity.this.getResources().getStringArray(R.array.sex_options));
        sexDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexSpinner.setAdapter(sexDataAdapter);
        sexSpinner.setSelection(sex);
    }

    protected void manageViews(){
        final EditText userNameEditText = (EditText) findViewById(R.id.userEditText);
        final TextInputLayout floatLabelName = (TextInputLayout) findViewById(R.id.float_label_user_id);
        final EditText userPasswordEditText = (EditText) findViewById(R.id.passwordEditText);
        final TextInputLayout floatLabelPassword = (TextInputLayout) findViewById(R.id.float_label_password);
        final EditText userPassNameEditText = (EditText) findViewById(R.id.passwordNewEditText);
        final TextInputLayout floatLabelPassName = (TextInputLayout) findViewById(R.id.float_label_password_new);
        final EditText userMailEditText = (EditText) findViewById(R.id.mailEditText);
        final TextInputLayout floatLabelMail = (TextInputLayout) findViewById(R.id.float_label_mail);
        final TextInputLayout floatLabelBirthdate = (TextInputLayout) findViewById(R.id.float_label_birthdate);

        floatLabelBirthdate.setErrorEnabled(false);
        floatLabelMail.setErrorEnabled(false);
        floatLabelName.setErrorEnabled(false);
        floatLabelPassName.setErrorEnabled(false);
        floatLabelPassword.setErrorEnabled(false);

        if(!isOnline()){
            Toast.makeText(LoginActivity.this, getResources().getString(R.string.no_internet), Toast.LENGTH_LONG).show();
        }

        //The user has just entered his email
        if(layoutUserMail.getVisibility() == View.VISIBLE){
            String userMail = userMailEditText.getText().toString();
            if(!TextUtils.isEmpty(userMail) && validMail(userMail)){
                fabProgressCircle.show();
                checkMailUser(userMail);
            }else{
                floatLabelMail.setError(getString(R.string.wrong_email));
            }
        }

        //The user is already registered
        if(layoutUserPassword.getVisibility() == View.VISIBLE){
            String userMail = userMailEditText.getText().toString();
            String userPassword = userPasswordEditText.getText().toString();
            if( !TextUtils.isEmpty(userMail)
                    && !TextUtils.isEmpty(userPassword)){
                fabProgressCircle.show();
                signInUser(userMail, userPassword);
            }else{
                if(TextUtils.isEmpty(userMail)){
                    floatLabelMail.setError(getString(R.string.wrong_email));
                    animateViewChange(layoutUserPassword, layoutUserMail);
                }else{
                    floatLabelPassword.setError(getString(R.string.wrong_password));
                }
            }
        }

        //The user is not registered
        if(layoutUserPassName.getVisibility() == View.VISIBLE){
            String userMail = userMailEditText.getText().toString();
            String userName = userNameEditText.getText().toString();
            String userPassword = userPassNameEditText.getText().toString();

            if( !TextUtils.isEmpty(userMail)
                    && !TextUtils.isEmpty(userPassword)
                    && !TextUtils.isEmpty(userName)){

                fabProgressCircle.show();
                checkNameUser(userName);
                setUpStylesGridView();
            }else{
                if(TextUtils.isEmpty(userMail)) {
                    floatLabelMail.setError(getString(R.string.wrong_email));
                    animateViewChange(layoutUserPassName, layoutUserMail);
                }else if(TextUtils.isEmpty(userName)){
                    floatLabelName.setError(getString(R.string.wrong_username));
                }else{
                    floatLabelPassName.setError(getString(R.string.wrong_password));
                }
            }
        }

        if(layoutUserInfo.getVisibility() == View.VISIBLE){
            String userMail = userMailEditText.getText().toString();
            String userName = userNameEditText.getText().toString();
            String userPassword = userPassNameEditText.getText().toString();

            if( !TextUtils.isEmpty(userMail)
                    && !TextUtils.isEmpty(userPassword)
                    && !TextUtils.isEmpty(userName)
                    && birthdateMillis != -1){

                fabProgressCircle.show();
                signUpUser(userName, userPassword, sex, userMail, birthdateMillis);
            }else{
                if(TextUtils.isEmpty(userMail)) {
                    floatLabelMail.setError(getString(R.string.wrong_email));
                    animateViewChange(layoutUserInfo, layoutUserPassName);
                    animateViewChange(layoutUserInfo, layoutUserMail);
                }else if(TextUtils.isEmpty(userName)){
                    floatLabelName.setError(getString(R.string.wrong_username));
                    animateViewChange(layoutUserInfo, layoutUserPassName);
                }else if(TextUtils.isEmpty(userPassword)){
                    floatLabelPassName.setError(getString(R.string.wrong_password));
                    animateViewChange(layoutUserInfo, layoutUserPassName);
                }else{
                    floatLabelBirthdate.setError(getString(R.string.wrong_birthdate));
                }
            }
        }
    }

    protected void setUpStylesGridView(){
        ITEMS = new ArrayList<>();

        TypedArray imgs = getResources().obtainTypedArray(R.array.styles_images);
        String[] styles = getResources().getStringArray(R.array.styles_names);

        for(int i = 0; i < styles.length; i++){
            PlaceStyle place = new PlaceStyle( styles[i], false, imgs.getResourceId(i, -1));
            ITEMS.add(place);
        }

        GridView gridView = (GridView) findViewById(R.id.stylesGridView);
        StylesGridViewAdapter gridAdapter = new StylesGridViewAdapter(this, R.layout.grid_style_item, ITEMS);
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                PlaceStyle style = ITEMS.get(position);
                final TextView textView = (TextView) view.findViewById(R.id.styleNameTextView);

                Boolean selected = style.getSelected();
                style.setSelected(!selected);
                int colorFrom;
                int colorTo;
                if(!selected){
                    colorFrom = ContextCompat.getColor(LoginActivity.this, R.color.gridBackgroundFooterClear);
                    colorTo = ContextCompat.getColor(LoginActivity.this, R.color.gridBackgroundFooter);
                }else{
                    colorFrom = ContextCompat.getColor(LoginActivity.this, R.color.gridBackgroundFooter);
                    colorTo = ContextCompat.getColor(LoginActivity.this, R.color.gridBackgroundFooterClear);
                }

                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.setDuration(800);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        textView.setBackgroundColor((int) animator.getAnimatedValue());
                    }

                });
                colorAnimation.start();

            }
        });
    }

    protected boolean validMail(String mail){
        return mail.contains("@") && mail.contains(".");
    }

    protected void animateViewChange (final View mView1, final View mView2){
        mView1.setEnabled(false);
        mView2.setEnabled(false);
        Animation slideIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in);
        Animation slideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out);

        slideIn.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
                mView2.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
            }
        });
        slideOut.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                mView1.setVisibility(View.INVISIBLE);
            }
        });

        mView1.startAnimation(slideOut);
        mView2.startAnimation(slideIn);
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    protected void storeSignedInUser(final String userName, final String token){
        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit().putBoolean(LoginActivity.SIGNED_IN_USER, true).apply();
        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit().putString(LoginActivity.USER_NAME, userName).apply();
        PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit().putString(LoginActivity.USER_TOKEN, token).apply();

        Intent mIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mIntent);
        finish();
    }

    protected void signInUser(final String userMail, final String userPassword){
        final TextInputLayout floatLabelMail = (TextInputLayout) findViewById(R.id.float_label_mail);
        final TextInputLayout floatLabelPassword = (TextInputLayout) findViewById(R.id.float_label_password);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface apiService =
                retrofit.create(RestInterface.class);

        Call<User> call = apiService.userSignIn(userMail, userPassword);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                fabProgressCircle.hide();
                int statusCode = response.code();
                switch(statusCode){
                    case 400:
                        //Bad request
                        Toast.makeText(LoginActivity.this, getString(R.string.wrong_server_direction), Toast.LENGTH_LONG).show();
                        break;
                }

                User result = response.body();
                if(result == null) return;

                UserData user = result.getUserData();

                Log.i("Sign in result", String.valueOf(result));
                if(user.getResult() == 1){
                    floatLabelMail.setError(getString(R.string.wrong_email));
                    animateViewChange(layoutUserPassword, layoutUserMail);
                }else if(user.getResult() == 2){
                    floatLabelPassword.setError(getString(R.string.wrong_password));
                }else{
                    String userName = user.getName();
                    String token = user.getToken();
                    storeSignedInUser(userName, token);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                // Log error here since request failed
                fabProgressCircle.hide();
                Log.i("Error signing in", String.valueOf(t));
                Toast.makeText(LoginActivity.this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void signUpUser(final String userName, final String userPassword, final int userSex, final String userEmail, final long birthdateMillis){
        final TextInputLayout floatLabelName = (TextInputLayout) findViewById(R.id.float_label_user_id);
        final TextInputLayout floatLabelPassName = (TextInputLayout) findViewById(R.id.float_label_password_new);
        final TextInputLayout floatLabelMail = (TextInputLayout) findViewById(R.id.float_label_mail);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface apiService =
                retrofit.create(RestInterface.class);

        String styles = "";
        if(ITEMS != null){
            for(int i = 0; i < ITEMS.size(); i++){
                if(ITEMS.get(i).getSelected()){
                    styles += String.valueOf(i)+"_";
                }
            }
        }

        Call<User> call = apiService.userSingUp(userName, userPassword, userSex, userEmail, birthdateMillis, styles);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                fabProgressCircle.hide();
                int statusCode = response.code();
                switch(statusCode){
                    case 400:
                        //Bad request
                        Toast.makeText(LoginActivity.this, getString(R.string.wrong_server_direction), Toast.LENGTH_LONG).show();
                        break;
                }

                User result = response.body();
                if(result == null) return;

                UserData user = result.getUserData();

                Log.i("Sign up result", String.valueOf(result));

                switch (user.getResult()){
                    case 0:
                        String token = user.getToken();
                        storeSignedInUser(user.getName(), token);
                        break;
                    case 1:
                        floatLabelName.setError(getString(R.string.used_username));
                        animateViewChange(layoutUserInfo, layoutUserPassName);
                        break;
                    case 2:
                        floatLabelMail.setError(getString(R.string.used_email));
                        animateViewChange(layoutUserInfo, layoutUserPassName);
                        animateViewChange(layoutUserPassName, layoutUserMail);
                        break;
                    case 3:
                        floatLabelMail.setError(getString(R.string.wrong_email));
                        animateViewChange(layoutUserInfo, layoutUserPassName);
                        animateViewChange(layoutUserPassName, layoutUserMail);
                        break;
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                // Log error here since request failed
                fabProgressCircle.hide();
                Log.i("Error signing up", String.valueOf(t));
                Toast.makeText(LoginActivity.this, getString(R.string.unexpected_error), Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void checkMailUser(final String mail){
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface apiService =
                retrofit.create(RestInterface.class);

        Call<Integer> call = apiService.userCheckMail(mail);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                fabProgressCircle.hide();
                int statusCode = response.code();
                Integer result = response.body();

                if(result == null) return;
                Log.i("Check mail result", String.valueOf(result));
                switch (result){
                    case -1:
                        Toast.makeText(LoginActivity.this, getString(R.string.wrong_server_direction), Toast.LENGTH_LONG).show();
                        break;
                    case 0:
                        animateViewChange(layoutUserMail, layoutUserPassword);
                        break;
                    case 1:
                        animateViewChange(layoutUserMail, layoutUserPassName);
                        break;
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                // Log error here since request failed
                fabProgressCircle.hide();
                Log.i("Error checking name", String.valueOf(t));
            }
        });
    }

    protected void checkNameUser(final String name){
        final TextInputLayout floatLabelName = (TextInputLayout) findViewById(R.id.float_label_user_id);
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RestInterface.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestInterface apiService =
                retrofit.create(RestInterface.class);

        Call<Integer> call = apiService.userCheckName(name);
        call.enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                fabProgressCircle.hide();
                int statusCode = response.code();
                Integer result = response.body();

                if(result == null) return;
                Log.i("Check mail result", String.valueOf(result));
                switch (result){
                    case -1:
                        Toast.makeText(LoginActivity.this, getString(R.string.wrong_server_direction), Toast.LENGTH_LONG).show();
                        break;
                    case 0:
                        animateViewChange(layoutUserPassName, layoutUserInfo);
                        break;
                    case 1:
                        floatLabelName.setError(getString(R.string.used_username));
                        break;
                }
            }

            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                // Log error here since request failed
                fabProgressCircle.hide();
                Log.i("Error checking name", String.valueOf(t));
            }
        });
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String date = dayOfMonth+"/"+(monthOfYear+1)+"/"+year;
        birthdateTextView.setText(date);

        Calendar dateCal = Calendar.getInstance();
        dateCal.set(Calendar.YEAR, year);
        dateCal.set(Calendar.MONTH, monthOfYear +1);
        dateCal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        dateCal.set(Calendar.HOUR_OF_DAY, 2);
        dateCal.set(Calendar.MINUTE, 30);
        dateCal.set(Calendar.SECOND, 30);
        dateCal.set(Calendar.MILLISECOND, 50);

        birthdateMillis = dateCal.getTimeInMillis();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
