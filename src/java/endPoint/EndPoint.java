/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package endPoint;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;

/**
 *
 * @author alexs
 */
@ServerEndpoint("/endpoint/{sala}/{usuario}")
public class EndPoint {

//    static List<Session> chatUsers = Collections.synchronizedList(new ArrayList<Session>());
    static HashMap<String, List<Session>> salas = new HashMap<>();
    
    static List<String> listaUsers = Collections.synchronizedList(new ArrayList<>());
    
    @OnOpen
    public void onOpen(Session ses, @PathParam("sala") String sala, @PathParam("usuario") String user) {        
                   
        if (!salas.containsKey(sala)) {
            salas.put(sala, Collections.synchronizedList(new ArrayList<Session>()));            
        }        
        
        if (!salas.get(sala).isEmpty()) {
            Iterator<Session> iterator = salas.get(sala).iterator();
            int i = 1;
            String fulano = user;
            do {                
                if (iterator.next().getUserProperties().get("name").equals(fulano)) {
                    fulano = user + i++;
                }
            } while (iterator.hasNext());            
            user = fulano;
        }        
        
        ses.getUserProperties().put("name", user);        
        ses.getUserProperties().put("sala", sala);        
        
        salas.get(sala).add(ses);        
        
        Gson gson = new Gson();        
        List<String> upRoonAdd = new ArrayList<>();
        upRoonAdd.addAll(salas.keySet());                
        
        Mensagem m = new Mensagem(ses.getUserProperties().get("name")+" entrou na sala\n");        
                
        for(String t : upRoonAdd){
            for(Session session : salas.get(t)) {
                try {
                    session.getBasicRemote().sendText(gson.toJson(upRoonAdd));
                } catch (IOException ex) {
                    Logger.getLogger(EndPoint.class.getName()).log(Level.SEVERE, null, ex);
                }
            }                            
        }
        
        listaUsers.add((String) ses.getUserProperties().get("name"));
        
        m.userList = listaUsers;

        
//       for(Session usu: salas.get(sala)){
//       } 
//        
        for(Session session : salas.get(sala)) {                            
            try {                
                session.getBasicRemote().sendText(gson.toJson(m));                
            } catch (IOException ex) {
                Logger.getLogger(EndPoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @OnMessage
    public String onMessage(Session ses, String message) throws IOException {
//        String username = (String) ses.getUserProperties().get("username");
        Mensagem m;
        Gson gson = new Gson();        
        String user = (String) ses.getUserProperties().get("name");
        String sala = (String) ses.getUserProperties().get("sala");
        
        DateFormat df = new SimpleDateFormat("hh:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.out.println(df.format(cal.getTime()));
        String[] split = message.split(" ");
        String key = split[0];
        
        if (key.contains("rename")) {                 
            String newUser = "";
            for(String s : split){
                if(!s.equals("rename")){
                    newUser += " " + s;
                }                
            }
            ses.getUserProperties().put("name", newUser);
            m = new Mensagem("Seu novo nome de usuário é " + ses.getUserProperties().get("name") + "\n");
            ses.getBasicRemote().sendText(gson.toJson(m));
        } else {                        
            Iterator<Session> iterator = salas.get(ses.getUserProperties().get("sala")).iterator();
            if (key.contains("send")) {
                m = new Mensagem(df.format(cal.getTime()) + " " + ses.getUserProperties().get("name") + ": " + split[1] + "\n");
               if(split[1].contains("-u")){                
                   m = new Mensagem(df.format(cal.getTime()) + " " + ses.getUserProperties().get("name") + ": " + split[3] + "\n");
                    while (iterator.hasNext()) {
                        Session s = iterator.next();  
                        if(s.getUserProperties().get("name").equals(split[2])){
                            try {                        
                                s.getBasicRemote().sendText(gson.toJson(m));
                            } catch (IOException ex) {
                                Logger.getLogger(EndPoint.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }                        
                    }
                    ses.getBasicRemote().sendText(gson.toJson(m));
               }else {                                                                
                while (iterator.hasNext()) {
                    Session s = iterator.next();                    
                    try {                        
                        s.getBasicRemote().sendText(gson.toJson(m));
                    } catch (IOException ex) {
                        Logger.getLogger(EndPoint.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }  
            }            
        }
        return null;
    }
    
    @OnClose
    public void onClose(Session ses) throws IOException {
        String user = (String) ses.getUserProperties().get("name");
        String sala = (String) ses.getUserProperties().get("sala");
        Mensagem m = new Mensagem();
        Gson gson = new Gson();
        
        salas.get(sala).remove(ses);
        if (salas.get(sala).isEmpty()) {
            listaUsers.clear();
            m.userList = listaUsers;
            salas.remove(sala);
            
        } else {
        
        Iterator<Session> iterator = salas.get(ses.getUserProperties().get("sala")).iterator();
        
                
        List<String> upRoomRemoved = new ArrayList<>();
        upRoomRemoved.addAll(salas.keySet());                
        
         m.setMensagem(user + " saiu!" + "\n");
                       
        
        for(Session usu: salas.get(sala)){
        listaUsers.remove((String) ses.getUserProperties().get("name"));
        m.userList = listaUsers;
        }
        
        while (iterator.hasNext()) {
            Session s = iterator.next();
            
            try {                
                s.getBasicRemote().sendText(gson.toJson(m));
            } catch (IOException ex) {
                Logger.getLogger(EndPoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
  }
}
