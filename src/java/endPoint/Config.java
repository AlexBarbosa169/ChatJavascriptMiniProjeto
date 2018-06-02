/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package endPoint;

import java.util.List;

/**
 *
 * @author alexs
 */
public class Config {
    private List<String> salas;
    private String usuario;
    private String salaUsuario;
    private String mensagem;        

    public Config(List<String> salas, String usuario, String salaUsuario, String mensagem) {
        this.salas = salas;
        this.usuario = usuario;
        this.salaUsuario = salaUsuario;
        this.mensagem = mensagem;
    }

    public List<String> getSalas() {
        return salas;
    }

    public void setSalas(List<String> salas) {
        this.salas = salas;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSalaUsuario() {
        return salaUsuario;
    }

    public void setSalaUsuario(String salaUsuario) {
        this.salaUsuario = salaUsuario;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
            
    
}
