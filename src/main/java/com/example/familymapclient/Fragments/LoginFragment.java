package com.example.familymapclient.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.familymapclient.R;

import Model.User;

public class LoginFragment extends Fragment {

    private Button loginButton;
    private Button registerButton;
    private Button femaleButton;
    private Button maleButton;
    private RadioGroup genders;

    private EditText serverHost;
    private EditText serverPort;
    private EditText username;
    private EditText password;
    private EditText firstName;
    private EditText lastName;
    private EditText emailAddress;

    public View onCreateView(LayoutInflater inflate, ViewGroup contain, Bundle saveInstance) {

        View view = inflate.inflate(R.layout.login_fragment, contain, false);

        loginButton = view.findViewById(R.id.login);
        registerButton = view.findViewById(R.id.register);
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);

        TextWatcher editTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!checkIfEmpty(serverHost,serverPort, username,password)) {
                    loginButton.setEnabled(true);
                    if (!checkIfNotRegisterable(serverHost,serverPort, username,password,firstName,lastName,
                            emailAddress)) {
                        registerButton.setEnabled(true);
                    }
                    else {
                        registerButton.setEnabled(false);
                    }
                }
                else {
                    loginButton.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {}
        };

        serverHost = view.findViewById(R.id.serverHostText);
        serverHost.addTextChangedListener(editTextWatcher);
        serverPort = view.findViewById(R.id.serverPortNum);
        serverPort.addTextChangedListener(editTextWatcher);
        username = view.findViewById(R.id.userNameIn);
        username.addTextChangedListener(editTextWatcher);
        password = view.findViewById(R.id.passwordIn);
        password.addTextChangedListener(editTextWatcher);
        firstName = view.findViewById(R.id.firstNameIn);
        firstName.addTextChangedListener(editTextWatcher);
        lastName = view.findViewById(R.id.lastNameIn);
        lastName.addTextChangedListener(editTextWatcher);
        emailAddress = view.findViewById(R.id.emailIn);
        emailAddress.addTextChangedListener(editTextWatcher);
        maleButton = view.findViewById(R.id.male_option);
        femaleButton = view.findViewById(R.id.female_option);
        genders = view.findViewById(R.id.genderOptions);

        loginButton.setOnClickListener(new LoginListener());
        registerButton.setOnClickListener(new RegisterListener());

        return view;
    }



    //////////////////////LISTENERS///////////////////////
    private class LoginListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int portNum;
            String host = serverHost.getText().toString();

            try {
                portNum = Integer.parseInt(serverPort.getText().toString());
                if(!checkIfEmpty(serverHost,serverPort,username,password)) {
                    loginButton.setEnabled(true);
                }
                String gender = handleLogin(host, portNum);
                if(!checkIfEmpty(serverHost,serverPort, username,password)) {
                    SignInTask signIn = new SignInTask();
                    signIn.execute(new User(username.getText().toString(), password.getText().toString(),
                            emailAddress.getText().toString(), firstName.getText().toString(),
                            lastName.getText().toString(), gender, null));
                    Toast.makeText(getContext(), "Logging User in.", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class RegisterListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            int portNum;
            String host = serverHost.getText().toString();

            try {
                portNum = Integer.parseInt(serverPort.getText().toString());
                String gender = handleLogin(host, portNum);
                if(!checkIfNotRegisterable(serverHost,serverPort,username,password,firstName,lastName,
                        emailAddress)) {
                    RegisterTask register = new RegisterTask();
                    register.execute(new User(username.getText().toString(), password.getText().toString(),
                            emailAddress.getText().toString(), firstName.getText().toString(),
                            lastName.getText().toString(), gender, null));
                    registerButton.setEnabled(true);
                    Toast.makeText(getContext(), "Registering User.", Toast.LENGTH_SHORT).show();
                }
            }
            catch (Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }



    ////////////////////////HELPERS//////////////////////////
    private String handleLogin(String host, int port) {
        ServerProxy server = ServerProxy.getInstance();
        server.setHost(host);
        server.setPort(port);
        String gender = "";
        int id = genders.getCheckedRadioButtonId();
        if (id == femaleButton.getId()) {
            gender = "f";
        }
        else if (id == maleButton.getId()) {
            gender = "m";
        }
        return gender;
    }
    private boolean checkIfEmpty(EditText serverHost, EditText serverPort, EditText username, EditText password) {
        return (serverHost.getText().toString().equals("") || serverPort.getText().toString().equals("")||
                username.getText().toString().equals("") || password.getText().toString().equals(""));
    }
    private boolean checkIfNotRegisterable(EditText serverHost, EditText serverPort, EditText username,
        EditText password, EditText firstName, EditText lastName, EditText emailAddress) {
        boolean check = checkIfEmpty(serverHost, serverPort, username, password);
        return (check || firstName.getText().toString().equals("") || lastName.getText().toString().equals("")
                || emailAddress.getText().toString().equals(""));
    }


    ///////////////////////ASYNC TASKS////////////////////////
    private class SignInTask extends AsyncTask<User, Void, Object[]> {
        @Override
        protected Object[] doInBackground(User... users) {
            return ServerProxy.login(users[0]);
        }
        @Override
        protected void onPostExecute(Object[] o) {
            Toast.makeText(getContext(), o[1].toString(), Toast.LENGTH_SHORT).show();
            if(o[0].toString().equals("true")) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Fragment frag = fm.findFragmentById(R.id.mainActivityPage);
                if (frag == null) {
                    frag = new MapFragment();
                    fm.beginTransaction().add(R.id.mainActivityPage, frag).commit();
                }
                else {
                    Fragment mapFragment = new MapFragment();
                    fm.beginTransaction().replace(R.id.mainActivityPage, mapFragment).commit();
                }
            }
        }
    }
    private class RegisterTask extends AsyncTask<User, Void, Object[]> {
        @Override
        protected Object[] doInBackground(User... users) {
            return ServerProxy.register(users[0]);
        }
        @Override
        protected void onPostExecute(Object[] o) {
            Toast.makeText(getContext(), o[1].toString(), Toast.LENGTH_SHORT).show();
            if(o[0].toString().equals("true")) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Fragment frag = fm.findFragmentById(R.id.mainActivityPage);
                if (frag == null) {
                    frag = new MapFragment();
                    fm.beginTransaction().add(R.id.mainActivityPage, frag).commit();
                }
                else {
                    Fragment mapFragment = new MapFragment();
                    fm.beginTransaction().replace(R.id.mainActivityPage, mapFragment).commit();
                }
            }
        }
    }
}