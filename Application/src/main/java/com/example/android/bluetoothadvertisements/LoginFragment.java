package com.example.android.bluetoothadvertisements;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by paul__000 on 4/26/2017.
 */

public class LoginFragment extends Fragment implements View.OnClickListener {

    private Button btn_submit;
    private EditText txt_un;
    private EditText txt_pwd;
    OnLoginSubmittedListener loginCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        btn_submit = (Button) view.findViewById(R.id.btn_login);
        btn_submit.setOnClickListener(this);

        txt_un = (EditText) view.findViewById(R.id.txt_login_name);
        txt_pwd = (EditText) view.findViewById(R.id.txt_login_pwd);

        ActionBar BLEActionBar = ((MainActivity)getActivity()).getSupportActionBar();
        BLEActionBar.setTitle("Log In");

        // Inflate the layout for this fragment
        return view;
    }

    /**
     * Called when login button is pressed
     */
    @Override
    public void onClick(View v) {
        String username = txt_un.getText().toString();
        String pwd = txt_pwd.getText().toString();

        if (!username.isEmpty() && !pwd.isEmpty()) {
            loginCallback.onLoginSubmitted(username, pwd);
        }
        else if (username.isEmpty()) {
            Toast.makeText(getActivity(), "No username specified", Toast.LENGTH_LONG).show();
        }
        else if (pwd.isEmpty()) {
            Toast.makeText(getActivity(), "No password specified", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(getActivity(), "Unexpected error", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            loginCallback = (OnLoginSubmittedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoginSubmittedListener");
        }
    }

    public interface OnLoginSubmittedListener {
        public void onLoginSubmitted(String username, String pwd);
    }
}
