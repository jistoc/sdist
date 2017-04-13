/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

import gui.ClienteGrafico;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
/**
 *
 * @author user
 */
public class Cliente extends Thread {
    
    DatagramSocket socket;
    ClienteGrafico cli;
    Thread threadCliente;
    private String usuario;
    InetAddress ip;
    int porta;
    
    public Cliente(String endereco, String porta, String usuario) throws SocketException, UnknownHostException{
        socket = new DatagramSocket();
        this.ip = InetAddress.getByName(endereco);
        this.porta = Integer.parseInt(porta);
        this.usuario = usuario;
    }
    
    
    public void run(){
       byte[] mensagem;
        try {
            while(true){
                mensagem = new byte[1024];
                DatagramPacket pacote = new DatagramPacket(mensagem, mensagem.length);
                socket.receive(pacote);
                lerPacote(pacote);
            }
        } catch (IOException ex) {
           ex.printStackTrace();
        } 
        
    }    
    public String conectar() throws IOException{
        byte[] m = new byte[1024];
        m = ("1#"+this.getUsuario()).getBytes();
        DatagramPacket out = new DatagramPacket(m,m.length,ip,porta);
        socket.send(out);
        m = new byte[1024];
        DatagramPacket in = new DatagramPacket(m, m.length);
        socket.receive(in);
        String stringData = new String(in.getData()).trim();
        String[] op = stringData.split("#");
        
        if (op[0].equals("2")) {
            threadCliente = new Thread(this);
            threadCliente.start();
            cli = new ClienteGrafico(this);
            cli.setVisible(true);
            atualizarUsuarios(in);
        }
        return op[0];
    }
    public void atualizarMensagens(DatagramPacket pacote) throws IOException{
        String dados = new String(pacote.getData()).trim();
        String[] msg = dados.split("#");
        int row = cli.getDtmUsuario().getRowCount();
        for(int i = 0; i < row ; i++){
            if(cli.getDtmUsuario().getValueAt(i,1).toString().equals(msg[1]) && cli.getDtmUsuario().getValueAt(i,2).toString().equals(msg[2])){
                cli.getTxtMensagens().append(cli.getDtmUsuario().getValueAt(i, 0).toString()+" disse: "+msg[3]+"\n");
            }
        }
    }
    public void desconectar() throws IOException{
        enviarMensagem("5#");
    } 
    public void enviarMensagem(String msg) throws IOException{
        byte[] dados = new byte[1024];
        dados = msg.getBytes();
        DatagramPacket pacote = new DatagramPacket(dados,dados.length,ip,porta);
        socket.send(pacote);
    }
    private void lerPacote(DatagramPacket pacote) throws IOException {
        String data = new String(pacote.getData()).trim();
        String[] msg = data.split("#");
        switch(msg[0]){
            case "2" : atualizarUsuarios(pacote); break;
            case "4" : atualizarMensagens(pacote); break;
            default : break;
        }
    }
    private void atualizarUsuarios(DatagramPacket pacote) {
        String s = new String(pacote.getData()).trim();
        String[] lista = s.split("#");
        int c = 1;
        cli.getDtmUsuario().setRowCount(0);
        while(c<lista.length){
            String i = lista[c];
            c++;
            String p = lista[c];
            c++;
            String n = lista[c];
            c++;
            Object[] u = {n,i,p};
            cli.getDtmUsuario().addRow(u);                    
        }
    }

    /**
     * @return the usuario
     */
    public String getUsuario() {
        return usuario;
    }
}
