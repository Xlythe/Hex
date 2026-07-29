package com.xlythe.hex.fragment;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.xlythe.hex.NetActivity;
import com.xlythe.hex.R;
import com.xlythe.hex.server.GameChatMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Responsive in-game chat backed by the server game's existing event stream. */
public final class GameChatDialogFragment extends DialogFragment {
    public static final String TAG = "game-chat";

    private final Runnable storeObserver = this::refresh;
    private final ChatAdapter adapter = new ChatAdapter();
    private ListView messageList;
    private EditText messageInput;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_game_chat, null, false);
        messageList = content.findViewById(R.id.chat_messages);
        messageInput = content.findViewById(R.id.chat_input);
        Button send = content.findViewById(R.id.chat_send);
        TextView empty = content.findViewById(R.id.chat_empty);

        messageList.setAdapter(adapter);
        messageList.setEmptyView(empty);
        send.setOnClickListener(view -> sendCurrentMessage());
        messageInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEND) return false;
            sendCurrentMessage();
            return true;
        });

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.game_chat_title)
                .setView(content)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();
        NetActivity activity = (NetActivity) requireActivity();
        activity.addChatObserver(storeObserver);
        activity.markChatRead();
        refresh();

        Dialog dialog = getDialog();
        Window window = dialog == null ? null : dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onStop() {
        ((NetActivity) requireActivity()).removeChatObserver(storeObserver);
        super.onStop();
    }

    private void sendCurrentMessage() {
        String message = messageInput.getText().toString().trim();
        if (message.isEmpty()) return;
        ((NetActivity) requireActivity()).sendGameChatMessage(message);
        messageInput.setText("");
    }

    private void refresh() {
        if (!isAdded() || messageList == null) return;
        NetActivity activity = (NetActivity) requireActivity();
        adapter.setMessages(activity.getGameChatStore().snapshot());
        activity.getGameChatStore().markRead();
        if (adapter.getCount() > 0) {
            messageList.post(() -> messageList.setSelection(adapter.getCount() - 1));
        }
    }

    private final class ChatAdapter extends BaseAdapter {
        private final List<GameChatMessage> messages = new ArrayList<>();

        void setMessages(List<GameChatMessage> updated) {
            messages.clear();
            messages.addAll(updated);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public GameChatMessage getItem(int position) {
            return messages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).id.hashCode();
        }

        @Override
        public View getView(int position, View recycled, ViewGroup parent) {
            View row = recycled;
            if (row == null) {
                row = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_game_chat_message, parent, false);
            }
            GameChatMessage message = getItem(position);
            LinearLayout bubble = row.findViewById(R.id.chat_bubble);
            TextView sender = row.findViewById(R.id.chat_sender);
            TextView text = row.findViewById(R.id.chat_message);
            TextView metadata = row.findViewById(R.id.chat_metadata);

            ((LinearLayout) row).setGravity(
                    message.ownMessage ? Gravity.END : Gravity.START);
            sender.setText(message.ownMessage
                    ? getString(R.string.game_chat_you)
                    : message.sender);
            text.setText(message.text);

            String time = DateFormat.getTimeFormat(requireContext())
                    .format(new Date(message.timestampMillis));
            if (message.delivery == GameChatMessage.Delivery.SENDING) {
                metadata.setText(getString(R.string.game_chat_sending, time));
            } else if (message.delivery == GameChatMessage.Delivery.FAILED) {
                metadata.setText(getString(R.string.game_chat_failed, time));
            } else {
                metadata.setText(time);
            }

            GradientDrawable shape = new GradientDrawable();
            shape.setColor(Color.parseColor(
                    message.ownMessage ? "#DCEEFF" : "#F1F2F4"));
            shape.setCornerRadius(18 * getResources().getDisplayMetrics().density);
            bubble.setBackground(shape);
            return row;
        }
    }
}
