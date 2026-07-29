package com.xlythe.hex.server;

import static com.xlythe.hex.server.IgGameCenterModels.ApiException;

import com.xlythe.hex.server.IgGameCenterModels.BoardRef;
import com.xlythe.hex.server.IgGameCenterModels.Event;
import com.xlythe.hex.server.IgGameCenterModels.GameOptions;
import com.xlythe.hex.server.IgGameCenterModels.HandlerResponse;
import com.xlythe.hex.server.IgGameCenterModels.LobbyBoard;
import com.xlythe.hex.server.IgGameCenterModels.Member;
import com.xlythe.hex.server.IgGameCenterModels.RegisteredUser;
import com.xlythe.hex.server.IgGameCenterModels.UserSession;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

/** Secure DOM parsing for the small XML documents returned by igGameCenter. */
public final class IgGameCenterXml {
    private IgGameCenterXml() {}

    public static Document parse(String xml) throws ApiException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(
                    new InputSource(new StringReader(xml)));
            String error = firstText(document, "errorMessage");
            if (error != null) throw new ApiException(error);
            return document;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Malformed XML response from igGameCenter", e);
        }
    }

    public static UserSession parseLogin(String xml) throws ApiException {
        Document document = parse(xml);
        return new UserSession(
                requiredText(document, "uid"),
                requiredText(document, "name"),
                requiredText(document, "session_id"));
    }

    public static RegisteredUser parseRegistration(String xml) throws ApiException {
        Document document = parse(xml);
        return new RegisteredUser(
                requiredText(document, "uid"),
                requiredText(document, "name"));
    }

    public static BoardRef parseBoardRef(String xml) throws ApiException {
        Document document = parse(xml);
        String sid = requiredText(document, "sid");
        String server = requiredText(document, "server");
        if (!IgGameCenterProtocol.isValidServerName(server)) {
            throw new ApiException("Invalid game server in igGameCenter response");
        }
        return new BoardRef(sid, server);
    }

    public static List<LobbyBoard> parseLobby(String xml) throws ApiException {
        Document document = parse(xml);
        List<LobbyBoard> boards = new ArrayList<>();
        NodeList sessions = document.getElementsByTagName("session");
        for (int i = 0; i < sessions.getLength(); i++) {
            Element session = (Element) sessions.item(i);
            String server = session.getAttribute("serv");
            if (!IgGameCenterProtocol.isValidServerName(server)) continue;
            List<Member> members = new ArrayList<>();
            NodeList memberNodes = session.getElementsByTagName("member");
            for (int j = 0; j < memberNodes.getLength(); j++) {
                Element member = (Element) memberNodes.item(j);
                members.add(new Member(
                        attr(member, "uid", ""),
                        attr(member, "nam", "Unknown"),
                        attr(member, "plc", "0"),
                        attr(member, "stat", "NONE"),
                        false,
                        false,
                        0,
                        -1));
            }
            boards.add(new LobbyBoard(
                    attr(session, "sid", ""),
                    server,
                    attr(session, "stat", "UNKNOWN"),
                    attr(session, "uid", ""),
                    "1".equals(session.getAttribute("priv")),
                    members));
        }
        return boards;
    }

    public static HandlerResponse parseHandler(String xml) throws ApiException {
        Document document = parse(xml);
        Element handler = requiredElement(document, "handlerData");
        Element session = requiredElement(handler, "sessionInfo");
        Element memberInfo = requiredElement(handler, "memberInfo");

        List<Member> players = new ArrayList<>();
        Element playerList = firstElement(handler, "playerList");
        if (playerList != null) {
            NodeList playerNodes = playerList.getElementsByTagName("player");
            for (int i = 0; i < playerNodes.getLength(); i++) {
                Element player = (Element) playerNodes.item(i);
                players.add(new Member(
                        attr(player, "uid", ""),
                        attr(player, "name", ""),
                        attr(player, "place", "0"),
                        attr(player, "stat", "NONE"),
                        "1".equals(player.getAttribute("active")),
                        "1".equals(player.getAttribute("finished")),
                        longAttr(player, "lastRefresh", 0),
                        longAttr(player, "timerLeft", -1)));
            }
        }

        List<Event> events = new ArrayList<>();
        Element eventList = firstElement(handler, "eventList");
        if (eventList != null) {
            NodeList eventNodes = eventList.getElementsByTagName("event");
            for (int i = 0; i < eventNodes.getLength(); i++) {
                Element event = (Element) eventNodes.item(i);
                events.add(new Event(
                        longAttr(event, "eid", 0),
                        longAttr(event, "stamp", 0),
                        attr(event, "uid", "0"),
                        attr(event, "type", ""),
                        event.hasAttribute("data") ? event.getAttribute("data") : null));
            }
        }

        GameOptions options = null;
        Element optionElement = firstElement(handler, "gameOptions");
        if (optionElement != null) {
            String hidden = childText(optionElement, "hidden");
            if (hidden == null) hidden = childText(optionElement, "private");
            options = new GameOptions(
                    intValue(childText(optionElement, "boardSize"), 11),
                    longValue(childText(optionElement, "timerTotal"), 0),
                    longValue(childText(optionElement, "timerInc"), 0),
                    "1".equals(childText(optionElement, "scored")),
                    "1".equals(hidden));
        }

        Element gameData = firstElement(handler, "gameData");
        String board = gameData == null ? null : childText(gameData, "board");
        return new HandlerResponse(
                attr(session, "cmd", ""),
                attr(session, "status", "INIT"),
                attr(session, "owner", ""),
                attr(memberInfo, "place", "0"),
                "1".equals(memberInfo.getAttribute("active")),
                players,
                events,
                options,
                board);
    }

    private static Element requiredElement(Node parent, String name) throws ApiException {
        Element element = firstElement(parent, name);
        if (element == null) throw new ApiException("Missing " + name + " in igGameCenter response");
        return element;
    }

    private static Element firstElement(Node parent, String name) {
        if (parent instanceof Document) {
            NodeList matches = ((Document) parent).getElementsByTagName(name);
            return matches.getLength() == 0 ? null : (Element) matches.item(0);
        }
        if (!(parent instanceof Element)) return null;
        NodeList matches = ((Element) parent).getElementsByTagName(name);
        return matches.getLength() == 0 ? null : (Element) matches.item(0);
    }

    private static String requiredText(Document document, String name) throws ApiException {
        String value = firstText(document, name);
        if (value == null) throw new ApiException("Missing " + name + " in igGameCenter response");
        return value;
    }

    private static String firstText(Document document, String name) {
        NodeList nodes = document.getElementsByTagName(name);
        if (nodes.getLength() == 0) return null;
        String value = nodes.item(0).getTextContent();
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static String childText(Element parent, String name) {
        NodeList nodes = parent.getElementsByTagName(name);
        if (nodes.getLength() == 0) return null;
        String value = nodes.item(0).getTextContent();
        return value == null ? null : value.trim();
    }

    private static String attr(Element element, String name, String fallback) {
        String value = element.getAttribute(name);
        return value == null || value.isEmpty() ? fallback : value;
    }

    private static long longAttr(Element element, String name, long fallback) {
        return longValue(element.getAttribute(name), fallback);
    }

    private static int intValue(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static long longValue(String value, long fallback) {
        try {
            return value == null || value.isEmpty() ? fallback : Long.parseLong(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
