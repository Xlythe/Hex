package com.xlythe.hex.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable response models used by both the Android UI and network player. */
public final class IgGameCenterModels {
    private IgGameCenterModels() {}

    public static final class ApiException extends Exception {
        public ApiException(String message) {
            super(message);
        }

        public ApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static final class UserSession {
        public final String uid;
        public final String name;
        public final String sessionId;

        public UserSession(String uid, String name, String sessionId) {
            this.uid = uid;
            this.name = name;
            this.sessionId = sessionId;
        }
    }

    public static final class RegisteredUser {
        public final String uid;
        public final String name;

        public RegisteredUser(String uid, String name) {
            this.uid = uid;
            this.name = name;
        }
    }

    public static final class BoardRef {
        public final String sid;
        public final String server;

        public BoardRef(String sid, String server) {
            this.sid = sid;
            this.server = server;
        }
    }

    public static final class Member {
        public final String uid;
        public final String name;
        public final String place;
        public final String status;
        public final boolean active;
        public final boolean finished;
        public final long lastRefresh;
        public final long timerLeft;

        public Member(
                String uid,
                String name,
                String place,
                String status,
                boolean active,
                boolean finished,
                long lastRefresh,
                long timerLeft) {
            this.uid = uid;
            this.name = name;
            this.place = place;
            this.status = status;
            this.active = active;
            this.finished = finished;
            this.lastRefresh = lastRefresh;
            this.timerLeft = timerLeft;
        }
    }

    public static final class Event {
        public final long eid;
        public final long stamp;
        public final String uid;
        public final String type;
        public final String data;

        public Event(long eid, long stamp, String uid, String type, String data) {
            this.eid = eid;
            this.stamp = stamp;
            this.uid = uid;
            this.type = type;
            this.data = data;
        }
    }

    public static final class GameOptions {
        public final int boardSize;
        public final long timerTotalSeconds;
        public final long timerIncrementSeconds;
        public final boolean scored;
        public final boolean hidden;

        public GameOptions(
                int boardSize,
                long timerTotalSeconds,
                long timerIncrementSeconds,
                boolean scored,
                boolean hidden) {
            this.boardSize = boardSize;
            this.timerTotalSeconds = timerTotalSeconds;
            this.timerIncrementSeconds = timerIncrementSeconds;
            this.scored = scored;
            this.hidden = hidden;
        }
    }

    public static final class HandlerResponse {
        public final String command;
        public final String status;
        public final String ownerUid;
        public final String localPlace;
        public final boolean localActive;
        public final List<Member> players;
        public final List<Event> events;
        public final GameOptions options;
        public final String board;

        public HandlerResponse(
                String command,
                String status,
                String ownerUid,
                String localPlace,
                boolean localActive,
                List<Member> players,
                List<Event> events,
                GameOptions options,
                String board) {
            this.command = command;
            this.status = status;
            this.ownerUid = ownerUid;
            this.localPlace = localPlace;
            this.localActive = localActive;
            this.players = Collections.unmodifiableList(new ArrayList<>(players));
            this.events = Collections.unmodifiableList(new ArrayList<>(events));
            this.options = options;
            this.board = board;
        }

        public long latestEventId(long fallback) {
            long latest = fallback;
            for (Event event : events) latest = Math.max(latest, event.eid);
            return latest;
        }
    }

    public static final class LobbyBoard {
        public final String sid;
        public final String server;
        public final String status;
        public final String ownerUid;
        public final boolean hidden;
        public final List<Member> members;

        public LobbyBoard(
                String sid,
                String server,
                String status,
                String ownerUid,
                boolean hidden,
                List<Member> members) {
            this.sid = sid;
            this.server = server;
            this.status = status;
            this.ownerUid = ownerUid;
            this.hidden = hidden;
            this.members = Collections.unmodifiableList(new ArrayList<>(members));
        }

        public String firstAvailablePlace() {
            boolean firstOccupied = false;
            boolean secondOccupied = false;
            for (Member member : members) {
                firstOccupied |= "1".equals(member.place);
                secondOccupied |= "2".equals(member.place);
            }
            if (!firstOccupied) return "1";
            if (!secondOccupied) return "2";
            return null;
        }
    }
}
