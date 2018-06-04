/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package endPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author alexs
 */
public class Mensagem {    
    String mensagem;
    List<String> userList = new ArrayList<>();  
    
    
    public Mensagem(){}
    
    public Mensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMensagem() {
        return mensagem;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(HashMap<String, String> userList) {
        this.userList = (List<String>) userList;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
        
}
