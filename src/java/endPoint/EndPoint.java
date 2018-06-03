/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package endPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import com.google.gson.Gson;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import javax.websocket.server.PathParam;

/**
 *
 * @author alexs
 */
@ServerEndpoint("/endpoint/{sala}/{usuario}")
public class EndPoint {

//    static List<Session> chatUsers = Collections.synchronizedList(new ArrayList<Session>());
    static HashMap<String, List<Session>> salas = new HashMap<>();    
    
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
        
        Mensagem m = new Mensagem(ses.getUserProperties().get("name")+" conectou");        
                
        for(String t : upRoonAdd){
            for(Session session : salas.get(t)) {
                try {
                    session.getBasicRemote().sendText(gson.toJson(upRoonAdd));
                } catch (IOException ex) {
                    Logger.getLogger(EndPoint.class.getName()).log(Level.SEVERE, null, ex);
                }
            }                            
        }
        
        HashMap<String,String> teste2 = new HashMap<>();        
        
       for(Session usu: salas.get(sala)){
           teste2.put((String) usu.getUserProperties().get("name") , "user");
       } 
        
        for(Session session : salas.get(sala)) {                            
            try {                
                session.getBasicRemote().sendText(gson.toJson(m));
                session.getBasicRemote().sendText(gson.toJson(teste2));                
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
            m = new Mensagem("Seu novo nome de usuário eh " + ses.getUserProperties().get("name") + "\n");
            ses.getBasicRemote().sendText(gson.toJson(m));
        } else {                        
            Iterator<Session> iterator = salas.get(ses.getUserProperties().get("sala")).iterator();
            if (key.contains("send")) {
                m = new Mensagem("Usuário: " + ses.getUserProperties().get("name") +
                        " hora: " + df.format(cal.getTime()) + " Mensagem: " + split[1] + "\n");
               if(split[1].contains("-u")){                
                   m = new Mensagem("Usuário: " + ses.getUserProperties().get("name") +
                        " hora: " + df.format(cal.getTime()) + " Mensagem: " + split[3] + "\n");
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
    public void onClose(Session ses) {
        String user = (String) ses.getUserProperties().get("name");
        String sala = (String) ses.getUserProperties().get("sala");
        salas.get(sala).remove(ses);
        if (salas.get(sala).isEmpty()) {
            salas.remove(sala);
        }
        Iterator<Session> iterator = salas.get(ses.getUserProperties().get("sala")).iterator();
        
        Gson gson = new Gson();        
        List<String> upRoomRemoved = new ArrayList<>();
        upRoomRemoved.addAll(salas.keySet());                
        
        Mensagem m = new Mensagem(user + " saiu!" + "\n");
        
        HashMap<String,String> upUserRemoved = new HashMap<>();                
        for(Session usu: salas.get(sala)){
           upUserRemoved.put((String) usu.getUserProperties().get("name") , "user");
        }
        
        while (iterator.hasNext()) {
            Session s = iterator.next();
            //            String st = gson.toJson(str);            
            try {                
                s.getBasicRemote().sendText(gson.toJson(m));
                s.getBasicRemote().sendText(gson.toJson(upRoomRemoved));
                s.getBasicRemote().sendText(gson.toJson(upUserRemoved));
            } catch (IOException ex) {
                Logger.getLogger(EndPoint.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
