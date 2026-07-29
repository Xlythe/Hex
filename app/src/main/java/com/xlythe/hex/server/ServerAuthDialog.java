package com.xlythe.hex.server;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

/** In-app sign-in and sign-up forms for the legacy account service. */
public final class ServerAuthDialog {
    private ServerAuthDialog() {}

    public static void showSignIn(Context context, Listener listener) {
        EditText username = field(context, "Username", InputType.TYPE_CLASS_TEXT);
        EditText password = field(
                context,
                "Password",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout content = content(context, username, password);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Sign in to igGameCenter")
                .setView(content)
                .setPositiveButton("Sign In", null)
                .setNeutralButton("Create Account", null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                String name = username.getText().toString().trim();
                String secret = password.getText().toString();
                if (!valid(name, secret, username, password)) return;
                password.setText("");
                dialog.dismiss();
                listener.onSignIn(name, secret);
            });
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(view -> {
                dialog.dismiss();
                showSignUp(context, listener);
            });
        });
        dialog.show();
    }

    public static void showSignUp(Context context, Listener listener) {
        EditText username = field(context, "Username", InputType.TYPE_CLASS_TEXT);
        EditText password = field(
                context,
                "Password",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText email = field(
                context,
                "Email (optional)",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        LinearLayout content = content(context, username, password, email);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Create igGameCenter account")
                .setMessage(
                        "This early-2010s service requires the password in the "
                                + "encrypted registration request. Hex never stores it as plaintext.")
                .setView(content)
                .setPositiveButton("Create Account", null)
                .setNeutralButton("Sign In", null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                String name = username.getText().toString().trim();
                String secret = password.getText().toString();
                if (!valid(name, secret, username, password)) return;
                String address = email.getText().toString().trim();
                password.setText("");
                dialog.dismiss();
                listener.onSignUp(name, secret, address);
            });
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(view -> {
                dialog.dismiss();
                showSignIn(context, listener);
            });
        });
        dialog.show();
    }

    private static boolean valid(
            String username,
            String password,
            EditText usernameField,
            EditText passwordField) {
        if (username.isEmpty()) {
            usernameField.setError("Username is required");
            return false;
        }
        if (password.isEmpty()) {
            passwordField.setError("Password is required");
            return false;
        }
        return true;
    }

    private static EditText field(Context context, String hint, int inputType) {
        EditText field = new EditText(context);
        field.setHint(hint);
        field.setInputType(inputType);
        field.setSingleLine(true);
        return field;
    }

    private static LinearLayout content(Context context, View... fields) {
        int padding = Math.round(24 * context.getResources().getDisplayMetrics().density);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(padding, 0, padding, 0);
        for (View field : fields) {
            content.addView(field, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        return content;
    }

    public interface Listener {
        void onSignIn(String username, String plaintextPassword);
        void onSignUp(String username, String plaintextPassword, String email);
    }
}
