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
    static HashMap<String,List<Session>> salas = new HashMap<>();     
            
    @OnOpen
    public void onOpen(Session ses,@PathParam("sala") String sala,@PathParam("usuario") String user) {        

        Config cfg; 
        if(!salas.containsKey(sala)){
            salas.put(sala, Collections.synchronizedList(new ArrayList<Session>()));                    
        }                    

        if(!salas.get(sala).isEmpty()){
            Iterator<Session> iterator = salas.get(sala).iterator();
            int i = 1;
            String fulano = user;
            do {            
                if(iterator.next().getUserProperties().get("name").equals(fulano)){
                    fulano = user + i++;
                }
            }while(iterator.hasNext());            
            user = fulano;
        }    
        
                
        ses.getUserProperties().put("name", user);        
        ses.getUserProperties().put("sala", sala); 
        
        salas.get(sala).add(ses);
        
        Gson gson = new Gson();                        
        List<String> teste = new ArrayList<>();
        teste.add("Teste");
        teste.addAll(salas.keySet());        
        cfg = new Config(teste,user,sala, user + " entrou na sala");
        String srtteste = gson.toJson(cfg);
        
        for (Session s : salas.get(sala)) {
                try {      
//                s.getBasicRemote().sendText("usersInfo" + "Quantidade de usuários: " + chatUsers.size());                            
//                        s.getBasicRemote().sendText(ses.getUserProperties().get("name") + " conectou" + ", na sala "+ses.getUserProperties().get("sala")+"\n" );                        
                          s.getBasicRemote().sendText(srtteste);                        
                } catch (IOException ex) {
                    Logger.getLogger(EndPoint.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }

    @OnMessage
    public String onMessage(Session ses, String message) throws IOException {
//        String username = (String) ses.getUserProperties().get("username");
        
        String user = (String) ses.getUserProperties().get("name");
        String sala = (String) ses.getUserProperties().get("sala");
        
        DateFormat df = new SimpleDateFormat("hh:mm:ss");
        Calendar cal = Calendar.getInstance();
        System.out.println(df.format(cal.getTime()));

        if(message.contains("rename")){
            String[] split = message.split(" ");    
            ses.getUserProperties().put("name", split[1]);
            ses.getBasicRemote().sendText("Seu novo nome de usuário eh "+ses.getUserProperties().get("name")+"\n");
        }else{
            
//        Gson gson = new Gson();        
//        String str = gson.toJson(ses.getUserProperties());
//        String str = gson.toJson(df.format(cal.getTime()));
            Config cfg; 
            Gson gson = new Gson();                        
            List<String> teste = new ArrayList<>();
            teste.add("Teste");
            teste.addAll(salas.keySet());        
            cfg = new Config(teste,user,sala,"Usuário: "+ ses.getUserProperties().get("name")+" hora: "+ df.format(cal.getTime()) +" Mensagem: " + message + "\n");
            String srtteste = gson.toJson(cfg);

        Iterator<Session> iterator = salas.get(ses.getUserProperties().get("sala")).iterator();
            while (iterator.hasNext()) {
                Session s = iterator.next();                        
    //            String st = gson.toJson(str);            
                try {                        
//                    s.getBasicRemote().sendText("Usuário: "+ ses.getUserProperties().get("name")+" hora: "+ df.format(cal.getTime()) +" Mensagem: " + message + "\n");
                    s.getBasicRemote().sendText(srtteste);
                } catch (IOException ex) {
                    Logger.getLogger(EndPoint.class.getName()).log(Level.SEVERE, null, ex);
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
        if(salas.get(sala).isEmpty()){
            salas.remove(sala);
        }
        Iterator<Session> iterator = salas.get(ses.getUserProperties().get("sala")).iterator();
            while (iterator.hasNext()) {
                Session s = iterator.next();                        
    //            String st = gson.toJson(str);            
                try {                        
                    s.getBasicRemote().sendText(user + " saiu!\n");
                } catch (IOException ex) {
                    Logger.getLogger(EndPoint.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

